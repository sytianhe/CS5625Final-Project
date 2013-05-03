/**
 * sanddune.fp
 * Fragment shader for simulating sand dune dynamics. 
 * Method based on "Modelling Desert Dune Fields Based on Discrete Dynamics" by STEVEN R. BISHOP et al. 
 *
 * Sand dune is reprepsented as a discrete height map over a grid.   
 * The x component stores current height.
 * The y component stores saltation events at neighboring grid points.   
 * @author homoflashmanicus
 */
 
 /* Some constant maximum number of random seed values which GLSL and Java have to agree on. */
const int MAX_RAND_SEED = 50;

uniform sampler2DRect PreviousSandDuneBuffer; //Since we cant both read and write to the same gbuffer.  We alternate between two of them 
uniform vec2 ScreenSize;
uniform int initialize; // Do we need to initialize the current gbuffer ? 
uniform bool whichBuffer;

uniform vec2 randSeed[MAX_RAND_SEED];  // Pseudo random number seeds for random number generat.
uniform int randSeedLength; // Number of random seeds.  Should be the same as 

const float eps = 0.001;
const vec2 wind = vec2(3.0,0.0);			// x-direction scaled by transport length
const float aspectRatio = 10.0; 			// Thickness of sand "slab"
const float angleOfRepose1 = 6.0; 			// approx 33.7 degrees
const float angleOfRepose2 = 2.5;	 		// approx 25.2 degrees
const float shadowZoneAngle = 1.5;			// approx 15 degrees
const float LT = 1.0;  					 	// transport length
const float LN = 3.0; 					 	// Neighborhood radius
const float shearVelocityLinear = 0.2;  	// Linear wind shear coefficient for computing probability that grain moves past site
const float shearVelocityNonLinear = 0.002; //  Nonlinear wind shearing coefficient for computing probability that grain moves past site
const float erosionProb = 0.5;         	// Probability that a grain of sand moves from it's current location
const float depositionProbSoft = 0.6; 		// Probability that transported land lands at a neighboring point that is already sandy 
const float depositionProbHard = 0.4; 		// Probability that transported land lands at a neighboring point that is bare



/**
 * Cheap random numbers. Via Stefan Gustavson (Stack Overflow).
 * Alternativley, try noise1 ?
 */
float rand(vec2 seed){
    return fract(sin(dot(gl_FragCoord.xy * seed , vec2(12.9898,78.233))) * 43758.5453);
}


/**
 * Compute mod of both directions of a tuple to size of screen
 */
 vec2 mod2Screen(vec2 coord)
 {
 	//return vec2( mod(coord.x, float(ScreenSize.x)), mod(coord.y, float(ScreenSize.y)));  
 	return coord;
 	//vec2 newCoord = vec2(coord.x/float(ScreenSize.x), coord.y/float(ScreenSize.y)   )
 	//return vec2( mod(coord.x, float(ScreenSize.x)), mod(coord.y, float(ScreenSize.y))); 
 	vec2 newCoord = coord;
 	if(coord.x <= 0.0){
 		newCoord.x += float(ScreenSize.x); 
 	}
 	if(coord.y <= 0.0){
 		newCoord.y += float(ScreenSize.y); 
 	}
 	if(coord.x >= float(ScreenSize.x)){
 		newCoord.x -= float(ScreenSize.x); 
 	}
 	if(coord.y >= float(ScreenSize.y)){
 		newCoord.y -= float(ScreenSize.y); 
 	}
 	return newCoord;
 }


 /**
 * Samples from old dune buffer and returns depth and grains set aside for transport.
 */
 vec4 sample(vec2 coord)
 {
	 return texture2DRect(PreviousSandDuneBuffer, coord); 
 }

 /**
 * Avg height of sand in a neighborhood of the lattice point
 */
 
 float havg()
 {
	vec2 center = gl_FragCoord.xy;
	float ha = 0.0;
	for (float i=center.x - LN; i<= center.x+LN; i+=1.0){
		for (float j=center.y - LN; j<= center.y+LN; j+=1.0){
			ha += sample(mod2Screen(center + vec2(i,j))).x;
		}
	}
	ha = ha / ( (LN +1.0 ) * ( LN + 1.0) ) ;
	
	return ha;
 }
 

 /**
 * Reference height of sand in a neighborhood of the lattice point
 */
 
 float href()
 {
	vec2 center = gl_FragCoord.xy;
	float hr = 0.0;
	float ha = havg();
	
	for (float i=center.x - LN; i<= center.x+LN; i = i+1.0){
		for (float j=center.y - LN; j<= center.y+LN; j = j+1.0){
			hr +=  abs( sample(mod2Screen(center + vec2(i,j))).x - ha );
		}
	}
	hr = ha - hr/(2.0 *(LN +1.0 ) * ( LN + 1.0));
	
	return hr;
 }
 
  /**
  * Check if current site is in the shadow of neighboring sites
  */
 bool isInShadow()
 {
 	float h = sample(gl_FragCoord.xy).x;
	float hNeighbor1 = sample( mod2Screen(gl_FragCoord.xy - wind) ).x ;
	float hNeighbor2 = sample( mod2Screen( gl_FragCoord.xy - vec2(2.0,2.0) * wind ) ).x ;

	return (hNeighbor1 > h + shadowZoneAngle/aspectRatio) && (hNeighbor2 > h + 2.0*shadowZoneAngle/aspectRatio);
 }
 
  /**
  * Compute gradiant of height field at current location (using center difference)
  */
 vec2 grad()
 {
  	vec2 center = vec2(gl_FragCoord.xy);
  	float h = sample(center).x;
 	vec2 g = vec2(0.0);
 	
 	g.x = (sample( center + vec2(1.0,0.0) ).x - sample( center + vec2(-1.0,0.0) ).x) / 2.0;
 	g.y = (sample( center + vec2(0.0,1.0) ).x - sample( center + vec2(0.0,-1.0) ).x) / 2.0;
 	
 	return g;
 }
 
 

void main()
{

	gl_FragData[0] = vec4(0.0);


	if(initialize == 1 ){
		//INITIALIZE SIMULATION TO RANDOM CONFIGURATION 
		if (gl_FragCoord.x < 200.0){
			gl_FragData[0] = vec4(1.0,0.0,0.0,0.0);
		}
	}	
	else {
	
		//RUN SIMULATION
	
		//INITIALIZE HEIGHT AND FREE SALTATION GRAINS 
		vec2 center = vec2(gl_FragCoord.xy);
		float h = sample(center).x;
		float hr = href();	
		float nSaltationGrainsComing = 0.0;
		if ( (center - wind).x >= 0.0 && (center - wind).y >= 0.0 ){
			nSaltationGrainsComing = sample(mod2Screen(center - wind)).y ;
		}
		float nSaltationGrainsGoing = 0.0;
		bool inShadow = isInShadow() ;
	
		//ACCUMULATE TOPPLED GRAINS FROM PREVIOUS ITERATION
		for (float i=- 1.0; i<= 1.0; i = i+2.0){
			for (float j=- 1.0; j<= 1.0; j = j+2.0){
				vec2 neighbor = center + vec2(i,j);
				vec2 pointer = sample(neighbor).zw;
				//IF GRAD POINTS FROM NEIGHBOR TO CENTER, THEN ADD A GRAIN
				if( abs(pointer.x - i)<eps && (pointer.y - j)<eps ){
					h += 1.0/aspectRatio;
				} 
			}
		}
	
	
		//CHECK IF THERES A SALTATION EVENT AT THIS SITE.  IF SO, FREE  TO THE Y COMPONENT FOR ACCUMULATION IN THE NEXT PASS
		if( h >= 1.0/aspectRatio - eps && !inShadow && rand(randSeed[0]) < erosionProb ){
			h -= 1.0/aspectRatio;
			nSaltationGrainsGoing += 1.0/aspectRatio;
		}
		
		//CHECK IT THERE ARE ANY SALTATION GRAINS COMING INTO THIS SITE
		if (nSaltationGrainsComing >= 1.0/aspectRatio - eps ){
			
			//COMPUTE PROBABILITY THAT GRAINS KEEP MOVING
			float P;
			
			if (h > 0.0){
				P = depositionProbSoft;
			}
			else{
				P = depositionProbHard;
			}
			
			P -= shearVelocityLinear * (h - hr); // linear exposure term
			
			if (h >= hr-eps){
				P -= shearVelocityNonLinear * (h - hr) * (h - hr);   // nonlinear exposure term
			}
			
			if (inShadow){
				P = 1.0;
			}			
			
			// CHECK UPWIND NEIGHBOR FOR FREE SAND GRAINS
			for(int i = 0; i <  int(min( nSaltationGrainsComing * aspectRatio, float(MAX_RAND_SEED) - 1.0))  ; i += 1 ){		
				//EITHER ACCUMULATE THE FREE GRAINS AT THIS LOCATION OR PASS THEM ON 
				if (rand(randSeed[i + 1]) > P ){
					nSaltationGrainsGoing += 1.0/aspectRatio;
				}
				else {
					h += 1.0/aspectRatio;
				}
			}
		}		
		
		//RELEASE GRAIN IN CASE THE SAND PILE GETS TOO STEEP
		//IF GRADIANT IS SUFFICIENTLY LARGE, TOPPLE GRAIN IN THE DIRECTION OF STEEPEST DESCENT
		//ONLY TOPPLE IN ONE DIRECTION
		vec2 g = grad();
		if (length(g) > angleOfRepose1/aspectRatio ){
			//REMOVE A GRAIN 
			h -= 1.0/aspectRatio;
			
			//AND LET NEIGHBOR KNOW THAT A GRAIN IS COMMING ITS WAY
			//SAVE RELATIVE LOCATION OF WHERE THE GRAIN IS GOING
			if (g.x > 1.0/aspectRatio ){
				gl_FragData[0].z = -1.0;
			}
			else if (g.x < -1.0/aspectRatio ){
				gl_FragData[0].z = 1.0;
			}
			if (g.y > 1.0/aspectRatio ){
				gl_FragData[0].w = -1.0;
			}
			else if (g.y < -1.0/aspectRatio ){
				gl_FragData[0].w = 1.0;
			}
		}
		
		gl_FragData[0].x = h ;
		gl_FragData[0].y =  nSaltationGrainsGoing;		
	}
}

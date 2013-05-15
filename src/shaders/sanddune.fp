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

const float eps = 0.1;
const vec2 wind = vec2(1.0,0.0);			// x-direction scaled by transport length
const int aspectRatio = 3; 					// Thickness of sand "slab"
const float angleOfRepose1 = 10.0; 			// approx 33.7 degrees ?
const float shadowZoneAngle1 = 2.0;			// approx 15 degrees ?
const float shadowZoneAngle2 = 3.0;			// approx 23 degrees ?
const int LT = 1;  					 		// transport length
const int LN = 4; 					 		// Neighborhood radius
const float shearVelocityLinear = 0.2;  //0.2;  	// Linear wind shear coefficient for computing probability that grain moves past site
const float shearVelocityNonLinear = 0.002; //  Nonlinear wind shearing coefficient for computing probability that grain moves past site
const float erosionProb = 0.5;         	    // Probability that a grain of sand moves from it's current location
const float depositionProbSoft = 0.6; 		// Probability that transported land lands at a neighboring point that is already sandy 
const float depositionProbHard = 0.4; 		// Probability that transported land lands at a neighboring point that is bare
const float maxHeight = 40.0;					//  Max allowed sand stack height.

/**
 * Cheap random numbers. Via Stefan Gustavson (Stack Overflow).
 */
float rand(vec2 seed){
    return fract(sin(dot(gl_FragCoord.xy * seed , vec2(12.9898,78.233))) * 43758.5453);
}


/**
 * Compute mod of both directions of a tuple to size of screen
 */
 vec2 mod2Screen(vec2 coord)
 {
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
	 return texture2DRect(PreviousSandDuneBuffer, mod2Screen(coord)); 
 }

 /**
 * Avg height of sand in a neighborhood of the lattice point
 */
 
 int havg()
 {
	vec2 center = gl_FragCoord.xy;
	float ha = 0.0;
	for (int i = -LN; i<= LN; i+=1){
		for (int j = -LN; j<= LN; j+=1){
			ha += sample( center + vec2(float(i),float(j)) ).x;
		}
	}
	ha = ha / ( ( float(LN) +1.0 ) * ( float(LN) + 1.0) );
	return int(ha);
 }
 

 /**
 * Reference height of sand in a neighborhood of the lattice point
 */
 
 int href()
 {
	vec2 center = gl_FragCoord.xy;
	float hr = 0.0;
	float ha = float(havg());
	
	for (int i= -LN; i<= LN; i += 1){
		for (int j= -LN; j<= LN; j += 1){
			hr +=  abs( sample(center + vec2(float(i),float(j))).x - ha );
		}
	}
	hr = ha - hr/( (float(LN) +1.0 ) * ( float(LN) + 1.0) )/ 2.0;
	
	return int(hr);
 }
 
  /**
  * Check if current site is in the shadow of neighboring sites
  */
 bool isInShadow()
 {
 	float h = sample(gl_FragCoord.xy).x;
	float hNeighbor1 = sample( gl_FragCoord.xy - vec2(1.0,0.0)).x ;
	float hNeighbor2 = sample( gl_FragCoord.xy - vec2(2.0,0.0)).x ;
	return (hNeighbor1 - h >=  shadowZoneAngle1 ) || (hNeighbor2 - h >=  shadowZoneAngle2);
 }
 
  /**
  * Compute gradiant of height field at current location (using center difference)
  */
 vec2 grad()
 {
  	vec2 center = vec2(gl_FragCoord.xy);
  	float h = sample(center).x;
 	vec2 g = vec2(0.0);
 	
 	float gX = sample( center + vec2(1.0,0.0) ).x - h ;
 	float gx = h - sample( center + vec2(-1.0,0.0) ).x;
 	float gY = sample( center + vec2(0.0,1.0) ).x - h ;
 	float gy =  h - sample( center + vec2(0.0,-1.0) ).x ;
 	
 	g.x = (gX + gx) / 2.0;
 	g.y = (gY + gy) / 2.0;
 	
 	//g.x = (sample( center + vec2(1.0,0.0) ).x - sample( center + vec2(-1.0,0.0) ).x) /2.0;
 	//g.y = (sample( center + vec2(0.0,1.0) ).x - sample( center + vec2(0.0,-1.0) ).x)/ 2.0 ;
 	
 	
 	return g;
 }
 
 

void main()
{

	gl_FragData[0] = vec4(0.0);


	if(initialize == 1 ){
		//INITIALIZE SIMULATION TO RANDOM CONFIGURATION 
		if ( 50.0 <  gl_FragCoord.x   && gl_FragCoord.x < 350.0  && 100.0 < gl_FragCoord.y &&  gl_FragCoord.y < 350.0){
			gl_FragData[0] = vec4(20.0,0.0,0.0,0.0);
		}
	}	
	else {
	
		//RUN SIMULATION
	
		//INITIALIZE HEIGHT AND FREE SALTATION GRAINS 
		vec2 center = vec2(gl_FragCoord.xy);
		int h = int(sample(center).x);
		int hr = href();	
		int nSaltationGrainsComing =  int(sample(center -  wind).y) ;
		int nSaltationGrainsGoing = 0;
		bool inShadow = isInShadow() ;

	
		//ACCUMULATE TOPPLED GRAINS FROM PREVIOUS ITERATION
		for (int i=- 1; i<= 1; i += 1){
			for (int j= -1; j<= 1; j += 1){
				if (i != 0 && j != 0){
					vec2 neighbor = center + vec2(float(i),float(j));
					vec2 pointer = sample(neighbor).zw;
					//IF TOPPLE POINTER POINTS FROM NEIGHBOR TO CENTER, THEN ADD A GRAIN
					if( abs( pointer.x - float(i) ) < eps || abs( pointer.y - float(j) ) < eps ){
						h += 1;
					}
				} 
			}
		}
		
		
	
	
		//CHECK IF THERES A SALTATION EVENT AT THIS SITE.  IF SO, FREE  TO THE Y COMPONENT FOR ACCUMULATION IN THE NEXT PASS
		if( h >0 && !inShadow && rand(randSeed[0]) < erosionProb ){
			h -= 1;
			nSaltationGrainsGoing +=1 ;
		}
		
		//CHECK IT THERE ARE ANY SALTATION GRAINS COMING INTO THIS SITE
		if (nSaltationGrainsComing > 0 ){
			
			//COMPUTE PROBABILITY THAT GRAINS KEEP MOVING
			float P;
			
			if (h > 0){
				P = depositionProbSoft;
			}
			else{
				P = depositionProbHard;
			}
			
			P -= shearVelocityLinear * float(h - hr); // linear exposure term
			
			if (h >= hr){
				P -= shearVelocityNonLinear * float(h - hr) * float(h - hr);   // nonlinear exposure term
			}
			
			if (inShadow){
				P = 1.0;
			}			
			
			// CHECK UPWIND NEIGHBOR FOR FREE SAND GRAINS
			for(int i = 0; i < int (min( float(nSaltationGrainsComing) , float(MAX_RAND_SEED) - 1.0) ) ; i += 1 ){		
				//EITHER ACCUMULATE THE FREE GRAINS AT THIS LOCATION OR PASS THEM ON 
				if (rand(randSeed[i + 1]) > P ){
					nSaltationGrainsGoing += 1;
				}
				else {
					h += 1;
				}
			}
		}		
	
		
		//RELEASE GRAIN IN CASE THE SAND PILE GETS TOO STEEP
		//IF GRADIANT IS SUFFICIENTLY LARGE, TOPPLE GRAIN IN THE DIRECTION OF STEEPEST DESCENT
		//ONLY TOPPLE IN ONE DIRECTION
		vec2 g = grad();
		if (length(g)  > angleOfRepose1 && h > 0){
			//REMOVE A GRAIN 
			h -= 1;
			
			//AND LET NEIGHBOR KNOW THAT A GRAIN IS COMMING ITS WAY
			//SAVE RELATIVE LOCATION OF WHERE THE GRAIN IS GOING
					
			if (g.x > eps ){
				gl_FragData[0].z = 1.0;
			}
			else if (g.x < -eps ){
				gl_FragData[0].z = -1.0;
			}
			if (g.y > eps ){
				gl_FragData[0].w = 1.0;
			}
			else if (g.y < -eps ){
				gl_FragData[0].w = -1.0;
			}
			
		}

		gl_FragData[0].x = max(min(float(h),maxHeight),0.0)  ;
		gl_FragData[0].y =  float(nSaltationGrainsGoing);		
	}
}

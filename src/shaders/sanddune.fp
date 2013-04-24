/**
 * sanddune.fp
 * 
 * Fragment shader for calculating silhouette edges as described in Decaudin's 1996 paper.
 * 
 * @author homoflashmanicus
 */

uniform sampler2DRect PreviousSandDuneBuffer;
uniform vec2 ScreenSize;
uniform bool Initialize;


const vec2 wind = vec2(1.0,0.0);


/**
 * Samples from old dune buffer and returns depth.
 */
float sample(vec2 coord)
{
	return texture2DRect(PreviousSandDuneBuffer, coord).x; 
}

/**
 * Compute mod of both directions of a tuple to size of screen
 */
 vec2 mod2Screen(vec2 coord)
 {
 	return vec2( mod(coord.x, float(ScreenSize.x)), mod(coord.y, float(ScreenSize.y)));  
 }


void main()
{
	if(Initialize){
		gl_FragColor = vec4(0.0);
	}	
	else {
		vec2 center = vec2(gl_FragCoord.xy);
		vec2 A = mod2Screen(center + vec2(-1.0, 1.0));	//nw
		vec2 B = mod2Screen(center + vec2(0.0, 1.0));	//n
		vec2 C = mod2Screen(center + vec2(1.0, 1.0));	//ne
		vec2 D = mod2Screen(center + vec2(-1.0, 0.0));	//w
		vec2 E = mod2Screen(center + vec2(1.0, 0.0));	//e
		vec2 F = mod2Screen(center + vec2(-1.0, -1.0));	//sw
		vec2 G = mod2Screen(center + vec2(0.0, -1.0));	//s
		vec2 H = mod2Screen(center + vec2(1.0, -1.0));	//se
		
		float h = sample(center);
		
		vec4 color = vec4(h,0.0,0.0,1.0);
		gl_FragColor = color;		
	}
}

/**
 * fog.fp
 * 
 * Fragment shader for the bloom post-processing algorithm.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Sean Ryan (ser99)
 * @date 2013-01-31
 */

/* Sampler for the final scene in the GBuffer. */
uniform sampler2DRect FinalSceneBuffer;
uniform sampler2DRect DepthSceneBuffer;
//uniform float KernelVariance;
//uniform int KernelWidth;
uniform float Threshold;

// TODO PA3 Prereq (Optional): Fill this in if you like bloom.
void main()
{
    vec2 center = vec2(gl_FragCoord.xy);
    vec4 colorS = texture2DRect(FinalSceneBuffer, center);
    // default fog color
    vec4 colorF = vec4(1.0,1.0,1.0,1.0);
    float depth = texture2DRect(DepthSceneBuffer, center).x;
    
    // Converts the depth buffer value to a linear value
   	float near = 0.1;
	float far = 100.0;
	depth =  (2.0 * near) / (far + near - depth * (far - near));
	
    // exponentail fog:
    float factor = exp(-Threshold * (far - depth)/ (far - depth));

    gl_FragColor = factor*colorS + (1.0 - factor)*colorF;
}

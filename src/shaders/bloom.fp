/**
 * bloom.fp
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

uniform float KernelVariance;
uniform int KernelWidth;
uniform float Threshold;

// TODO PA3 Prereq (Optional): Fill this in if you like bloom.
void main()
{
    vec2 center = vec2(gl_FragCoord.xy);
	vec4 color;
	float average = 0.0;
	float brightness;
	float w;
	float sumW = 0.0;

	float textCenterX = center.x;
	float textCenterY = center.y;
	
	for (float i = textCenterX - float(KernelWidth); i <= textCenterX + float(KernelWidth); i=i+1.0){
		for (float j = textCenterY - float(KernelWidth); j <= textCenterY + float(KernelWidth); j = j+1.0){
			color = texture2DRect(FinalSceneBuffer, vec2(i,j));
			
			brightness = (color.x + color.y + color.z)/3.0;
			
			if (Threshold > brightness){
				brightness = 0.0;
			}
			else{
				brightness = 1.0;
			}

			w = exp(-(pow(float(textCenterX - i),2.0) + pow(float(textCenterY - j),2.0))/(2.0 * KernelVariance));
			sumW +=w;
			average += brightness * w;
		}
	}
	

    gl_FragColor = texture2DRect(FinalSceneBuffer, gl_FragCoord.xy) + average/sumW*vec4(1.0,1.0,1.0,1.0);
}

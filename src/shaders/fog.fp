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
uniform sampler2DRect PositionBuffer;
uniform sampler2DRect DepthSceneBuffer;
uniform sampler2D PerlinNoise;
uniform float ViewportWidth;
uniform float ViewportHeight;
uniform float Threshold;
uniform mat4 O2WMatrix;
uniform vec2 OffsetDirection;

// TODO PA3 Prereq (Optional): Fill this in if you like bloom.
void main()
{
	vec3 EyespacePosition = texture2DRect(PositionBuffer, gl_FragCoord.xy).xyz;
    vec2 center = vec2(gl_FragCoord.xy);
    vec4 colorS = texture2DRect(FinalSceneBuffer, center);
    
    // default fog color
    vec4 colorF = vec4(255.0/256.0,
    				   250.0/256.0,
    				   205.0/256.0,
    				   1.0);
    				   
    // default fog height
    float height = 2.0;
    
    float depth = texture2DRect(DepthSceneBuffer, center).x;
    vec4 WorldSpacePos = O2WMatrix*vec4(EyespacePosition, 1.0);
    vec4 eyeWorldSpacePos = O2WMatrix * vec4(0.0,0.0,0.0,1.0);
    
    float noisePerlin = texture2D(PerlinNoise, vec2((WorldSpacePos.x + 100.0)/200.0, (WorldSpacePos.z + 100.0)/200.0)).x;
    height = height + 5.0*noisePerlin;
    
   	float near = 0.1;
	float far = 100.0;
	
	// Converts the depth buffer value to a linear value
	depth = (2.0 * near) / (far + near - depth * (far - near));
	if (eyeWorldSpacePos.y > height){
		if(WorldSpacePos.y > height){
			 depth = 0.0;
		}
		else{
			depth = depth * (height - WorldSpacePos.y) / (eyeWorldSpacePos.y - WorldSpacePos.y);
		}
	}
	else {
		if(WorldSpacePos.y > height){
			depth = depth * (height - eyeWorldSpacePos.y) / ( WorldSpacePos.y - eyeWorldSpacePos.y);
		}
	}


    // exponentail fog:
    float factor = exp(- (Threshold+2.0) * depth);

    gl_FragColor = factor*colorS +  (1.0 - factor)*colorF;
}

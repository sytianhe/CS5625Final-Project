/**
 * fog.fp
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
uniform float time;

const vec2 speed = vec2(-1.0,0.0);

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
    float height = 1.5;
    
    float depth = texture2DRect(DepthSceneBuffer, center).x;
    vec4 WorldSpacePos = O2WMatrix*vec4(EyespacePosition, 1.0);
    vec4 eyeWorldSpacePos = O2WMatrix * vec4(0.0,0.0,0.0,1.0);
    
    vec2 shiftCoord = mod(vec2((WorldSpacePos.x + 75.0)/150.0, (WorldSpacePos.z +75.0)/150.0) + speed* time, 1.0 ); 
    float noisePerlin = texture2D(PerlinNoise, shiftCoord).x;
    height = height * noisePerlin;
    
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

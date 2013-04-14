/**
 * ssao.fp
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Sean Ryan (ser99)
 * @date 2013-03-23
 */

uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;

#define MAX_RAYS 100
uniform int NumRays;
uniform vec3 SampleRays[MAX_RAYS];
uniform float SampleRadius;

uniform mat4 ProjectionMatrix;
uniform vec2 ScreenSize;


/* Decodes a vec2 into a normalized vector See Renderer.java for more info. */
vec3 decode(vec2 v)
{
	vec3 n;
	n.z = 2.0 * dot(v.xy, v.xy) - 1.0;
	n.xy = normalize(v.xy) * sqrt(1.0 - n.z*n.z);
	return n;
}

void main()
{
	// TODO PA4: Implement SSAO. Your output color should be grayscale where white is unobscured and black is fully obscured.
	
	// Construct the orthogonal coordinates.
	vec3 EyespaceNormal = decode(vec2(texture2DRect(DiffuseBuffer, gl_FragCoord.xy).a, texture2DRect(PositionBuffer, gl_FragCoord.xy).a));
	vec3 EyespacePosition = texture2DRect(PositionBuffer, gl_FragCoord.xy).xyz;
	
	if (EyespacePosition.z < 0.0){
		normalize(EyespaceNormal);
		vec3 axis1 = vec3(EyespaceNormal.x, -EyespaceNormal.z, EyespaceNormal.y);
		normalize(axis1);
		vec3 axis2 = cross(EyespaceNormal, axis1);
		normalize(axis2);
		
		// Construct the matrix;
		mat3 toEyeSpaceMat = mat3(axis1, axis2, EyespaceNormal);
		
		float count = 0.0;
		float totcount = 0.0;
		
		for (int i = 0; i<NumRays; i++){
		    vec3 newPoint = vec3(normalize(toEyeSpaceMat*SampleRays[i])*SampleRadius+EyespacePosition);
			vec4 samplePoint = ProjectionMatrix*vec4(newPoint, 1.0);
			//samplePoint = samplePoint/samplePoint.w;
			
			vec2 lookup = vec2((samplePoint.x/samplePoint.w+1.0)/2.0*ScreenSize.x, (samplePoint.y/samplePoint.w+1.0)/2.0*ScreenSize.y);
			float depth = texture2DRect(PositionBuffer, lookup).z;
			float dotnormal = 1.0 * dot(normalize(toEyeSpaceMat*SampleRays[i]), EyespaceNormal);
			
			if (depth < 0.0){
				if (depth > newPoint.z){
						count = count + dotnormal;
				}
			}
			
			totcount = totcount + dotnormal;
		}
		
		float brightness = 1.0-  count/totcount;
		gl_FragColor = vec4(brightness, brightness, brightness, 1.0);
	}
	else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
}

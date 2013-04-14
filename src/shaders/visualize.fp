/**
 * visualize.fp
 * 
 * Fragment shader for the data visualization post-processing stage.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Sean Ryan (ser99)
 * @date 2013-01-31
 */

/* We need the anisotropic ward ID so that we can detect tangent data. */
const int ANISOTROPIC_WARD_MATERIAL_ID = 6;

/* Samplers for each texture of the GBuffer. */
uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;
uniform sampler2DRect MaterialParams1Buffer;
uniform sampler2DRect MaterialParams2Buffer;

/* Determines what kind of data we are rendering. */
const int NORMALS_MODE = 0;
const int TANGENTS_MODE = 1;
const int BITANGENTS_MODE = 2;
uniform int VisMode;

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

	// TODO PA3 Prereq (Optional): Paste in your vizualization shader if you feel like it.
	vec3 normal = decode(vec2(texture2DRect(DiffuseBuffer,  gl_FragCoord.xy).a,
	                          texture2DRect(PositionBuffer, gl_FragCoord.xy).a));
	vec4 materialParams1 = texture2DRect(MaterialParams1Buffer, gl_FragCoord.xy);
	vec4 materialParams2 = texture2DRect(MaterialParams2Buffer, gl_FragCoord.xy);
	int materialID = int(materialParams1.x);
	
	/* TODO PA1: Output a color to visualize the g-buffer data at the given pixel.
	 *           You should visualize the data indicated by VisMode.
	 */
	
	//initialize fragment 
	gl_FragColor = vec4(0.0,0.0,0.0,1.0);
	 
	if (VisMode == NORMALS_MODE) {
		gl_FragColor = vec4(0.5*normal+vec3(0.5),1.0);
	}
	else if (VisMode == TANGENTS_MODE && materialID == ANISOTROPIC_WARD_MATERIAL_ID) {
	
		vec3 tangent = decode(texture2DRect(MaterialParams1Buffer, gl_FragCoord.xy).yz);
		
		normalize(tangent);
		tangent -= dot(tangent, normal) * normal;
		normalize(tangent);
		
		gl_FragColor = vec4(0.5*tangent+vec3(0.5),1.0);
	}
	else if (VisMode == BITANGENTS_MODE && materialID == ANISOTROPIC_WARD_MATERIAL_ID) {
		float handedness = float(sign(materialParams1.x));
		vec3 tangent = decode(texture2DRect(MaterialParams1Buffer, gl_FragCoord.xy).yz);	
		
		normalize(tangent);
		tangent -= dot(tangent, normal) * normal;
		normalize(tangent);
		
		vec3 bitangent = cross(normal, tangent)*handedness;
		gl_FragColor = vec4(0.5*bitangent+vec3(0.5),1.0);
	}
	else {
		gl_FragColor = vec4(0.0);
	}
}

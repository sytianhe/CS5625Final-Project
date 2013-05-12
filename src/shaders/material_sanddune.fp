/**
 * material_sanddune.fp
 * 
 * Fragment shader shader which writes material information needed for Lambertian shading to
 * the gbuffer.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-27
 */

/* ID of Lambertian material, so the lighting shader knows what material
 * this pixel is. */
const int LAMBERTIAN_MATERIAL_ID = 2;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;

/* Textures and flags for whether they exist. */
uniform float SamplerWidth;
uniform float SamplerHeight;
uniform sampler2DRect SandDuneHeightMap;
uniform bool HasSandDuneHeightMap;

/* Fragment position, normal, and texcoord passed from the vertex shader. */
varying vec3 EyespacePosition;
varying vec3 EyespaceNormal;
varying vec2 TexCoord;

/* Encodes a normalized vector as a vec2. See Renderer.java for more info. */
vec2 encode(vec3 n)
{
	return normalize(n.xy) * sqrt(0.5 * n.z + 0.5);
}

void main()
{
	
	vec2 enc = encode(normalize(EyespaceNormal));
	
	if (HasSandDuneHeightMap) {
	
		float h11 = texture2DRect(SandDuneHeightMap, TexCoord).x;		
    	float h01 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(-1,0) ).x;
    	float h10 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(0,-1) ).x;
    	float h21 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(1,0) ).x;
    	float h12 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(0,1) ).x;
    	
    	
    	float h00 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(-1,-1) ).x;
    	float h20 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(1,-1) ).x;
    	float h02 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(-1,1) ).x;
    	float h22 = texture2DRect(SandDuneHeightMap, TexCoord + vec2(1,1) ).x;    	
    	
    	
    	vec3 va = normalize(vec3(2.0,h21-h01,0.0));
    	vec3 vb = normalize(vec3(0.0,h12-h10,2.0));
    	vec3 bump = cross(va,vb);
    	//enc = encode(normalize(gl_NormalMatrix * bump));

	}
	gl_FragData[0] = vec4(DiffuseColor, enc.x);

	gl_FragData[1] = vec4(EyespacePosition, enc.y);
	gl_FragData[2] = vec4(float(LAMBERTIAN_MATERIAL_ID), 0.0, 0.0, 0.0);
	gl_FragData[3] = vec4(0.0);
}

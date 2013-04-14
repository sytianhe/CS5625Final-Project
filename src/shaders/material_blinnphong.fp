/**
 * material_blinnphong.fp
 * 
 * Fragment shader which writes material information needed for Blinn-Phong shading to
 * the gbuffer.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-24
 */

/* ID of Blinn-Phong material, so the lighting shader knows what material
 * this pixel is. */
const int BLINNPHONG_MATERIAL_ID = 3;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;
uniform vec3 SpecularColor; 
uniform float PhongExponent;

/* Textures and flags for whether they exist. */
uniform sampler2D DiffuseTexture;
uniform sampler2D SpecularTexture;
uniform sampler2D ExponentTexture;

uniform bool HasDiffuseTexture;
uniform bool HasSpecularTexture;
uniform bool HasExponentTexture;

/* Fragment position and normal, and texcoord, from vertex shader. */
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
	// TODO PA3 Prereq: Store diffuse color, position, encoded normal, material ID, and all other useful data in the g-buffer.
	vec2 enc = encode(normalize(EyespaceNormal));
	
	if (HasDiffuseTexture) {
		gl_FragData[0] = vec4(texture2D(DiffuseTexture, TexCoord).xyz * DiffuseColor, enc.x);
	}
	else {
		gl_FragData[0] = vec4(DiffuseColor, enc.x);
	}
	
	gl_FragData[1] = vec4(EyespacePosition, enc.y);
	
	gl_FragData[2] = vec4(float(BLINNPHONG_MATERIAL_ID), 0.0, 0.0, 0.0);
	
	if (HasSpecularTexture) {
		gl_FragData[3] = vec4(texture2D(SpecularTexture, TexCoord).xyz*SpecularColor, 0.0);
	}
	else {
		gl_FragData[3] = vec4(SpecularColor, 0.0);
	}
	
	if (HasExponentTexture) {
		gl_FragData[3].a = texture2D(ExponentTexture, TexCoord).x*255.0;
	}
	else {
		gl_FragData[3].a = PhongExponent;
	}
}

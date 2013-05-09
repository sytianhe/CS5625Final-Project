/**
 * material_normal_map.fp
 * 
 * Vertex shader shader which writes material information needed for Normal Map shading to
 * the gbuffer.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), John DeCorato (jd537), Sean Ryan (ser99), Ivo Boyadzhiev (iib2)
 * @date 2013-02-2012
 */
 
 #version 110
 
 /* ID of Blinn-Phong material, since the normal map only effects things pre-color computation. */
const int BLINNPHONG_MATERIAL_ID = 3;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;
uniform vec3 SpecularColor;
uniform float PhongExponent;

/* Textures and flags for whether they exist. */
uniform sampler2D DiffuseTexture;
uniform sampler2D SpecularTexture;
uniform sampler2D ExponentTexture;
uniform sampler2D NormalTexture;
uniform sampler2D HeightTexture;

uniform bool HasDiffuseTexture;
uniform bool HasSpecularTexture;
uniform bool HasExponentTexture;
uniform bool HasNormalTexture;
uniform bool HasHeightTexture;

varying vec3 EyespacePosition;
varying vec3 EyespaceNormal;
varying vec2 TexCoord;

varying vec3 EyespaceTangent;
varying vec3 EyespaceBiTangent;
varying float handedness;

/* Encodes a normalized vector as a vec2. See Renderer.java for more info. */
vec2 encode(vec3 n)
{
	return normalize(n.xy) * sqrt(0.5 * n.z + 0.5);
}

void main()
{
	// TODO PA2: Store diffuse color, position, encoded normal, material ID, and all other useful data in the g-buffer.
	//			 Use the normal map and tangent vectors to get a new normal.
	
	// Find offset for TexCoord:
	vec2 texCoord = TexCoord;
	if (HasHeightTexture) {
		float height = texture2D(HeightTexture, TexCoord).r;
		float v = height * 0.04 - 0.02;
		texCoord = TexCoord - 2.0*normalize(EyespacePosition).xy*v;
		//texCoord = vec2(height,0);
	}
	
	vec2 enc;
	
	if (HasNormalTexture) {
		vec3 normal = normalize(EyespaceNormal);
		vec3 tangent = normalize(EyespaceTangent);
		vec3 bitangent = normalize(EyespaceBiTangent);
		
		vec3 tangent_orth = tangent - dot(normal, tangent) * normal;
		normalize(tangent_orth);		
		//vec3 bitangent_orth = bitangent - (dot(normal, bitangent)*normal) - (dot(tangent_orth, bitangent))*tangent_orth/dot(tangent_orth, tangent_orth);
		
		vec3 bitangent_orth = handedness * cross(normal, tangent_orth);
		//normalize(bitangent_orth);
		
		vec3 color = normalize(texture2D(NormalTexture, texCoord).xyz-vec3(0.5));
		mat3 tbn = mat3(tangent, bitangent, normal);
		vec3 res = normalize(tbn*color);
		
		enc = encode(res);
		
	}
	else {	
		enc = encode(normalize(EyespaceNormal));
	}
	
	if (HasDiffuseTexture) {
		gl_FragData[0] = vec4(texture2D(DiffuseTexture, texCoord).xyz * DiffuseColor, enc.x);
	}
	else {
		gl_FragData[0] = vec4(DiffuseColor, enc.x);
	}
	
	gl_FragData[1] = vec4(EyespacePosition, enc.y);
	
	gl_FragData[2] = vec4(float(BLINNPHONG_MATERIAL_ID), 0.0, 0.0, 0.0);
	
	if (HasSpecularTexture) {
		gl_FragData[3] = vec4(texture2D(SpecularTexture, texCoord).xyz*SpecularColor, 0.0);
	}
	else {
		gl_FragData[3] = vec4(SpecularColor, 0.0);
	}
	
	if (HasExponentTexture) {
		gl_FragData[3].a = texture2D(ExponentTexture, texCoord).x*255.0;
	}
	else {
		gl_FragData[3].a = PhongExponent;
	}
}
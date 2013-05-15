/**
 * material_bark.fp
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
 
 //#version 110

/** A lambertian like material. */ 
const int LAMBERTIAN_MATERIAL_ID = 2;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;

/* Textures and flags for whether they exist. */
uniform sampler2D DiffuseTexture;
uniform bool HasDiffuseTexture;
uniform vec2 TextureSize;

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

/* Apply standard transform to height. */
float normalizeHeight(float h)
{
	return 20.0*(h)  ;
}

/* Get height from diffuse texture. */
float getHeightFromDiffuse(vec2 coord)
{
	vec3 h = texture2D(DiffuseTexture, coord).xyz;
	return normalizeHeight(  (h.x + h.y + h.z)/3.0  );
}


void main()
{

	//Compute height from frag brightness
	// Find offset for TexCoord:
	
	vec2 texCoord = TexCoord;
	vec2 enc;
	
	if (HasDiffuseTexture) {
	
		//setup normal mapping
		vec3 normal = normalize(EyespaceNormal);
		vec3 tangent = normalize(EyespaceTangent);
		vec3 bitangent = normalize(EyespaceBiTangent);
		
		vec3 tangent_orth = normalize( tangent - dot(normal, tangent) * normal );
		vec3 bitangent_orth =  cross(normal, tangent_orth);
		mat3 tbn = mat3(tangent, bitangent, normal);
		
		
		//Get heights in the neighborhood of the frag
		float h = getHeightFromDiffuse(TexCoord);
		vec3 eyeDir =  - normalize( EyespacePosition * tbn).xyz;
		texCoord = TexCoord +  0.02 * (eyeDir.xy * h ) / TextureSize    ;
		
		//compute normal map from height map, and compute height map from diffuse texture.
		float h11 = getHeightFromDiffuse(texCoord);		
    	float h01 = getHeightFromDiffuse(texCoord + vec2(-1.0,.0)/TextureSize );
    	float h10 = getHeightFromDiffuse(texCoord + vec2(0.0,-1.0)/TextureSize );
    	float h21 = getHeightFromDiffuse(texCoord + vec2(1.0,0.0)/TextureSize );
    	float h12 = getHeightFromDiffuse(texCoord + vec2(0.0,1.0)/TextureSize );
    	
    	vec3 va = normalize(vec3(2.0,0.0, ( h21-h01 )) );
    	vec3 vb = normalize(vec3(0.0,2.0, ( h12-h10 )) );
    	
    	vec3 bump = cross(va,vb);
		vec3 res = normalize(tbn*bump);
		
		enc = encode(res);
		enc = encode(normalize(EyespaceNormal));
		
		gl_FragData[0] = vec4(texture2D(DiffuseTexture, TexCoord).xyz * DiffuseColor, enc.x);
		
	}
	else {	
		enc = encode(normalize(EyespaceNormal));
		gl_FragData[0] = vec4(DiffuseColor, enc.x);
	}
	
	gl_FragData[1] = vec4(EyespacePosition, enc.y);
	gl_FragData[2] = vec4(float(LAMBERTIAN_MATERIAL_ID), 0.0, 0.0, 0.0);
	gl_FragData[3] = vec4(0.0);
	
}
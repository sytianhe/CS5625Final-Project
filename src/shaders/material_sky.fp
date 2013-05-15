/**
 * material_sky.fp
 * 
 * 
 * @author Tianhe Zhang (tz249)
 * @date 2013-05-14
 */

/* ID of unshaded material, so the lighting shader knows what material
 * this pixel is. */
const int SKY_MATERIAL_ID = 4;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;
uniform sampler2D DiffuseTexture;
uniform sampler2D StarTexture;
uniform bool HasDiffuseTexture;
uniform bool HasStarTexture;
uniform vec3 SunPosition;
uniform float DayTime;


/* Fragment position and normal passed from the vertex shader. */
varying vec3 EyespacePosition;
varying vec3 EyespaceNormal;
varying vec2 TexCoord;
varying vec3 EyespaceSunPosition;

/* Encodes a normalized vector as a vec2. See Renderer.java for more info. */
vec2 encode(vec3 n)
{
	return normalize(n.xy) * sqrt(0.5 * n.z + 0.5);
}

void main()
{
	/* Encode the eyespace normal. */
	vec2 enc = encode(normalize(EyespaceNormal));
	
	/* Store diffuse, position, encoded normal, and the material ID into the gbuffer. Position
	 * and normal aren't used for shading, but they might be required by a post-processing effect,
	 * so we still have to write them out. */
	if (HasDiffuseTexture) {
		gl_FragData[0] = vec4(texture2D(DiffuseTexture, TexCoord).xyz * DiffuseColor, enc.x);
	}
	else {
		gl_FragData[0] = vec4(DiffuseColor, enc.x);
	}
	gl_FragData[1] = vec4(EyespacePosition, enc.y);
	if (HasStarTexture){
		gl_FragData[2] = vec4(float(SKY_MATERIAL_ID), texture2D(StarTexture, TexCoord).xyz );//* DiffuseColor);	
	}
	else{
		gl_FragData[2] = vec4(float(SKY_MATERIAL_ID), 0.0, 0.0, 0.0);
	}
	gl_FragData[3] = vec4(EyespaceSunPosition, DayTime);
}
/**
 * ubershader.fp
 * 
 * Fragment shader for the "ubershader" which lights the contents of the gbuffer. This shader
 * samples from the gbuffer and then computes lighting depending on the material type of this 
 * fragment.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Ivaylo Boyadzhiev (iib2)
 * @date 2012-03-24
 */

/* Copy the IDs of any new materials here. */
const int UNSHADED_MATERIAL_ID = 1;
const int LAMBERTIAN_MATERIAL_ID = 2;
const int BLINNPHONG_MATERIAL_ID = 3;
const int SKY_MATERIAL_ID = 4;

/* Some constant maximum number of lights which GLSL and Java have to agree on. */
#define MAX_LIGHTS 40

/* Samplers for each texture of the GBuffer. */
uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;
uniform sampler2DRect MaterialParams1Buffer;
uniform sampler2DRect MaterialParams2Buffer;
uniform sampler2DRect SilhouetteBuffer;
uniform sampler2DRect SSAOBuffer;
uniform sampler2DRect SandDune1Buffer;
uniform sampler2DRect SandDune2Buffer;


uniform bool EnableToonShading;
uniform bool EnableSSAO;

/* Uniform specifying the sky (background) color. */
uniform vec3 SkyColor;

/* Uniforms describing the lights. */
uniform int NumLights;
uniform vec3 LightPositions[MAX_LIGHTS];
uniform vec3 LightAttenuations[MAX_LIGHTS];
uniform vec3 LightColors[MAX_LIGHTS];

/* Shadow depth textures and information */
uniform int HasShadowMaps;
uniform sampler2D ShadowMap;
uniform int ShadowMode;
uniform vec3 ShadowCamPosition;
uniform float bias;
uniform float ShadowSampleWidth;
uniform float ShadowMapWidth;
uniform float ShadowMapHeight;
uniform float LightWidth;

#define DEFAULT_SHADOW_MAP 0
#define PCF_SHADOW_MAP 1
#define PCSS SHADOW_MAP 2

/* Pass the shadow camera Projection * View matrix to help transform points, as well the Camera inverse-view Matrix */
uniform mat4 LightMatrix;
uniform mat4 InverseViewMatrix;

/* Decodes a vec2 into a normalized vector See Renderer.java for more info. */
vec3 decode(vec2 v)
{
	vec3 n;
	n.z = 2.0 * dot(v.xy, v.xy) - 1.0;
	n.xy = normalize(v.xy) * sqrt(1.0 - n.z*n.z);
	return n;
}

// Converts the depth buffer value to a linear value
float DepthToLinear(float value)
{
	float near = 0.1;
	float far = 100.0;
	return (2.0 * near) / (far + near - value * (far - near));
}

/** Returns a binary value for if this location is shadowed. 0 = shadowed, 1 = not shadowed.
 */
float getShadowVal(vec4 shadowCoord, vec2 offset) 
{
	// TODO PA3: Implement this function (see above).

	return 1.0;
}

/** Calculates regular shadow map algorithm shadow strength
 *
 * @param shadowCoord The location of the position in the light projection space
 */
 float getDefaultShadowMapVal(vec4 shadowCoord)
 {
 	// TODO PA3: Implement this function (see above).
 	float shadowDist = texture2D(ShadowMap, shadowCoord.xy).x;
 	
 	if(shadowCoord.z-shadowDist < bias){
 		return 1.0;
 	}
	else{
		return 0.0;
	}
 }
 
/** Calculates PCF shadow map algorithm shadow strength
 *
 * @param shadowCoord The location of the position in the light projection space
 */
 float getPCFShadowMapVal(vec4 shadowCoord)
 {
 	// TODO PA3: Implement this function (see above).
 	float Num = (2.0*ShadowSampleWidth + 1.0);
 	float sum = 0.0;
 	for (float i = -ShadowSampleWidth; i<= ShadowSampleWidth; i = i+1.0){
 		for (float j = -ShadowSampleWidth; j <= ShadowSampleWidth; j = j+1.0){
 		
 			float shadowDist = texture2D(ShadowMap, vec2(shadowCoord.x + i/ShadowMapWidth, shadowCoord.y + j/ShadowMapHeight)).x; 
 			if( shadowCoord.z-shadowDist < bias){
 				sum += 1.0;
 			}
 		}
 	}
 	
 	return sum/(Num*Num);
 }
 
 /** Calculates PCSS shadow map algorithm shadow strength
 *
 * @param shadowCoord The location of the position in the light projection space
 */
 float getPCSSShadowMapVal(vec4 shadowCoord)
 {
 	float near = 0.1;
 	float far = 100.0;
 	
 	// TODO PA3: Implement this function (see above).
 	
 	// Get the kernal width
 	float kernalWidth = LightWidth * (DepthToLinear(shadowCoord.z) - near)/DepthToLinear(shadowCoord.z)/2.0;
 	
 	// Get the blocker depth
 	float blockerDepth = 0.0;
 	int counter = 0;
 	int totCounter = 0;
 	
 	for (float i = -kernalWidth; i<= kernalWidth; i = i+1.0){
 		for (float j = -kernalWidth; j <= kernalWidth; j = j+1.0){
 		
 			float shadowDist = texture2D(ShadowMap, vec2(shadowCoord.x + i/ShadowMapWidth, shadowCoord.y + j/ShadowMapHeight)).x; 
 			if(shadowCoord.z-shadowDist > bias){
 				blockerDepth += DepthToLinear(shadowDist); // decompress depth
 				counter += 1;
 			}
 			totCounter += 1;
 		}
 	}
 	
 	// Check if it is totally in the shadow
 	if (totCounter == counter){
 		return 0.0;
 	}
 	
 	// Check if it is totally lit
 	if (counter == 0){
 		return 1.0;
 	}
 	
 	// Average blocker depth
 	blockerDepth = blockerDepth / (float (counter));
 	
 	// Find Penumbra estimation
 	float penumbraWidth = LightWidth * (DepthToLinear(shadowCoord.z) - blockerDepth) / blockerDepth;
 	
 	// Get the kernal width using penumbra estimation
 	//float newKernalWidth = penumbraWidth * near / DepthToLinear(shadowCoord.z) / 2.0;
 	float newKernalWidth = penumbraWidth / 2.0;
 	
 	// Apply normal PCF
 	float newSum = 0.0;
 	float newTotSum = 0.0;
 	for (float i = -newKernalWidth; i<= newKernalWidth; i = i+1.0){
 		for (float j = -newKernalWidth; j <= newKernalWidth; j = j+1.0){
 		
 			float shadowDist = texture2D(ShadowMap, vec2(shadowCoord.x + i/ShadowMapWidth, shadowCoord.y + j/ShadowMapHeight)).x; 
 			if(shadowCoord.z-shadowDist < bias){
 				newSum += 1.0;
 			}
 			newTotSum += 1.0;
 		}
 	}
 	
 	// Return percentage
 	return newSum/newTotSum;
 }

/** Gets the shadow value based on the current shadowing mode
 *
 * @param position The eyespace position of the surface at this fragment
 *
 * @return A 0-1 value for how shadowed the object is. 0 = shadowed and 1 = lit
 */
float getShadowStrength(vec3 position) {
	// TODO PA3: Transform position to ShadowCoord
	
	vec4 ShadowCoord = vec4(position.x, position.y, position.z, 1.0);
	ShadowCoord = InverseViewMatrix * ShadowCoord;
	ShadowCoord = LightMatrix * ShadowCoord;
	
	mat4 biasMatrix;
	biasMatrix[0] = vec4(0.5, 0.0, 0.0, 0.0);
	biasMatrix[1] = vec4(0.0, 0.5, 0.0, 0.0);
	biasMatrix[2] = vec4(0.0, 0.0, 0.5, 0.0);
	biasMatrix[3] = vec4(0.5, 0.5, 0.5, 1.0);
	ShadowCoord = biasMatrix*ShadowCoord;
	
	// Normalize by dividing w.
	ShadowCoord = ShadowCoord/ShadowCoord.w;
	
	if (ShadowMode == DEFAULT_SHADOW_MAP)
	{
		return getDefaultShadowMapVal(ShadowCoord);
	}
	else if (ShadowMode == PCF_SHADOW_MAP)
	{
		return getPCFShadowMapVal(ShadowCoord);
	}
	else
	{
		return getPCSSShadowMapVal(ShadowCoord);
	}
}

/**
 * Performs the "3x3 nonlinear filter" mentioned in Decaudin 1996 to detect silhouettes
 * based on the silhouette buffer.
 */
float silhouetteStrength()
{
	// TODO PA3 Prereq (Optional): Paste in your silhouetteStrength code if you like toon shading.
	float k = 0.3;
	vec4 gMin = vec4(1.0);
	vec4 gMax = vec4(0.0);	
	vec4 color = vec4(0.0);
	for(int i = int(gl_FragCoord.x)-1; i<=int(gl_FragCoord.x)+1; i++){
		for(int j = int(gl_FragCoord.y)-1; j<=int(gl_FragCoord.y)+1; j++){
			color = texture2DRect(SilhouetteBuffer, vec2(float(i),float(j)));
			gMin.x = min(gMin.x, color.x); gMax.x = max(gMax.x, color.x);
			gMin.y= min(gMin.y, color.y); gMax.y = max(gMax.y, color.y);
			gMin.z = min(gMin.z, color.z); gMax.z = max(gMax.z, color.z);
			gMin.w = min(gMin.w, color.w); gMax.w = max(gMax.w, color.w);
		}
	}
	vec4 g = (gMax - gMin)/k;
	return min(pow(length(g), 2.0),1.0);
}

/**
 * Performs Lambertian shading on the passed fragment data (color, normal, etc.) for a single light.
 * 
 * @param diffuse The diffuse color of the material at this fragment.
 * @param position The eyespace position of the surface at this fragment.
 * @param normal The eyespace normal of the surface at this fragment.
 * @param lightPosition The eyespace position of the light to compute lighting from.
 * @param lightColor The color of the light to apply.
 * @param lightAttenuation A vectr of (constant, linear, quadratic) attenuation coefficients for this light.
 * 
 * @return The shaded fragment color; for Lambertian, this is `lightColor * diffuse * n_dot_l`.
 */
vec3 shadeLambertian(vec3 diffuse, vec3 position, vec3 normal, vec3 lightPosition, vec3 lightColor, vec3 lightAttenuation)
{
	vec3 lightDirection = normalize(lightPosition - position);
	float ndotl = max(0.0, dot(normal, lightDirection));

	// TODO PA3 Prereq (Optional): Paste in your n.l and n.h thresholding code if you like toon shading.
	if (EnableToonShading){	 	
		ndotl = step(0.1, ndotl);	 	
	}
	
	float r = length(lightPosition - position);
	float attenuation = 1.0 / dot(lightAttenuation, vec3(1.0, r, r * r));
	
	return lightColor * attenuation * diffuse * ndotl;
}

/**
 * Performs Blinn-Phong shading on the passed fragment data (color, normal, etc.) for a single light.
 *  
 * @param diffuse The diffuse color of the material at this fragment.
 * @param specular The specular color of the material at this fragment.
 * @param exponent The Phong exponent packed into the alpha channel. 
 * @param position The eyespace position of the surface at this fragment.
 * @param normal The eyespace normal of the surface at this fragment.
 * @param lightPosition The eyespace position of the light to compute lighting from.
 * @param lightColor The color of the light to apply.
 * @param lightAttenuation A vector of (constant, linear, quadratic) attenuation coefficients for this light.
 * 
 * @return The shaded fragment color.
 */
vec3 shadeBlinnPhong(vec3 diffuse, vec3 specular, float exponent, vec3 position, vec3 normal,
	vec3 lightPosition, vec3 lightColor, vec3 lightAttenuation)
{
	vec3 viewDirection = -normalize(position);
	vec3 lightDirection = normalize(lightPosition - position);
	vec3 halfDirection = normalize(lightDirection + viewDirection);
		
	float ndotl = max(0.0, dot(normal, lightDirection));
	float ndoth = max(0.0, dot(normal, halfDirection));
	
	// TODO PA3 Prereq (Optional): Paste in your n.l and n.h thresholding code if you like toon shading.
	
	float pow_ndoth = (ndotl > 0.0 && ndoth > 0.0 ? pow(ndoth, exponent) : 0.0);

	float r = length(lightPosition - position);
	float attenuation = 1.0 / dot(lightAttenuation, vec3(1.0, r, r * r));
	
	return lightColor * attenuation * (diffuse * ndotl + specular * pow_ndoth);
}

/**
 * Performs Blinn-Phong shading on the passed fragment data (color, normal, etc.) for a single light.
 *  
 * @param diffuse The diffuse color of the material at this fragment.
 * @param position of the sun
 * @param daytime
 * @return The shaded fragment color.
 */
vec3 shadeSky(vec3 diffuse, vec3 sunPosition, float daytime)
{
	// Apply the idea of skybox in paper of Braun and Cohen
	
	// SETUP min and max luminance for each channel
	float lmin_R = 75.0;
	float lmax_R = 228.0;
	float lmin_G = 35.0;
	float lmax_G = 192.0;
	float lmin_B = 75.0;
	float lmax_B = 256.0;
	
	// SETUP start time of sunrise and sunset
	float sunRiseSt = 5.0; // 5:00 AM
	float sunSetSt = 17.0; // 5:00 PM
	
	// SETUP the interval of sunrise and sunset (in hours);
	float sunRiseLength = 3.0;
	float sunSetLength = 4.0;
	
	// CALCULATE sunrise and sunset luminance changing rate.
	float sunRiseRate_R = (lmax_R-lmin_R)/sunRiseLength;
	float sunRiseRate_G = (lmax_G-lmin_G)/sunRiseLength;
	float sunRiseRate_B = (lmax_B-lmin_B)/sunRiseLength;
	
	float sunSetRate_R = (lmax_R-lmin_R)/sunSetLength;
	float sunSetRate_G = (lmax_G-lmin_G)/sunSetLength;
	float sunSetRate_B = (lmax_B-lmin_B)/sunSetLength;
	
	vec3 color;
	if ((daytime > sunRiseSt) && (daytime <= sunRiseSt + sunRiseLength))
	{
		color = vec3(diffuse.x * (lmin_R + (daytime - sunRiseSt)*sunRiseRate_R)/256.0, 
					 diffuse.y * (lmin_G + (daytime - sunRiseSt)*sunRiseRate_G)/256.0,
					 diffuse.z * (lmin_B + (daytime - sunRiseSt)*sunRiseRate_B)/256.0);
	}
	else if ((daytime > sunRiseSt + sunRiseLength) && (daytime <= sunSetSt))
	{
		color = vec3(diffuse.x * lmax_R /256.0, 
					 diffuse.y * lmax_G /256.0, 
					 diffuse.z * lmax_B /256.0);
	}
	else if ((daytime > sunSetSt) && (daytime <= sunSetSt + sunSetLength))
	{
		color = vec3(diffuse.x * (lmax_R - (daytime - sunSetSt)*sunSetRate_R)/256.0, 
					 diffuse.y * (lmax_G - (daytime - sunSetSt)*sunSetRate_G)/256.0,
					 diffuse.z * (lmax_B - (daytime - sunSetSt)*sunSetRate_B)/256.0);
	}
	else {
		color = vec3(diffuse.x * lmin_R /256.0, 
					 diffuse.y * lmin_G /256.0, 
					 diffuse.z * lmin_B /256.0);	
	}
	return color;
}

void main()
{
	/* Sample gbuffer. */
	vec3 diffuse         = texture2DRect(DiffuseBuffer, gl_FragCoord.xy).xyz;
	vec3 position        = texture2DRect(PositionBuffer, gl_FragCoord.xy).xyz;
	vec4 materialParams1 = texture2DRect(MaterialParams1Buffer, gl_FragCoord.xy);
	vec4 materialParams2 = texture2DRect(MaterialParams2Buffer, gl_FragCoord.xy);
	vec3 normal          = decode(vec2(texture2DRect(DiffuseBuffer, gl_FragCoord.xy).a,
	                                   texture2DRect(PositionBuffer, gl_FragCoord.xy).a));
	
	/* Initialize fragment to black. */
	gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);

	/* Branch on material ID and shade as appropriate. */
	int materialID = int(materialParams1.x);

	if (materialID == 0)
	{
		/* Must be a fragment with no geometry, so set to sky (background) color. */
		gl_FragColor = vec4(SkyColor, 1.0);
	}
	else if (materialID == UNSHADED_MATERIAL_ID)
	{
		/* Unshaded material is just a constant color. */
		gl_FragColor.rgb = diffuse;
	}
	else if (materialID == LAMBERTIAN_MATERIAL_ID)
	{
		/* Accumulate Lambertian shading for each light. */
		for (int i = 0; i < NumLights; ++i)
		{
			gl_FragColor.rgb += shadeLambertian(diffuse, position, normal, LightPositions[i], LightColors[i], LightAttenuations[i]);
		}
	}
	else if (materialID == BLINNPHONG_MATERIAL_ID)
	{
		/* Accumulate Blinn-Phong shading for each light. */
		for (int i = 0; i < NumLights; ++i)
		{
			gl_FragColor.rgb += shadeBlinnPhong(diffuse, materialParams2.rgb, materialParams2.a,
				position, normal, LightPositions[i], LightColors[i], LightAttenuations[i]);
		}
	}
	else if (materialID == SKY_MATERIAL_ID)
	{
		gl_FragColor.rgb = shadeSky(diffuse, materialParams2.xyz, materialParams2.w);
	}
	else
	{
		/* Unknown material, so just use the diffuse color. */
		gl_FragColor.rgb = diffuse;
	}

	if (EnableToonShading)
	{
		gl_FragColor.rgb = mix(gl_FragColor.rgb, vec3(0.0), silhouetteStrength()); 
	}
	
	if (HasShadowMaps == 1 && materialID != 0) {	
		gl_FragColor.rgb *= getShadowStrength(position);
	}
	
	if (EnableSSAO)
	{
		gl_FragColor.rgb *= texture2DRect(SSAOBuffer, gl_FragCoord.xy).rgb;
	}
	
}

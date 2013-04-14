/**
 * silhouette.fp
 * 
 * Fragment shader for calculating silhouette edges as described in Decaudin's 1996 paper.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-04-01
 */

uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;

/* Decodes a vec2 into a normalized vector See Renderer.java for more info. */
vec3 decode(vec2 v)
{
	vec3 n;
	n.z = 2.0 * dot(v.xy, v.xy) - 1.0;
	n.xy = normalize(v.xy) * sqrt(1.0 - n.z*n.z);
	return n;
}

/**
 * Samples from position and normal buffer and returns (nx, ny, nz, depth) packed into one vec4.
 */
vec4 sample(vec2 coord)
{
	vec3 n = decode(vec2(texture2DRect(DiffuseBuffer, coord).a, texture2DRect(PositionBuffer, coord).a));
	return vec4(n, texture2DRect(PositionBuffer, coord).z);
}

void main()
{
	// TODO PA3 Prereq (Optional): Paste in your silhouette shader code if you like toon shading.
	vec2 center = vec2(gl_FragCoord.xy);
	vec2 A = center + vec2(-1.0, 1.0);
	vec2 B = center + vec2(0.0, 1.0);
	vec2 C = center + vec2(1.0, 1.0);
	vec2 D = center + vec2(-1.0, 0.0);
	vec2 E = center + vec2(1.0, 0.0);
	vec2 F = center + vec2(-1.0, -1.0);
	vec2 G = center + vec2(0.0, -1.0);
	vec2 H = center + vec2(1.0, -1.0);
	
	vec4 g = 1.0/8.0*(abs(sample(A)-sample(center))
					+2.0*abs(sample(B)-sample(center))
					+abs(sample(C)-sample(center))
					+2.0*abs(sample(D)-sample(center))
					+2.0*abs(sample(E)-sample(center))
					+abs(sample(F)-sample(center))
					+2.0*abs(sample(G)-sample(center))
					+abs(sample(H)-sample(center)));
	
	gl_FragColor = g;	
}

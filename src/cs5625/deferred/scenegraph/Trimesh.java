package cs5625.deferred.scenegraph;

import java.nio.FloatBuffer;
import java.util.HashMap;

import cs5625.deferred.misc.OpenGLResourceObject;

/**
 * Trimesh.java
 * 
 * Trimesh subclasses Mesh to implement a mesh of triangles. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class Trimesh extends Mesh implements OpenGLResourceObject
{
	@Override
	public int getVerticesPerPolygon()
	{
		return 3;
	}	
	
	/**
	 * Calculates tangent vectors for this triangle mesh.
	 * 
	 * Algorithm source: Lengyel, Eric. �Computing Tangent Space Basis Vectors for an Arbitrary Mesh�. 
	 * Terathon Software 3D Graphics Library, 2001. http://www.terathon.com/code/tangent.html.
	 */
	@Override
	public FloatBuffer calculateTangentVectors()
	{
		if (getVertexCount() == 0 || getPolygonCount() == 0)
		{
			return null;
		}
		
		/* This is a very direct port of Eric Lengyel's code, so I leave most of the explanation
		 * to him. Go read his page if you don't understand! */
		
		/* Allocate temporary buffers and loop over triangles. */
		float tan1[] = new float[getVertexCount() * 3];
		float tan2[] = new float[getVertexCount() * 3];
		
		for (int triIndex = 0; triIndex < getPolygonCount(); ++triIndex)
		{
			/* Get vertex indices of this triangle. */
			int i1 = mPolygonData.get(3 * triIndex + 0);
			int i2 = mPolygonData.get(3 * triIndex + 1);
			int i3 = mPolygonData.get(3 * triIndex + 2);
			
			computeAndAccumulateTangentVectors(i1, i2, i3, tan1, tan2);			
		}

		/* Average and normalize the accumulated tangent vectors. */
		return averageAndNormalizeAllTangentVectors(tan1, tan2);
	}
	
	/**
	 * Creates a shallow copy of the given mesh (it will share references to all member data).
	 * This allows us to do useful things like create many instances of some object with
	 * different names and materials but only one set of float buffers for all instances.
	 */
	@SuppressWarnings("unchecked")
	public Mesh clone()
	{
		Trimesh copy = new Trimesh();
		copy.setName(getName());
		copy.setMaterial(getMaterial());
		copy.setEdgeData(getEdgeData());
		copy.setPolygonData(getPolygonData());
		copy.setTexCoordData(getTexCoordData());
		copy.setVertexData(getVertexData());
		copy.setNormalData(getNormalData());
		copy.vertexAttribData = (HashMap<String, FloatBuffer>) vertexAttribData.clone();
		
		return copy;
	}
}

package cs5625.deferred.scenegraph;

import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * Quadmesh.java
 * 
 * Quadmesh subclasses Mesh to implement a mesh of quadrilaterals. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-04-06
 */
public class Quadmesh extends Mesh
{
	@Override
	public int getVerticesPerPolygon()
	{
		return 4;
	}	
	
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
		
		for (int quadIndex = 0; quadIndex < getPolygonCount(); ++quadIndex)
		{
			/* Treat this quad as being made of two triangles. */
			for (int triVertex = 1; triVertex <= 2; ++triVertex)
			{
				/* Get vertex indices of this triangle. */
				int i1 = mPolygonData.get(3 * quadIndex + 0);
				int i2 = mPolygonData.get(3 * quadIndex + triVertex);
				int i3 = mPolygonData.get(3 * quadIndex + triVertex + 1);				
				
				computeAndAccumulateTangentVectors(i1, i2, i3, tan1, tan2);				
			}
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
		Quadmesh copy = new Quadmesh();
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

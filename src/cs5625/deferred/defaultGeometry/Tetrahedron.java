package cs5625.deferred.defaultGeometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Trimesh;
//import cs5625.deferred.scenegraph.Quadmesh;

public class Tetrahedron {

private Trimesh mMesh;
	
	private static final float[] cubeVerts = new float[] { 	0.0f, 0.0f, 0.0f,
															0.0f, 1.0f, 0.0f, 
															1.0f, 0.0f, 0.0f, 
															1.0f, 1.0f, 0.0f,
															0.0f, 0.0f, 1.0f,
															0.0f, 1.0f, 1.0f, 
															1.0f, 0.0f, 1.0f, 
															1.0f, 1.0f, 1.0f};
	
	private static final int[] tetraTris= new int[] { 	0, 3, 5,
														0, 3, 6,
														5, 6, 0,
														5, 6, 3};
	
	public Tetrahedron(){
		final int numVertices = 8;
		final int numTris = 4;
		
		FloatBuffer positionData = Buffers.newDirectFloatBuffer(3 * numVertices);
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		positionData.put(cubeVerts);
		
		IntBuffer indexData = Buffers.newDirectIntBuffer(3 * numTris);
		
		indexData.put(tetraTris);
		
		positionData.rewind();
		indexData.rewind();
		
		this.mMesh = new Trimesh();
		this.mMesh.setVertexData(positionData);
		this.mMesh.setPolygonData(indexData);
		this.mMesh.setNormalData(normalData);
		this.mMesh.setTexCoordData(texCoordData);		
	}
	
	public Trimesh getTriMesh(){
		return mMesh;
	}
	
}

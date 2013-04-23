package cs5625.deferred.defaultGeometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Quadmesh;

public class FourQuadMesh extends Quadmesh{

	private Quadmesh mMesh;
	
	private static final float[] planeVerts = new float[] { 0.0f, 0.0f, 0.0f,
															0.0f, 0.5f, 0.0f, 
															0.0f, 1.0f, 0.0f, 
															0.5f, 0.0f, 0.0f,
															0.5f, 0.5f, 0.0f,
															0.5f, 1.0f, 0.0f,
															1.0f, 0.0f, 0.0f,
															1.0f, 0.5f, 0.0f,
															1.0f, 1.0f, 0.0f,};
	
	private static final int[] planeQuads= new int[] { 	0, 1, 4, 3,
														4, 7, 6, 3,
														2, 5, 4, 1,
														5, 8, 7, 4};
	
	public FourQuadMesh(){
		final int numVertices = 9;
		final int numQuads = 4;
		
		FloatBuffer positionData = Buffers.newDirectFloatBuffer(3 * numVertices);
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		positionData.put(planeVerts);
		
		IntBuffer indexData = Buffers.newDirectIntBuffer(4 * numQuads);
		
		indexData.put(planeQuads);
		
		positionData.rewind();
		indexData.rewind();
		
		this.mMesh = new Quadmesh();
		this.mMesh.setVertexData(positionData);
		this.mMesh.setPolygonData(indexData);
		this.mMesh.setNormalData(normalData);
		this.mMesh.setTexCoordData(texCoordData);		
	}
	
	public Quadmesh getTriMesh(){
		return mMesh;
	}
	
}

package cs5625.deferred.defaultGeometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Quadmesh;

public class CubeQuadMesh {

private Quadmesh mMesh;
	
	private static final float[] cubeVerts = new float[] { 	-1.0f, -1.0f, -1.0f,
															-1.0f, 1.0f, -1.0f, 
															1.0f, -1.0f, -1.0f, 
															1.0f, 1.0f, -1.0f,
															-1.0f, -1.0f, 1.0f,
															-1.0f, 1.0f, 1.0f, 
															1.0f, -1.0f, 1.0f, 
															1.0f, 1.0f, 1.0f};
	
	private static final int[] cubeQuads= new int[] { 	0, 1, 3, 2,
														2, 3, 7, 6,
														0, 1, 5, 4,
														4, 5, 7, 6,
														7, 3, 1, 5,
														6, 2, 0, 4};
	
	private static final int[] creaseVertices = new int[] {0,1}; 
	
	public CubeQuadMesh(){
		final int numVertices = 8;
		final int numQuads = 6;
		
		FloatBuffer positionData = Buffers.newDirectFloatBuffer(3 * numVertices);
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		positionData.put(cubeVerts);
		
		IntBuffer indexData = Buffers.newDirectIntBuffer(4 * numQuads);
		
		indexData.put(cubeQuads);
		
		positionData.rewind();
		indexData.rewind();
		
		IntBuffer creaseData = Buffers.newDirectIntBuffer(2);
		creaseData.put(creaseVertices);
		
		creaseData.rewind();
		
		this.mMesh = new Quadmesh();
		this.mMesh.setVertexData(positionData);
		this.mMesh.setPolygonData(indexData);
		this.mMesh.setNormalData(normalData);
		this.mMesh.setTexCoordData(texCoordData);
		this.mMesh.setEdgeData(creaseData);
	}
	
	public Quadmesh getQuadMesh(){
		return mMesh;
	}
	
	
}

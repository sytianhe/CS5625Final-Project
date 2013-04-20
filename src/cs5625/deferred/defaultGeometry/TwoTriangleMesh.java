package cs5625.deferred.defaultGeometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Trimesh;
//import com.jogamp.common.nio.Buffers;

public class TwoTriangleMesh {
	
	private Trimesh mMesh;
	
	private static final float[] planeVerts = new float[] { -1.0f, -1.0f, 0.0f,
															-1.0f, 1.0f, 0.0f, 
															1.0f, 1.0f, 0.0f, 
															1.0f, -1.0f, 0.0f};
	
	private static final int[] planeTriangles = new int[] { 0, 2, 1, 0, 2, 3 };
	
	public TwoTriangleMesh(){
		final int numVertices = 4;
		final int numTris = 2;
		
		FloatBuffer positionData = Buffers.newDirectFloatBuffer(3 * numVertices);
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		positionData.put(planeVerts);
		
		IntBuffer indexData = Buffers.newDirectIntBuffer(3 * numTris);
		
		indexData.put(planeTriangles);
		
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

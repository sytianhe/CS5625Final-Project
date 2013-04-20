package cs5625.deferred.defaultGeometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Trimesh;

public class ThreeTriangleMesh {
	
	private Trimesh mMesh;
	
	public ThreeTriangleMesh(){
		final int numVertices = 5;
		final int numTris = 3;
		float positions[] = new float[numVertices * 3];
		int indices[] = new int[numTris * 3];
		
		positions[0 * 3 + 0] = 0.0f;
		positions[0 * 3 + 1] = 0.0f;
		positions[0 * 3 + 2] = 0.0f;
		
		positions[1 * 3 + 0] = 0.5f;
		positions[1 * 3 + 1] = 0.0f;
		positions[1 * 3 + 2] = 0.0f;
		
		positions[2 * 3 + 0] = 1.0f;
		positions[2 * 3 + 1] = 0.0f;
		positions[2 * 3 + 2] = 0.0f;
		
		positions[3 * 3 + 0] = 0.0f;
		positions[3 * 3 + 1] = 1.0f;
		positions[3 * 3 + 2] = 0.0f;
		
		positions[4 * 3 + 0] = 1.0f;
		positions[4 * 3 + 1] = 1.0f;
		positions[4 * 3 + 2] = 0.0f;
		
		indices[0 * 3 + 0] = 0;
		indices[0 * 3 + 1] = 1;
		indices[0 * 3 + 2] = 3;
		
		indices[1 * 3 + 0] = 4;
		indices[1 * 3 + 1] = 1;
		indices[1 * 3 + 2] = 2;
		
		indices[2 * 3 + 0] = 4;
		indices[2 * 3 + 1] = 1;
		indices[2 * 3 + 2] = 3;
		
		FloatBuffer positionData = Buffers.newDirectFloatBuffer(3 * numVertices);
		positionData.put(positions);
		
		IntBuffer indexData = Buffers.newDirectIntBuffer(3 * numTris);
		indexData.put(indices);
		
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		positionData.rewind();
		indexData.rewind();
		
		this.mMesh = new Trimesh();
		
		this.mMesh.setPolygonData(indexData);
		this.mMesh.setVertexData(positionData);
		this.mMesh.setNormalData(normalData);
		this.mMesh.setTexCoordData(texCoordData);	
	}
	
	public Trimesh getTriMesh(){
		return mMesh;
	}
}

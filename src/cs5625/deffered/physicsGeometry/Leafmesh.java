package cs5625.deffered.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.catmullclark.CCSubdiv;
import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.loop.LoopSubdiv;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Trimesh;

public class Leafmesh extends Trimesh {
	
	public Leafmesh(ArrayList<Point3f>list) {
		
		// Allocate buffers for all data.
		mVertexData   = Buffers.newDirectFloatBuffer(3 * 5);
		mNormalData   = Buffers.newDirectFloatBuffer(3 * 5);
		mTexCoordData = Buffers.newDirectFloatBuffer(2 * 5);
		mPolygonData  = Buffers.newDirectIntBuffer( 4 * 3 ); 
		
		mVertexData.put(list.get(0).x);
		mVertexData.put(list.get(0).y);
		mVertexData.put(list.get(0).z);
		
		mVertexData.put(list.get(1).x);
		mVertexData.put(list.get(1).y);
		mVertexData.put(list.get(1).z);

		mVertexData.put(list.get(2).x);
		mVertexData.put(list.get(2).y);
		mVertexData.put(list.get(2).z);
		
		mVertexData.put(list.get(3).x);
		mVertexData.put(list.get(3).y);
		mVertexData.put(list.get(3).z);
		
		mVertexData.put(list.get(4).x);
		mVertexData.put(list.get(4).y);
		mVertexData.put(list.get(4).z);
		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(0f);

//		
//		mNormalData.put(0f);
//		mNormalData.put(0f);
//		mNormalData.put(height);
//
//		mNormalData.put(-width/2f);
//		mNormalData.put(0f);
//		mNormalData.put(height/2f);
//		
//		mNormalData.put(width/2f);
//		mNormalData.put(0f);
//		mNormalData.put(height/2f);
//		
//		mNormalData.put(0f);
//		mNormalData.put(-height*width/1000f);
//		mNormalData.put(height/2f);
				

//		mPolygonData.put(1);
//		mPolygonData.put(2);
//		mPolygonData.put(4);
//		mPolygonData.put(0);
//	
//
//		mPolygonData.put(3);
//		mPolygonData.put(0);
//		mPolygonData.put(4);
//		mPolygonData.put(2);
		
		mPolygonData.put(2);
		mPolygonData.put(4);
		mPolygonData.put(1);
		
		mPolygonData.put(0);
		mPolygonData.put(1);
		mPolygonData.put(4);
		
		mPolygonData.put(2);
		mPolygonData.put(3);
		mPolygonData.put(4);
		
		mPolygonData.put(0);
		mPolygonData.put(4);
		mPolygonData.put(3);
		
		setName("TreeLeave");
		mVertexData.rewind();
		mNormalData.rewind();
		mTexCoordData.rewind();
		mPolygonData.rewind();
	}
	
	public void subdivide(int numberofSub){
		for (int i = 0; i< numberofSub; i++){
			EdgeDS edgeDS = new EdgeDS(this);
			LoopSubdiv ccSubdiv = new LoopSubdiv(edgeDS);
			Mesh newMesh = (Trimesh)ccSubdiv.getNewMesh();
			this.setVertexData(newMesh.getVertexData());
			this.setEdgeData(newMesh.getEdgeData());
			this.setNormalData(newMesh.getNormalData());
			this.setPolygonData(newMesh.getPolygonData());
		}
	}
}
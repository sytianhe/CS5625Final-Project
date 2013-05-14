package cs5625.deferred.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

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
		
		Vector3f v30 = new Vector3f(list.get(3).x - list.get(0).x, list.get(3).y - list.get(0).y, list.get(3).z - list.get(0).z);
		Vector3f v40 = new Vector3f(list.get(4).x - list.get(0).x, list.get(4).y - list.get(0).y, list.get(4).z - list.get(0).z);
		Vector3f v10 = new Vector3f(list.get(1).x - list.get(0).x, list.get(1).y - list.get(0).y, list.get(1).z - list.get(0).z);
		Vector3f v32 = new Vector3f(list.get(3).x - list.get(2).x, list.get(3).y - list.get(2).y, list.get(3).z - list.get(2).z);
		Vector3f v42 = new Vector3f(list.get(4).x - list.get(2).x, list.get(4).y - list.get(2).y, list.get(4).z - list.get(2).z);
		Vector3f v12 = new Vector3f(list.get(1).x - list.get(2).x, list.get(1).y - list.get(2).y, list.get(1).z - list.get(2).z);

		Vector3f plane1 = new Vector3f();
		Vector3f plane2 = new Vector3f();
		Vector3f plane3 = new Vector3f();
		Vector3f plane4 = new Vector3f();

		plane1.cross(v40, v30);
		plane1.normalize();
		plane2.cross(v10, v40);
		plane2.normalize();
		plane3.cross(v32, v42);
		plane3.normalize();
		plane4.cross(v42, v12);
		plane4.normalize();
		
		Vector3f v1 = new Vector3f();
		v1.add(plane1, plane2);
		v1.normalize();
		Vector3f v2 = new Vector3f();
		v2.add(plane3, plane4);
		v2.normalize();
		Vector3f v3 = new Vector3f();
		v3.add(plane1, plane3);
		v3.normalize();
		Vector3f v4 = new Vector3f();
		v4.add(plane2, plane4);
		v4.normalize();
		
		Vector3f v5 = new Vector3f();
		v5.add(v3, v1);
		v5.normalize();
		
		mNormalData.put(v1.x);
		mNormalData.put(v1.y);
		mNormalData.put(v1.z);
		
		mNormalData.put(v2.x);
		mNormalData.put(v2.y);
		mNormalData.put(v2.z);
		
		mNormalData.put(v3.x);
		mNormalData.put(v3.y);
		mNormalData.put(v3.z);
		
		mNormalData.put(v4.x);
		mNormalData.put(v4.y);
		mNormalData.put(v4.z);
		
		mNormalData.put(v5.x);
		mNormalData.put(v5.y);
		mNormalData.put(v5.z);
		
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
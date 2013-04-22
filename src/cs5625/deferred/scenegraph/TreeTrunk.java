package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.catmullclark.CCSubdiv;
import cs5625.deferred.datastruct.EdgeDS;

/**
 * TreeTrunk.java
 * 
 * A fun little procedural mesh. Used for our tree trunk generation.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics) final project.
 * 
 * @author Tianhe Zhang (tz249)
 */
public class TreeTrunk extends Quadmesh {

	public TreeTrunk(ArrayList<Point3f> ControlPoints, float topRadius, float bottomRadius) {
		
		// Allocate buffers for all data.
		mVertexData   = Buffers.newDirectFloatBuffer(3 * 4 * ControlPoints.size());
		mNormalData   = Buffers.newDirectFloatBuffer(3 * 4 * ControlPoints.size());
		mTexCoordData = Buffers.newDirectFloatBuffer(2 * 4 * ControlPoints.size());
		mPolygonData  = Buffers.newDirectIntBuffer(4 * 4 * (ControlPoints.size() -1 ) + 4); //+1 for the top and bottom
		
		// Getting normals
		for(int i = 0; i<ControlPoints.size(); i++){
			float radius = (1f-i*1f/ControlPoints.size())*(topRadius - bottomRadius) + bottomRadius;
			
			Vector3f normal = new Vector3f();
			Vector3f tangent = new Vector3f();
			Vector3f normalDir = new Vector3f();
			Vector3f tangentDir = new Vector3f();			
			Vector3f temp = new Vector3f();
			if (i == 0 ){
				temp.set(ControlPoints.get(i+1));
				temp.sub(ControlPoints.get(i));
				temp.normalize();
				normal = new Vector3f(temp.x, -temp.z, temp.y);
				tangent.cross(normal, temp);
				tangent.normalize();
			}
			else if(i == ControlPoints.size()-1){
				temp.set(ControlPoints.get(i));
				temp.sub(ControlPoints.get(i-1));
				temp.normalize();
				normal = new Vector3f(temp.x, -temp.z, temp.y);
				tangent.cross(normal, temp);
				tangent.normalize();
			}
			else{
				temp.set(ControlPoints.get(i+1));
				temp.sub(ControlPoints.get(i-1));
				temp.normalize();
				normal = new Vector3f(temp.x, -temp.z, temp.y);
				tangent.cross(normal, temp);
				tangent.normalize();
			}
			normalDir.set(normal);
			tangentDir.set(tangent);
			normal.scale(radius*1.41f);
			tangent.scale(radius*1.41f);
			mVertexData.put(ControlPoints.get(i).x + normal.x);
			mVertexData.put(ControlPoints.get(i).y + normal.y);
			mVertexData.put(ControlPoints.get(i).z + normal.z);
			mNormalData.put(ControlPoints.get(i).x + normalDir.x);
			mNormalData.put(ControlPoints.get(i).y + normalDir.y);
			mNormalData.put(ControlPoints.get(i).z + normalDir.z);

			mVertexData.put(ControlPoints.get(i).x + tangent.x);
			mVertexData.put(ControlPoints.get(i).y + tangent.y);
			mVertexData.put(ControlPoints.get(i).z + tangent.z);
			mNormalData.put(ControlPoints.get(i).x + tangentDir.x);
			mNormalData.put(ControlPoints.get(i).y + tangentDir.y);
			mNormalData.put(ControlPoints.get(i).z + tangentDir.z);

			mVertexData.put(ControlPoints.get(i).x - normal.x );
			mVertexData.put(ControlPoints.get(i).y - normal.y );
			mVertexData.put(ControlPoints.get(i).z - normal.z );
			mNormalData.put(ControlPoints.get(i).x - normalDir.x );
			mNormalData.put(ControlPoints.get(i).y - normalDir.y );
			mNormalData.put(ControlPoints.get(i).z - normalDir.z );
			
			mVertexData.put(ControlPoints.get(i).x - tangent.x);
			mVertexData.put(ControlPoints.get(i).y - tangent.y);
			mVertexData.put(ControlPoints.get(i).z - tangent.z);
			mNormalData.put(ControlPoints.get(i).x - tangentDir.x);
			mNormalData.put(ControlPoints.get(i).y - tangentDir.y);
			mNormalData.put(ControlPoints.get(i).z - tangentDir.z);
		}
	
		for (int i = 0; i<ControlPoints.size()-1; i++){
			for(int j = 0; j<4; j++){

				int jj = (j+1)%4;
				mPolygonData.put(i*4+j); 
				mPolygonData.put(i*4+jj); 	
				mPolygonData.put((i+1)*4+jj);	
				mPolygonData.put((i+1)*4+j); 
		
			}
		}
//		mPolygonData.put(0);
//		mPolygonData.put(1);
//		mPolygonData.put(2);
//		mPolygonData.put(3);
		
		mPolygonData.put(4 * ControlPoints.size()-1);
		mPolygonData.put(4 * ControlPoints.size()-2);
		mPolygonData.put(4 * ControlPoints.size()-3);
		mPolygonData.put(4 * ControlPoints.size()-4);


		setName("TreeTrunk");
		mVertexData.rewind();
		mNormalData.rewind();
		mTexCoordData.rewind();
		mPolygonData.rewind();
		
	}
	
	public void subdivide(int numberofSub){
		for (int i = 0; i< numberofSub; i++){
			EdgeDS edgeDS = new EdgeDS(this);
			CCSubdiv ccSubdiv = new CCSubdiv(edgeDS);
			Mesh newMesh = (Quadmesh)ccSubdiv.getNewMesh();
			this.setVertexData(newMesh.getVertexData());
			this.setEdgeData(newMesh.getEdgeData());
			this.setNormalData(newMesh.getNormalData());
			this.setPolygonData(newMesh.getPolygonData());
		}
	}
}

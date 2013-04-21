package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

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
		mPolygonData  = Buffers.newDirectIntBuffer(4 * 4 * (ControlPoints.size() -1 ));
		
		// Getting normals
		for(int i = 0; i<ControlPoints.size(); i++){
			float radius = (1f-i*1f/ControlPoints.size())*(topRadius - bottomRadius) + bottomRadius;
			
			Vector3f normal = new Vector3f();
			Vector3f tangent = new Vector3f();
			Vector3f temp = new Vector3f();
			if (i == 0 ){
				temp.set(ControlPoints.get(i+1));

				temp.sub(ControlPoints.get(i));
				System.out.println(ControlPoints.get(i+1));
				System.out.println(ControlPoints.get(i));
				System.out.println("temp is, "+temp);
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

			normal.scale(radius);
			tangent.scale(radius);
			mVertexData.put(ControlPoints.get(i).x + normal.x + tangent.x);
			mVertexData.put(ControlPoints.get(i).y + normal.y + tangent.y);
			mVertexData.put(ControlPoints.get(i).z + normal.z + tangent.z);
			mVertexData.put(ControlPoints.get(i).x - normal.x + tangent.x);
			mVertexData.put(ControlPoints.get(i).y - normal.y + tangent.y);
			mVertexData.put(ControlPoints.get(i).z - normal.z + tangent.z);
			mVertexData.put(ControlPoints.get(i).x - normal.x - tangent.x);
			mVertexData.put(ControlPoints.get(i).y - normal.y - tangent.y);
			mVertexData.put(ControlPoints.get(i).z - normal.z - tangent.z);
			mVertexData.put(ControlPoints.get(i).x + normal.x - tangent.x);
			mVertexData.put(ControlPoints.get(i).y + normal.y - tangent.y);
			mVertexData.put(ControlPoints.get(i).z + normal.z - tangent.z);
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
		
		setName("TreeTrunk");

		mVertexData.rewind();
		mNormalData.rewind();
		mTexCoordData.rewind();
		mPolygonData.rewind();
		
	}

}

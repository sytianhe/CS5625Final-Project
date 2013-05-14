package cs5625.deferred.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Quadmesh;

/**
 * TreeTrunk.java
 * 
 * A fun little procedural mesh. Used for our tree trunk generation.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics) final project.
 * 
 * @author Tianhe Zhang (tz249)
 */
public class Branchmesh extends Quadmesh {

	public Branchmesh(ArrayList<Point3f> ControlPoints, float bottomRadius , float topRadius ) {

		// Allocate buffers for all data.
		mVertexData   = Buffers.newDirectFloatBuffer(3 * 4 * ControlPoints.size());
		mNormalData   = Buffers.newDirectFloatBuffer(3 * 4 * ControlPoints.size());
		mTexCoordData = Buffers.newDirectFloatBuffer(2 * 4 * ControlPoints.size());
		mPolygonData  = Buffers.newDirectIntBuffer(4 * 4 * (ControlPoints.size() -1 ) + 4); //+1 for the top and bottom

		// Getting normals
		int n = ControlPoints.size();
		for(int i = 0; i<n; i++){
			//linear interpolation
			float radius = (1f-i*1f/ControlPoints.size())*(  bottomRadius -  topRadius ) + topRadius;
			//nonlinear interpolation
			//float radius =  topRadius + (  bottomRadius - topRadius )  * (1.5f + (float) Math.atan( -i + n/8 ) ) / 1.5f; 
			
			Vector3f normal = new Vector3f();
			Vector3f tangent = new Vector3f();
			Vector3f normalDir = new Vector3f();
			Vector3f tangentDir = new Vector3f();			
			Vector3f temp = new Vector3f();
			if (i< ControlPoints.size()-1){
				if (i == 0 ){
					temp.set(ControlPoints.get(i+1));
					temp.sub(ControlPoints.get(i));
					temp.normalize();
					normal.set(temp.x, -temp.z, temp.y);
					tangent.cross(normal, temp);
					tangent.normalize();
					normalDir.set(normal);
					tangentDir.set(tangent);
					normal.scale(radius*1.41f);
					tangent.scale(radius*1.41f);

				}
				else{
					temp.set(ControlPoints.get(i+1));
					temp.sub(ControlPoints.get(i-1));
					temp.normalize();
					normal.set(temp.x, -temp.z, temp.y);
					tangent.cross(normal, temp);
					tangent.normalize();
					normalDir.set(normal);
					tangentDir.set(tangent);
					normal.scale(radius*1.41f);
					tangent.scale(radius*1.41f);
				}
				mVertexData.put(ControlPoints.get(i).x + normal.x);
				mVertexData.put(ControlPoints.get(i).y + normal.y);
				mVertexData.put(ControlPoints.get(i).z + normal.z);
				mNormalData.put(normalDir.x);
				mNormalData.put(normalDir.y);
				mNormalData.put(normalDir.z);
				
				mVertexData.put(ControlPoints.get(i).x + tangent.x);
				mVertexData.put(ControlPoints.get(i).y + tangent.y);
				mVertexData.put(ControlPoints.get(i).z + tangent.z);
				mNormalData.put(tangentDir.x);
				mNormalData.put(tangentDir.y);
				mNormalData.put(tangentDir.z);

				mVertexData.put(ControlPoints.get(i).x - normal.x );
				mVertexData.put(ControlPoints.get(i).y - normal.y );
				mVertexData.put(ControlPoints.get(i).z - normal.z );
				mNormalData.put(- normalDir.x );
				mNormalData.put(- normalDir.y );
				mNormalData.put(- normalDir.z );

				mVertexData.put(ControlPoints.get(i).x - tangent.x);
				mVertexData.put(ControlPoints.get(i).y - tangent.y);
				mVertexData.put(ControlPoints.get(i).z - tangent.z);
				mNormalData.put(- tangentDir.x);
				mNormalData.put(- tangentDir.y);
				mNormalData.put(- tangentDir.z);
			}
			else if (i == ControlPoints.size()-1){  //TOP CONTROL POINT IS SPECIAL (!!!)
				temp.set(ControlPoints.get(i));
				temp.sub(ControlPoints.get(i-1));
				temp.normalize();
				normal.set(temp.x, -temp.z, temp.y);
				tangent.cross(normal, temp);
				tangent.normalize();
				normal.scale(radius*1.41f);
				tangent.scale(radius*1.41f);

				normalDir.set(ControlPoints.get(i));
				normalDir.sub(ControlPoints.get(i-1));
				normalDir.normalize();
				
				mVertexData.put(ControlPoints.get(i).x + normal.x);
				mVertexData.put(ControlPoints.get(i).y + normal.y);
				mVertexData.put(ControlPoints.get(i).z + normal.z);
				mNormalData.put(normalDir.x);
				mNormalData.put(normalDir.y);
				mNormalData.put(normalDir.z);

				mVertexData.put(ControlPoints.get(i).x + tangent.x);
				mVertexData.put(ControlPoints.get(i).y + tangent.y);
				mVertexData.put(ControlPoints.get(i).z + tangent.z);
				mNormalData.put(normalDir.x);
				mNormalData.put(normalDir.y);
				mNormalData.put(normalDir.z);

				mVertexData.put(ControlPoints.get(i).x - normal.x );
				mVertexData.put(ControlPoints.get(i).y - normal.y );
				mVertexData.put(ControlPoints.get(i).z - normal.z );
				mNormalData.put(normalDir.x);
				mNormalData.put(normalDir.y);
				mNormalData.put(normalDir.z);

				mVertexData.put(ControlPoints.get(i).x - tangent.x);
				mVertexData.put(ControlPoints.get(i).y - tangent.y);
				mVertexData.put(ControlPoints.get(i).z - tangent.z);
				mNormalData.put(normalDir.x);
				mNormalData.put(normalDir.y);
				mNormalData.put(normalDir.z);
			}
			
			//ADD TEXTURE COORDINATES.  THE BOUND TEXTURE WILL BE MAPPED TO EACH FACE OF THE BRANCH (BEFORE SUBDIVISION)
			if (i % 2 == 0){
				mTexCoordData.put(0f);
				mTexCoordData.put(0f);
				mTexCoordData.put(1f);
				mTexCoordData.put(0f);
				mTexCoordData.put(0f);
				mTexCoordData.put(0f);
				mTexCoordData.put(1f);
				mTexCoordData.put(0f);
			}
			else{
				mTexCoordData.put(0f);
				mTexCoordData.put(1f);
				mTexCoordData.put(1f);
				mTexCoordData.put(1f);
				mTexCoordData.put(0f);
				mTexCoordData.put(1f);
				mTexCoordData.put(1f);
				mTexCoordData.put(1f);
			}
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

}

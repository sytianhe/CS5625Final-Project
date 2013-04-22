package cs5625.deferred.scenegraph;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import com.jogamp.common.nio.Buffers;

/**
 * MengerSponge.java
 * 
 * A fun little procedural mesh. Useful for viewing how SSAO shadowing works in tight areas.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Sean Ryan (ser99)
 * @date 2013-03-28
 */
public class MengerSponge extends Quadmesh {

	public MengerSponge(int steps) {
		setName("MengerSponge");
		
		// Grab the mesh data for a single cube from a file.
		Mesh cube = null;
		try {
			cube = Geometry.load("models/cube.obj", false, true).get(0).getMeshes().get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int cubeSize = 4 * cube.getPolygonCount();
		FloatBuffer cubePoints  = cube.getVertexData();
		FloatBuffer cubeNormals = cube.getNormalData();
		FloatBuffer cubeTexture = cube.getTexCoordData();
		IntBuffer   cubeIndices = cube.getPolygonData();
		
		// Recursively create the sub-cube center points.
		ArrayList<Point3f> cubeCenters = new ArrayList<Point3f>();
		mengerStep(cubeCenters, new Point3f(), 1.0f, steps);
		
		// Allocate buffers for all data.
		mVertexData   = Buffers.newDirectFloatBuffer(3 * cubeSize * cubeCenters.size());
		mNormalData   = Buffers.newDirectFloatBuffer(3 * cubeSize * cubeCenters.size());
		mTexCoordData = Buffers.newDirectFloatBuffer(2 * cubeSize * cubeCenters.size());
		mPolygonData  = Buffers.newDirectIntBuffer(cubeSize * cubeCenters.size());
		
		int cubeCount = 0;
		float finalScale = (float)Math.pow(1.0f/3.0f, steps);
		for (Point3f center : cubeCenters) {
			for (int i = 0; i < cubeSize; i++) {
				// Build a new set of positions using the center and scale.
				Point3f cubePoint = new Point3f();
				cubePoint.x = cubePoints.get();
				cubePoint.y = cubePoints.get();
				cubePoint.z = cubePoints.get();
				
				cubePoint.scale(finalScale);
				cubePoint.add(center);
				
				mVertexData.put(cubePoint.x);
				mVertexData.put(cubePoint.y);
				mVertexData.put(cubePoint.z);
				
				// Use the same normals and texture coordinates.
				for (int j = 0; j < 3; j++) {
					mNormalData.put(cubeNormals.get());
				}
				
				for (int j = 0; j < 2; j++) {
					mTexCoordData.put(cubeTexture.get());
				}
				
				// Offset the index by the cube count.
				mPolygonData.put(cubeIndices.get() + cubeCount * cubeSize);
			}
			
			cubePoints.rewind();
			cubeNormals.rewind();
			cubeTexture.rewind();
			cubeIndices.rewind();
			
			cubeCount++;
		}
		
		mVertexData.rewind();
		mNormalData.rewind();
		mTexCoordData.rewind();
		mPolygonData.rewind();
	}

	/**
	 * Recursively computes the centers of all cubes in the sponge.
	 * @param cubeCenters The centers are accumulated here.
	 * @param center The center of the current iteration.
	 * @param scale The scale of the current iteration.
	 * @param stepsToGo How many levels we still have to do.
	 */
	private void mengerStep(ArrayList<Point3f> cubeCenters, Point3f center, float scale, int stepsToGo) {
		if (stepsToGo <= 0) {
			cubeCenters.add(center);
		} else {
			float newScale = scale / 3.0f;
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						// If no more than one index is zero...
						if ((x != 0 && y != 0) || (x != 0 && z != 0) || (y != 0 && z != 0)) {
							Point3f newCenter = new Point3f(center);
							newCenter.x += 2.0f * (float) x * newScale;
							newCenter.y += 2.0f * (float) y * newScale;
							newCenter.z += 2.0f * (float) z * newScale;

							mengerStep(cubeCenters, newCenter, newScale, stepsToGo - 1);
						}
					}
				}
			}
		}
	}

}

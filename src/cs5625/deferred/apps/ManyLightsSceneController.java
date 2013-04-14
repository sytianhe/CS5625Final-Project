package cs5625.deferred.apps;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.SceneObject;

/**
 * DefaultSceneController.java
 * 
 * The default scene controller creates a simple scene and allows the user to orbit the camera 
 * and preview the renderer's gbuffer.
 * 
 * Drag the mouse to orbit the camera, and scroll to zoom. Numbers 1-9 preview individual gbuffer 
 * textures, and 0 views the shaded result.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class ManyLightsSceneController extends SceneController
{
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	
	/* The object containing all the little lights. */
	private SceneObject mLightCloud;
	
	@Override
	public void initializeScene()
	{
		try
		{
			/* Use a Random with a fixed seed to make the scene predictable. */
			Random rand = new Random(314);
			
			/* Load the default scene without materials. */
			mSceneRoot.addGeometry(Geometry.load("models/default-scene.obj", false, true));
						
			/* Example of manipulating an object by name. */
			mSceneRoot.findDescendantByName("fighter1").getOrientation().set(new AxisAngle4f(0.0f, 1.0f, 0.0f, -(float)Math.PI / 4.0f));

			/* This object will contain the cloud of point lights we're about to create. */
			mLightCloud = new SceneObject();
			mLightCloud.getPosition().y = 2.0f;
			mSceneRoot.addChild(mLightCloud);
			
			/* Go ahead and create the lights. */
			for (int i = 0; i < 30; ++i)
			{
				PointLight light = new PointLight();
				
				/* Pick a random fairly-saturated color. */
				Color3f lightColor = new Color3f(Color.getHSBColor(rand.nextFloat(), 0.5f + 0.5f * rand.nextFloat(), 1.0f));
				light.setColor(lightColor);
				
				/* Pick a random position for the light. */ 
				light.setPosition(new Point3f(
						(rand.nextFloat() - 0.5f) * 8.0f, 
						(rand.nextFloat() - 0.5f) * 8.0f, 
						(rand.nextFloat() - 0.5f) * 8.0f));
				
				/* Add a sphere as a child of the light (so we can see where it is), and set the sphere to the same color as the light. */
				Geometry sphere = Geometry.load("models/lowpolysphere.obj", false, false).get(0);
				sphere.setScale(0.1f);
				sphere.getMeshes().get(0).setMaterial(new UnshadedMaterial(lightColor));
				light.addChild(sphere);
				
				/* Add this new light to the cloud. */
				mLightCloud.addChild(light);
			}
		}
		catch (Exception err)
		{
			/* If anything goes wrong, just die. */
			err.printStackTrace();
			System.exit(-1);
		}
		
		/* Initialize camera position. */
		updateCamera();
	}
	
	/**
	 * Updates the camera position and orientation based on orbit parameters.
	 */
	private void updateCamera()
	{
		/* Compose the "horizontal" and "vertical" rotations. */
		Quat4f longitudeQuat = new Quat4f();
		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mCameraLongitude * (float)Math.PI / 180.0f));
		
		Quat4f latitudeQuat = new Quat4f();
		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mCameraLatitude * (float)Math.PI / 180.0f));

		mCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
		
		/* Set the camera's position so that it looks towards the origin. */
		mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
		Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		mCameraRadius += mouseWheel.getUnitsToScroll();
		updateCamera();
		requiresRender();
	}

	@Override
	public void mousePressed(MouseEvent mouse)
	{
		/* Remember the starting point of a drag. */
		mLastMouseDrag = mouse.getPoint();
	}
	
	@Override
	public void mouseDragged(MouseEvent mouse)
	{
		/* Calculate dragged delta. */
		float deltaX = -(mouse.getPoint().x - mLastMouseDrag.x);
		float deltaY = -(mouse.getPoint().y - mLastMouseDrag.y);
		mLastMouseDrag = mouse.getPoint();
		
		if (mouse.getButton() == MouseEvent.BUTTON1)
		{
			/* Left mouse --> update camera. */
			
			/* Update longitude, wrapping as necessary. */
			mCameraLongitude += deltaX;
			
			if (mCameraLongitude > 360.0f)
			{
				mCameraLongitude -= 360.0f;
			}
			else if (mCameraLongitude < 0.0f)
			{
				mCameraLongitude += 360.0f;
			}
			
			/* Update latitude, clamping as necessary. */
			if (Math.abs(mCameraLatitude + deltaY) <= 89.0f)
			{
				mCameraLatitude += deltaY;
			}
			else
			{
				mCameraLatitude = 89.0f * Math.signum(mCameraLatitude);
			}
		
			updateCamera();
		}
		else
		{
			/* Other buttons -> rotate light cloud. */
			Quat4f xquat = new Quat4f();
			xquat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, deltaX * (float)Math.PI / 180.0f));
			
			Quat4f yquat = new Quat4f();
			yquat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, deltaY * (float)Math.PI / 180.0f));

			/* Perform this rotation in the camera's frame for natural interaction:
			 * newOrientation = cameraOrientation * delta * inverse(cameraOrientation) * existingOrientation. */
			Quat4f result = new Quat4f(mCamera.getOrientation());
			result.mul(xquat);
			result.mul(yquat);
			result.mulInverse(mCamera.getOrientation());
			result.mul(mLightCloud.getOrientation());
			
			mLightCloud.getOrientation().set(result);
		}
		
		requiresRender();
	}
}

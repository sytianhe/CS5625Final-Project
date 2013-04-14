package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.PointLight;

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
public class ShadowMapSceneController extends SceneController
{
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Keeps track of shadow camera's orbit position. */
	private float mShadowCameraLongitude = -50.0f, mShadowCameraLatitude = -40.0f;
	private float mShadowCameraRadius = 15.f;
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	
	@Override
	public void initializeScene()
	{
		try
		{
			/* Load default scene with materials. */
			mSceneRoot.addGeometry(Geometry.load("models/default-scene.obj", true, true));
			
			/* Example of manipulating an object by name. */
			mSceneRoot.findDescendantByName("fighter1").getOrientation().set(new AxisAngle4f(0.0f, 1.0f, 0.0f, -(float)Math.PI / 4.0f));
			((Geometry)mSceneRoot.findDescendantByName("Cylinder")).getMeshes().get(0).setMaterial(new LambertianMaterial(new Color3f(0.64f, 0.47f, 0.26f)));
			
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();
			
			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);
			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);		 	
		}
		catch (Exception err)
		{
			/* If anything goes wrong, just die. */
			err.printStackTrace();
			System.exit(-1);
		}
		
		this.hasShadows = true;
		
		/* Initialize camera position. */
		updateCamera();
		updateShadowCamera();
	}
		
	/**
	 * Updates the camera position and orientation based on orbit parameters.
	 */
	protected void updateCamera()
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
	
	/**
	 * Updates the camera position and orientation based on orbit parameters.
	 */
	protected void updateShadowCamera()
	{
		/* Compose the "horizontal" and "vertical" rotations. */
		Quat4f longitudeQuat = new Quat4f();
		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mShadowCameraLongitude * (float)Math.PI / 180.0f));
		
		Quat4f latitudeQuat = new Quat4f();
		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mShadowCameraLatitude * (float)Math.PI / 180.0f));

		mShadowCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
		
		/* Set the camera's position so that it looks towards the origin. */
		mShadowCamera.setPosition(new Point3f(0.0f, 0.0f, mShadowCameraRadius));
		Util.rotateTuple(mShadowCamera.getOrientation(), mShadowCamera.getPosition());
		
		mSceneRoot.findDescendantByName("CameraLight").setPosition(mShadowCamera.getPosition());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		if (!isShadowCamMode) {
			mCameraRadius += mouseWheel.getUnitsToScroll();
			updateCamera();
			requiresRender();
		}
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
		
		if (!isShadowCamMode && !moveShadowCam) 
		{
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
			/* Update longitude, wrapping as necessary. */
			mShadowCameraLongitude += deltaX;
			
			if (mShadowCameraLongitude > 360.0f)
			{
				mShadowCameraLongitude -= 360.0f;
			}
			else if (mShadowCameraLongitude < 0.0f)
			{
				mShadowCameraLongitude += 360.0f;
			}
			
			/* Update latitude, clamping as necessary. */
			if (Math.abs(mShadowCameraLatitude + deltaY) <= 89.0f)
			{
				mShadowCameraLatitude += deltaY;
			}
			else
			{
				mShadowCameraLatitude = 89.0f * Math.signum(mShadowCameraLatitude);
			}
		
			updateShadowCamera();
		}
		requiresRender();
	}
}

package cs5625.deferred.rendering;

import javax.vecmath.Matrix4f;

import cs5625.deferred.scenegraph.SceneObject;

/**
 * Camera.java
 * 
 * Represents a perspective camera. Since Camera inherits from SceneObject, you could add it as a 
 * child of another object in the scene to have it follow that object, or add geometry or lights 
 * as children of the camera to have those objects follow the camera.   
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), John DeCorato (jd537)
 * @date 2012-03-23
 */
public class Camera extends SceneObject
{
	/* Perspective camera attributes. */
	private float mFOV = 45.0f;
	private float mNear = 0.1f;
	private float mFar = 100.0f;
	
	private boolean mIsShadowMapCamera = false;
	
	/**
	 * Returns the camera field of view angle, in degrees.
	 * 
	 * This is the full angle, not the half angle.
	 */
	public float getFOV()
	{
		return mFOV;
	}

	/**
	 * Sets the camera's field of view.
	 * @param fov Desired field of view, in degrees. Must be in the interval (0, 180).
	 */
	public void setFOV(float fov)
	{
		mFOV = fov;
	}

	/**
	 * Returns the camera near plane distance.
	 */
	public float getNear()
	{
		return mNear;
	}

	/**
	 * Sets the camera near plane distance.
	 * 
	 * @param near The near plane. Must be positive.
	 */
	public void setNear(float near)
	{
		mNear = near;
	}

	/**
	 * Returns the camera far plane distance.
	 */
	public float getFar()
	{
		return mFar;
	}

	/**
	 * Sets the camera far plane distance.
	 * @param far The far plane; must be farther away than the near plane.
	 */
	public void setFar(float far)
	{
		mFar = far;
	}
	
	/**
	 * Returns if this is a shadow map camera
	 */
	public boolean getIsShadowMapCamera()
	{
		return mIsShadowMapCamera;
	}
	
	/**
	 * Sets shadow map camera state
	 */
	public void setIsShadowMapCamera(boolean isShadowMapCamera)
	{
		mIsShadowMapCamera = isShadowMapCamera;
	}
	
	
	/**
	 *  Get the view matrix that send points from world space into this camera local space 
	 */
	public Matrix4f getViewMatrix() {
		Matrix4f mView = getWorldSpaceTransformationMatrix4f();
		mView.invert();
		
		return mView;
	}
	
	public Matrix4f getProjectionMatrix(float width, float height) {
		float aspect = width/ height;
		float s = (float) (1f / (Math.tan(mFOV * 0.5 * Math.PI / 180)));
		return new Matrix4f(
				s/aspect, 0f, 0f, 0f,
				0f, s, 0f, 0f,
				0f, 0f, -(mFar + mNear) / (mFar - mNear), -2 * mFar * mNear / (mFar - mNear),
				0f, 0f, -1f, 0f);
	}
}

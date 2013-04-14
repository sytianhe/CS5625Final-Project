package cs5625.deferred.scenegraph;

import javax.vecmath.Color3f;

/**
 * Light.java
 * 
 * Abstract base class for light sources.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class Light extends SceneObject
{
	private Color3f mColor = new Color3f(1.0f, 1.0f, 1.0f);

	/**
	 * Returns the color of the light.
	 */
	public Color3f getColor()
	{
		return mColor;
	}

	/**
	 * Sets the color of the light.
	 */
	public void setColor(Color3f color)
	{
		mColor = color;
	}
}

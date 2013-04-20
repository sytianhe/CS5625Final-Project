package cs5625.deferred.scenegraph;

/**
 * PointLight.java
 * 
 * Represents an omnidirectional light at a specific position. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-24
 */
public class PointLight extends Light
{
	/* Attenuation defaults to quadratic for realism plus constant for non-blowing-up near the light. */
	private float mConstantAttenuation = 1.0f;
	private float mLinearAttenuation = 0.0f;
	private float mQuadraticAttenuation = 1.0f;
	
	public float getConstantAttenuation()
	{
		return mConstantAttenuation;
	}

	public void setConstantAttenuation(float atten)
	{
		mConstantAttenuation = atten;
	}

	public float getLinearAttenuation()
	{
		return mLinearAttenuation;
	}

	public void setLinearAttenuation(float atten)
	{
		mLinearAttenuation = atten;
	}

	public float getQuadraticAttenuation()
	{
		return mQuadraticAttenuation;
	}

	public void setQuadraticAttenuation(float atten)
	{
		mQuadraticAttenuation = atten;
	}
}

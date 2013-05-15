package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;

/**
 * SkyMaterial.java
 * 
 * Implements a Lambertian (perfectly diffuse) material.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Tianhe Zhang (tz249)
 * @date 2012-03-27
 */
public class SkyMaterial extends Material
{
	/* Lambertian material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
	private Texture2D mDiffuseTexture = null;
	private Texture2D mStarTexture = null;
	private Point3f mSunPosition = new Point3f(); // sun location
	private float mDayTime =0f; // time during the day

	/* Uniform locations. */
	private int mDiffuseUniformLocation = -1;
	private int mHasDiffuseTextureUniformLocation = -1;
	private int mHasStarTextureUniformLocation = -1;
	private int mDayTimeUniformLocation = -1;
	private int mSunPositionUniformLocation = -1;
	
	public SkyMaterial()
	{
		/* Default constructor. */
	}

	public SkyMaterial(Color3f diffuse)
	{
		mDiffuseColor.set(diffuse);
	}

	public Color3f getDiffuseColor()
	{
		return mDiffuseColor;
	}
	
	public void setDiffuseColor(Color3f diffuse)
	{
		mDiffuseColor = diffuse;
	}
	
	public Texture2D getDiffuseTexture()
	{
		return mDiffuseTexture;
	}
	
	public void setDiffuseTexture(Texture2D texture)
	{
		mDiffuseTexture = texture;
	}
	
	public Texture2D getStarTexture()
	{
		return mStarTexture;
	}
	
	public void setStarTexture(Texture2D texture)
	{
		mStarTexture = texture;
	}
	
	public Point3f getSunPostion()
	{
		return mSunPosition;
	}
	
	public void setSunPosition(Point3f pos)
	{
		mSunPosition = new Point3f(pos);
	}
	public float getDayTime()
	{
		return mDayTime;
	}
	
	public void setDayTime(float time)
	{
		mDayTime = time;
	}

	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader, and any textures, and update uniforms. */
		getShaderProgram().bind(gl);

		// TODO PA3 Prereq: Set shader uniforms and bind any textures.
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		gl.glUniform3f(mSunPositionUniformLocation, mSunPosition.x, mSunPosition.y, mSunPosition.z);
		gl.glUniform1f(mDayTimeUniformLocation, mDayTime);
		if (mDiffuseTexture == null) {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 1);
			mDiffuseTexture.bind(gl, 0);
		}
		
		if (mStarTexture == null) {
			gl.glUniform1i(mHasStarTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasStarTextureUniformLocation, 1);
			mStarTexture.bind(gl, 1);
		}
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind anything bound in bind(). */
		getShaderProgram().unbind(gl);

		// TODO PA3 Prereq: Unbind any used textures.
		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
		if (mStarTexture != null) mStarTexture.unbind(gl);
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
		mHasStarTextureUniformLocation = shader.getUniformLocation(gl, "HasStarTexture");
		mSunPositionUniformLocation = shader.getUniformLocation(gl, "SunPosition");
		mDayTimeUniformLocation = shader.getUniformLocation(gl, "DayTime");

		/* This uniform won't ever change, so just set it here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
		gl.glUniform1i(shader.getUniformLocation(gl, "StarTexture"), 1);
		shader.unbind(gl);
	}

	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_sky";
	}

}

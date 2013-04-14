package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;

/**
 * BlinnPhongMaterial.java
 * 
 * Implements the Blinn-Phong shading model.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-24
 */
public class BlinnPhongMaterial extends Material
{
	/* Blinn-Phong material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
	private Color3f mSpecularColor = new Color3f(1.0f, 1.0f, 1.0f);
	private float mPhongExponent = 50.0f;
	
	/* Optional textures for texture parameterized rendering. */
	private Texture2D mDiffuseTexture = null;
	private Texture2D mSpecularTexture = null;
	private Texture2D mExponentTexture = null;
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mSpecularUniformLocation = -1;
	private int mExponentUniformLocation = -1;
	private int mHasDiffuseTextureUniformLocation = -1;
	private int mHasSpecularTextureUniformLocation = -1;
	private int mHasExponentTextureUniformLocation = -1;
	
	public BlinnPhongMaterial()
	{
		/* Default constructor. */
	}

	public BlinnPhongMaterial(Color3f diffuse)
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

	public Color3f getSpecularColor()
	{
		return mSpecularColor;
	}
	
	public void setSpecularColor(Color3f specular)
	{
		mSpecularColor = specular;
	}
	
	public float getPhongExponent()
	{
		return mPhongExponent;
	}
	
	public void setPhongExponent(float exponent)
	{
		mPhongExponent = exponent;
	}

	public Texture2D getDiffuseTexture()
	{
		return mDiffuseTexture;
	}
	
	public void setDiffuseTexture(Texture2D texture)
	{
		mDiffuseTexture = texture;
	}

	public Texture2D getSpecularTexture()
	{
		return mSpecularTexture;
	}
	
	public void setSpecularTexture(Texture2D texture)
	{
		mSpecularTexture = texture;
	}
	
	public Texture2D getExponentTexture()
	{
		return mExponentTexture;
	}
	
	public void setExponentTexture(Texture2D texture)
	{
		mExponentTexture = texture;
	}

	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_blinnphong";
	}
		
	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader and any textures, and update uniforms. */
		getShaderProgram().bind(gl);
		
		// TODO PA3 Prereq: Set shader uniforms and bind any textures.
		// TODO PA1: Set shader uniforms and bind any textures.		
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		gl.glUniform3f(mSpecularUniformLocation, mSpecularColor.x, mSpecularColor.y, mSpecularColor.z);
		gl.glUniform1f(mExponentUniformLocation, mPhongExponent);
		
		if (mDiffuseTexture == null) {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 1);
			mDiffuseTexture.bind(gl, 0);
		}
		
		if (mSpecularTexture == null) {
			gl.glUniform1i(mHasSpecularTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasSpecularTextureUniformLocation, 1);
			mSpecularTexture.bind(gl, 1);
		}
		
		if (mExponentTexture == null) {
			gl.glUniform1i(mHasExponentTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasExponentTextureUniformLocation, 1);
			mExponentTexture.bind(gl, 2);
		}
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		mSpecularUniformLocation = shader.getUniformLocation(gl, "SpecularColor");
		mExponentUniformLocation = shader.getUniformLocation(gl, "PhongExponent");
		
		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
		mHasSpecularTextureUniformLocation = shader.getUniformLocation(gl, "HasSpecularTexture");
		mHasExponentTextureUniformLocation = shader.getUniformLocation(gl, "HasExponentTexture");
		
		/* These are only set once, so set them here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
		gl.glUniform1i(shader.getUniformLocation(gl, "SpecularTexture"), 1);
		gl.glUniform1i(shader.getUniformLocation(gl, "ExponentTexture"), 2);
		shader.unbind(gl);
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind everything bound in bind(). */
		getShaderProgram().unbind(gl);
		
		// TODO PA3 Prereq: Unbind any used textures.		
		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
		if (mSpecularTexture != null) mSpecularTexture.unbind(gl);
		if (mExponentTexture != null) mExponentTexture.unbind(gl);
	}
}

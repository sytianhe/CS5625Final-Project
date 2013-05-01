package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;

/**
 * LambertianMaterial.java
 * 
 * Implements a Lambertian (perfectly diffuse) material.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-27
 */
public class VertexLambertianMaterial extends Material
{
	/* Lambertian material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
	private Texture2D mDiffuseTexture = null;

	/* Uniform locations. */
	private int mDiffuseUniformLocation = -1;
	private int mHasDiffuseTextureUniformLocation = -1;
	
	private int mPhiUniformLocation = -1;
	private int mHUniformLocation = -1;
	private float phi;
	private float h;
	
	public VertexLambertianMaterial()
	{
		/* Default constructor. */
	}

	public VertexLambertianMaterial(Color3f diffuse, float Phi, float H)
	{
		mDiffuseColor.set(diffuse);
		this.phi = Phi;
		this.h = H;
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
	
	public float getPhi() {
		return phi;
	}

	public void setPhi(float x) {
		this.phi = x;
	}
	
	public void setDiffuseTexture(Texture2D texture)
	{
		mDiffuseTexture = texture;
	}

	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader, and any textures, and update uniforms. */
		getShaderProgram().bind(gl);

		// TODO PA3 Prereq: Set shader uniforms and bind any textures.
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		gl.glUniform1f(mPhiUniformLocation, phi);
		gl.glUniform1f(mHUniformLocation, h);
		
		if (mDiffuseTexture == null) {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 1);
			mDiffuseTexture.bind(gl, 0);
		}
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind anything bound in bind(). */
		getShaderProgram().unbind(gl);

		// TODO PA3 Prereq: Unbind any used textures.
		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
		mPhiUniformLocation = shader.getUniformLocation(gl, "phi");
		mHUniformLocation = shader.getUniformLocation(gl, "h");

		/* This uniform won't ever change, so just set it here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
		shader.unbind(gl);
	}

	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_vertexlambertian";
	}

}

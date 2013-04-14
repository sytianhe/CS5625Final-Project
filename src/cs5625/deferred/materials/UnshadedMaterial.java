package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;

/**
 * UnshadedMaterial.java
 * 
 * Implements an unshaded (constant color) material.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-27
 */
public class UnshadedMaterial extends Material
{
	private Color3f mColor = new Color3f(1.0f, 1.0f, 1.0f);
	private int mUniformLocation = -1;
	
	public UnshadedMaterial()
	{
		/* Nothing. */
	}
	
	public UnshadedMaterial(Color3f color)
	{
		mColor.set(color);
	}
	
	public Color3f getColor()
	{
		return mColor;
	}
	
	public void setColor(Color3f color)
	{
		mColor = color;
	}

	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader and update color uniform. */
		getShaderProgram().bind(gl);
		gl.glUniform3f(mUniformLocation, mColor.x, mColor.y, mColor.z);
	}

	@Override
	public void unbind(GL2 gl)
	{
		getShaderProgram().unbind(gl);
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mUniformLocation = shader.getUniformLocation(gl, "Color");
	}

	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_unshaded";
	}

}

package cs5625.deferred.materials;

import javax.media.opengl.GL2;

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
public class MandelbrotMaterial extends Material
{
	private float mMandelX = -2;
	private float mMandelY = -2 ;
	private float mMandelWidth = 4;
	private float mMandelHeight = 4;
	private float mMandelIterations = 64;
	
	private int mMandelXUniformLocation = -1;
	private int mMandelYUniformLocation = -1;
	private int mMandelWidthUniformLocation = -1;
	private int mMandelHeightUniformLocation = -1;
	private int mMandelIterationsUniformLocation = -1;
	
	
	public MandelbrotMaterial()
	{

	}

	public MandelbrotMaterial(float x, float y,float w, float h,float iterations)
	{
		mMandelX = x;
		mMandelY = y;
		mMandelWidth = w;
		mMandelHeight = h ;
		mMandelIterations = iterations;	
	}
	
	public float getX() {
		return mMandelX;
	}

	public void setX(float x) {
		this.mMandelX = x;
	}

	public float getY() {
		return mMandelY;
	}

	public void setY(float y) {
		this.mMandelY = y;
	}

	public float getHeight() {
		return mMandelHeight;
	}

	public void setHeight(float height) {
		this.mMandelHeight = height;
	}

	public float getWidth() {
		return mMandelWidth;
	}

	public void setWidth(float width) {
		this.mMandelWidth = width;
	}

	public float getIterations() {
		return mMandelIterations;
	}

	public void setIterations(int iterations) {
		this.mMandelIterations = iterations;
	}


	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_mandelbrot";
	}
		
	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader and any textures, and update uniforms. */
		getShaderProgram().bind(gl);
		
		gl.glUniform1f(mMandelXUniformLocation, mMandelX);
		gl.glUniform1f(mMandelYUniformLocation, mMandelY);
		gl.glUniform1f(mMandelWidthUniformLocation, mMandelWidth);
		gl.glUniform1f(mMandelHeightUniformLocation, mMandelHeight);
		gl.glUniform1f(mMandelIterationsUniformLocation, mMandelIterations);
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mMandelXUniformLocation = shader.getUniformLocation(gl, "mandel_x");
		mMandelYUniformLocation = shader.getUniformLocation(gl, "mandel_y");
		mMandelWidthUniformLocation = shader.getUniformLocation(gl, "mandel_width");
		mMandelHeightUniformLocation = shader.getUniformLocation(gl, "mandel_height");
		mMandelIterationsUniformLocation = shader.getUniformLocation(gl, "mandel_iterations");		
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind everything bound in bind(). */
		getShaderProgram().unbind(gl);
		
	}
}

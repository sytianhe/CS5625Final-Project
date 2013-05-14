package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;

/**
 * NormapMapMaterial.java
 * 
 * Implements the Blinn-Phong shading model with the option for a normal map.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), John DeCorato (jd537)
 * @date 2013-02-12
 */
public class ParallaxMapMaterial extends Material{
	/* Blinn-Phong material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
	private Color3f mSpecularColor = new Color3f(1.0f, 1.0f, 1.0f);
	private float mPhongExponent = 50.0f;
	
	/* Optional textures for texture parameterized rendering. */
	private Texture2D mDiffuseTexture = null;
	private Texture2D mSpecularTexture = null;
	private Texture2D mExponentTexture = null;
	
	/* Normal map */
	private Texture2D mNormalTexture = null;
	
	/* Height map */
	private Texture2D mHeightTexture = null;
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mSpecularUniformLocation = -1;
	private int mExponentUniformLocation = -1;

	private int mHasDiffuseTextureUniformLocation = -1;
	private int mHasSpecularTextureUniformLocation = -1;
	private int mHasExponentTextureUniformLocation = -1;
	private int mHasNormalTextureUniformLocation = -1;
	private int mHasHeightTextureUniformLocation = -1;
	
	public ParallaxMapMaterial() {
		
	}
	
	public ParallaxMapMaterial(Color3f diffuseColor) {
		mDiffuseColor = diffuseColor;
	}
	
	
	public ParallaxMapMaterial(Color3f diffuseColor, Texture2D normalTexture, Texture2D heightTexture) {
		mDiffuseColor = diffuseColor;
		mNormalTexture = normalTexture;
		mHeightTexture = heightTexture;
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

	
	public Texture2D getNormalTexture()
	{
		return mNormalTexture;
	}
	
	public void setNormalTexture(Texture2D texture)
	{
		mNormalTexture = texture;
	}
	
	public Texture2D getHeightTexture()
	{
		return mHeightTexture;
	}
	
	public void setHeightTexture(Texture2D texture)
	{
		mHeightTexture = texture;
	}
	
	@Override
	public void bind(GL2 gl) throws OpenGLException {
		getShaderProgram().bind(gl);
		
		//TODO PA2: Set shader uniforms and bind textures		
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		gl.glUniform3f(mSpecularUniformLocation, mSpecularColor.x, mSpecularColor.y, mSpecularColor.z);
		gl.glUniform1f(mExponentUniformLocation, mPhongExponent);
		//gl.glUniform1f(mHasNormalTextureUniformLocation );

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
		if (mNormalTexture == null) {
			gl.glUniform1i(mHasNormalTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasNormalTextureUniformLocation, 1);
			mNormalTexture.bind(gl, 3);
		}
		if (mHeightTexture == null) {
			gl.glUniform1i(mHasHeightTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasHeightTextureUniformLocation, 1);
			mHeightTexture.bind(gl, 4);
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
		mHasNormalTextureUniformLocation = shader.getUniformLocation(gl, "HasNormalTexture");
		mHasHeightTextureUniformLocation = shader.getUniformLocation(gl, "HasHeightTexture");
		
		/* These are only set once, so set them here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
		gl.glUniform1i(shader.getUniformLocation(gl, "SpecularTexture"), 1);
		gl.glUniform1i(shader.getUniformLocation(gl, "ExponentTexture"), 2);
		gl.glUniform1i(shader.getUniformLocation(gl, "NormalTexture"), 3);
		gl.glUniform1i(shader.getUniformLocation(gl, "HeightTexture"), 4);
		shader.unbind(gl);
	}
	
	@Override
	public void unbind(GL2 gl) {
		getShaderProgram().unbind(gl);
		
		// TODO PA2: Unbind any used textures.
		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
		if (mSpecularTexture != null) mSpecularTexture.unbind(gl);
		if (mExponentTexture != null) mExponentTexture.unbind(gl);
		if (mNormalTexture != null) mNormalTexture.unbind(gl);
		if (mHeightTexture != null) mHeightTexture.unbind(gl);
		
	}

	@Override
	public String[] getRequiredVertexAttributes() {
		return new String[] {"VertexTangent"};
	}
	
	@Override
	public String getShaderIdentifier() {
		return "shaders/testParallax";
	}

}

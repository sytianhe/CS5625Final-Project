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
public class BarkMaterial extends Material{
	/* Blinn-Phong material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);

	
	/* Optional textures for texture parameterized rendering. */
	private Texture2D mDiffuseTexture = null;
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mTextureSizeLocation = -1; 
	private int mHasDiffuseTextureUniformLocation = -1;	
	
	
	public BarkMaterial() {
		
	}
	
	public BarkMaterial(Color3f diffuseColor) {
		mDiffuseColor = diffuseColor;
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


	
	@Override
	public void bind(GL2 gl) throws OpenGLException {
		getShaderProgram().bind(gl);
		
		//TODO PA2: Set shader uniforms and bind textures		
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);


		if (mDiffuseTexture == null) {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasDiffuseTextureUniformLocation, 1);
			gl.glUniform2f(mTextureSizeLocation, mDiffuseTexture.getWidth(), mDiffuseTexture.getHeight()) ;
			mDiffuseTexture.bind(gl, 0);
		}
	}

	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		mTextureSizeLocation = shader.getUniformLocation(gl, "TextureSize");
		
		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
		
		/* These are only set once, so set them here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
		gl.glUniform1i(shader.getUniformLocation(gl, "SpecularTexture"), 1);
		gl.glUniform1i(shader.getUniformLocation(gl, "ExponentTexture"), 2);
		shader.unbind(gl);
	}
	
	@Override
	public void unbind(GL2 gl) {
		getShaderProgram().unbind(gl);
		
		// TODO PA2: Unbind any used textures.
		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
	}

	@Override
	public String[] getRequiredVertexAttributes() {
		return new String[] {"BarkMaterial"};
	}
	
	@Override
	public String getShaderIdentifier() {
		return "shaders/material_bark";
	}

}

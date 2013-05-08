package cs5625.deferred.materials;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.rendering.FramebufferObject;
import cs5625.deferred.rendering.ShaderProgram;


public class SandDuneMaterial extends Material{
	
	/* Setup framebuffer objects for generating dynamic height maps */
	/* The sanddune map FBO */
	protected FramebufferObject mSBufferFBO;
	
	protected final int SBuffer_SandDune1Index = 0;
	protected final int SBuffer_SandDune2Index = 1;

	/* Lambertian material properties. */
	private Color3f mDiffuseColor = new Color3f(1.00f, 1.00f, 0.00f);
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mSandDuneFBOLocation = -1;
	private int mHasSandDuneFBOUniformLocation = -1;

	
	public SandDuneMaterial()
	{
		/* Default constructor. */
	}

	/* Constructor with reference to sand dune frame buffer object storing height map. */
	public SandDuneMaterial(FramebufferObject fbo)
	{
		setSandDuneFBO(fbo);
	}
	
	public Color3f getDiffuseColor()
	{
		return mDiffuseColor;
	}
	
	public void setDiffuseColor(Color3f diffuse)
	{
		mDiffuseColor = diffuse;
	}

	public FramebufferObject  getSandDuneFBO()
	{
		return mSBufferFBO;
	}
	
	public void setSandDuneFBO(FramebufferObject fbo)
	{
		mSBufferFBO = fbo;
	}

	@Override
	public String getShaderIdentifier()
	{
		return "shaders/material_sanddune";
	}
		
	@Override
	public void bind(GL2 gl) throws OpenGLException
	{
		/* Bind shader and any textures, and update uniforms. */
		getShaderProgram().bind(gl);
			
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
				
		if (mSBufferFBO == null) {
			gl.glUniform1i(mHasSandDuneFBOUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasSandDuneFBOUniformLocation, 1);
			mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).bind(gl,0);
		}
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		
		mHasSandDuneFBOUniformLocation = shader.getUniformLocation(gl, "HasSandDuneHeightMap");
		
		/* These are only set once, so set them here. */
		shader.bind(gl);
		
		gl.glUniform1i(shader.getUniformLocation(gl, "SandDuneHeightMap"), 0);
		shader.unbind(gl);
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind everything bound in bind(). */
		getShaderProgram().unbind(gl);
		
		// TODO PA3 Prereq: Unbind any used textures.		
		if (mSBufferFBO != null) mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).unbind(gl);
	}
	
}
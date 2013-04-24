package cs5625.deferred.materials;
import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.ShaderProgram;


public class SandDuneMaterial extends Material{

	/* Blinn-Phong material properties. */
	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
	private Color3f mSpecularColor = new Color3f(1.0f, 1.0f, 1.0f);
	private float mPhongExponent = 50.0f;
	
	/* Optional textures for texture parameterized rendering. */
	private Texture2D mSandDuneTexture = null;
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mSandDuneTextureLocation = -1;
	private int mHasSandDuneTextureUniformLocation = -1;

	
	public SandDuneMaterial()
	{
		/* Default constructor. */
	}

	public SandDuneMaterial(Color3f diffuse)
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



	public Texture2D getSandDuneTexture()
	{
		return mSandDuneTexture;
	}
	
	public void setSandDuneTexture(Texture2D texture)
	{
		mSandDuneTexture = texture;
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
		
		// TODO PA3 Prereq: Set shader uniforms and bind any textures.
		// TODO PA1: Set shader uniforms and bind any textures.		
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		
		if (mSandDuneTexture == null) {
			gl.glUniform1i(mHasSandDuneTextureUniformLocation, 0);
		}
		else {
			gl.glUniform1i(mHasSandDuneTextureUniformLocation, 1);
			mSandDuneTexture.bind(gl, 0);
		}
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		
		mHasSandDuneTextureUniformLocation = shader.getUniformLocation(gl, "HasSandDuneTexture");
		
		/* These are only set once, so set them here. */
		shader.bind(gl);
		gl.glUniform1i(shader.getUniformLocation(gl, "SandDuneTexture"), 0);
		shader.unbind(gl);
	}

	@Override
	public void unbind(GL2 gl)
	{
		/* Unbind everything bound in bind(). */
		getShaderProgram().unbind(gl);
		
		// TODO PA3 Prereq: Unbind any used textures.		
		if (mSandDuneTexture != null) mSandDuneTexture.unbind(gl);
	}
}

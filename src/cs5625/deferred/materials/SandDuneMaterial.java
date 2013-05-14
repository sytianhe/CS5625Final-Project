package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.rendering.FramebufferObject;
import cs5625.deferred.rendering.Renderer;
import cs5625.deferred.rendering.ShaderProgram;


public class SandDuneMaterial extends Material{
	
	/* Setup framebuffer objects for generating dynamic height maps */
	/* The sanddune map FBO */
	protected Renderer mRenderer; 
	protected FramebufferObject mSBufferFBO;
	
	protected final int SBuffer_SandDune1Index = 0;
	protected final int SBuffer_SandDune2Index = 1;

	/* Lambertian material properties. */
	private Color3f mDiffuseColor = new Color3f(1.00f, 1.00f, 0.00f);
	private float mHeightFactor = 0.01f;
	
	/* Uniform locations for the shader. */
	private int mDiffuseUniformLocation = -1;
	private int mHasSandDuneFBOUniformLocation = -1;
	private int mTextureWidthLocation = -1; 
	private int mTextureHeightLocation = -1;
	private int mHeightFactorLocation = -1;
	
	/* Either use or dont use the SandDune simulation for shading. */
	private boolean mUseSandDuneSimulation = true;

	
	public SandDuneMaterial()
	{
		/* Default constructor. */
	}

	/* Constructor with reference to renderer.  
	 * The rendere manages a sand dune frame buffer object running which holds a dynamic height map. */
	public SandDuneMaterial(Renderer renderer) 
	{
		mRenderer = renderer;
		setSandDuneFBO(renderer.getSandDuneFrameBufferObject());
	}
	
	public Color3f getDiffuseColor()
	{
		return mDiffuseColor;
	}
	
	public void setDiffuseColor(Color3f diffuse)
	{
		mDiffuseColor = diffuse;
	}
	
	public void setSandDuneSimulation(boolean tf){
		mUseSandDuneSimulation = tf;
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
		/* Make sure we have the most recent frame buffer reference  .*/
		setSandDuneFBO(mRenderer.getSandDuneFrameBufferObject());

		/* Bind shader and any textures, and update uniforms. */
		getShaderProgram().bind(gl);
					
		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
		gl.glUniform1f(mHeightFactorLocation, mHeightFactor);
		if (!mUseSandDuneSimulation ||  mSBufferFBO == null) {
			gl.glUniform1i(mHasSandDuneFBOUniformLocation, 0);
		}
		else {
			gl.glUniform1f(mTextureWidthLocation, (float) mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).getWidth());		
			gl.glUniform1f(mTextureHeightLocation, (float) mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).getHeight());
			
			gl.glUniform1i(mHasSandDuneFBOUniformLocation, 1);
			mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).bind(gl,0);			
		}
	}
	
	@Override
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Get locations of uniforms in this shader. */
		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
		mTextureWidthLocation = shader.getUniformLocation(gl, "SamplerWidth");
		mTextureHeightLocation = shader.getUniformLocation(gl, "SamplerHeight");
		mHeightFactorLocation = shader.getUniformLocation(gl, "HeightFactor");

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
		
		if (mSBufferFBO != null) mSBufferFBO.getColorTexture(SBuffer_SandDune1Index).unbind(gl);
	}
	
}
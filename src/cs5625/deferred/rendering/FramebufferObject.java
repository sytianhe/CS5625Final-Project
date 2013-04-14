package cs5625.deferred.rendering;

import javax.media.opengl.GL2;

import cs5625.deferred.materials.Texture.Datatype;
import cs5625.deferred.materials.Texture.Format;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.OpenGLResourceObject;

/**
 * FramebufferObject.java
 * 
 * The FramebufferObject class encapsulates render-to-texture capability. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class FramebufferObject implements OpenGLResourceObject
{
	/*
	 * Private FBO data. Public things have associated 'get' methods.
	 */
	private Texture2D mColorTextures[];
	private Texture2D mDepthTexture;
	private int mHandle = -1;
	private int mWidth = -1;
	private int mHeight = -1;
	private boolean mIsBound = false;
	private int mPreviousBinding[] = new int[1];
	
	/**
	 * Creates a new FBO with the passed attributes and number of render targets.
	 *
	 * @param gl The OpenGL context in which this FBO lives.
	 * @param format The format for data in the color textures. 
	 * @param datatype The datatype for data in the color textures. 
	 * @param width The width of the renderable viewport and all the textures.
	 * @param height The height of the renderable viewport and all the textures.
	 * @param colorTextureCount The number of associated color textures/render targets. 
	 *        May specify 0 if you don't care about color output. It is an error to request
	 *        0 color textures and no depth buffer.
	 * @param makeDepthTexture True if a depth texture should be created. False if you don't need a depth buffer.
	 *        It is an error to request 0 color textures and no depth buffer.
	 * @param rectTextures If true, the created textures will be rectangular textures. If false, they will be regular 2D textures, 
	 *        which requires GL_EXT_texture_non_power_of_two if `width` and `height` aren't powers of two.
	 */
	public FramebufferObject(GL2 gl, Texture2D.Format format, Texture2D.Datatype datatype, int width, int height, int colorTextureCount, boolean makeDepthTexture, boolean rectTextures) throws OpenGLException
	{
		/* Sanity check. */
		if (colorTextureCount == 0 && !makeDepthTexture)
		{
			throw new OpenGLException("It is not valid to make an FBO with no color buffers and no depth buffer.");
		}
		
		int maxColorTextures[] = new int[1];
		gl.glGetIntegerv(GL2.GL_MAX_COLOR_ATTACHMENTS, maxColorTextures, 0);
		if (colorTextureCount > maxColorTextures[0])
		{
			throw new OpenGLException("Cannot create an FBO with " + colorTextureCount + " render targets. Your graphics card only supports " + maxColorTextures[0] + ".");
		}
		
		mWidth = width;
		mHeight = height;
		
		/* Create OpenGL FBO. */
		int names[] = new int[1];
		gl.glGenFramebuffers(1, names, 0);
		mHandle = names[0];
		
		/* Remember previous FBO binding, and then bind this one. */
		int previousBinding[] = new int[1];
		gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, previousBinding, 0);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, mHandle);
		
		/* Create and attach color textures. */
		mColorTextures = new Texture2D[colorTextureCount];
		
		for (int i = 0; i < colorTextureCount; ++i)
		{
			mColorTextures[i] = new Texture2D(gl, format, datatype, width, height, null, rectTextures);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + i, mColorTextures[i].getTextureTarget(), mColorTextures[i].getHandle(), 0);
		}
		
		/* Create and attach depth texture, if requested. */
		if (makeDepthTexture)
		{
			mDepthTexture = new Texture2D(gl, Format.DEPTH, Datatype.INT32, width, height, null, rectTextures);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, mDepthTexture.getTextureTarget(), mDepthTexture.getHandle(), 0);
		}

		/* Make sure everything is set up properly. */
		int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if (status != GL2.GL_FRAMEBUFFER_COMPLETE)
		{
			throw new OpenGLException("Framebuffer incomplete: " + status + ".");
		}
		
		/* Restore whatever FBO was bound before this function was called. */
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, previousBinding[0]);
	}
	
	/**
	 * Destroys this framebuffer object. 
	 * 
	 * The owned textures are also destroyed, so copy any you need first!
	 */
	public void releaseGPUResources(GL2 gl)
	{
		/* Release textures. */
		for (Texture2D tex : mColorTextures)
		{
			tex.releaseGPUResources(gl);
		}
		
		if (mDepthTexture != null)
		{
			mDepthTexture.releaseGPUResources(gl);
		}
		
		/* Destroy the FBO itself. */
		if (mHandle >= 0)
		{
			int names[] = new int[1];
			names[0] = mHandle;
			gl.glDeleteFramebuffers(1, names, 0);
			mHandle = -1;
		}
	}
	
	/**
	 * Binds a given texture to the first color target 
	 * This is useful when we want to render to a dynamic cube map texture. 
	 * 
	 * You can write to them by using the `gl_FragData[]` array from your shaders.
	 * While bound for rendering, the specified textures cannot be used for texturing operations.
	 */
	public void bindGiven(GL2 gl, int textureTarget, int textureHandle, int colorTextureIndex) throws OpenGLException
	{		
		/* Save state and adjust viewport if this is the first bind. */
		if (!mIsBound)
		{
			gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_VIEWPORT_BIT);
			gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, mPreviousBinding, 0);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, getHandle());
			gl.glViewport(0, 0, mWidth, mHeight);
			mIsBound = true;
		}
		
		/* Attach the given texture */
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + colorTextureIndex, textureTarget, textureHandle, 0);	
		
		/* Set draw buffer to the requested color attachment. */
		gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0 + colorTextureIndex);
		
		/* Make sure it worked. */
		OpenGLException.checkOpenGLError(gl);
	}
	
	
	/**
	 * Binds a single color texture of this FBO for rendering. 
	 * 
	 * You can write to this texture by setting either `gl_FragColor` or `gl_FragData[0]` from your shaders.
	 * While bound for rendering, the specified texture cannot be used for texturing operations.
	 */
	public void bindOne(GL2 gl, int colorTextureIndex) throws OpenGLException
	{
		/* Sanity check. */
		if (colorTextureIndex < 0 || colorTextureIndex >= getColorTextureCount())
		{
			throw new AssertionError("Color texture index out of range: required that 0 <= " + colorTextureIndex + " < " + getColorTextureCount() + ".");
		}
		
		/* Save state and adjust viewport if this is the first bind. */
		if (!mIsBound)
		{
			gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_VIEWPORT_BIT);
			gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, mPreviousBinding, 0);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, getHandle());
			gl.glViewport(0, 0, mWidth, mHeight);
			mIsBound = true;
		}
		
		/* Set draw buffer to the requested color attachment. */
		gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0 + colorTextureIndex);
		
		/* Make sure it worked. */
		OpenGLException.checkOpenGLError(gl);
	}
	
	/**
	 * Binds several specified color textures of this FBO for rendering.
	 *
	 * You can write to them by using the `gl_FragData[]` array from your shaders.
	 * While bound for rendering, the specified textures cannot be used for texturing operations.
	 */
	public void bindSome(GL2 gl, int indices[]) throws OpenGLException
	{		
		/* Save state and adjust viewport if this is the first bind. */
		if (!mIsBound)
		{
			gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_VIEWPORT_BIT);
			gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, mPreviousBinding, 0);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, getHandle());
			gl.glViewport(0, 0, mWidth, mHeight);
			mIsBound = true;
		}
		
		/* Set draw buffers to all color attachments. */
		int bindings[] = new int[indices.length];
		
		for (int i = 0; i < indices.length; ++i)
		{
			if (indices[i] >= getColorTextureCount())
			{
				throw new OpenGLException("FBO bind index " + indices[i] + " out of range; only " + getColorTextureCount() + " color textures.");
			}
			
			bindings[i] = GL2.GL_COLOR_ATTACHMENT0 + indices[i];
		}
		
		gl.glDrawBuffers(indices.length, bindings, 0);

		/* Make sure it worked. */
		OpenGLException.checkOpenGLError(gl);
	}

	/**
	 * Binds all color textures of this FBO for rendering.
	 *
	 * You can write to them by using the `gl_FragData[]` array from your shaders.
	 * While bound for rendering, the specified textures cannot be used for texturing operations.
	 */
	public void bindAll(GL2 gl) throws OpenGLException
	{		
		/* Save state and adjust viewport if this is the first bind. */
		if (!mIsBound)
		{
			gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_VIEWPORT_BIT);
			gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, mPreviousBinding, 0);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, getHandle());
			gl.glViewport(0, 0, mWidth, mHeight);
			mIsBound = true;
		}
		
		/* Set draw buffers to all color attachments. */
		int bindings[] = new int[getColorTextureCount()];
		
		for (int i = 0; i < getColorTextureCount(); ++i)
		{
			bindings[i] = GL2.GL_COLOR_ATTACHMENT0 + i;
		}
		
		gl.glDrawBuffers(getColorTextureCount(), bindings, 0);

		/* Make sure it worked. */
		OpenGLException.checkOpenGLError(gl);
	}
	
	/**
	 * Unbinds all color textures for rendering. You can now use them for texturing operations. 
	 */
	public void unbind(GL2 gl) throws OpenGLException
	{
		if (mIsBound)
		{
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, mPreviousBinding[0]);
			gl.glPopAttrib();
			mIsBound = false;

			OpenGLException.checkOpenGLError(gl);
		}
	}
	
	/**
	 * Returns the OpenGL handle/name/id of this framebuffer object.
	 */
	public int getHandle()
	{
		return mHandle;
	}

	/**
	 * The width of the FBO and all its textures, in pixels.
	 */
	public int getWidth()
	{
		return mWidth;
	}
	
	/** 
	 * The height of the FBO and all its textures, in pixels.
	 */
	public int getHeight()
	{
		return mHeight;
	}

	/**
	 * Returns the number of color textures associated with this FBO.
	 */
	public int getColorTextureCount()
	{
		return mColorTextures.length;
	}
	
	/**
	 * Returns the requested color texture.
	 */
	public Texture2D getColorTexture(int index)
	{
		return mColorTextures[index];
	}
	
	/**
	 * Returns the depth texture associated with this FBO, if any.
	 */
	public Texture2D getDepthTexture()
	{
		return mDepthTexture;
	}
}

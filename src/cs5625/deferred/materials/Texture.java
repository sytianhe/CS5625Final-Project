package cs5625.deferred.materials;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.OpenGLResourceObject;

/**
 * Texture.java
 * 
 * Serves as a base class for all OpenGL textures. Datatypes and functionality common to 
 * all types of textures are defined here.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Ivo Boyadzhiev (iib2)
 * @date 2012-03-23
 */
public abstract class Texture implements OpenGLResourceObject
{
	
	/** 
	 * The Datatype enum specifies how each channel of each sample of a texture is stored on the GPU.
	 */
	public enum Datatype
	{
		INT8, 
		INT16, 
		INT32, 
		FLOAT16, 
		FLOAT32;
		
		public int toGLtype() throws OpenGLException
		{
			switch(this)
			{
			case INT8:    return GL2.GL_UNSIGNED_BYTE;
			case INT16:   return GL2.GL_UNSIGNED_SHORT;
			case INT32:   return GL2.GL_UNSIGNED_INT;
			case FLOAT16: return GL2.GL_FLOAT;
			case FLOAT32: return GL2.GL_FLOAT;
			}
			
			throw new OpenGLException("Unknown Datatype enum: " + this + ".");
		}
	}

	/**
	 * The Format enum specifies what color or depth channels a texture contains.
	 */
	public enum Format
	{
		RGB, 
		RGBA, 
		LUMINANCE, 
		DEPTH;
		
		public int toGLformat() throws OpenGLException
		{
			switch(this)
			{
			case RGB:       return GL2.GL_RGB;
			case RGBA:      return GL2.GL_RGBA;
			case LUMINANCE: return GL2.GL_LUMINANCE;
			case DEPTH:     return GL2.GL_DEPTH_COMPONENT;
			}
			
			throw new OpenGLException("Unknown Format enum: " + this + ".");
		}

		public int toGLinternalformat(Datatype type) throws OpenGLException
		{
			switch(type)
			{
			case INT8: 
				switch(this)
				{
				case RGB:  		return GL2.GL_RGB8;
				case RGBA: 		return GL2.GL_RGBA8;
				case LUMINANCE: return GL2.GL_LUMINANCE8;
				default:        break;
				}
				break;
				
			case INT16: 
				switch(this)
				{
				case RGB:  		return GL2.GL_RGB16;
				case RGBA: 		return GL2.GL_RGBA16;
				case LUMINANCE: return GL2.GL_LUMINANCE16;
				case DEPTH:		return GL2.GL_DEPTH_COMPONENT16;
				}
				break;

			case INT32: 
				switch(this)
				{
				case DEPTH:		return GL2.GL_DEPTH_COMPONENT32;
				default:        break;
				}
				break;

			case FLOAT16: 
				switch(this)
				{
				case RGB:  		return GL2GL3.GL_RGB16F; // Use GL2GL3 to resolve ambiguous member.
				case RGBA: 		return GL2.GL_RGBA16F;
				case LUMINANCE: return GL2.GL_LUMINANCE16F;
				default:        break;
				}
				break;

			case FLOAT32:
				switch(this)
				{
				case RGB:  		return GL2.GL_RGB32F;
				case RGBA: 		return GL2.GL_RGBA32F;
				case LUMINANCE: return GL2.GL_LUMINANCE32F;
				case DEPTH:		return GL2.GL_DEPTH_COMPONENT32F;
				}
				break;
			}
			
			throw new OpenGLException("Invalid Format/Datatype combination: " + this + " and " + type + ".");
		}
	}
	
	/**
	 * The WrapMode enum controls the treatment of texture coordinates which are out of [0, 1] range (for power-of-two
	 * textures) or [0, width) or [0, height), respectively (for rectangular textures). 
	 */
	public enum WrapMode
	{
		REPEAT,
		CLAMP,
		CLAMP_TO_EDGE;
		
		public int toGLmode() throws OpenGLException
		{
			switch(this)
			{
				case REPEAT: return GL2.GL_REPEAT;
				case CLAMP:  return GL2.GL_CLAMP;
				case CLAMP_TO_EDGE:  return GL2.GL_CLAMP_TO_EDGE;
			}
			
			throw new OpenGLException("Unknown WrapMode enum: " + this + ".");
		}
	}
	
	/**
	 * Private variables common to all types of textures.
	 */
	protected Format mFormat = Format.RGBA;
	protected Datatype mDatatype = Datatype.INT8;
	private WrapMode mWrapMode = WrapMode.CLAMP_TO_EDGE;
	private int mHandle = -1;
	private int mBoundUnit = -1;
	private int mMaxMipMapLevel = 4;
	
	/**
	 * Texture constructor creates an OpenGL texture object.
	 */
	public Texture(GL2 gl) throws OpenGLException
	{
		int names[] = new int[1];
		gl.glGenTextures(1, names, 0);
		mHandle = names[0];
		
		try
		{
			OpenGLException.checkOpenGLError(gl);
		}
		catch (OpenGLException err)
		{
			gl.glDeleteTextures(1, names, 0);
			mHandle = -1;
			throw err;
		}
	}
	
	/**
	 * Returns the OpenGL name/handle/id of this texture.
	 * @return
	 */
	public int getHandle()
	{
		return mHandle;
	}
	
	/**
	 * Releases the OpenGL texture underlying this object.
	 */
	public void releaseGPUResources(GL2 gl)
	{
		if (mHandle < 0)
		{
			return;
		}
		
		int names[] = new int[1];
		names[0] = mHandle;
		gl.glDeleteTextures(1, names, 0);
		mHandle = -1;
			
		try
		{
			OpenGLException.checkOpenGLError(gl);
		}
		catch (OpenGLException err)
		{
			System.out.println("OpenGL error while releasing resources! " + err.getLocalizedMessage());
		}
	}
	
	/**
	 * Subclasses implement this to describe what target (e.g. GL_TEXTURE_2D) they require.
	 */
	public abstract int getTextureTarget();
	
	/**
	 * Bind this texture to the indicated texture unit.
	 * 
	 * Unbinds the texture if it was previously bound.
	 
	 * @param textureUnit The index of the texture unit to bind to, in the range [0, Texture2D.getNumTextureUnits() - 1].
	 */
	public void bind(GL2 gl, int textureUnit) throws OpenGLException
	{
		/* Sanity check. */
		if (textureUnit < 0 || textureUnit >= getNumTextureUnits(gl))
		{
			throw new OpenGLException("Cannot bind to out-of-range texture unit " + textureUnit + "; max is " + (getNumTextureUnits(gl) - 1));
		}
		
		/* Save the currently active texture, and then activate the requested one. */
		int previousActive[] = new int[1];
		gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
		gl.glActiveTexture(GL2.GL_TEXTURE0 + textureUnit);

		/* Unbind any previous binding. */
		unbind(gl);
		
		/* Bind. */
		int target = getTextureTarget();
		
		gl.glBindTexture(target, mHandle);
		gl.glEnable(target);
		mBoundUnit = textureUnit;
				
		/* Restore the previously active texture unit. */
		gl.glActiveTexture(previousActive[0]);
		
		OpenGLException.checkOpenGLError(gl);
	}
	
	/**
	 * Returns true if this texture is currently bound.
	 */
	public boolean isBound()
	{
		return (mBoundUnit >= 0);
	}
	
	/**
	 * Returns the texture unit this texture is currently bound to, or -1 if not bound.
	 */
	public int getBoundTextureUnit()
	{
		return mBoundUnit;
	}
	
	/**
	 * Unbinds this texture if it is currently bound.
	 */
	public void unbind(GL2 gl)
	{
		if (isBound())
		{
			/* Save the currently active texture, and then activate the requested one. */
			int previousActive[] = new int[1];
			gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE0 + mBoundUnit);

			/* Unbind. */
			int target = getTextureTarget();
			
			gl.glBindTexture(target, 0);
			gl.glDisable(target);
			mBoundUnit = -1;

			/* Restore the previously active texture unit. */
			gl.glActiveTexture(previousActive[0]);
		}
	}
	
	/**
	 * Returns the texture's wrap mode.
	 */
	public WrapMode getWrapMode()
	{
		return mWrapMode;
	}
	
	/**
	 * Sets the texture's wrap mode (for all coordinates).
	 */
	public void setWrapMode(GL2 gl, WrapMode mode) throws OpenGLException
	{
		int gl_mode = mWrapMode.toGLmode();
		int target = getTextureTarget();
		boolean wasBound = isBound();
		
		int previousActive[] = new int[1];
		gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
		
		if (!wasBound)
		{
			bind(gl, 0);
		}		
		
		gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());
		
		gl.glTexParameteri(target, GL2.GL_TEXTURE_WRAP_S, gl_mode);
		gl.glTexParameteri(target, GL2.GL_TEXTURE_WRAP_T, gl_mode);
		gl.glTexParameteri(target, GL2.GL_TEXTURE_WRAP_R, gl_mode);
		
		gl.glActiveTexture(previousActive[0]);
		
		if (!wasBound)
		{
			unbind(gl);
		}
	}
	
	/**
	 * The format of the texture's pixel data.
	 */
	public Format getFormat()
	{
		return mFormat;
	}
	
	/**
	 * The datatype of the texture's pixel data.
	 */
	public Datatype getDatatype()
	{
		return mDatatype;
	}
	
	
	/**
	 * Get the maximum mip map level
	 */
	public int getMaxMipMapLevel()
	{
		return mMaxMipMapLevel;	
	}
	
	/**
	 * Set the maximum mip map level
	 */
	public void setMaxMipMapLevel(int maxMipMapLevel)
	{
		mMaxMipMapLevel = maxMipMapLevel;		
	}
	
	/**
	 * Turn on/off the mip map functionality
	 */
	public void enableMipMap(GL2 gl, boolean isEnabled) throws OpenGLException
	{
		int target = getTextureTarget();
		
		boolean wasBound = isBound();
		
		int previousActive[] = new int[1];
		gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
		
		if (!wasBound)
		{
			bind(gl, 0);
		}		
		
		gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());

		if (isEnabled) {			
			gl.glGenerateMipmap(target);
			gl.glTexParameteri(target, GL2. GL_TEXTURE_BASE_LEVEL, 0);
		    gl.glTexParameteri(target, GL2.GL_TEXTURE_MAX_LEVEL, mMaxMipMapLevel);
		    
			gl.glTexParameterf(target, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);
			gl.glTexParameterf(target, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			gl.glTexParameterf(target, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		} else {
			gl.glTexParameterf(target, GL2.GL_GENERATE_MIPMAP, GL2.GL_FALSE);
			gl.glTexParameteri(target, GL2. GL_TEXTURE_BASE_LEVEL, 0);
		    gl.glTexParameteri(target, GL2.GL_TEXTURE_MAX_LEVEL, 0);

		}		
		
		gl.glActiveTexture(previousActive[0]);
		
		if (!wasBound)
		{
			unbind(gl);
		}
	}
	
	/**
	 * Enables or disables interpolation during texture sampling.
	 * Use this whenever your texture contains a quantity that cannot be interpolated.
	 * Note that interpolation is always on by default.
	 */
	public void enableInterpolation(GL2 gl, boolean isEnabled) throws OpenGLException
	{
		int target = getTextureTarget();
		
		boolean wasBound = isBound();
		
		int previousActive[] = new int[1];
		gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
		
		if (!wasBound)
		{
			bind(gl, 0);
		}
		
		gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());

		if (isEnabled) {
			gl.glTexParameteri(target, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(target, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		} else {
			gl.glTexParameteri(target, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(target, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		}
		
		gl.glActiveTexture(previousActive[0]);
		
		if (!wasBound)
		{
			unbind(gl);
		}
	}
		
	/**
	 * Returns the number of texture units available to shaders in the given OpenGL context.
	 */
	public static int getNumTextureUnits(GL2 gl)
	{
		int result[] = new int[1];
		gl.glGetIntegerv(GL2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, result, 0);
		return result[0];
	}
}

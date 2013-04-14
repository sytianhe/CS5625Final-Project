package cs5625.deferred.materials;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.Util;

/**
 * Texture2D.java
 * 
 * The Texture2D class represents a 2-dimensional (power-of-two or rectangular) OpenGL texture.
 * You can load textures from image files or render into them using the FramebufferObject class.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class Texture2D extends Texture
{
	/*
	 * Texture object state. Public things have associated 'get' methods.
	 */
	private int mWidth = -1;
	private int mHeight = -1;
	private int mTarget = -1;
	
	/**
	 * Creates a new texture object with the passed attributes and data.
	 * The "rectangularness" of the texture is chosen automatically.
	 * 
	 * @param gl The OpenGL context in which this texture lives.
	 * @param format The format of the provided pixel data.
	 * @param datatype The datatype of the provided pixel data.
	 * @param width The width of the provided pixel data.
	 * @param height The height of the provided pixel data.
	 * @param data The pixel data to copy into the texture. May be null, in which
	 *        case an empty texture of the indicated format and size is created.
	 */
	public Texture2D(GL2 gl, Format format, Datatype datatype, int width, int height, Buffer data) throws OpenGLException
	{
		super(gl);
		initialize(gl, format, datatype, width, height, !(isPOT(width) && isPOT(height)), data);
	}

	/**
	 * Creates a new texture object with the passed attributes and data.
	 *
	 * @param gl The OpenGL context in which this texture lives.
	 * @param format The format of the provided pixel data.
	 * @param datatype The datatype of the provided pixel data.
	 * @param width The width of the provided pixel data.
	 * @param height The height of the provided pixel data.
	 * @param data The pixel data to copy into the texture. May be null, in which
	 *        case an empty texture of the indicated format and size is created.
	 * @param rectTexture If true, the texture will be a rectangular texture. If false, it will not
	 * 		  be (requires GL_EXT_texture_non_power_of_two for non-power-of-two textures in this case).
	 */
	public Texture2D(GL2 gl, Format format, Datatype datatype, int width, int height, Buffer data, boolean rectTexture) throws OpenGLException
	{
		super(gl);
		initialize(gl, format, datatype, width, height, rectTexture, data);
	}
	
	/**
	 * Private constructor used by the static function `load()`.
	 */
	private Texture2D(GL2 gl) throws OpenGLException
	{
		super(gl);
	}
	
	/**
	 * Creates a new texture object with data loaded from an image file.
	 * The "rectangularness" of the texture is chosen automatically.
	 * 
	 * @param gl The OpenGL context in which this texture lives.
	 * @param identifier The name of the image to load.
	 * 
	 * @return The new texture, or null if the file doesn't exist.
	 */
	public static Texture2D load(GL2 gl, String identifier) throws OpenGLException, IOException
	{
		URL url = Texture2D.class.getClassLoader().getResource(identifier);
		if (url == null)
		{
			throw new IOException("Could not find texture file '" + identifier + "'.");
		}
		
		Texture2D result = new Texture2D(gl);
		BufferedImage image;
		
		/* Try to load image. */
		try
		{
			image = ImageIO.read(url);
		}
		catch (IOException err)
		{
			result.releaseGPUResources(gl);
			throw err;
		}
		
		/* Create buffer of image data. */
		Buffer imageData = createBufferFromImage(image);
		int width = image.getWidth();
		int height = image.getHeight();

		/* Initialize with that data. */
		result.initialize(gl, Format.RGBA, Datatype.INT8, width, height, !(isPOT(width) && isPOT(height)), imageData);
		return result;
	}

	/**
	 * Creates a new texture object with data loaded from an image file.
	 * 
	 * @param gl The OpenGL context in which this texture lives.
	 * @param identifier The name of the image to load.
	 * @param rectTexture If true, the texture will be a rectangular texture. If false, it will not
	 * 		  be (requires GL_EXT_texture_non_power_of_two for non-power-of-two textures in this case).
	 *
	 * @return The new texture, or null if the file doesn't exist.
	 */
	public static Texture2D load(GL2 gl, String identifier, boolean rectTexture) throws OpenGLException, IOException
	{
		URL url = Texture2D.class.getClassLoader().getResource(identifier);
		if (url == null)
		{
			throw new IOException("Could not find texture file '" + identifier + "'.");
		}
		
		Texture2D result = new Texture2D(gl);
		BufferedImage image;
		
		/* Try to load image. */
		try
		{
			image = ImageIO.read(url);
		}
		catch (IOException err)
		{
			result.releaseGPUResources(gl);
			throw err;
		}

		/* Create buffer of image data. */
		Buffer imageData = createBufferFromImage(image);
		int width = image.getWidth();
		int height = image.getHeight();

		/* Initialize with that data. */
		result.initialize(gl, Format.RGBA, Datatype.INT8, width, height, rectTexture, imageData);
		return result;
	}

	/**
	 * Initializes a new texture object with the passed attributes and data.
	 *
	 * @param gl The OpenGL context in which this texture lives.
	 * @param format The format of the provided pixel data.
	 * @param datatype The datatype of the provided pixel data.
	 * @param width The width of the provided pixel data.
	 * @param height The height of the provided pixel data.
	 * @param rectTexture If true, the texture will be a rectangular texture. If false, it will not
	 * 		  be (requires GL_EXT_texture_non_power_of_two for non-power-of-two textures in this case).
	 * @param data The pixel data to copy into the texture. May be null, in which
	 *        case an empty texture of the indicated format and size is created.
	 */
	private void initialize(GL2 gl, Format format, Datatype datatype, int width, int height, boolean rectTexture, Buffer data) throws OpenGLException
	{
		try
		{
			/* Get GL formats first, in case something is invalid. */
			int gltype = datatype.toGLtype();
			int glformat = format.toGLformat();
			int glinternalformat = format.toGLinternalformat(datatype);
			
			/* Get texture target. */
			mTarget = (rectTexture ? GL2.GL_TEXTURE_RECTANGLE_ARB : GL2.GL_TEXTURE_2D);

			/* If everything seems good, proceed. */
			mWidth = width;
			mHeight = height;
			mFormat = format;
			mDatatype = datatype;

			/* Bind and send texture data to OpenGL. */
			bind(gl, 0);

			int previousActive[] = new int[1];
			gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());

			gl.glTexImage2D(mTarget, 0, glinternalformat, width, height, 0, glformat, gltype, data);
			gl.glTexParameteri(mTarget, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(mTarget, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			
			gl.glActiveTexture(previousActive[0]);

			/* Unbind and make sure it all went smoothly. */
			unbind(gl);
			OpenGLException.checkOpenGLError(gl);
		}
		catch (OpenGLException err)
		{
			/* Clean up on error. */
			releaseGPUResources(gl);
			throw err;
		}
	}
	
	/**
	 * Create a buffer of raw pixel data from the passed image.
	 * The returned data is RGBA8 formatted (Format.RGBA and Datatype.INT8).
	 */
	private static Buffer createBufferFromImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		/* Allocate space to hold data. */
		ByteBuffer result = Buffers.newDirectByteBuffer(width * height * 4);

		/* Loop over pixels and fill buffer. */
		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				int pixel = image.getRGB(x, height - 1 - y);
				
				/* getRGB() returns an ARGB-packed int */
				int alpha = (pixel >> 24) & 0xff;
				int red   = (pixel >> 16) & 0xff;
				int green = (pixel >> 8)  & 0xff;
				int blue  = (pixel >> 0)  & 0xff;
					
				/* Repack into RGBA order for OpenGL. */
				result.put((byte)red);
				result.put((byte)green);
				result.put((byte)blue);
				result.put((byte)alpha);
			}
		}
		
		/* All done. */
		result.rewind();
		return result;
	}
	
	/**
	 * The width of the texture, in texels.
	 */
	public int getWidth()
	{
		return mWidth;
	}
	
	/** 
	 * The height of the texture, in texels.
	 */
	public int getHeight()
	{
		return mHeight;
	}
	
	/**
	 * Returns true if this is a rectangular (GL_TEXTURE_RECTANGLE) texture.
	 */
	public boolean isRectTexture()
	{
		return (mTarget == GL2.GL_TEXTURE_RECTANGLE);
	}
	
	/**
	 * Copies the texture image from the GPU and returns it in a buffer.
	 *
	 * The format of the returned data is described by `getFormat()` and `getDatatype()`, with the exception
	 * that 16-bit floating-point textures are read onto the CPU as regular 32-bit floats.
	 */
	public Buffer copyTextureImage(GL2 gl) throws OpenGLException
	{
		return copyTextureImage(gl, getFormat(), getDatatype());
	}
	
	/**
	 * Copies the texture image from the GPU and returns it in a buffer.
	 *
	 * @param format The desired format of the returned data. Does not have to match the format of the texture.
	 * @param datatype The desired datatype of the returned data. Does not have to match the datatype of the texture.
	 */
	public Buffer copyTextureImage(GL2 gl, Format format, Datatype datatype) throws OpenGLException
	{
		int numChannels;
	
		/* Determine number of channels per pixel. */
		switch(format)
		{
		case RGB:       numChannels = 3; break;
		case RGBA:      numChannels = 4; break;
		case LUMINANCE: numChannels = 1; break; 
		case DEPTH:     numChannels = 1; break;
		default:		throw new OpenGLException("Invalid Format enum " + format + ".");
		}
		
		/* Allocate buffer based on datatype. */
		Buffer result;
		int bufferSize = getWidth() * getHeight() * numChannels;
		
		switch(datatype)
		{
		case INT8:    result = Buffers.newDirectByteBuffer(bufferSize);  break;
		case INT16:   result = Buffers.newDirectShortBuffer(bufferSize); break;
		case INT32:   result = Buffers.newDirectIntBuffer(bufferSize);   break;
		case FLOAT16: result = Buffers.newDirectFloatBuffer(bufferSize); break;
		case FLOAT32: result = Buffers.newDirectFloatBuffer(bufferSize); break;
		default:	  throw new OpenGLException("Invalid Datatype enum " + datatype + ".");
		}
		
		/* Bound if we weren't already. */
		boolean wasBound = isBound();
		if (!wasBound)
		{
			bind(gl, 0);
		}
		
		/* Switch to this texture, get pixel data, and switch back. */
		int previousActive[] = new int[1];
		gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, previousActive, 0);
		gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());

		gl.glGetTexImage(getTextureTarget(), 0, mFormat.toGLformat(), mDatatype.toGLtype(), result);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0 + getBoundTextureUnit());
		
		/* Unbind if we were unbound before. */
		if (!wasBound)
		{
			unbind(gl);
		}
		
		/* Make sure it all went smoothly, and done. */
		OpenGLException.checkOpenGLError(gl);
		return result;
	}
	
	/**
	 * Renders a fullscreen quad with this texture.
	 */
	public void blit(GL2 gl) throws OpenGLException
	{
		/* Save state, set color to white, and bind. */
		gl.glPushAttrib(GL2.GL_CURRENT_BIT);		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		bind(gl, 0);
		
		/* Compute texture coordinates and render. */
		float smax = (isRectTexture() ? mWidth : 1.0f);
		float tmax = (isRectTexture() ? mHeight: 1.0f);
			
		Util.drawFullscreenQuad(gl, smax, tmax);
		
		/* Unbind, restore state, check for errors, and done. */
		unbind(gl);
		gl.glPopAttrib();
		
		OpenGLException.checkOpenGLError(gl);
	}
	
	@Override
	public int getTextureTarget() {
		return mTarget;
	}

	/**
	 * Returns true if the passed number is a power of two. Used for determining the texture target.
	 */
	private static boolean isPOT(int n)
	{
		return (n != 0 && ((n & (n - 1)) == 0));
	}
};

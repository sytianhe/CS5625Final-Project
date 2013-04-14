package cs5625.deferred.misc;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * OpenGLException.java
 * 
 * OpenGLException objects are thrown when an OpenGL error occurs (e.g. `glGetError() != 0`), or 
 * for other OpenGL-related errors. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class OpenGLException extends Exception
{

	/* Make Eclipse happy; since Exception is serializable, we must define a format version. */
	private static final long serialVersionUID = 1L;

	/** 
	 * Default constructor.
	 */
	public OpenGLException()
	{
		super();
	}

	/** 
	 * Constructor from string message.
	 */
	public OpenGLException(String arg0)
	{
		super(arg0);
	}

	/** 
	 * Copy constructor.
	 */
	public OpenGLException(Throwable arg0)
	{
		super(arg0);
	}

	/** 
	 * Message and cause constructor.
	 */
	public OpenGLException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}
	
	/**
	 * Checks the OpenGL error state using `glGetError()`, and throws an exception if 
	 * there is indeed an error.
	 * 
	 * You should call this method frequently in your OpenGL code.
	 */
	public static void checkOpenGLError(GL2 gl) throws OpenGLException
	{
		int err = gl.glGetError();
		
		if (err != 0)
		{
			GLU glu = GLU.createGLU();
			throw new OpenGLException("OpenGL error " + err + ": " + glu.gluErrorString(err) + ".");
		}
	}
}

package cs5625.deferred.misc;

/**
 * ScenegraphException.java
 * 
 * Thrown when an illegal scenegraph operation is attempted (e.g. inserting 
 * a node as a child of multiple nodes).
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-24
 */
public class ScenegraphException extends Exception
{
	/* Exceptions are serializable, so make Eclipse happy by defining serialized version. */
	private static final long serialVersionUID = 1L;

	public ScenegraphException()
	{
		super();
	}

	public ScenegraphException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public ScenegraphException(String arg0)
	{
		super(arg0);
	}

	public ScenegraphException(Throwable arg0)
	{
		super(arg0);
	}	
}

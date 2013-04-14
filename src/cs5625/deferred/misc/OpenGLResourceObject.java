package cs5625.deferred.misc;

import javax.media.opengl.GL2;

/**
 * OpenGLResourceObject.java
 * 
 * Classes which own OpenGL resources (textures, shaders, etc.) should implement
 * this interface so those resources can be freed when they are no longer needed.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public interface OpenGLResourceObject
{
	/**
	 * Releases any OpenGL resources owned by this object. The object is allowed to 
	 * be useless after this method; it's like a destructor. 
	 */
	public void releaseGPUResources(GL2 gl);
}

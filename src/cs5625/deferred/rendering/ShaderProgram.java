package cs5625.deferred.rendering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.media.opengl.GL2;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.OpenGLResourceObject;

/**
 * ShaderProgram.java
 * 
 * The ShaderProgram object manages a GLSL shader and allows you to load shader source files from disk.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class ShaderProgram implements OpenGLResourceObject
{
	
	/* Internal variables of the shader. */
	private int mHandle = -1;
	private int mPreviousBinding[] = new int[1];
	private boolean mIsBound = false;
	
	/**
	 * Loads a shader resource named by the given identifier.
	 *  
	 * @param gl The OpenGL state.
	 * @param identifier The identifier of the shader to load, using the class resource loader.
	 *        This should be the package path to the shader files, with no extension; for example, 
	 *        pass "shaders/foo" to load "shaders/foo.vp" and "shaders/foo.fp".
	 */
	public ShaderProgram(GL2 gl, String identifier) throws OpenGLException, IOException
	{
		/* Create the two shader objects. */
		int vertexShader   = createShaderObject(gl, GL2.GL_VERTEX_SHADER,   identifier + ".vp");
		int fragmentShader = createShaderObject(gl, GL2.GL_FRAGMENT_SHADER, identifier + ".fp");
		
		/* If neither were created, we have no shader. */
		if (vertexShader == 0 && fragmentShader == 0)
		{
			throw new OpenGLException("No source found for shader '" + identifier + "'.");
		}
		
		/* Create the shader program object. */
		mHandle = gl.glCreateProgram();		
		
		/* Attach vertex and fragment shaders, if they each exist. */
		if (vertexShader != 0)
		{
			gl.glAttachShader(mHandle, vertexShader);
		}
		
		if (fragmentShader != 0)
		{
			gl.glAttachShader(mHandle, fragmentShader);
		}
		
		/* Link the program and check its status. */
		gl.glLinkProgram(mHandle);
		int linkedSuccessfully[] = new int[1];;
		gl.glGetProgramiv(mHandle, GL2.GL_LINK_STATUS, linkedSuccessfully, 0);
		
		if (linkedSuccessfully[0] != GL2.GL_TRUE)
		{
			/* If linking failed, get the info log to tell us why. */
			int infoLogLength[] = new int[1];
			gl.glGetProgramiv(mHandle, GL2.GL_INFO_LOG_LENGTH, infoLogLength, 0);
			
			byte infoLog[] = new byte[infoLogLength[0]];
			int infoLogWritten[] = new int[1];
			gl.glGetProgramInfoLog(mHandle, infoLogLength[0], infoLogWritten, 0, infoLog, 0);
			
			/* Delete shader and program objects. */
			gl.glDeleteProgram(mHandle);
			mHandle = -1;
			
			if (vertexShader != 0)
			{
				gl.glDeleteShader(vertexShader);
			}
			
			if (fragmentShader != 0)
			{
				gl.glDeleteShader(fragmentShader);
			}
			
			/* Throw an exception explaining what broke. */
			throw new OpenGLException("Failed to link shader '" + identifier + "': " + new String(infoLog));
		}
	}
	
	/**
	 * Creates a single shader object with the given identifier.
	 * 
	 * @param gl The OpenGL state.
	 * @param type OpenGL constant (e.g. GL_VERTEX_SHADER) indicating the type of shader to create.
	 * @param identifier The identifier of the shader file to load. Must include extension.
	 * 
	 * @return The handle/name/id of the new shader object.
	 */
	private int createShaderObject(GL2 gl, int type, String identifier) throws OpenGLException, IOException
	{
		/* Read the source code file. */
		URL url = ShaderProgram.class.getClassLoader().getResource(identifier);
		if (url == null)
		{
			throw new IOException("Could not find shader file '" + identifier + "'.");
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		String code = "";
		String line = reader.readLine();
		
		while (line != null)
		{
			code += line + "\n";
			line = reader.readLine();
		}

		/* No code, no shader. */
		if (code == "")
		{
			return 0;
		}
		
		/* Create the shader object, send the code to OpenGL, and compile. */
		int shader = gl.glCreateShader(type);
		gl.glShaderSource(shader, 1, new String[]{code}, new int[]{code.length()}, 0);
		gl.glCompileShader(shader);

		/* Check shader status to make sure compilation was successful. */
		int compiledSuccessfully[] = new int[1];
		gl.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, compiledSuccessfully, 0);

		if (compiledSuccessfully[0] == GL2.GL_TRUE)
		{
			return shader;
		}
		else
		{
			/* On error, get the info log to tell us what went wrong. */
			int infoLogLength[] = new int[1];
			gl.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, infoLogLength, 0);

			int writtenLogLength[] = new int[1];
			byte infoLog[] = new byte[infoLogLength[0]];
			gl.glGetShaderInfoLog(shader, infoLogLength[0], writtenLogLength, 0, infoLog, 0);
			
			/* Delete the failed shader object. */
			gl.glDeleteShader(shader);
			
			/* Throw an exception detailing the problem. */
			String typeString = (type == GL2.GL_VERTEX_SHADER ? "vertex" : (type == GL2.GL_FRAGMENT_SHADER ? "fragment" : Integer.toString(type)));
			throw new OpenGLException("Failed to compile '" + identifier + "' " + typeString + " shader: " + new String(infoLog));
		}
	}
	
	/**
	 * Returns a handle to the OpenGL shader object.
	 */
	public int getHandle()
	{
		return mHandle;
	}

	/**
	 * Activates this shader so it will be used for subsequent rendering.
	 * 
	 * You must also bind a shader to update its uniforms (other than bindable uniform buffers).
	 */
	public void bind(GL2 gl)
	{
		if (!isBound())
		{
			/* Remember the shader bound before us. */
			gl.glGetIntegerv(GL2.GL_CURRENT_PROGRAM, mPreviousBinding, 0);
			gl.glUseProgram(mHandle);
			mIsBound = true;
		}
	}
	
	/**
	 * Returns true if this shader is currently bound.
	 */
	public boolean isBound()
	{
		return mIsBound;
	}

	/**
	 * Unbinds this shader, so it will not be used for subsequent rendering.
	 * 
	 * This will restore whatever shader was bound when this shader's `bind()` function was called, 
	 * which might be none, in which case you're back to the fixed-functionality pipeline.
	 */
	public void unbind(GL2 gl)
	{
		if (isBound())
		{
			gl.glUseProgram(mPreviousBinding[0]);
			mIsBound = false;
		}
	}
	
	/**
	 * Returns the location of the named uniform in this shader, or -1 if the uniform
	 * doesn't appear (or isn't used) in the shader.

	 * @param gl The OpenGL state.
	 * @param uniformName Name of the desired uniform.
	 * 
	 * @return Location of the named uniform, to be used with the `glUniform**()` functions.
	 */
	public int getUniformLocation(GL2 gl, String uniformName)
	{
		return gl.glGetUniformLocation(mHandle, uniformName);
	}
	
	/**
	 * Returns the location of the named attribute in this shader, or -1 if the attribute
	 * doesn't appear (or isn't used) in the shader.

	 * @param gl The OpenGL state.
	 * @param attributeName Name of the desired attribute.
	 * 
	 * @return Location of the named attribute, to be used with the `glVertexAttribute**()` functions.
	 */
	public int getAttribLocation(GL2 gl, String attributeName)
	{
		return gl.glGetAttribLocation(mHandle, attributeName);
	}
	
	/**
	 * Queries the shader program for the vector size of a uniform array.
	 * For example, Renderer uses this to figure out how many lights the ubershader supports.
	 * 
	 * @param gl The OpenGL state.
	 * @param arrayName The name of the array with no bracket notation attached.
	 * @return The vector size of the array or zero if it doesn't exist.
	 */
	public int getUniformArraySize(GL2 gl, String arrayName)
	{
		int count[] = new int[1];
		int maxLen[] = new int[1];
		
		/* Start by figuring out how many uniforms there are and what the name buffer size should be. */
	    gl.glGetProgramiv(getHandle(), GL2.GL_ACTIVE_UNIFORMS, count, 0);
	    gl.glGetProgramiv(getHandle(), GL2.GL_ACTIVE_UNIFORM_MAX_LENGTH, maxLen, 0);
	    
	    if (maxLen[0] < 1)
	    {
	    	// Pick some large buffer size if the query fails.
	    	// This is known to happen on Intel GPUs.
	    	maxLen[0] = 512;
	    }
	    
	    int size[] = new int[1];
	    int type[] = new int[1];
	    int used[] = new int[1];
		byte name[] = new byte[maxLen[0]];

		/* Loop over the uniforms until we find "arrayName" or "arrayName[0]" and grab its size. */
	    for(int i = 0; i < count[0]; ++i)
	    {
	    	/* We provide arrays for all fields (even if we don't use them) to prevent crashes. */
	    	gl.glGetActiveUniform(getHandle(), i, maxLen[0], used, 0, size, 0, type, 0, name, 0);
	    	
	    	String str = new String(name, 0, used[0]);
	    	if(str.equals(arrayName) || str.equals(arrayName + "[0]"))
	    	{
	    		return size[0];
	    	}
	    }
	    
	    try
		{
			OpenGLException.checkOpenGLError(gl);
		}
		catch (OpenGLException err)
		{
			err.printStackTrace();
		}
	    
	    // Not found.
	    return 0;
	}
	
	@Override
	public void releaseGPUResources(GL2 gl)
	{
		if (mHandle < 0)
		{
			return;
		}
		
		/* Get list of attached shader objects. */
		int attachedShadersCount[] = new int[1];
		gl.glGetProgramiv(mHandle, GL2.GL_ATTACHED_SHADERS, attachedShadersCount, 0);
		
		int attachedShaderHandles[] = new int[attachedShadersCount[0]];
		gl.glGetAttachedShaders(mHandle, attachedShadersCount[0], null, 0, attachedShaderHandles, 0);
		
		/* Delete the program object. */
		gl.glDeleteProgram(mHandle);
		mHandle = -1;

		/* Delete each attached shader object. */
		for (int shader : attachedShaderHandles)
		{
			gl.glDeleteShader(shader);
		}
		
		/* We can't throw an exception here, but we can at least report if something went wrong. */
		try
		{
			OpenGLException.checkOpenGLError(gl);
		}
		catch (OpenGLException err)
		{
			err.printStackTrace();
		}
	}
}

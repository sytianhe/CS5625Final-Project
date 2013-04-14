package cs5625.deferred.materials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Color3f;

import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.OpenGLResourceObject;
import cs5625.deferred.misc.Util;
import cs5625.deferred.rendering.ShaderProgram;
import cs5625.deferred.scenegraph.Geometry;

/**
 * Material.java
 * 
 * The Material abstract class serves as a base for all object materials. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public abstract class Material implements OpenGLResourceObject
{
	/** This material's shader, if any. */
	private ShaderProgram mShaderProgram = null;
	
	/**
	 * Activates this material, causing it to be applied to all subsequent rendering until deactivated.
	 * 
	 * `retrieveShader()` is always called before this, so `getShaderProgram()` will return the up-to-date 
	 * shader for this material.
	 * @param gl The OpenGL state.
	 */
	public abstract void bind(GL2 gl) throws OpenGLException;
	
	/**
	 * Deactivates this material, so that subsequent rendering does not use it.
	 * 
	 * OpenGL should be restored to the state it was in before bind() was called, but since 
	 * subclasses are responsible for doing the save and restore, it can't be guaranteed.
	 */
	public abstract void unbind(GL2 gl);

	/**
	 * Subclasses must return the identifier of their shader.
	 */
	public abstract String getShaderIdentifier();
	
	/**
	 * Returns the shader program this material uses to fill the deferred renderer's gbuffer.
	 */
	public ShaderProgram getShaderProgram()
	{
		return mShaderProgram;
	}
	
	/**
	 * Returns an array of names of vertex attributes which a mesh must have to render correctly
	 * using this material, or an empty array if none are required.
	 */
	public String[] getRequiredVertexAttributes()
	{
		return new String[0];
	}
	
	/**
	 * Retrieves the shader for this material from the cache, loading and initializing it if necessary, 
	 * and stores it in the 'mShaderProgram' instance variable.
	 * 
	 * If this material object is retrieving its shader for the first time, `initializeShader()` is called 
	 * before this method returns. 
	 * 
	 * @param gl The OpenGL state, necessary for creating a new shader.
	 * @param shaderCache If the shader for this material already exists here, that object is returned. 
	 *        If it doesn't, the shader is loaded and added to the cache.
	 */
	public void retrieveShader(GL2 gl, Map<Class<? extends Material>, ShaderProgram> shaderCache)
	{
		if (!shaderCache.containsKey(this.getClass()))
		{
			try
			{
				shaderCache.put(this.getClass(), new ShaderProgram(gl, getShaderIdentifier()));
			}
			catch (Exception err)
			{
				err.printStackTrace();
				System.exit(-1);
			}
		}
		
		ShaderProgram shader = shaderCache.get(this.getClass());
		
		if (mShaderProgram != shader)
		{
			mShaderProgram = shader;
			initializeShader(gl, shader);
		}
	}
	
	/**
	 * Initializes a newly retrieved shader. 
	 * 
	 * The default implementation does nothing. Subclasses should retrieve uniform locations. 
	 * The shader may or may not have been initialized by another material object (of the same class) already.
	 * 
	 * @param gl The OpenGL state.
	 * @param shader This material's shader; same as that returned by `getShaderProgram()`, just passed here 
	 *        for convenience.
	 */
	protected void initializeShader(GL2 gl, ShaderProgram shader)
	{
		/* Do nothing by default. */
	}
	
	@Override
	public void releaseGPUResources(GL2 gl)
	{
		if (mShaderProgram != null)
		{
			mShaderProgram.releaseGPUResources(gl);
		}
	}
	
	/**
	 * Loads materials from a .mtl file. 
	 * 
	 * @param identifier The identifier of the material file to load. Of the form 'models/my_materials.mtl'. 
	 * 
	 * @return A map containing the named materials loaded from the file, keyed on material name. 
	 */
	public static Map<String, Material> load(String identifier) throws IOException
	{
		/* Open the file. */
		URL url = Geometry.class.getClassLoader().getResource(identifier);
		if (url == null)
		{
			throw new IOException("Could not find material file '" + identifier + "'.");
		}
		
		Map<String, Material> materialsMap = new HashMap<String, Material>();		
		BlinnPhongMaterial currentMaterial = null;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

		/* Read and parse each line. */
		String line = reader.readLine();
		while (line != null)
		{
			line = line.trim();
		
			if (line.length() == 0)
			{
				/* Skip blank lines. */
			}
			else if (line.charAt(0) == '#')
			{
				/* Skip comment lines. */
			}
			else
			{
				/* Split the line on whitespace to identify the command. */
				String words[] = Util.splitString(line, " \t\n\r", false);

				if (words[0].equals("newmtl"))
				{
					/* Create the material. All materials here are Blinn-Phong. */
					currentMaterial = new BlinnPhongMaterial();
					String materialName = line.substring("newmtl".length()).trim();
					materialsMap.put(materialName, currentMaterial);
				}
				else if (words[0].equals("Kd"))
				{
					/* Set diffuse color. */
					currentMaterial.setDiffuseColor(new Color3f(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
				}
				else if (words[0].equals("Ks"))
				{
					/* Set specular color. */
					currentMaterial.setSpecularColor(new Color3f(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
				}
				else if (words[0].equals("Ns"))
				{
					/* Set shininess. */
					currentMaterial.setPhongExponent(Float.parseFloat(words[1]));
				}
				else if (words[0].equals("map_Kd"))
				{
					/* Set diffuse texture. */
					String textureName = line.substring("map_Kd".length()).trim();
					try
					{
						currentMaterial.setDiffuseTexture(Texture2D.load(GLU.getCurrentGL().getGL2(), Util.makeIdentifierOfSibling(identifier, textureName)));
					}
					catch (OpenGLException err)
					{
						System.out.println("Error creating texture '" + textureName + "' in material file '" + identifier + "'.");
						err.printStackTrace();
					}
				}
				else if (words[0].equals("map_Ks"))
				{
					/* Set diffuse texture. */
					String textureName = line.substring("map_Ks".length()).trim();
					try
					{
						currentMaterial.setSpecularTexture(Texture2D.load(GLU.getCurrentGL().getGL2(), Util.makeIdentifierOfSibling(identifier, textureName)));
					}
					catch (OpenGLException err)
					{
						System.out.println("Error creating texture '" + textureName + "' in material file '" + identifier + "'.");
						err.printStackTrace();
					}
				}
				else
				{
					/* Other commands ignored. */
				}
			}
			
			line = reader.readLine();
		}

		/* Done. */
		return materialsMap;
	}
}

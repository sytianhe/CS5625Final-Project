package cs5625.deferred.misc;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;

import cs5625.deferred.materials.Texture2D;

/**
 * Util.java
 * 
 * Class with a few general utility functions.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Ivaylo Boyadzhiev (iib2)
 * @date 2012-03-24
 */
public class Util
{
	/**
	 * Rotates the tuple (vector or point) by a quaternion.
	 * 
	 * Just does `quat * tuple * inverse(quat)`.
	 * 
	 * @param quat The quaternion to rotate by.
	 * @param tuple The tuple to rotate. The rotation is done in-place; on 
	 *        output, `tuple` has been rotated by `quat`.
	 */
	public static void rotateTuple(Quat4f quat, Tuple3f tuple)
	{
		if (tuple.x == 0.0f && tuple.y == 0.0f && tuple.z == 0.0f)
		{
			return;
		}
		
		/* Quat4f.mul() implicitly normalizes the result, so remember the length. */
		float length = (float)Math.sqrt(tuple.x * tuple.x + tuple.y * tuple.y + tuple.z * tuple.z);
		tuple.scale(1.0f / length);
		
		/* quat * tuple * inverse(quat) */
		Quat4f temp = new Quat4f(quat);
		temp.mul(new Quat4f(tuple.x, tuple.y, tuple.z, 0.0f));
		temp.mulInverse(quat);
		
		tuple.x = temp.x * length;
		tuple.y = temp.y * length;
		tuple.z = temp.z * length;
	}
	
	/**
	 * Get the 3-by-3 matrix, representing the quaternion
	 * 
	 * @param quat The quaternion to transform to a matrix. 
	 * 
	 * @return The 3-by-3 (rotation) matrix, representing the quaternion.
	 */
	public static Matrix3f getRotationMatrix3f(Quat4f quat)
	{
		float x = quat.x, y = quat.y, z = quat.z, w = quat.w;
		
		return new Matrix3f(1 - 2*y*y - 2*z*z, 2*x*y + 2*w*z, 2*x*z - 2*w*y,
				2*x*y - 2*w*z, 1 - 2*x*x - 2*z*z, 2*y*z + 2*w*x,
				2*x*z + 2*w*y, 2*y*z - 2*w*x, 1-2*x*x - 2*y*y);
	}
	
	/**
	 * Get the 4-by-4 matrix, representing the quaternion
	 * 
	 * @param quat The quaternion to transform to a matrix. 
	 * 
	 * @return The 4-by-4 (rotation) matrix, representing the quaternion,
	 * the translation is left to 0.
	 */
	public static Matrix4f getRotationMatrix4f(Quat4f quat)
	{
		float x = quat.x, y = quat.y, z = quat.z, w = quat.w;
		
		return new Matrix4f(1 - 2*y*y - 2*z*z, 2*x*y + 2*w*z, 2*x*z - 2*w*y, 0,
				2*x*y - 2*w*z, 1 - 2*x*x - 2*z*z, 2*y*z + 2*w*x, 0,
				2*x*z + 2*w*y, 2*y*z - 2*w*x, 1-2*x*x - 2*y*y, 0,
				0, 0, 0, 1);
	}
	
	
	
	/**
	 * Returns a column-major float array of the passed matrix, suitable for `glMultMatrixf()` and the like.
	 */
	public static float[] fromMatrix4f(Matrix4f m)
	{
		return new float[]{
			m.m00, m.m10, m.m20, m.m30,
			m.m01, m.m11, m.m21, m.m31,
			m.m02, m.m12, m.m22, m.m32,
			m.m03, m.m13, m.m23, m.m33
		};
	}
	
	/**
	 * Returns a column-major float array of the passed matrix, suitable for `glMultMatrixf()` and the like.
	 */
	public static float[] fromMatrix3f(Matrix3f m)
	{
		return new float[]{
			m.m00, m.m10, m.m20,
			m.m01, m.m11, m.m21,
			m.m02, m.m12, m.m22
		};
	}
	
	/**
	 * Splits the passed string into words separated by any one of a list of characters.
	 * 
	 * This method is faster than String.split(), which uses regular expressions. We don't
	 * need regex splitting here.
	 * 
	 * @param str The string to split.
	 * @param delims Words in the string are separated by any of these.
	 * @param keepEmptyWords If true, empty words (between consecutive delimeter characters) are 
	 *        included in the result array.
	 *        
	 * @return Array of words separated by characters in delims.
	 */
	public static String[] splitString(String str, String delims, boolean keepEmptyWords)
	{
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		
		int offset = 0;
		
		/* Loop over characters in the string. */
		while (offset < str.length())
		{
			char c = str.charAt(offset);
			
			if (delims.indexOf(c) < 0)
			{
				/* If this isn't in the delimeters list, add it to the current word. */
				builder.append(c);
			}
			else
			{
				/* This is a delimeter, so add the current word, if any, to the results list, 
				 * and clear the builder. */ 
				if (builder.length() > 0 || keepEmptyWords)
				{
					result.add(builder.toString());
					builder.delete(0, builder.length());
				}
			}
			
			/* Next character. */
			++offset;
		}
		
		/* Add the last word, if any. */
		if (builder.length() > 0 || keepEmptyWords)
		{
			result.add(builder.toString());
		}

		return result.toArray(new String[]{});
	}
	
	/**
	 * Helper function to join a string array by inserting a delimeter between each element.
	 *
	 * @param strs The strings to join.
	 * @param delimeter The delimeter to insert between each element of `strs`.
	 * 
	 * @return A string of the form `strs[0] + delimeter + strs[1] + delimeter + strs[2] ...`.
	 */
	public static String joinString(String[] strs, String delimeter)
	{
		if (strs.length == 0)
		{
			return "";
		}
		
		String result = strs[0];
		
		for (int i = 1; i < strs.length; ++i)
		{
			result += delimeter + strs[i];
		}
		
		return result;
	}
	
	/**
	 * Helper function to generate the identifier of a file in the same directory as another file.
	 */
	public static String makeIdentifierOfSibling(String identifier, String siblingName)
	{
		String result[] = splitString(identifier, "/", true);
		result[result.length - 1] = siblingName;
		return joinString(result, "/");
	}
	
	/**
	 * Utility function which draws a fullscreen quad.
	 * 
	 * Can be used whenever you need one (e.g. postprocessing).
	 * 
	 * @param gl The OpenGL state.
	 * @param smax The maximum s (or u) texture coordinate for the quad. The minimum is assumed to be 0.
	 * @param tmax The maximum t (or v) texture coordinate for the quad. The minimum is assumed to be 0.
	 */
	public static void drawFullscreenQuad(GL2 gl, float smax, float tmax) throws OpenGLException
	{
		/* Save which matrix is active. */
		gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
		
		/* Reset projection and modelview matrices. */
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		/* Render the quad. */
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
		gl.glTexCoord2f(smax, 0.0f); gl.glVertex2f( 1.0f, -1.0f);
		gl.glTexCoord2f(smax, tmax); gl.glVertex2f( 1.0f,  1.0f);
		gl.glTexCoord2f(0.0f, tmax); gl.glVertex2f(-1.0f,  1.0f);
		gl.glEnd();
		
		/* Restore matrices. */
		gl.glPopMatrix();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();

		/* Restore active matrix. */
		gl.glPopAttrib();
		
		/* Make sure nothing went wrong. */
		OpenGLException.checkOpenGLError(gl);
	}
	
	/**
	 * Clears the display and renders a fullscreen quad with the passed texture.
	 * 
	 * @param gl The OpenGL state.
	 * @param texture The texture to display.
	 */
	public static void renderTextureFullscreen(GL2 gl, Texture2D texture) throws OpenGLException
	{
		/* Save state and make sure the output will overwrite whatever was there. This way
		 * we don't have to waste time clearing buffers. */
		gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_ENABLE_BIT);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
		
		/* Draw the texture. */
		texture.blit(gl);
		
		/* Restore state. */
		gl.glPopAttrib();
	}
}

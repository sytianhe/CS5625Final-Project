package cs5625.deferred.scenegraph;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.misc.OpenGLResourceObject;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;

/**
 * SceneObject.java
 * 
 * The SceneObject class represents a node in the scenegraph. It provides basic
 * position and orientation information, and methods for manipulating the hierarchy 
 * and for transforming between different spaces.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class SceneObject implements OpenGLResourceObject
{
	/* Attributes common to all SceneObject subclasses. */
	protected Point3f mPosition = new Point3f();
	protected Quat4f mOrientation = new Quat4f(0.0f, 0.0f, 0.0f, 1.0f);
	private float mScale = 1.0f;
	private String mName = "";
	private SceneObject mParent = null;
	private boolean mIsVisible = true;
	
	/* List of child nodes. */
	private ArrayList<SceneObject> mChildren = new ArrayList<SceneObject>();
	
	/**
	 * Updates any animation for this node at each frame, if any.
	 * Default implementation calls `animate(dt)` on children.
	 * 
	 * @param dt The time delta (in seconds) since the last frame.
	 */
	public void animate(float dt)
	{
		for (SceneObject child : mChildren)
		{
			child.animate(dt);
		}
	}
	
	/**
	 * Returns this object's parent node, if any.
	 */
	public SceneObject getParent()
	{
		return mParent;
	}
	
	/**
	 * Returns the visibility state of the object
	 */
	public boolean isVisible()
	{
		return mIsVisible;
	}
	
	public void setVisible(boolean visible)
	{
		mIsVisible = visible;
	}
	
	/**
	 * Returns all direct child nodes of this node.
	 */
	public List<SceneObject> getChildren()
	{
		return mChildren;
	}

	/**
	 * Adds a new child node to this node. 
	 * 
	 * @throws ScenegraphException If the new node is already a child of another node. 
	 */
	public void addChild(SceneObject child) throws ScenegraphException
	{
		if (child.mParent != null)
		{
			throw new ScenegraphException("Cannot add child to multiple parents.");
		}
		
		mChildren.add(child);
		child.mParent = this;
	}

	/**
	 * Adds new children nodes to this node. 
	 * 
	 * @throws ScenegraphException If any new nodes are already children of another node. 
	 */
	public void addChildren(Iterable<SceneObject> children) throws ScenegraphException
	{
		for (SceneObject child : children)
		{
			addChild(child);
		}
	}

	/**
	 * Adds a list of geometry objects as new children of this node. 
	 * 
	 * Convenience function so you can do `someSceneNode.addGeometry(Geometry.load(...))`.
	 * 
	 * @throws ScenegraphException If any of the new nodes are already children of another node. 
	 */
	public void addGeometry(Iterable<Geometry> children) throws ScenegraphException
	{
		for (Geometry child : children)
		{
			addChild(child);
		}
	}
	
	/**
	 * Removes a child node from this node. 
	 * 
	 * @throws ScenegraphException If the given node is not a child of this node. 
	 */
	public void removeChild(SceneObject child) throws ScenegraphException
	{
		if (child.mParent != this)
		{
			throw new ScenegraphException("Object to remove doesn't have parent of this node.");
		}
		
		if (!mChildren.remove(child))
		{
			throw new ScenegraphException("Object to remove is not a child of this ScenegraphObject.");
		}

		child.mParent = null;
	}
	
	/**
	 * Removes children from this node. 
	 * 
	 * @throws ScenegraphException If any new nodes are not a child of this node. 
	 */
	public void removeChildren(Iterable<SceneObject> children) throws ScenegraphException
	{
		for (SceneObject child : children)
		{
			removeChild(child);
		}
	}
	
	/**
	 * Returns the first child of this object with the given name, or null if no child has that name.
	 */
	public SceneObject findChildByName(String name)
	{
		for (SceneObject child : mChildren)
		{
			if (child.getName().equals(name))
			{
				return child;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the first descendant of this object with the given name, or null if no descendant has that name.
	 * 
	 * Search order is depth-first.
	 */
	public SceneObject findDescendantByName(String name)
	{
		for (SceneObject child : mChildren)
		{
			SceneObject descendant = child.findDescendantByName(name);
			
			if (descendant != null)
			{
				return descendant;
			}
			
			if (child.getName().equals(name))
			{
				return child;
			}
		}
		
		return null;
	}
	
	/**
	 * Traverses the hierarchy rooted at this node and calculates tangent vectors for all meshes, 
	 * storing the result in each mesh's attributes array under the name "VertexTangent". This is 
	 * useful for loading a bunch of geometry and making it normal-mapping-ready in one method
	 * call, but it can be expensive for large meshes.
	 */
	public void calculateTangentVectorsForAllGeometry()
	{
		for (SceneObject child : mChildren)
		{
			child.calculateTangentVectorsForAllGeometry();
		}
	}

	/**
	 * Returns the position of this object in its parent's space.
	 */
	public Point3f getPosition()
	{
		return mPosition;
	}
	
	/**
	 * Returns the position of this object in world space.
	 */
	public Point3f getWorldspacePosition()
	{
		return transformPointToWorldSpace(new Point3f(0.0f, 0.0f, 0.0f));
	}

	/**
	 * Sets the position of this object in its parent's space.
	 */
	public void setPosition(Point3f position)
	{
		mPosition = position;
	}
	
	/**
	 * Returns the orientation of this object in its parent's space.
	 */
	public Quat4f getOrientation()
	{
		return mOrientation;
	}
	
	/**
	 * Returns the rotation matrix of this object in its parent's space.
	 */
	public Matrix3f getRotationMatrix3f() 
	{
		return Util.getRotationMatrix3f(getOrientation());
	}
	
	/**
	 * Returns the rotation matrix of this object in world space.
	 */
	public Matrix3f getWorldSpaceRotationMatrix3f() 
	{
		return Util.getRotationMatrix3f(transformOrientationToWorldSpace(new Quat4f(0.0f, 0.0f, 0.0f, 1.0f)));
	}
	
	/**
	 * Returns the rotation matrix of this object in eye space
	 */
	public Matrix3f getEyeSpaceRotationMatrix3f()
	{
		return Util.getRotationMatrix3f(transformOrientationFromWorldSpace(new Quat4f(0.0f, 0f, 0f, 1.0f)));
	}
	
	/**
	 * Returns the transformation (rotation+translation) matrix of this object in world space.
	 * This matrix can be used to send points from local space to world space.
	 * The inverse can be used to do the opposite.
	 */
	public Matrix4f getWorldSpaceTransformationMatrix4f() 
	{
		Matrix4f mat = new Matrix4f();
		
		/* Set the rotational component of this matrix */
		mat.set(getEyeSpaceRotationMatrix3f());
		
		/* Get the world space position of this object */
		Point3f worldSpacePos = getWorldspacePosition();
		
		/* Set the translation component of this matrix */
		mat.m03 = (worldSpacePos.x);
		mat.m13 = (worldSpacePos.y);
		mat.m23 = (worldSpacePos.z);
		mat.m33 = (1);
		
		return mat;
	}

	/**
	 * Sets the orientation of this object in its parent's space.
	 */
	public void setOrientation(Quat4f orientation)
	{
		mOrientation = orientation;
	}
	
	/**
	 * Returns the scale of this object in its parent's space.
	 */
	public float getScale()
	{
		return mScale;
	}
	
	/**
	 * Sets the scale of this object in its parent's space.
	 */
	public void setScale(float scale)
	{
		mScale = scale;
	}
	
	/**
	 * Returns the name of this object. 
	 * 
	 * Objects can be identified by name for convenience (e.g. the `findChildByName()` 
	 * and `findDescendantByName()` methods). The `Geometry` class will name objects 
	 * as they are named in the loaded model file, allowing you to load an entire scene 
	 * and manipulate individual objects by name.
	 */
	public String getName()
	{
		return mName;
	}
	
	/**
	 * Sets the name of this object.
	 */
	public void setName(String name)
	{
		mName = name;
	}

	/**
	 * Transforms a point in this object's local space into its parent's space.
	 */
	public Point3f transformPointToParentSpace(Point3f point)
	{
		Point3f result = new Point3f(point);
		
		result.scale(mScale);
		Util.rotateTuple(mOrientation, result);
		result.add(mPosition);
		
		return result;
	}
	
	/**
	 * Transforms a point in this object's parent's space into its local space.
	 */
	public Point3f transformPointFromParentSpace(Point3f point)
	{
		Quat4f invOrientation = new Quat4f();
		invOrientation.inverse(mOrientation);

		Point3f result = new Point3f(point);
		
		result.sub(mPosition);
		Util.rotateTuple(invOrientation, result);
		result.scale(1.0f / mScale);
		
		return result;
	}

	/**
	 * Transforms a point in this object's local space into world space.
	 */
	public Point3f transformPointToWorldSpace(Point3f point)
	{
		if (mParent == null)
		{
			return transformPointToParentSpace(point);
		}
		else
		{
			return mParent.transformPointToWorldSpace(transformPointToParentSpace(point));
		}
	}
	
	/**
	 * Transforms a point in world space into this object's local space.
	 */
	public Point3f transformPointFromWorldSpace(Point3f point)
	{
		if (mParent == null)
		{
			return transformPointFromParentSpace(point);
		}
		else
		{
			return transformPointFromParentSpace(mParent.transformPointFromWorldSpace(point));
		}
	}
	
	/**
	 * Transforms a direction in this object's local space into its parent's space.
	 */
	public Vector3f transformVectorToParentSpace(Vector3f direction)
	{
		Vector3f result = new Vector3f(direction);
		
		result.scale(mScale);
		Util.rotateTuple(mOrientation, result);
		
		return result;
	}
	
	/**
	 * Transforms a direction in this object's parent's space into its local space.
	 */
	public Vector3f transformVectorFromParentSpace(Vector3f direction)
	{
		Quat4f invOrientation = new Quat4f();
		invOrientation.inverse(mOrientation);

		Vector3f result = new Vector3f(direction);
		
		Util.rotateTuple(invOrientation, result);
		result.scale(1.0f / mScale);
		
		return result;
	}
	
	/**
	 * Transforms a direction in this object's local space into world space. 
	 */
	public Vector3f transformVectorToWorldSpace(Vector3f direction)
	{
		if (mParent == null)
		{
			return transformVectorToParentSpace(direction);
		}
		else
		{
			return mParent.transformVectorToWorldSpace(transformVectorToParentSpace(direction));
		}
	}
	
	/**
	 * Transforms a direction in world space into this object's local space.
	 */
	public Vector3f transformVectorFromWorldSpace(Vector3f direction)
	{
		if (mParent == null)
		{
			return transformVectorFromParentSpace(direction);
		}
		else
		{
			return transformVectorFromParentSpace(mParent.transformVectorFromWorldSpace(direction));
		}
	}
	
	/**
	 * Transforms a distance in this object's local space into its parent's space.
	 */
	public float transformDistanceToParentSpace(float distance)
	{
		return distance * mScale;
	}
	
	/**
	 * Transforms a distance in this object's parent's space to its local space.
	 */
	public float transformDistanceFromParentSpace(float distance)
	{
		return distance / mScale;
	}

	/**
	 * Transforms a distance in this object's local space into world space.
	 */
	public float transformDistanceToWorldSpace(float distance)
	{
		if (mParent == null)
		{
			return transformDistanceToParentSpace(distance);
		}
		else
		{
			return mParent.transformDistanceToWorldSpace(transformDistanceToParentSpace(distance));
		}
	}
	
	/**
	 * Transforms a distance in world space into this object's local space.
	 */
	public float transformDistanceFromWorldSpace(float distance)
	{
		if (mParent == null)
		{
			return transformDistanceFromParentSpace(distance);
		}
		else
		{
			return transformDistanceFromParentSpace(mParent.transformDistanceFromWorldSpace(distance));
		}
	}
	
	/**
	 * Transforms an orientation in this object's local space into its parent's space.
	 */
	public Quat4f transformOrientationToParentSpace(Quat4f orientation)
	{
		Quat4f result = new Quat4f(mOrientation);
		result.mul(orientation);
		return result;
	}
	
	/**
	 * Transforms an orientation in this object's parent's space into its local space.
	 */
	public Quat4f transformOrientationFromParentSpace(Quat4f orientation)
	{
		Quat4f result = new Quat4f();
		result.inverse(mOrientation);
		result.mul(orientation);
		return result;
	}

	/**
	 * Transforms an orientation in this object's local space into world space.
	 */
	public Quat4f transformOrientationToWorldSpace(Quat4f orientation)
	{
		if (mParent == null)
		{
			return transformOrientationToParentSpace(orientation);
		}
		else
		{
			return mParent.transformOrientationToWorldSpace(transformOrientationToParentSpace(orientation));
		}
	}
	
	/**
	 * Transforms an orientation in world space into this object's local space.
	 */
	public Quat4f transformOrientationFromWorldSpace(Quat4f orientation)
	{
		if (mParent == null)
		{
			return transformOrientationFromParentSpace(orientation);
		}
		else
		{
			return transformOrientationFromParentSpace(mParent.transformOrientationFromWorldSpace(orientation));
		}
	}

	/**
	 * Releases any OpenGL resources owned by descendant objects. 
	 */
	public void releaseGPUResources(GL2 gl)
	{
		for (SceneObject child : mChildren)
		{
			child.releaseGPUResources(gl);
		}
	}

}

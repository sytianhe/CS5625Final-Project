package cs5625.deferred.datastruct;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;


/**
 * VertexAttributeData.java
 * 
 * Holds the vertex attributes 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class VertexAttributeData {
	private Point3f mPosition;
	private Point2f mTexCoord;
	
	public VertexAttributeData(Point3f pos, Point2f tex){
		mPosition = pos;
		mTexCoord = tex;
	}
		
	public Point3f getPosition()
	{
		return this.mPosition;
	}
	
	public Point2f getTexCoord()
	{
		return this.mTexCoord;
	}
		
}

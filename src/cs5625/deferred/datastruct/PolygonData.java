package cs5625.deferred.datastruct;

import java.util.ArrayList;

/**
 * PolygonData.java
 * 
 * Holds the per polygon data as part of our edgeDS 
 * 
 * It has the list of vertices that define this polygon, and a list of edges that define it's boundary.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class PolygonData {


	private ArrayList<Integer> allVertices;
	private ArrayList<Integer> allEdges;
	
	private int newFaceVertexID;
	
	public PolygonData(int e0_, int e1_, int e2_, int v0_, int v1_, int v2_){
		
		this.allEdges = new ArrayList<Integer>();
		this.allEdges.add(e0_);
		this.allEdges.add(e1_);
		this.allEdges.add(e2_);
		
		this.allVertices = new ArrayList<Integer>();
		this.allVertices.add(v0_);
		this.allVertices.add(v1_);
		this.allVertices.add(v2_);
	}
	
	public PolygonData(int e0_, int e1_, int e2_, int e3_, int v0_, int v1_, int v2_, int v3_){
		
		this.allEdges = new ArrayList<Integer>();
		this.allEdges.add(e0_);
		this.allEdges.add(e1_);
		this.allEdges.add(e2_);
		this.allEdges.add(e3_);
		
		this.allVertices = new ArrayList<Integer>();
		this.allVertices.add(v0_);
		this.allVertices.add(v1_);
		this.allVertices.add(v2_);
		this.allVertices.add(v3_);
	}
	

	
	public ArrayList<Integer> getAllVertices()
	{
		return this.allVertices;		
	}
	
	public ArrayList<Integer> getAllEdges()
	{
		return this.allEdges;		
	}

	public int getNewFaceVertexID() {
		return newFaceVertexID;
	}

	public void setNewFaceVertexID(int newFaceVertexID) {
		this.newFaceVertexID = newFaceVertexID;
	}
	
}

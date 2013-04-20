package cs5625.deferred.datastruct;

import java.util.ArrayList;


/**
 * EdgeData.java
 * 
 * This class has the per edge data. That is, the two vertices that it connects,
 *  a placeholder for the edge vertexID that will be created during subdivison
 *   and a list of polygons that share this edge. Note that the
 *   getOtherEdgesOfLeftFace() and getOtherEdgesOfRightFace() functions of EdgeDS
 *   class return a list of edges starting from i0.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class EdgeData {
	//the vertexIDs for the two vertices that are connected
	private int i0, i1;
	//vertexID for the vertex that will be created during subdivision
	private int vertexIDNew;
	//list of all polygons that include this edge
	private ArrayList<Integer> polygonIDs;
	
	
	public EdgeData(int i0_, int i1_)
		{
		this.i0 = i0_;
		this.i1 = i1_;
		this.polygonIDs = new ArrayList<Integer>();
//		this.edgeIDs = new TreeSet<Integer>();
		}
	
	public void addPolygon(int polygonID)
	{
		if(this.polygonIDs.contains(polygonID) == false)
		{
			this.polygonIDs.add(polygonID);
		}
	}
	
	public ArrayList<Integer> getPolys()
	{
		return this.polygonIDs;
	}
	
//	public void addEdge(int edgeID)
//	{
//		this.edgeIDs.add(edgeID);
//	}
	
	public int getVertex0(){
		return this.i0;
	}
	
	public int getVertexIDNew() {
		return vertexIDNew;
	}

	public void setVertexIDNew(int vertexIDNew) {
		this.vertexIDNew = vertexIDNew;
	}

	public int getVertex1(){
		return this.i1;
	}
	
	public int getNewVertexID(){
		return this.vertexIDNew;
	}
}

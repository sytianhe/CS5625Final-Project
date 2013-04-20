package cs5625.deferred.datastruct;


/**
 * EdgeVertexPair.java
 * 
 * Implements a edge vertex tuple
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class EdgeVertexPair {
	public int edgeID, vertexID;
	
	public EdgeVertexPair(int edge, int vertex){
		this.edgeID = edge;
		this.vertexID = vertex;
	}
	
}

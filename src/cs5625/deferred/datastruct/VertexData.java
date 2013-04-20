package cs5625.deferred.datastruct;


import java.util.ArrayList;

/**
 * VertexData.java
 * 
 * This class has the per vertex data as part of our edgeDS. That is, the vertex
 * attributes (like position), a list of (vertex,edge) pairs that represent this
 * vertex's connectivity and a placeholder for reference to the new even vertex
 * that will be created during subdivision
 * 
 *  NOTE:  If a vertex v0 is connected to vertex v1 via edge e0, then (e0,v1) pair
 *  will be stored in v0 and (e0,v0) pair will be stored in v1.  
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class VertexData {
	//the actual attributes
	public VertexAttributeData mData;
	//the new attributes
	private int mNewVertexID;
	//list of all the (edgeID,vertexID) tuples that are connected to this vertex
	private ArrayList<EdgeVertexPair> currentEdgeVertexPairs;
	

	/**
	 * Add pair_ to this vertex's connectivity list
	 * But don't add twice
	 * @param pair_ The thing to be added
	 */
	public void addVertexConnectivity(EdgeVertexPair pair_)
		{
		
		for(EdgeVertexPair pair: currentEdgeVertexPairs)
			{
			//already connected
			if(pair.vertexID == pair_.vertexID)
				return;
			}
		
		currentEdgeVertexPairs.add(pair_);

		}
	
	/**
	 * Find out in case there is an edge to otherVertexID
	 * Return Maybe since an edge might not exist
	 * @param otherVertexID
	 * @return
	 */
	public  Maybe<Integer> getEdgeIDforEdgeWithThisVertex(int otherVertexID) 
		{
		if(this.currentEdgeVertexPairs == null)
			{
			return (new Maybe<Integer> ());
			}
		
		for (EdgeVertexPair edgeVertexPair : currentEdgeVertexPairs) 
			{
			if(edgeVertexPair.vertexID == otherVertexID)
				{
				Integer temp = new Integer (edgeVertexPair.edgeID);
				return new Maybe<Integer> (temp);
				}
			}
		return (new Maybe<Integer> ());
		}

	public VertexData(VertexAttributeData attributes)
		{
		this.mData = attributes;
		this.currentEdgeVertexPairs = null;
		
		this.currentEdgeVertexPairs = new ArrayList<EdgeVertexPair>();
	
		}
	
	
	
	/**
	 * Should be self explanatory
	 * true if edge exists
	 * @param otherVertexID
	 * @return
	 */
	public boolean checkIfConnected(int otherVertexID)
		{
		if(this.currentEdgeVertexPairs == null)
			{
			return false;
			}
		else
			{
			for (EdgeVertexPair edgeVertexPair : currentEdgeVertexPairs) 
				{
				if(edgeVertexPair.vertexID == otherVertexID)
					{
					return true;
					}
				}
			return false;
			}
		}
	
	/**
	 * Return just the VertexIDs of the the vertices that are connected to this
	 * Does not account for the new edge vertices
	 * will return those created before subdivision
	 * @return
	 */
	public ArrayList<Integer> getConnectedVertices()
		{
		ArrayList<Integer> connectedVertices = new ArrayList<Integer>();
		
		if(this.currentEdgeVertexPairs != null)
			{
			for (EdgeVertexPair edgeVertexPair : currentEdgeVertexPairs) 
				{
				connectedVertices.add(edgeVertexPair.vertexID);
				}			 
			}
		return connectedVertices;
		}

	/**
	 * Return just the edgeIDs of the edges that are connected to this
	 * 
	 * Does not account for the new edge vertices
	 * will return those created before subdivision
	 * @return
	 */
	public ArrayList<Integer> getConnectedEdges()
		{
		ArrayList<Integer> connectedVertices = new ArrayList<Integer>();
		
		if(this.currentEdgeVertexPairs != null)
			{
			for (EdgeVertexPair edgeVertexPair : currentEdgeVertexPairs) 
				{
				connectedVertices.add(edgeVertexPair.edgeID);
				}			 
			}
		return connectedVertices;
		}
	
	public int getValence()
	{
		if(this.currentEdgeVertexPairs == null)
			return 0;
		else
			return this.currentEdgeVertexPairs.size();
	}

	public int getNewVertexID() {
		return mNewVertexID;
	}

	public void setNewVertexID(int mNewVertexID) {
		this.mNewVertexID = mNewVertexID;
	}
	
}

package cs5625.deferred.datastruct;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Quadmesh;
import cs5625.deferred.scenegraph.Trimesh;


/**
 * EdgeDS.java
 * 
 * The EdgeDS  holds the data structure needed for subdivision. The EdgeDS class
 * 	has functionality to access edges, vertices, and faces. The EdgeDS can be
 * created by supplying a Trimesh or a Quadmesh object. Each vertex, face, or edge
 * is assigned a unique integer ID.
 * 
 *  All the per-edge data, per-face data, per-vertex data are stored internally as
 *  TreeMaps. That is, a key value pair where the keys are
 *  vertexIDs/edgeIDs/polygonIDs and values are VertexData/EdgeData/PolygonData.
 *  Look at the individual EdgeData/PolygonData/VertexData classes below for more
 *  information on what data is stored per edge/vertex/polygon.
 *  
 *  You will need the edgeID/polygonID/vertexID to lookup the edge/polygon/vertex
 *  data respectively. TO itereate over all edges/vertices/polygons, you will need all the vertexIDs, all
 *  the edgeIDs and all the polygonIDs. For this, use getVertexIDs(), getEdgeIDs() and
 *  getPolygonIDs() respectively. Helper functions are provided to perform
 *   lookups using these IDs. Look at getEdgeData(), getVertexData() and getPolygonData().
 *   
 *   
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class EdgeDS {
	//map from vertexID to (it's attributes, it's connectivity)
	private TreeMap<Integer,VertexData> vertexMap;
	//map from edgeID to pair of vertices
	private TreeMap<Integer,EdgeData> edgeMap;
	//map from triangle ID to triangle data
	private TreeMap<Integer,PolygonData> polygonMap;
	//map from vertexID to it's attributes, for new vertices only
	private TreeMap<Integer,VertexAttributeData> newVertexMap;
	//map from edgeID to crease info 
	private TreeSet<Integer> creaseSet;
	
	private Mesh mMesh;
	
	private int currentEdgeID;
	private int currentPolygonID;
	
	
	private int getNewEdgeID()
		{
		this.currentEdgeID = this.currentEdgeID +1;
		return this.currentEdgeID; 
		}
	
	private int getNewPolygonID()
		{
		this.currentPolygonID = this.currentPolygonID +1;
		return this.currentPolygonID; 
		}
		
	/**
	 * Get the edges from, to
	 * Look at type signature for better documentation
	 * @param vertexIDfrom
	 * @param vertexIDto
	 * @return
	 */
	private Maybe<Integer> getEdgeFromTo(int vertexIDfrom, int vertexIDto)
		{
		//look into the from vertex's connectivity list if the to vertex exists
		VertexData data = this.vertexMap.get(vertexIDfrom);
		Maybe<Integer> sharedEdgeID;
		
		if(data == null)
			{
			sharedEdgeID = new Maybe<Integer>();	
			}
		else
			{
			sharedEdgeID = data.getEdgeIDforEdgeWithThisVertex(vertexIDto);
			}
		return sharedEdgeID;
		}
	
	/**
	 * Find an existing edge between the two,
	 * failing that, create a new edge ID 
	 * @param vertexID1
	 * @param vertexID2
	 * @return
	 */
	private int getEdgeBetweenVertices(int vertexID1, int vertexID2)
		{
		//we need to look into vertexData for both vertices
		Maybe<Integer> sharedEdgeID1 = this.getEdgeFromTo(vertexID1, vertexID2);
		
		if(sharedEdgeID1.hasData() == true)
			{
			return sharedEdgeID1.getData();
			}
		
		Maybe<Integer> sharedEdgeID2 = this.getEdgeFromTo(vertexID2, vertexID1);
		if(sharedEdgeID2.hasData() == true)
			{
			return sharedEdgeID2.getData();
			}
		//only if neither of the two vertices have it, then we create a new edge
		//both of these are Nothing, so create new edge
		return getNewEdgeID();
		}
	
	private EdgePair addDataToVertexMap3(int thisVertexID, int otherVertexID1, int otherVertexID2, VertexAttributeData v0 )
		{
		VertexData temp;
		int e0ID, e1ID;
		
		temp = this.vertexMap.get(thisVertexID);
		if(temp == null)
			{
			//new vertex
			temp = new VertexData(v0);
			}
			
		e0ID = this.getEdgeBetweenVertices(thisVertexID, otherVertexID1);
		e1ID = this.getEdgeBetweenVertices(thisVertexID, otherVertexID2);
		
		temp.addVertexConnectivity(new EdgeVertexPair(e0ID,otherVertexID1));
		temp.addVertexConnectivity(new EdgeVertexPair(e1ID,otherVertexID2));
			
		this.vertexMap.put(thisVertexID, temp);
		
		return new EdgePair(e0ID, e1ID);
		}
	
	private void addTriangle(int i0, int i1, int i2, VertexAttributeData v0, VertexAttributeData v1, VertexAttributeData v2)
		{	
		EdgeData e0 = new EdgeData(i0, i1);
		EdgeData e1 = new EdgeData(i1, i2);
		EdgeData e2 = new EdgeData(i2, i0);
		
		EdgePair pair0 = this.addDataToVertexMap3(i0, i1, i2, v0);
		EdgePair pair1 = this.addDataToVertexMap3(i1, i2, i0, v1);
		EdgePair pair2 = this.addDataToVertexMap3(i2, i0, i1, v2);

		//the edgeIDs for the three triangles involved in this
		int e0ID = pair0.getE0ID();
		int e1ID = pair1.getE0ID();
		int e2ID = pair2.getE0ID();
		
		//now add to the edgeMap
		this.edgeMap.put(e0ID, e0);
		this.edgeMap.put(e1ID, e1);
		this.edgeMap.put(e2ID, e2);
		
		//now add data to triangleMap
		
		int triID = getNewPolygonID();
		this.polygonMap.put(triID, new PolygonData(e0ID, e1ID, e2ID, i0, i1, i2));
		}

	private void addQuad(int i0, int i1, int i2, int i3, VertexAttributeData v0, VertexAttributeData v1, VertexAttributeData v2, VertexAttributeData v3)
		{	
		EdgeData e0 = new EdgeData(i0, i1);
		EdgeData e1 = new EdgeData(i1, i2);
		EdgeData e2 = new EdgeData(i2, i3);
		EdgeData e3 = new EdgeData(i3, i0);
		
		EdgePair pair0 = this.addDataToVertexMap3(i0, i1, i3, v0);
		EdgePair pair1 = this.addDataToVertexMap3(i1, i2, i0, v1);
		EdgePair pair2 = this.addDataToVertexMap3(i2, i3, i1, v2);
		EdgePair pair3 = this.addDataToVertexMap3(i3, i0, i2, v3);
	
		//the edgeIDs for the three triangles involved in this
		int e0ID = pair0.getE0ID();
		int e1ID = pair1.getE0ID();
		int e2ID = pair2.getE0ID();
		int e3ID = pair3.getE0ID();
		
		//now add to the edgeMap
		this.edgeMap.put(e0ID, e0);
		this.edgeMap.put(e1ID, e1);
		this.edgeMap.put(e2ID, e2);
		this.edgeMap.put(e3ID, e3);
		
		//now add data to triangleMap		
		int triID = getNewPolygonID();
		this.polygonMap.put(triID, new PolygonData(e0ID, e1ID, e2ID, e3ID, i0, i1, i2, i3));
		}

	
	/**
	 * Pass in a trimesh, have this data structure create for you
	 * @param trimesh
	 */
	public EdgeDS(Trimesh trimesh){
		this.mMesh = (Mesh)trimesh;
		
		//add all the triangles to this data structure
		FloatBuffer positionData = trimesh.getVertexData();
		FloatBuffer normalData = trimesh.getNormalData();
		FloatBuffer textureData = trimesh.getTexCoordData();
		IntBuffer indexData = trimesh.getPolygonData();
		IntBuffer creaseData = trimesh.getEdgeData();
		
		int numTriangles = trimesh.getPolygonCount();
		
		int i;
		
		int index0, index1, index2;
		float x,y,z,nx,ny,nz,tx,ty;
		
		this.vertexMap = new TreeMap<Integer, VertexData>();	
		this.edgeMap = new TreeMap<Integer, EdgeData>();
		this.polygonMap = new TreeMap<Integer, PolygonData>();
		
		this.currentEdgeID = 0;
		this.currentPolygonID = 0;
				
		for(i=0; i<numTriangles; i++)
			{
			index0 = indexData.get(3 * i + 0);
			index1 = indexData.get(3 * i + 1);
			index2 = indexData.get(3 * i + 2);
			
			x = positionData.get(3 * index0 + 0);
			y = positionData.get(3 * index0 + 1);
			z = positionData.get(3 * index0 + 2);
			nx = normalData.get(3 * index0 + 0);
			ny = normalData.get(3 * index0 + 1);
			nz = normalData.get(3 * index0 + 2);
			tx = textureData.get(2 * index0);
			ty = textureData.get(2 * index0 + 1);
			
			VertexAttributeData v0 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty), new Vector3f(nx,ny,nz));
			
			x = positionData.get(3 * index1 + 0);
			y = positionData.get(3 * index1 + 1);
			z = positionData.get(3 * index1 + 2);
			nx = normalData.get(3 * index1 + 0);
			ny = normalData.get(3 * index1 + 1);
			nz = normalData.get(3 * index1 + 2);
			tx = textureData.get(2 * index1);
			ty = textureData.get(2 * index1 + 1);
			
			VertexAttributeData v1 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty), new Vector3f(nx,ny,nz));
			
			x = positionData.get(3 * index2 + 0);
			y = positionData.get(3 * index2 + 1);
			z = positionData.get(3 * index2 + 2);
			nx = normalData.get(3 * index1 + 0);
			ny = normalData.get(3 * index1 + 1);
			nz = normalData.get(3 * index1 + 2);
			tx = textureData.get(2 * index2);
			ty = textureData.get(2 * index2 + 1);
			
			VertexAttributeData v2 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty), new Vector3f(nx,ny,nz));
			
			this.addTriangle(index0, index1, index2, v0, v1, v2);		
			}
		
		this.newVertexMap = new TreeMap<Integer,VertexAttributeData>();
		
		this.creaseSet = new TreeSet<Integer>();
		//build up creaseMap
		//only store those which are actually creases
		
		int numEdges = 0;
		if (creaseData != null){
			numEdges = creaseData.capacity()/2;
		}
		for(i=0; i<numEdges; i++)
		{
			//get the two vertexIDs
			int v0 = creaseData.get(2 * i + 0);
			int v1 = creaseData.get(2 * i + 1);
			//get it's corresponding edgeID
			int edgeID = this.getEdgeBetweenVertices(v0, v1);
			this.creaseSet.add(edgeID);
		}
		
		this.addPolygonDataToEdgeMap();

		
	}

	/**
	 * Pass in a quadmesh, have this data structure create for you
	 * @param quadmesh
	 */
	public EdgeDS(Quadmesh quadmesh){
		this.mMesh = (Mesh)quadmesh;
		
		//add all the triangles to this data structure
		FloatBuffer positionData = quadmesh.getVertexData(); 
		FloatBuffer normalData = quadmesh.getNormalData(); 
		FloatBuffer textureData = quadmesh.getTexCoordData();
		IntBuffer indexData = quadmesh.getPolygonData();
		IntBuffer creaseData = quadmesh.getEdgeData();
		
		int numQuads = quadmesh.getPolygonCount();
		
		int i;
		
		int index0, index1, index2, index3;
		float x,y,z,nx,ny,nz,tx,ty;
		
		this.vertexMap = new TreeMap<Integer, VertexData>();	
		this.edgeMap = new TreeMap<Integer, EdgeData>();
		this.polygonMap = new TreeMap<Integer, PolygonData>();
		
		this.currentEdgeID = 0;
		this.currentPolygonID = 0;
				
		for(i=0; i<numQuads; i++)
			{
			index0 = indexData.get(4 * i + 0);
			index1 = indexData.get(4 * i + 1);
			index2 = indexData.get(4 * i + 2);
			index3 = indexData.get(4 * i + 3);
			
			x = positionData.get(3 * index0 + 0);
			y = positionData.get(3 * index0 + 1);
			z = positionData.get(3 * index0 + 2);			
			nx = normalData.get(3 * index0 + 0);
			ny = normalData.get(3 * index0 + 1);
			nz = positionData.get(3 * index0 + 2);	
			tx = textureData.get(2 * index0);
			ty = textureData.get(2 * index0 + 1);
			
			VertexAttributeData v0 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty),  new Vector3f(nx,ny,nz));
			
			x = positionData.get(3 * index1 + 0);
			y = positionData.get(3 * index1 + 1);
			z = positionData.get(3 * index1 + 2);
			nx = normalData.get(3 * index1 + 0);
			ny = normalData.get(3 * index1 + 1);
			nz = positionData.get(3 * index1 + 2);	
			tx = textureData.get(2 * index1);
			ty = textureData.get(2 * index1 + 1);
			
			VertexAttributeData v1 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty),  new Vector3f(nx,ny,nz));
			
			x = positionData.get(3 * index2 + 0);
			y = positionData.get(3 * index2 + 1);
			z = positionData.get(3 * index2 + 2);
			tx = textureData.get(2 * index2);
			ty = textureData.get(2 * index2 + 1);
			
			VertexAttributeData v2 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty),  new Vector3f(nx,ny,nz));
			
			x = positionData.get(3 * index3 + 0);
			y = positionData.get(3 * index3 + 1);
			z = positionData.get(3 * index3 + 2);
			tx = textureData.get(2 * index3);
			ty = textureData.get(2 * index3 + 1);
			
			VertexAttributeData v3 = new VertexAttributeData(new Point3f(x,y,z), new Point2f(tx,ty),  new Vector3f(nx,ny,nz));;
			
			this.addQuad(index0, index1, index2, index3, v0, v1, v2, v3);		
			}
		
		this.newVertexMap = new TreeMap<Integer,VertexAttributeData>();
		
		this.creaseSet = new TreeSet<Integer>();
		//build up creaseMap
		//only store those which are actually creases
		int numEdges = 0;
		if (creaseData != null){
			numEdges = creaseData.capacity()/2;
		}
		for(i=0; i<numEdges; i++)
		{
			//get the two vertexIDs
			int v0 = creaseData.get(2 * i + 0);
			int v1 = creaseData.get(2 * i + 1);
			//get it's corresponding edgeID
			int edgeID = this.getEdgeBetweenVertices(v0, v1);
			this.creaseSet.add(edgeID);
		}
		
		this.addPolygonDataToEdgeMap();

		
	}

	private void addPolygonDataToEdgeMap()
	{
		//iterate over all triangles
		for (Map.Entry<Integer,PolygonData> e : this.polygonMap.entrySet()) 
		{
			int polygonID = e.getKey();
			PolygonData triData = e.getValue();
		
			//each edge should know about this polygon
			EdgeData edgeData;
		
			for(int edge : triData.getAllEdges())
			{
				//add them to edgeMap
				edgeData = this.edgeMap.get(edge);
				edgeData.addPolygon(polygonID);
				this.edgeMap.put(edge, edgeData);
			}
			
		}
	}
	
	/**
	 * return true if creaseEdge 
	 * @param edgeID
	 * @return
	 */
	public boolean isCreaseEdge(int edgeID)
	{
		if(this.creaseSet.contains(edgeID) == true)
			return true;
		else
			return false;
	}
	
	/**
	 * return crease edges for vertedID (if vertex is on a crease)
	 * @param vertexID
	 */
	public ArrayList<Integer> getCreaseEdges(int vertexID)
	{
		ArrayList<Integer> creaseEdges = new ArrayList<Integer>();

		VertexData vertex = getVertexData(vertexID);
		for(int edgeID : vertex.getConnectedEdges()){
			if( isCreaseEdge(edgeID ) ){
				creaseEdges.add(edgeID);
			}
		}
		return creaseEdges;
	}
	
	/**
	 * return true if edge is on a boundary
	 * @param edgeID
	 */
	public boolean isBoundaryEdge(int edgeID)
	{
		ArrayList<Integer> leftEdges = getOtherEdgesOfLeftFace(edgeID);
		ArrayList<Integer> rightEdges = getOtherEdgesOfRightFace(edgeID);
		
		if(leftEdges.size()==0 || rightEdges.size()==0 )
			return true;
		else
			return false;
	}
	
	/**
	 * return true if vertex is on a boundary
	 * @param vertexID
	 */
	public boolean isBoundaryVertex(int vertexID)
	{
		VertexData vertex = getVertexData(vertexID);
		for(int edgeID : vertex.getConnectedEdges()){
			if( isBoundaryEdge(edgeID ) ) return true;
		}
		return false;
	}
	
	/**
	 * return boundery edges for vertedID (if vertex is on a boundary
	 * @param vertexID
	 */
	public ArrayList<Integer> getBoundaryEdges(int vertexID)
	{
		ArrayList<Integer> boundaryEdges = new ArrayList<Integer>();

		VertexData vertex = getVertexData(vertexID);
		for(int edgeID : vertex.getConnectedEdges()){
			if( isBoundaryEdge(edgeID ) ){
				boundaryEdges.add(edgeID);
			}
		}
		return boundaryEdges;
	}
	
	
	private ArrayList<Integer> reorderEdges(ArrayList<Integer> allEdges, int mainEdge)
	{
		ArrayList<Integer> beforeList = new ArrayList<Integer>();
		ArrayList<Integer> afterList = new ArrayList<Integer>();
		
		boolean mainEdgeCrossed = false;
		for(int otherEdge: allEdges)
		{
			if(mainEdgeCrossed == true)
			{
				afterList.add(otherEdge);
			}
			if(otherEdge == mainEdge)
			{				
				mainEdgeCrossed = true;
			}
			if(mainEdgeCrossed == false)
			{
				beforeList.add(otherEdge);
			}
		}

		afterList.addAll(beforeList);
		return afterList;
	}
	
	private ArrayList<Integer> fixWindingOrder( ArrayList<Integer> otherEdges, int mainEdge)
	{
		//get first edge
		int headEdge = otherEdges.get(0);
		//see if the headEdge meets with main edge
		EdgeData mainEdgeData = this.edgeMap.get(mainEdge);
		EdgeData headEdgeData = this.edgeMap.get(headEdge);
		
		int mainEdgeV0 = mainEdgeData.getVertex0();
		
		int headEdgeV0 = headEdgeData.getVertex0();
		int headEdgeV1 = headEdgeData.getVertex1();
		
		
		if(mainEdgeV0 == headEdgeV0 || mainEdgeV0 == headEdgeV1)
			return otherEdges;
		else
		{
			Collections.reverse(otherEdges);
			return otherEdges;
		}		
		
	}
	
	private ArrayList<Integer> getOtherEdges(int polyID, int edgeID)
	{
		
		ArrayList<Integer> allEdges = this.polygonMap.get(polyID).getAllEdges();
		
		//reorder edges
		ArrayList<Integer> reorderedEdges = this.reorderEdges(allEdges, edgeID);
		return this.fixWindingOrder(reorderedEdges, edgeID);
	}

	/**
	 * 
	 *  Given an edgeID, it will return all the edges
	 *  that are part of the left polygon on that edge in clockwise order. A for loop on these
	 *  edges will traverse all the edges of the left polygon.
	 *  
	 *  	 For simplicity, the edgeID that is passed in as an argument will not
	 *  be included in the returned list.  The first edge in the list is guaranteed to
	 *  meet the vertex with id i0 of the input edgeID.
	 *  	 
	 * @param edgeID
	 * @return
	 */
	public ArrayList<Integer> getOtherEdgesOfLeftFace(int edgeID)
	{
		//get the left polygon for this edge
		int leftPolyID = this.edgeMap.get(edgeID).getPolys().get(0);
		
		return this.getOtherEdges(leftPolyID, edgeID);
	}
	

	/**
	 * 
	 * Given an edgeID, it will return all the edges
	 * that are part of the right polygon on that edge.  You can walk this list one by
	 * one in a simple for loop and traverse all the edges of the right polygon.
	 *  For simplicity, the edgeID that is passed in as an argument will not be included in
	 *  the returned list.  The first edge in the list is guaranteed to meet the i0
	 *  vertex of the input edgeID. NOTE: since boundary edges have only one polygon
	 *  attached to them, calling this function on a boundary edge will return an empty
	 *  list. 
	 * @param edgeID
	 * @return
	 */
	
	public ArrayList<Integer> getOtherEdgesOfRightFace(int edgeID)
	{
		//get the right polygon for this edge
		ArrayList<Integer> polys = this.edgeMap.get(edgeID).getPolys(); 
		if(polys.size() >=2 )
		{
			int rightPolyID = polys.get(1);
			return this.getOtherEdges(rightPolyID, edgeID);
		}
		else
		{
			return new ArrayList<Integer>();
		}
	}
	
	
	

	/*
	 * the following three functions return the edge/polygon/vertex dat for individual edge/polygon/vertexIDs. 
	 * 	 
	 */
	
	public EdgeData getEdgeData(int edgeID)
		{
		return this.edgeMap.get(edgeID);
		}
	
	public VertexData getVertexData(int vertexID)
		{
		return this.vertexMap.get(vertexID);
		}
	
	public PolygonData getPolygonData(int triangleID)
	{
		return this.polygonMap.get(triangleID);
	}
	
	/*
	 * the following three functions return al the edge/polygon/vertexIDs for the coarse mesh.
	 * 
	 * 	 
	 */
	
	public Set<Integer> getVertexIDs()
	{
		return this.vertexMap.keySet();
	}

	public Set<Integer> getEdgeIDs()
	{
		return this.edgeMap.keySet();
	}
	
	public Set<Integer> getPolygonIDs()
	{
		return this.polygonMap.keySet();
	}

	public Mesh getMesh() {
		return mMesh;
	}

	public Set<Integer> getCreaseEdgeIDs(){
		return creaseSet;
	}
	
}



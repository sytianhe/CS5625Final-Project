package cs5625.deferred.loop;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.datastruct.EdgeData;
import cs5625.deferred.datastruct.PolygonData;
import cs5625.deferred.datastruct.VertexAttributeData;
import cs5625.deferred.scenegraph.Mesh;

/**
 * LoopSubdiv.java
 * 
 * Perform the subdivision in this class/package 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class LoopSubdiv {
	
	private Mesh mMesh;
	
	public LoopSubdiv(EdgeDS edgeDS)
	{
		// TODO PA5: Fill in this function to perform loop subdivision
		
		//COMPUTE SIZES OF NEW MESH GEOMETRY
		int nVertices = edgeDS.getVertexIDs().size() + edgeDS.getEdgeIDs().size();
		int nCreaseEdges = 2*edgeDS.getCreaseEdgeIDs().size();
		int nFaces = 4*edgeDS.getPolygonIDs().size();
		
		//INITIALIZE NEW BUFFERS FOR NEW MESH
		FloatBuffer vertexBuf = FloatBuffer.allocate(nVertices*3);
		FloatBuffer texCoordBuf = FloatBuffer.allocate(nVertices*2);
		IntBuffer edgeBuf = IntBuffer.allocate(nCreaseEdges*2);
		IntBuffer faceBuf = IntBuffer.allocate(nFaces*3);

		int oddVertexCounter = 0;
		for (int edgeID:edgeDS.getEdgeIDs()){

			EdgeData edge = edgeDS.getEdgeData(edgeID);
			int v1 = edge.getVertex0();
			int v2 = edge.getVertex1();
			
			Point3f targetPosition = new Point3f();
			Point2f targetTexture = new Point2f();
			
			// edge cases
			if (!edgeDS.isCreaseEdge(edgeID) && !edgeDS.isBoundaryEdge(edgeID)){
				int v3;
				EdgeData oneofleftedges = edgeDS.getEdgeData(edgeDS.getOtherEdgesOfLeftFace(edgeID).get(0));
				if (oneofleftedges.getVertex0()==v1){
					v3 = oneofleftedges.getVertex1();
				}
				else{
					v3 = oneofleftedges.getVertex0();
				}
				
				int v4;
				EdgeData oneofrightedges = edgeDS.getEdgeData(edgeDS.getOtherEdgesOfRightFace(edgeID).get(0));
				if (oneofrightedges.getVertex0()==v1){
					v4 = oneofrightedges.getVertex1();
				}
				else{
					v4 = oneofrightedges.getVertex0();
				}
				
				VertexAttributeData vDatav1 = edgeDS.getVertexData(v1).mData;
				VertexAttributeData vDatav2 = edgeDS.getVertexData(v2).mData;
				VertexAttributeData vDatav3 = edgeDS.getVertexData(v3).mData;
				VertexAttributeData vDatav4 = edgeDS.getVertexData(v4).mData;
				
				targetPosition.set(vDatav1.getPosition());
				targetPosition.add(vDatav2.getPosition());
				targetPosition.scale(0.375f);
				targetPosition.scaleAdd(0.125f, vDatav3.getPosition(), targetPosition);
				targetPosition.scaleAdd(0.125f, vDatav4.getPosition(), targetPosition);
				
				targetTexture.set(vDatav1.getTexCoord());
				targetTexture.add(vDatav2.getTexCoord());
				targetTexture.scale(0.375f);
				targetTexture.scaleAdd(0.125f, vDatav3.getTexCoord(), targetTexture);
				targetTexture.scaleAdd(0.125f, vDatav4.getTexCoord(), targetTexture);
			}
			else{
				VertexAttributeData vDatav1 = edgeDS.getVertexData(v1).mData;
				VertexAttributeData vDatav2 = edgeDS.getVertexData(v2).mData;
				
				targetPosition.set(vDatav1.getPosition());
				targetPosition.add(vDatav2.getPosition());
				targetPosition.scale(0.5f);
				
				targetTexture.set(vDatav1.getTexCoord());
				targetTexture.add(vDatav2.getTexCoord());
				targetTexture.scale(0.5f);
				
			}
			//SAVE NEW POINT TO BUFFER
			vertexBuf.put(targetPosition.x);
			vertexBuf.put(targetPosition.y);
			vertexBuf.put(targetPosition.z);
			
			texCoordBuf.put(targetTexture.x);
			texCoordBuf.put(targetTexture.y);				
			
			edge.setVertexIDNew(oddVertexCounter);
			oddVertexCounter +=1;
		}
		
		for(int vertexID:edgeDS.getVertexIDs()){
			float beta;
			
			ArrayList<Integer>creaseEdges = edgeDS.getCreaseEdges(vertexID);
			creaseEdges.addAll(edgeDS.getBoundaryEdges(vertexID));

			Point3f targetPosition = new Point3f(edgeDS.getVertexData(vertexID).mData.getPosition());
			Point2f targetTexture = new Point2f(edgeDS.getVertexData(vertexID).mData.getTexCoord());
			
			// Not on an edge
			if (creaseEdges.size() <= 1 ){
				int numberConnectedEdges = edgeDS.getVertexData(vertexID).getConnectedEdges().size();

				if (numberConnectedEdges > 3){
					beta = 3f/(numberConnectedEdges*8f);
					//beta = 3f/(numberConnectedEdges*(numberConnectedEdges+2f));
					//beta = (5f/8f - (float) Math.pow(3 + 2 * Math.cos(2*Math.PI /numberConnectedEdges ), 2)/64f )/numberConnectedEdges;
				}
				else{//numberConnectedEdges = 3
					beta = 0.1875f;
				}
				// 1-n*beta
				targetPosition.scale(1f - numberConnectedEdges*beta);
				targetTexture.scale(1f - numberConnectedEdges*beta);
				// beta
				for (int edgeID :edgeDS.getVertexData(vertexID).getConnectedEdges()){
					int newVertexId = edgeDS.getEdgeData(edgeID).getVertex0();
					if (newVertexId == vertexID) newVertexId = edgeDS.getEdgeData(edgeID).getVertex1();
					Vector3f tempPos = new Vector3f(edgeDS.getVertexData(newVertexId).mData.getPosition());  
					Vector2f tempTexCoord = new Vector2f(edgeDS.getVertexData(newVertexId).mData.getTexCoord());  

					
					targetPosition.scaleAdd(beta, tempPos, targetPosition);
					targetTexture.scaleAdd(beta, tempTexCoord, targetTexture);
				}
			}
			// On an edge
			else if (creaseEdges.size() == 2){
				targetPosition.scale(0.75f);
				targetTexture.scale(0.75f);
				
				//NEW VERTEX 1
				int newVertexId = edgeDS.getEdgeData(creaseEdges.get(0)).getVertex0();
				if (newVertexId == vertexID) newVertexId = edgeDS.getEdgeData(creaseEdges.get(0)).getVertex1();
				Vector3f tempPos = new Vector3f(edgeDS.getVertexData(newVertexId).mData.getPosition());  
				Vector2f tempTexCoord = new Vector2f(edgeDS.getVertexData(newVertexId).mData.getTexCoord()); 
				targetPosition.scaleAdd(0.125f,tempPos, targetPosition);
				targetTexture.scaleAdd(0.125f, tempTexCoord, targetTexture);
				
				//NEW VERTEX 2
				newVertexId = edgeDS.getEdgeData(creaseEdges.get(1)).getVertex0();
				if (newVertexId == vertexID) newVertexId = edgeDS.getEdgeData(creaseEdges.get(1)).getVertex1();
				tempPos = new Vector3f(edgeDS.getVertexData(newVertexId).mData.getPosition());  
				tempTexCoord = new Vector2f(edgeDS.getVertexData(newVertexId).mData.getTexCoord()); 

				targetPosition.scaleAdd(0.125f,tempPos, targetPosition);
				targetTexture.scaleAdd(0.125f, tempTexCoord, targetTexture);
			}
			
			else {
				// do nothing if more than 2 creases
			}
						
			//SAVE NEW POINT TO BUFFER
			vertexBuf.put(targetPosition.x);
			vertexBuf.put(targetPosition.y);
			vertexBuf.put(targetPosition.z);
			
			texCoordBuf.put(targetTexture.x);
			texCoordBuf.put(targetTexture.y);				

			edgeDS.getVertexData(vertexID).setNewVertexID(oddVertexCounter);
			oddVertexCounter +=1;
		}
		
		//adding external edges
		for (int edgeID:edgeDS.getEdgeIDs()){
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			// for each edge, add four points for two new edges
			if (edgeDS.isCreaseEdge(edgeID)){
				edgeBuf.put(edgeDS.getVertexData(edge.getVertex0()).getNewVertexID());
				edgeBuf.put(edge.getNewVertexID());
				edgeBuf.put(edge.getNewVertexID());			
				edgeBuf.put(edgeDS.getVertexData(edge.getVertex1()).getNewVertexID());
			}
		}
		
		//adding internal edges and faces
		for (int faceID:edgeDS.getPolygonIDs()){
			PolygonData face = edgeDS.getPolygonData(faceID);
			EdgeData edge0 = edgeDS.getEdgeData(face.getAllEdges().get(0));
			EdgeData edge1 = edgeDS.getEdgeData(face.getAllEdges().get(1));
			EdgeData edge2 = edgeDS.getEdgeData(face.getAllEdges().get(2));
			
			// adding faces (4 sub faces for each faces)
			// sub face 1
			if(edge0.getVertex0() == edge1.getVertex0() || edge0.getVertex0() == edge1.getVertex1()){
				faceBuf.put(edgeDS.getVertexData(edge0.getVertex0()).getNewVertexID());
			}
			else{
				faceBuf.put(edgeDS.getVertexData(edge0.getVertex1()).getNewVertexID());
			}
			faceBuf.put(edge1.getNewVertexID());
			faceBuf.put(edge0.getNewVertexID());
			
			// sub face 2
			faceBuf.put(edge1.getNewVertexID());
			if(edge1.getVertex0() == edge2.getVertex0() || edge1.getVertex0() == edge2.getVertex1()){
				faceBuf.put(edgeDS.getVertexData(edge1.getVertex0()).getNewVertexID());
			}
			else{
				faceBuf.put(edgeDS.getVertexData(edge1.getVertex1()).getNewVertexID());
			}
			faceBuf.put(edge2.getNewVertexID());
			
			// sub face 3
			faceBuf.put(edge0.getNewVertexID());
			faceBuf.put(edge2.getNewVertexID());
			if(edge2.getVertex0() == edge0.getVertex0() || edge2.getVertex0() == edge0.getVertex1()){
				faceBuf.put(edgeDS.getVertexData(edge2.getVertex0()).getNewVertexID());
			}
			else{
				faceBuf.put(edgeDS.getVertexData(edge2.getVertex1()).getNewVertexID());
			}
			
			// sub face 4 (internal triangle)
			faceBuf.put(edge0.getNewVertexID());
			faceBuf.put(edge1.getNewVertexID());
			faceBuf.put(edge2.getNewVertexID());
		}
		
		vertexBuf.rewind();
		texCoordBuf.rewind();
		edgeBuf.rewind();
		faceBuf.rewind();
		
		this.mMesh = edgeDS.getMesh();
		this.mMesh.setVertexData(vertexBuf);
		this.mMesh.setTexCoordData(texCoordBuf);
		this.mMesh.setEdgeData(edgeBuf);
		this.mMesh.setPolygonData(faceBuf);
		this.mMesh.setNormalData(null);		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}
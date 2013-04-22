package cs5625.deferred.catmullclark;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.datastruct.EdgeData;
import cs5625.deferred.datastruct.PolygonData;
import cs5625.deferred.datastruct.VertexAttributeData;
import cs5625.deferred.scenegraph.Mesh;

public class CCSubdiv {
	
	private Mesh mMesh;
	
	public CCSubdiv(EdgeDS edgeDS)
	{
		// TODO PA5: Fill in this function to perform catmull clark subdivision
		//COMPUTE SIZES OF NEW MESH GEOMETRY
		int nVertices = edgeDS.getVertexIDs().size() + edgeDS.getEdgeIDs().size() + edgeDS.getPolygonIDs().size();
	
		int nFaces = 0;
		
		for (int faceId: edgeDS.getPolygonIDs()){
			int tmp = edgeDS.getPolygonData(faceId).getAllEdges().size();
			nFaces += tmp;
		}
		
		int nCreaseEdges = 2*edgeDS.getCreaseEdgeIDs().size();
		
		//INITIALIZE NEW BUFFERS FOR NEW MESH
		FloatBuffer vertexBuf = FloatBuffer.allocate(nVertices*3);
		FloatBuffer normalBuf = FloatBuffer.allocate(nVertices*3);
		FloatBuffer texCoordBuf = FloatBuffer.allocate(nVertices*2);
		IntBuffer edgeBuf = IntBuffer.allocate(nCreaseEdges*2);
		IntBuffer faceBuf = IntBuffer.allocate(nFaces*4);

		int VertexCounter = 0;
		
		// ITERATING FACE AND ADDING FACE VERTICES
		for (int faceID:edgeDS.getPolygonIDs()){
			Point3f targetPosition = new Point3f(0f, 0f, 0f);
			Point2f targetTexture = new Point2f(0f, 0f);
			
			PolygonData face = edgeDS.getPolygonData(faceID);
			for (int faceVertices:face.getAllVertices()){
				targetPosition.add(edgeDS.getVertexData(faceVertices).mData.getPosition());
				targetTexture.add(edgeDS.getVertexData(faceVertices).mData.getTexCoord());
			}
			float nVertsOnFace = (float) face.getAllVertices().size(); 
			targetPosition.scale(1/nVertsOnFace);
			targetTexture.scale(1/nVertsOnFace);
			
			vertexBuf.put(targetPosition.x);
			vertexBuf.put(targetPosition.y);
			vertexBuf.put(targetPosition.z);
			
			texCoordBuf.put(targetTexture.x);
			texCoordBuf.put(targetTexture.y);
			
			face.setNewFaceVertexID(VertexCounter);
			VertexCounter +=1;
		}
		
		// ITERATING OVER EDGES AND ADDING EDGE VERTICES
		for (int edgeID:edgeDS.getEdgeIDs()){

			EdgeData edge = edgeDS.getEdgeData(edgeID);
			int v1 = edge.getVertex0();
			int v2 = edge.getVertex1();
			
			Point3f targetPosition = new Point3f();
			Point2f targetTexture = new Point2f();

			if (!edgeDS.isBoundaryEdge(edgeID) && !edgeDS.isCreaseEdge(edgeID)){
			
				int v3 = edgeDS.getPolygonData(edge.getPolys().get(0)).getNewFaceVertexID();
				int v4 = edgeDS.getPolygonData(edge.getPolys().get(1)).getNewFaceVertexID();
			
				VertexAttributeData vDatav1 = edgeDS.getVertexData(v1).mData;
				VertexAttributeData vDatav2 = edgeDS.getVertexData(v2).mData;
				
				Point3f v3Position = new Point3f(vertexBuf.get(3*v3), vertexBuf.get(3*v3+1), vertexBuf.get(3*v3+2));
				Point3f v4Position = new Point3f(vertexBuf.get(3*v4), vertexBuf.get(3*v4+1), vertexBuf.get(3*v4+2));
				
				Point2f v3TexCoord = new Point2f(texCoordBuf.get(2*v3), vertexBuf.get(2*v3+1));
				Point2f v4TexCoord = new Point2f(texCoordBuf.get(2*v4), vertexBuf.get(2*v4+1));
				
				targetPosition.set(vDatav1.getPosition());
				targetPosition.add(vDatav2.getPosition());
				targetPosition.add(v3Position);
				targetPosition.add(v4Position);
				targetPosition.scale(0.25f);
				
				targetTexture.set(vDatav1.getTexCoord());
				targetTexture.add(vDatav2.getTexCoord());
				targetTexture.add(v3TexCoord);
				targetTexture.add(v4TexCoord);
				targetTexture.scale(0.25f);
			}
			else{ // IF ON A CREASE EDGE
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
			
			edge.setVertexIDNew(VertexCounter);
			VertexCounter +=1;
		}
		
		// ITERATING THROUGH VERTICES AND ADD UPDATED VERTICES
		for(int vertexID:edgeDS.getVertexIDs()){
			float beta;
			
			ArrayList<Integer>creaseEdges = edgeDS.getCreaseEdges(vertexID);
			creaseEdges.addAll(edgeDS.getBoundaryEdges(vertexID));

			Point3f targetPosition = new Point3f(edgeDS.getVertexData(vertexID).mData.getPosition());
			Point2f targetTexture = new Point2f(edgeDS.getVertexData(vertexID).mData.getTexCoord());
			
			// Not on an crease/edge
			if (creaseEdges.size() <= 1 ){
				int numberConnectedEdges = edgeDS.getVertexData(vertexID).getConnectedEdges().size();
				
				beta = 1f/(numberConnectedEdges*numberConnectedEdges);
		
				targetPosition.scale(1f - 2f/numberConnectedEdges);
				targetTexture.scale(1f - 2f/numberConnectedEdges);
				
				Set<Integer> faces = new HashSet<Integer>();
				for(int edgeID : edgeDS.getVertexData(vertexID).getConnectedEdges()){
					EdgeData edge = edgeDS.getEdgeData(edgeID);
					faces.addAll(edge.getPolys());
				}
				
				// PREVIOUS ITERATION EDGES
				for (int connectedVertexID :edgeDS.getVertexData(vertexID).getConnectedVertices()){
					Vector3f tempPos = new Vector3f(edgeDS.getVertexData(connectedVertexID).mData.getPosition());  
					Vector2f tempTexCoord = new Vector2f(edgeDS.getVertexData(connectedVertexID).mData.getTexCoord());
					targetPosition.scaleAdd(beta, tempPos, targetPosition);
					targetTexture.scaleAdd(beta, tempTexCoord, targetTexture);
				}

				for(int faceID : faces){
					PolygonData face = edgeDS.getPolygonData(faceID);
					int v = face.getNewFaceVertexID();
					Point3f vPosition = new Point3f(vertexBuf.get(3*v), vertexBuf.get(3*v+1), vertexBuf.get(3*v+2));
					Point2f vTexCoord = new Point2f(texCoordBuf.get(2*v), vertexBuf.get(2*v+1));
					
					targetPosition.scaleAdd(beta,vPosition, targetPosition);
					targetTexture.scaleAdd(beta, vTexCoord, targetTexture);
				}
			}
			
			// On an edge
			else if (creaseEdges.size() == 2){
				targetPosition.scale(0.75f);
				targetTexture.scale(0.75f);
				
				//NEW VERTEX 1
				int newVertexId;
				if (edgeDS.getEdgeData(creaseEdges.get(0)).getVertex0() == vertexID){
					newVertexId = edgeDS.getEdgeData(creaseEdges.get(0)).getVertex1();
				}
				else{
					newVertexId = edgeDS.getEdgeData(creaseEdges.get(0)).getVertex0();
				}
				Vector3f tempPos = new Vector3f(edgeDS.getVertexData(newVertexId).mData.getPosition());  
				Vector2f tempTexCoord = new Vector2f(edgeDS.getVertexData(newVertexId).mData.getTexCoord());				
				targetPosition.scaleAdd(0.125f,tempPos, targetPosition);
				targetTexture.scaleAdd(0.125f, tempTexCoord, targetTexture);
				
				//NEW VERTEX 2
				if (edgeDS.getEdgeData(creaseEdges.get(1)).getVertex0() == vertexID){
					newVertexId = edgeDS.getEdgeData(creaseEdges.get(1)).getVertex1();
				}
				else{
					newVertexId = edgeDS.getEdgeData(creaseEdges.get(1)).getVertex0();
				}
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

			edgeDS.getVertexData(vertexID).setNewVertexID(VertexCounter);
			VertexCounter +=1;
		}
		
		//adding external edges
		for (int edgeID:edgeDS.getEdgeIDs()){
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			// only add edge if it is a crease edge
			if (edgeDS.isCreaseEdge(edgeID)){
				// for each edge, add four points for two new edges
				edgeBuf.put(edgeDS.getVertexData(edge.getVertex0()).getNewVertexID());
				edgeBuf.put(edge.getNewVertexID());
				edgeBuf.put(edge.getNewVertexID());			
				edgeBuf.put(edgeDS.getVertexData(edge.getVertex1()).getNewVertexID());
			}
		}
		
		//adding internal edges and faces
		for (int faceID:edgeDS.getPolygonIDs()){
			
			PolygonData face = edgeDS.getPolygonData(faceID);
			int centerID = face.getNewFaceVertexID();
			int eIndex0 = face.getAllEdges().get(0);
			ArrayList<Integer> orderedFaceEdges = edgeDS.getOtherEdgesOfLeftFace(eIndex0);
			
			if (! orderedFaceEdges.contains(face.getAllEdges().get(1))){
				orderedFaceEdges = edgeDS.getOtherEdgesOfRightFace(eIndex0);		
			}
			orderedFaceEdges.add(eIndex0);

			//Build a face for each pair of adjacent edges around the face
			int nEdgesFace = orderedFaceEdges.size();
			for(int edgeIndex = 0; edgeIndex< nEdgesFace ; edgeIndex++){
				int nextEdgeIndex = (edgeIndex+1)%nEdgesFace;
				EdgeData edge0 = edgeDS.getEdgeData(orderedFaceEdges.get(edgeIndex));
				EdgeData edge1 = edgeDS.getEdgeData(orderedFaceEdges.get(nextEdgeIndex));

				if(edge0.getVertex0() == edge1.getVertex0() || edge0.getVertex0() == edge1.getVertex1()){
					faceBuf.put(edgeDS.getVertexData(edge0.getVertex0()).getNewVertexID());
				}
				else{
					faceBuf.put(edgeDS.getVertexData(edge0.getVertex1()).getNewVertexID());
				}
				faceBuf.put(edge1.getNewVertexID());
				faceBuf.put(centerID);
				faceBuf.put(edge0.getNewVertexID());
			}
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

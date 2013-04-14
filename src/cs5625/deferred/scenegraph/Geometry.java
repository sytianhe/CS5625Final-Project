package cs5625.deferred.scenegraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Point2i;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;

/**
 * Geometry.java
 * 
 * The Geometry class contains (in the way of Trimesh objects) the geometry of a scene. It also
 * provides functionality to load model files from disk. 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class Geometry extends SceneObject
{
	/* List of meshes in this object. */
	private ArrayList<Mesh> mMeshes = new ArrayList<Mesh>();
	
	/**
	 * Returns the list of meshes in this geometry object.
	 */
	public List<Mesh> getMeshes()
	{
		return mMeshes;
	}
	
	/**
	 * Adds a mesh to this geometry object. 
	 * 
	 * A mesh may be contained in multiple geometry objects, allowing you to 
	 * share data if you have multiple identical objects in your scene.
	 */
	public void addMesh(Mesh mesh)
	{
		mMeshes.add(mesh);
	}

	/**
	 * Adds a list of meshes to this geometry object. 
	 * 
	 * A mesh may be contained in multiple geometry objects, allowing you to 
	 * share data if you have multiple identical objects in your scene.
	 */
	public void addMeshes(List<Mesh> meshes)
	{
		mMeshes.addAll(meshes);
	}
	
	/**
	 * Removes a mesh from this geometry object.
	 * 
	 * @throws ScenegraphException If the given mesh isn't in this object.
	 */
	public void removeMesh(Mesh mesh) throws ScenegraphException
	{
		if (!mMeshes.remove(mesh))
		{
			throw new ScenegraphException("Mesh to remove is not in this Geometry object.");
		}
	}
	
	/**
	 * Returns the first mesh with the given name, or null if none with that name.
	 */
	public Mesh findMeshByName(String name)
	{
		for (Mesh mesh : mMeshes)
		{
			if (mesh.getName().equals(name))
			{
				return mesh;
			}
		}
		
		return null;
	}

	@Override
	public void releaseGPUResources(GL2 gl)
	{
		super.releaseGPUResources(gl);
		
		for (Mesh mesh : mMeshes)
		{
			mesh.releaseGPUResources(gl);
		}
	}
	
	@Override
	public void calculateTangentVectorsForAllGeometry()
	{
		super.calculateTangentVectorsForAllGeometry();
		
		for (Mesh mesh : mMeshes)
		{
			if (!mesh.vertexAttribData.containsKey("VertexTangent"))
			{
				mesh.vertexAttribData.put("VertexTangent", mesh.calculateTangentVectors());
			}
		}
	}
	
	/**
	 * Loads models from a wavefront object (.obj) file.
	 * 
	 * Each object defined in the file (with the 'o' command) will be loaded as a separate Geometry object, 
	 * and each material (specified with the 'mtllib' and 'usemtl' commands) will be loaded as a separate 
	 * mesh. This loader does not support relative (negative) indexing.
	 * 
	 * Polygons specified using the 'f' command will be triangulated and stored in the triangle mesh.
	 * Edges (specified with the 'f' command followed by only 2 vertices) will be stored in the corresponding 
	 * mesh's edge data buffer.
	 * 
	 * The 'mtllib' command will look for material files in the same directory as the model file, loading them
	 * with `Material.load()`.
	 * 
	 * @param identifier The identifier, or package path, to the model file to load. 
	 *        Typically of the form "models/foo.obj".
	 * @param loadMaterials If true, material and texture files referenced from the .obj model will be loaded, and
	 *        errors generated if they cannot be found. If false, all objects are left with the default material.
	 * @param centerObjects All vertices in the .obj file are in the same coordinate frame. If 
	 *        `centerObjects` is true, each object is positioned at the average position of its
	 *        vertices, and that average position is subtracted from its vertices. The objects will 
	 *        render in the same place, but will translate and rotate about their own centers, instead
	 *        of about the overall scene origin. If false, positions are not modified.
	 *        
	 * @return A list of geometry objects from the model file. Null if the file doesn't exist.
	 */
	public static List<Geometry> load(String identifier, boolean loadMaterials, boolean centerObjects) throws IOException
	{
		/* Declare temporary lists used to store all the data read from the file before indexing 
		 * and breaking up into individual meshes. */
		ArrayList<Vector3f> allVertices = new ArrayList<Vector3f>();
		ArrayList<Vector3f> allNormals = new ArrayList<Vector3f>();
		ArrayList<Vector2f> allTexcoords = new ArrayList<Vector2f>();
		ArrayList<Point3i[]> allPolygons = new ArrayList<Point3i[]>();
		ArrayList<Point2i> allEdges = new ArrayList<Point2i>();
		
		/* Lists to store the separations between objects specified in the model file. */
		ArrayList<Integer> objectDelimeters = new ArrayList<Integer>();
		ArrayList<String> objectNames = new ArrayList<String>();

		/* Lists to store the separations between meshes (materials) specified in the model file. */
		ArrayList<Integer> meshDelimeters = new ArrayList<Integer>();
		ArrayList<String> meshMaterialNames = new ArrayList<String>();
		ArrayList<Integer> edgeDelimeters = new ArrayList<Integer>();

		/* Map to store all materials loaded from the file. */
		HashMap<String, Material> allMaterials = new HashMap<String, Material>();
		
		/* Read the file into those temporary buffers. */
		parseRawOBJfile(
				identifier, loadMaterials, 
				allVertices, allNormals, allTexcoords, allPolygons, allEdges, 
				objectDelimeters, objectNames, meshDelimeters, meshMaterialNames, edgeDelimeters, allMaterials);
		
		/* PHASE 2 -- reassemble temporary buffers into our desired data structures. */
		
		/* Create a list to hold the results. */
		ArrayList<Geometry> results = new ArrayList<Geometry>();

		/* Keep track of our current index into the mesh delimeter arrays. */
		int meshIndex = 0;

		/* Loop and create each object. */
		for (int objectIndex = 0; objectIndex < objectDelimeters.size() - 1; ++objectIndex)
		{
			/* Create this object and gets its bounds in the index list. */
			Geometry currentObject = new Geometry();
			currentObject.setName(objectNames.get(objectIndex));
			int objectLastIndex = objectDelimeters.get(objectIndex + 1);
			
			/* Keep track of the average vertex position in this object. */
			Point3f currentPositionAverage = new Point3f(0.0f, 0.0f, 0.0f);
			int currentPositionCount = 0;
			
			/* Loop over each mesh in this object. */
			while (meshIndex < meshDelimeters.size() - 1 && meshDelimeters.get(meshIndex + 1) <= objectLastIndex)
			{
				/* Grab first and last indices in this mesh. */
				int meshFirstIndex = meshDelimeters.get(meshIndex);
				int meshLastIndex = meshDelimeters.get(meshIndex + 1);
				
				/* Skip empty meshes. */
				if (meshFirstIndex == meshLastIndex)
				{
					++meshIndex;
					continue;
				}
				
				/* The wavefront object format indexes vertices, normals, and texture coordinates separately, but
				 * OpenGL indexes them together. To handle that, we have to find all unique "vIndex/txIndex/nIndex" triples
				 * and make a vertex in the trimesh for each one. */
				HashMap<Point3i, Integer> uniqueVertices = new HashMap<Point3i, Integer>(meshLastIndex - meshFirstIndex + 1);
				
				/* If all polygons in this mesh are quads, we want to make a Quadmesh object. */
				boolean allPolysAreQuads = true;
				
				/* If not all polygons are quads, we need to know how many triangles they break into. */ 
				int trimeshTriangleCount = 0;
				
				for (int meshVertexIndex = meshFirstIndex; meshVertexIndex < meshLastIndex; ++meshVertexIndex)
				{
					/* Map each index triplet to the index at which it first appears. */
					Point3i[] poly = allPolygons.get(meshVertexIndex);
					
					/* Keep track of whether this mesh is a quad mesh. */
					if (poly.length != 4)
					{
						allPolysAreQuads = false;
					}
					
					/* An n-gon triangulates into n - 2 triangles. */
					trimeshTriangleCount += (poly.length - 2);
					
					/* Add each {v, t, n} index triplet of this polygon to the unique list of index triplets. */ 
					for (Point3i indexSet : poly)
					{
						if (!uniqueVertices.containsKey(indexSet))
						{
							uniqueVertices.put(indexSet, uniqueVertices.size());
						}
					}
				}
				
				/* Make a list of all edges in the mesh as pairs of final vertex indices. This might involve duplicating edges 
				 * specified in the .obj file if that edge was specified between vertex positions which got duplicated. */
				ArrayList<Point2i> edges = new ArrayList<Point2i>();
				
				for (int edgeIndex = edgeDelimeters.get(meshIndex); edgeIndex < edgeDelimeters.get(meshIndex + 1); ++edgeIndex)
				{
					/* For this edge, find all vertices which its start and end vertices were split into. */
					Point2i edgeVertices = allEdges.get(edgeIndex);
					ArrayList<Integer> startVertices = findAllVerticesWithPositionIndex(uniqueVertices, edgeVertices.x);
					ArrayList<Integer> endVertices = findAllVerticesWithPositionIndex(uniqueVertices, edgeVertices.y);
					
					/* Add an edge between each {start, end} pair. */
					for (Integer vStart : startVertices)
					{
						for (Integer vEnd: endVertices)
						{
							edges.add(new Point2i(vStart, vEnd));
						}
					}
				}
				
				/* Now we can start assembling the actual mesh buffers. First allocate them. */
				FloatBuffer meshVertices = Buffers.newDirectFloatBuffer(3 * uniqueVertices.size());
				FloatBuffer meshTexcoords = Buffers.newDirectFloatBuffer(2 * uniqueVertices.size());
				FloatBuffer meshNormals = Buffers.newDirectFloatBuffer(3 * uniqueVertices.size());
				
				/* Now loop over the unique index triplets and make a vertex for each one. */
				for (Point3i indexSet : uniqueVertices.keySet())
				{
					/* The index triplet is {position, texcoord, normal}, so retrieve each of those. */
					Vector3f vertex = allVertices.get(indexSet.x);
					Vector2f texcoord = allTexcoords.get(indexSet.y);
					Vector3f normal = allNormals.get(indexSet.z);
					
					/* Look up the index of this triplet into the resulting vertices. */
					int vIndex = uniqueVertices.get(indexSet);
					
					/* Store the vertex data into the mesh buffers. */
					meshVertices.put(3 * vIndex + 0, vertex.x);
					meshVertices.put(3 * vIndex + 1, vertex.y);
					meshVertices.put(3 * vIndex + 2, vertex.z);

					meshTexcoords.put(2 * vIndex + 0, texcoord.x);
					meshTexcoords.put(2 * vIndex + 1, texcoord.y);

					meshNormals.put(3 * vIndex + 0, normal.x);
					meshNormals.put(3 * vIndex + 1, normal.y);
					meshNormals.put(3 * vIndex + 2, normal.z);
					
					/* Accumulate in position average. */
					currentPositionAverage.add(vertex);
					++currentPositionCount;
				}
				
				/* Since these nio buffers are (sort of) stream oriented, rewind them. */
				meshVertices.rewind();
				meshTexcoords.rewind();
				meshNormals.rewind();
				
				Mesh mesh;
				
				if (allPolysAreQuads)
				{
					/* Create a quad mesh. */

					/* Allocate and fill the index buffer. */
					IntBuffer quads = Buffers.newDirectIntBuffer(4 * (meshLastIndex - meshFirstIndex));
					
					for (int meshVertexIndex = meshFirstIndex; meshVertexIndex < meshLastIndex; ++meshVertexIndex)
					{
						Point3i quad[] = allPolygons.get(meshVertexIndex);
						assert(quad.length == 4);
						
						for (Point3i vertex : quad)
						{
							quads.put(uniqueVertices.get(vertex));
						}
					}
					
					quads.rewind();

					/* Create the mesh object itself. */
					Quadmesh quadmesh = new Quadmesh();
					quadmesh.setPolygonData(quads);
					mesh = quadmesh;
				}
				else
				{
					/* Create a triangle mesh, triangulate polygons, and fill index buffer. */
					IntBuffer triangles = Buffers.newDirectIntBuffer(3 * trimeshTriangleCount);
					
					for (int meshVertexIndex = meshFirstIndex; meshVertexIndex < meshLastIndex; ++meshVertexIndex)
					{
						Point3i poly[] = allPolygons.get(meshVertexIndex);
						
						for (int i = 2; i < poly.length; ++i)
						{
							triangles.put(uniqueVertices.get(poly[0]));
							triangles.put(uniqueVertices.get(poly[i - 1]));
							triangles.put(uniqueVertices.get(poly[i]));
						}
					}
					
					triangles.rewind();
					
					/* Create the mesh object itself. */
					Trimesh trimesh = new Trimesh();
					trimesh.setPolygonData(triangles);
					mesh = trimesh;
				}
								
				/* Assign the mesh (either tri or quad) the vertex data buffers. */
				mesh.setVertexData(meshVertices);
				mesh.setNormalData(meshNormals);
				mesh.setTexCoordData(meshTexcoords);
				
				/* Fill edge buffer, if any edges. */
				if (edges.size() > 0)
				{
					IntBuffer meshEdges = Buffers.newDirectIntBuffer(2 * edges.size());
					
					for (Point2i edge : edges)
					{
						meshEdges.put(edge.x);
						meshEdges.put(edge.y);
					}
					
					meshEdges.rewind();
					mesh.setEdgeData(meshEdges);
				}
				
				/* Assign the mesh's name and material. */
				mesh.setName(meshMaterialNames.get(meshIndex));
				
				if (loadMaterials && allMaterials.containsKey(mesh.getName()))
				{
					mesh.setMaterial(allMaterials.get(mesh.getName()));
				}
				
				/* Now that we have the mesh, add it to the current object. */
				currentObject.addMesh(mesh);
				
				/* Move to the next mesh defined in the file. */
				++meshIndex;
			}
			
			/* If we are supposed to re-center each object, do that now. */
			if (centerObjects)
			{
				currentPositionAverage.scale(1.0f / currentPositionCount);
				
				for (Mesh mesh : currentObject.getMeshes())
				{
					FloatBuffer vertices = mesh.getVertexData();
					
					for (int v = 0; v < mesh.getVertexCount(); ++v)
					{
						vertices.put(3 * v + 0, vertices.get(3 * v + 0) - currentPositionAverage.x);
						vertices.put(3 * v + 1, vertices.get(3 * v + 1) - currentPositionAverage.y);
						vertices.put(3 * v + 2, vertices.get(3 * v + 2) - currentPositionAverage.z);
					}
				}
				
				currentObject.getPosition().set(currentPositionAverage);
			}
			
			/* Add the finished object if it's not empty. */
			if (currentObject.getMeshes().size() > 0)
			{
				results.add(currentObject);
			}
		}
		
		return results;
	}
	
	private static void parseRawOBJfile(
		String identifier, 
		boolean loadMaterials,
		ArrayList<Vector3f> allVertices, 
		ArrayList<Vector3f> allNormals, 
		ArrayList<Vector2f> allTexcoords,
		ArrayList<Point3i[]> allPolygons, 
		ArrayList<Point2i> allEdges, 
		ArrayList<Integer> objectDelimeters, 
		ArrayList<String> objectNames, 
		ArrayList<Integer> meshDelimeters,
		ArrayList<String> meshMaterialNames,
		ArrayList<Integer> edgeDelimeters,
		HashMap<String, Material> allMaterials) throws IOException
	{
		/* Open the file. */
		URL url = Geometry.class.getClassLoader().getResource(identifier);
		if (url == null)
		{
			throw new IOException("Could not find model file '" + identifier + "'.");
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

		/* Dummy entries at index 0 so that the 1-based indexing of the OBJ format can be used directly. */
		allVertices.add(new Vector3f(0.0f, 0.0f, 0.0f));
		allNormals.add(new Vector3f(0.0f, 0.0f, 0.0f));
		allTexcoords.add(new Vector2f(0.0f, 0.0f));
		
		/* Add a default object and material, in case none is declared. */
		objectDelimeters.add(allPolygons.size());
		objectNames.add(identifier);
		
		meshDelimeters.add(allPolygons.size());
		edgeDelimeters.add(allEdges.size());

		meshMaterialNames.add("");
		allMaterials.put("", new BlinnPhongMaterial());
		
		/* Read each line and add the parsed data to the appropriate temporary list. */
		String line = reader.readLine();
		while (line != null)
		{
			line = line.trim();
			
			if (line.length() == 0)
			{
				/* Skip blank lines. */
			}
			else if (line.charAt(0) == '#')
			{
				/* Skip comment lines. */
			}
			else
			{
				/* Split the line on whitespace to identify the command. */
				String words[] = Util.splitString(line, " \t\n\r", false);
				
				if (words[0].equals("v"))
				{
					/* Parse the 3 coordinates of the vertex and add to vertices list. */
					allVertices.add(new Vector3f(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
				}
				else if (words[0].equals("vn"))
				{
					/* Parse the 3 coordinates of the normal and add to normals list. */
					allNormals.add(new Vector3f(Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])));
				}
				else if (words[0].equals("vt"))
				{
					/* Parse the 2 coordinates of the texture coordinate and add to texcoords list. */
					allTexcoords.add(new Vector2f(Float.parseFloat(words[1]), Float.parseFloat(words[2])));
				}
				else if (words[0].equals("f"))
				{
					/* This is a face command. In the .obj format, faces may be arbitrary-sized polygons. This framework 
					 * only supports triangle and quad meshes; any mesh which contains only 4-gons will be loaded into a 
					 * Quadmesh, and all others will be triangulated and loaded into a Trimesh. */
					
					/* If the face is really an edge, add it to the edges array, otherwise add to faces. */
					if (words.length == 3)
					{
						allEdges.add(new Point2i(parseOBJIndices(words[1]).x, parseOBJIndices(words[2]).x)); 
					}
					else if (words.length > 3)
					{
						Point3i polygon[] = new Point3i[words.length - 1];
					
						for (int i = 1; i < words.length; ++i)
						{
							polygon[i - 1] = parseOBJIndices(words[i]); 
						}
						
						allPolygons.add(polygon);
					}
				}
				else if (words[0].equals("o"))
				{
					/* Record the number of indices up until this new object. */
					objectDelimeters.add(allPolygons.size());
					objectNames.add(line.substring("o".length()).trim());
					
					/* Start a new mesh at the same point as this new object. */
					meshDelimeters.add(allPolygons.size());
					meshMaterialNames.add(meshMaterialNames.get(meshMaterialNames.size() - 1));
					
					/* Remember which edges go to which mesh. */
					edgeDelimeters.add(allEdges.size());
				}
				else if (words[0].equals("mtllib") && loadMaterials)
				{
					/* Try to load the named material file, assumed to be in the same directory as the model. */
					String mtlFileName = line.substring("mtllib".length()).trim();
					allMaterials.putAll(Material.load(Util.makeIdentifierOfSibling(identifier, mtlFileName)));
				}
				else if (words[0].equals("usemtl"))
				{
					/* Record the number of indices up to this new material. */
					meshDelimeters.add(allPolygons.size());
					meshMaterialNames.add(line.substring("usemtl".length()).trim());
					
					/* Remember which edges go to which mesh. */
					edgeDelimeters.add(allEdges.size());
				}
				else
				{
					/* Silently ignore unknown commands. */
				}
			}
			
			/* Read the next line until there are no more. */
			line = reader.readLine();
		}
		
		/* Add dummy entries to the delimeter lists to simplify the loop logic in the next step. */
		objectDelimeters.add(allPolygons.size());
		meshDelimeters.add(allPolygons.size());
		edgeDelimeters.add(allEdges.size());
	}
	
	/**
	 * Helper function to parse a face vertex index triplet string from a wavefront obj file.
	 * 
	 * @param str The index triplet, of the format "v/t/n", where 'v' is the vertex index, 't' is 
	 *        the texture coordinate index, and 'n' is the normal index. The texture coordinate 
	 *        and normal indices may both be omitted, so "v" and "v/t" and "v//n" are both also
	 *        valid forms. The indices of any omitted fields are assigned 0 (which is an invalid
	 *        index in the 1-based OBJ format). 
	 *        
	 * @return The three indices parsed from 'str'.
	 * @throws IOException If 'str' does not have 1, 2, or 3 slash-separated fields.
	 */
	private static Point3i parseOBJIndices(String str) throws IOException
	{
		String indices[] = Util.splitString(str, "/", true);
			
		switch(indices.length)
		{
		case 1: return new Point3i(parseIntDefaultZero(indices[0]), 0, 0);
		case 2:	return new Point3i(parseIntDefaultZero(indices[0]), parseIntDefaultZero(indices[1]), 0);
		case 3: return new Point3i(parseIntDefaultZero(indices[0]), parseIntDefaultZero(indices[1]), parseIntDefaultZero(indices[2]));
		}
		
		throw new IOException("Malformed face vertex specification '" + str + "'.");
	}
	
	/**
	 * Helper function for parsing OBJ index fields; parses the string into an integer, 
	 * defaulting to 0 for an empty string.
	 */
	private static int parseIntDefaultZero(String str)
	{
		if (str.length() > 0)
		{
			return Integer.parseInt(str);
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Returns an array of all {v, t, n} vertices which have a given {v} position.
	 * 
	 * @param uniqueVertices The map of {v, t, n} index triplets to final vertex indices.
	 * @param vertexPositionIndex The {v} value to search for.
	 * 
	 * @return List of all final vertex indices whose {v} value equals vertexPositionIndex.
	 */
	private static ArrayList<Integer> findAllVerticesWithPositionIndex(HashMap<Point3i, Integer> uniqueVertices, int vertexPositionIndex)
	{
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		for (Point3i indexTriplet : uniqueVertices.keySet())
		{
			if (indexTriplet.x == vertexPositionIndex)
			{
				results.add(uniqueVertices.get(indexTriplet));
			}
		}
		
		return results;
	}
}

package cs5625.deffered.physicsGeometry;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import cs5625.deferred.defaultGeometry.FourQuadMesh;
import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForceParticlePlane3;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Quadmesh;

public class Ground extends Geometry {
	
	private ArrayList<Particle> P = new ArrayList<Particle>();
	private ArrayList<Particle> IW = new ArrayList<Particle>();
	
	public Ground(){
		try {
			Mesh groundMesh = Ground.load("models/plane.obj", true, true).get(0).getMeshes().get(0);
			((Quadmesh) groundMesh).subdivide(1);
			groundMesh.setMaterial(new LambertianMaterial(new Color3f(0.00f, 0.70f, 0.70f)));
			this.addMesh(groundMesh);
			
			P.add(new Particle(new Point3d(10,0,0))); 
			P.add(new Particle(new Point3d(-10,0,0))); 
			P.add(new Particle(new Point3d(0,0,-10))); 
			P.add(new Particle(new Point3d(-10,0,10))); 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addInteractionWith(Particle p){
		IW.add(p);
	}
	
	public void addToParticleSystem(ParticleSystem PS){
		for (Particle p : P){
			PS.addParticle(p);
		}
		for(Particle p : IW){
			PS.addForce(new SpringForceParticlePlane3(p, P.get(0), P.get(1), P.get(2), PS )  );
			PS.addForce(new SpringForceParticlePlane3(p, P.get(2), P.get(3), P.get(0), PS )  );
		}
		
	}


}

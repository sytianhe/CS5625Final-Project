package cs5625.deferred.physicsGeometry;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.defaultGeometry.FourQuadMesh;
import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForceParticlePlane3;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Quadmesh;

public class Ground extends PhysicsGeometry {
		
	public Ground(){
		try {
			Mesh groundMesh = Ground.load("models/bigplane.obj", true, true).get(0).getMeshes().get(0);
			((Quadmesh) groundMesh).subdivide(0);
			groundMesh.setMaterial(new LambertianMaterial(new Color3f(1.00f, 1.00f, 0.00f)));
			this.addMesh(groundMesh);
			
			addControlPoint( new Point3f(100,0,0)); 
			addControlPoint(new Point3f(-100,0,0)); 
			addControlPoint(new Point3f(0,0,-100)); 
			addControlPoint(new Point3f(-100,0,100)); 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for(Particle p : getControlParticles()){
			PS.addForce(new PenaltyForceParticlePlane3(p, getControlParticles().get(0), getControlParticles().get(1), getControlParticles().get(2), PS )  );
			PS.addForce(new PenaltyForceParticlePlane3(p, getControlParticles().get(2), getControlParticles().get(3), getControlParticles().get(0), PS )  );
		}
		
	}


}

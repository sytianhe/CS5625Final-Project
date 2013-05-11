package cs5625.deferred.physicsGeometry;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.defaultGeometry.FourQuadMesh;
import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.materials.SandDuneMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForceParticlePlane3;
import cs5625.deferred.rendering.FramebufferObject;
import cs5625.deferred.rendering.Renderer;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Quadmesh;

public class Ground extends PhysicsGeometry {
		
	public Ground(Renderer mRenderer){
		try {
			Mesh groundMesh = Ground.load("models/bigplane.obj", false, true).get(0).getMeshes().get(0);
			((Quadmesh) groundMesh).subdivide(8);
			groundMesh.setMaterial(new SandDuneMaterial(mRenderer));
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
		for (PhysicsGeometry pg : getInteractsWith()){
			for(Particle p : pg.getControlParticles()){
				PS.addForce(new PenaltyForceParticlePlane3(p, getControlParticles().get(0), getControlParticles().get(1), getControlParticles().get(2), PS )  );
				PS.addForce(new PenaltyForceParticlePlane3(p, getControlParticles().get(2), getControlParticles().get(3), getControlParticles().get(0), PS )  );
			}
			PS.addForce(new PenaltyForceParticlePlane3(pg.getOriginParticle(), getControlParticles().get(0), getControlParticles().get(1), getControlParticles().get(2), PS ) );
			PS.addForce(new PenaltyForceParticlePlane3(pg.getOriginParticle(), getControlParticles().get(2), getControlParticles().get(3), getControlParticles().get(0), PS ) );
		}
	}


}

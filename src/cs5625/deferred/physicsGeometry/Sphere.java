package cs5625.deferred.physicsGeometry;

import java.io.IOException;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForce2Particle;
import cs5625.deferred.physics.PenaltyForceSphere;
import cs5625.deferred.scenegraph.Mesh;


public class Sphere extends PhysicsGeometry {
	
	public Sphere(){
		init();
	}
	public Sphere(Point3f p){
		init();
		this.setPosition(p);
		//this.addControlPoint(new Point3f() );
	}
	private void init(){
        try {
			Mesh sphereMesh = Sphere.load("models/sphere.obj", true, false).get(0).getMeshes().get(0);
			sphereMesh.setMaterial(new LambertianMaterial(new Color3f(1.00f, 0.00f, 0.00f)));
			this.addMesh(sphereMesh);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		this.getOriginParticle().setRadius(1.0);
//		for (PhysicsGeometry pg : getInteractsWith()){
//			for (Particle p2 : pg.getControlParticles()){
//				//PS.addForce(new PenaltyForce2Particle(this.getOriginParticle(), p2 ) );
//			}
//		}
		PS.addForce(new PenaltyForceSphere(this.getOriginParticle(), PS));
	}

}

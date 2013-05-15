package cs5625.deferred.physicsGeometry;

import java.io.IOException;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForceSphere;
import cs5625.deferred.scenegraph.Mesh;


public class Sphere extends PhysicsGeometry {
	
	private Color3f color = new Color3f(1.00f, 0.00f, 0.00f);
	private Color3f selectColor = new Color3f(1.00f, 1.0f, 1f);
	private float timeScale = 25f;
	private float time = 0;
	private LambertianMaterial material = new LambertianMaterial(color);
	
	public Sphere(){
		init();
	}
	public Sphere(Point3f p){
		init();
		this.setPosition(p);
	}
	private void init(){
        try {
			Mesh sphereMesh = Sphere.load("models/sphere.obj", true, false).get(0).getMeshes().get(0);
			sphereMesh.setMaterial(material);
			this.addMesh(sphereMesh);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		this.getOriginParticle().setRadius(1.0);
		PS.addForce(new PenaltyForceSphere(getOriginParticle(), PS) );
	}

	
	public void animateHelper(float dt){
		if (isSelected()){
			Color3f ic = new Color3f();
			ic.interpolate(color, selectColor, (float) ((float) 0.5+Math.sin(time*timeScale)));
			material.setDiffuseColor(ic);
			time += dt;
		}
		else{
			material.setDiffuseColor(color);
			time = 0;
		}
	}
}

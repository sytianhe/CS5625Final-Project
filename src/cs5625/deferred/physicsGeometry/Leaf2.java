package cs5625.deferred.physicsGeometry;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBendingTheta;
import cs5625.deferred.scenegraph.Mesh;
;

public class Leaf2 extends PhysicsGeometry
{
	private int numSubdivisions = 1;
	private float width = 0f;
	private Material material = new LambertianMaterial(new Color3f(0.10f, 0.70f, 0.10f)) ;
	
	public Leaf2(float height, float width){
		this.width = width;

		// ADDING THREE CONTROL POINTS (ALONG THE STEM)
		// Bottom point
		this.addControlPoint(new Point3f(0f,0f,0f));
		// Top point
		this.addControlPoint(new Point3f(0f,0f,height));
		// Middle point
		this.addControlPoint(new Point3f(0f,-height*width/10f,height/4f));
		
		Leafmesh2 newleaf = new Leafmesh2(getControlPoints(), width);
		newleaf.subdivide(numSubdivisions);
		this.mMeshes.add( newleaf );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
	}

	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		
		for (Particle p: this.getControlParticles()){
			p.setPin(false);
			//p.setMass(0.2);
			p.setRadius(0.01);
		}
		
		// Pin the bottom and middle points. 
		getControlParticles().get(0).setPin(true);
		getControlParticles().get(2).setPin(true);
		
		// Adding spring force bottom -> middle, middle -> top
		SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(0), getControlParticles().get(2), PS);
		PS.addForce(f);
		f = new SpringForce2Particle(getControlParticles().get(2), getControlParticles().get(1), PS);
		PS.addForce(f);
		
		//Bending on the stem. Angle can be changed
		SpringForceBendingTheta ff = new SpringForceBendingTheta(getControlParticles().get(0), getControlParticles().get(2), getControlParticles().get(1), new Vector3d(0,1,0));
		PS.addForce(ff);
	}
	
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);
		this.mMeshes.clear();
		Leafmesh2 newtree = new Leafmesh2(getControlPoints(), width);

		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
	}
}
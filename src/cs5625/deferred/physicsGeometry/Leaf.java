package cs5625.deferred.physicsGeometry;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBending;
import cs5625.deferred.scenegraph.Mesh;
;


public class Leaf extends PhysicsGeometry
{
	private int numSubdivisions = 0;
	private float height = 0f;
	private float width = 0f;
	private Material material = new LambertianMaterial(new Color3f(0.10f, 0.70f, 0.10f)) ;
	
	public Leaf(float height, float width){
		this.height = height;
		this.width = width;
		
		this.addControlPoint(new Point3f(0f,0f,0f));
		this.addControlPoint(new Point3f(width/2f,0f,height/4f));
		this.addControlPoint(new Point3f(0f,0f,height));
		this.addControlPoint(new Point3f(-width/2f,0f,height/4f));
		this.addControlPoint(new Point3f(0f,-height*width/10f,height/4f));
		
		Leafmesh newtree = new Leafmesh(getControlPoints());
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
	}

	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		
		for (Particle p: this.getControlParticles()){
			//p.setPin(false);
			//p.setMass(0.25);
			p.setRadius(0.01);
		}
		
		getControlParticles().get(0).setPin(true);
		getControlParticles().get(4).setPin(true);
		
		for (int i = 0; i<4; i++){
			int j = (i+1)%4;
			SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(i), getControlParticles().get(j), PS);
			PS.addForce(f);
		}
		SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(0), getControlParticles().get(4), PS);
		PS.addForce(f);
		
		f = new SpringForce2Particle(getControlParticles().get(2), getControlParticles().get(4), PS);
		PS.addForce(f);
		
		f = new SpringForce2Particle(getControlParticles().get(1), getControlParticles().get(3), PS);
		PS.addForce(f);
		
		
		


		//bending?
		SpringForceBending ff = new SpringForceBending(getControlParticles().get(0), getControlParticles().get(4), getControlParticles().get(2));
		PS.addForce(ff);
		
	//	ff = new SpringForceBending(getControlParticles().get(1), getControlParticles().get(4), getControlParticles().get(3));
	//	PS.addForce(ff);
		
	}
	
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);
		this.mMeshes.clear();
		Leafmesh newtree = new Leafmesh(getControlPoints());
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
//		
	}
}
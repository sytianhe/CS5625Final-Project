package cs5625.deffered.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Material;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForceSphere;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBending;
import cs5625.deferred.physics.SpringForceBendingTheta;
import cs5625.deferred.physics.SpringForceParticleEdge;
import cs5625.deferred.scenegraph.Mesh;
;


public class Branch extends PhysicsGeometry
{
	private int numSubdivisions = 0;
	private Material material = new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f));
	private float height  = 0.25f;
	private float width = 0.1f;
	
	public Branch(ArrayList<Point3f>list){
		this.addControlPoints(list);
		Branchmesh branchmesh = new Branchmesh(list, height, width);
		branchmesh.subdivide(numSubdivisions);
		this.mMeshes.add( branchmesh );
		((Mesh) this.mMeshes.get(0)).setMaterial(new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
	}
	
	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for (Particle p: getControlParticles()){
			p.setPin(false);
			p.setRadius(width);
		}
		getControlParticles().get(0).setPin(true);
		getControlParticles().get(1).setPin(true);
		
		for (int i = 0; i<getControlParticles().size() - 1; i++){
			SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(i), getControlParticles().get(i+1), PS);
			PS.addForce(f);
			//PS.addForce(new SpringForceParticleEdge(f, PS));
			//System.out.println(getControlParticles().get(i));
			//PS.addForce(new PenaltyForceSphere(getControlParticles().get(i), PS));
		}
		
		for (int i = 1; i<getControlParticles().size() - 1; i++){
			SpringForceBendingTheta f = new SpringForceBendingTheta(getControlParticles().get(i-1), getControlParticles().get(i), getControlParticles().get(i+1), new Vector3d(0,0,0) );
			PS.addForce(f);
		}
	}
	
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);

		this.mMeshes.clear();
		Branchmesh newtree = new Branchmesh(this.getControlPoints(), height, width);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);		
	}
}
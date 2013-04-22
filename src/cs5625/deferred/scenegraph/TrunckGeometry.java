package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.Force;
import cs5625.deferred.physics.SpringForce2Particle;


public class TrunckGeometry extends Geometry
{
	private ArrayList<Particle> particleList = new ArrayList<Particle>();
	
	public TrunckGeometry(ArrayList<Point3f>list){
		for (Point3f p : list){
			this.particleList.add(new Particle(new Point3d(p)));
		}
	}

	@Override
	public void addToParticleSystem(ParticleSystem PS){
		for (Particle p: particleList){
			PS.addParticle(p);
			p.setPin(false);
		}
		particleList.get(0).setPin(true);
		//particleList.get(1).setPin(true);
		
		for (int i = 0; i<particleList.size() - 1; i++){
			SpringForce2Particle f = new SpringForce2Particle(particleList.get(i), particleList.get(i+1), PS);
			PS.addForce(f);
		}

	}
	
	@Override
	public void animate(float dt)
	{
		//super.animate(dt);
		ArrayList<Point3f>controlPoints = new ArrayList<Point3f>();
		for (Particle p: particleList){
			controlPoints.add(new Point3f(p.x));
		}
		this.mMeshes.clear();
		TreeTrunk newtree = new TreeTrunk(controlPoints, 0.5f, 0.1f);
		newtree.subdivide(2);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(new UnshadedMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
		
	}
}
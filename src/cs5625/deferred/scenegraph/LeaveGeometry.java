package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBending;
;


public class LeaveGeometry extends Geometry
{
	private int numSubdivisions = 0;
	ArrayList<Point3f>list = new ArrayList<Point3f>();
	ArrayList<Particle> particleList = new ArrayList<Particle>();
	
	public LeaveGeometry(float height, float width){
		//this.numSubdivisions = numSub;

		list.add(new Point3f(0f,0f,0f));
		list.add(new Point3f(width/2f,0f,height/4f));
		list.add(new Point3f(0f,0f,height));
		list.add(new Point3f(-width/2f,0f,height/4f));
		list.add(new Point3f(0f,-height*width/10f,height/4f));

		TreeLeave newtree = new TreeLeave(list);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(new UnshadedMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
	}

	@Override
	public void addToParticleSystem(ParticleSystem PS){
		Point3f worldPos = new Point3f();
		for (Point3f p: list){
			worldPos.set(p);
			worldPos.set(transformPointToWorldSpace(worldPos));
			particleList.add(new Particle(new Point3d(worldPos)));
		}
		
		for (Particle p: particleList){
			PS.addParticle(p);
			p.setPin(false);
			p.setRadius(0.1);
		}
//		particleList.get(0).setPin(true);
//		particleList.get(4).setPin(true);
		
		for (int i = 0; i<4; i++){
			int j = (i+1)%4;
			SpringForce2Particle f = new SpringForce2Particle(particleList.get(i), particleList.get(j), PS);
			PS.addForce(f);
		}
		SpringForce2Particle f = new SpringForce2Particle(particleList.get(0), particleList.get(4), PS);
		PS.addForce(f);
		
		f = new SpringForce2Particle(particleList.get(2), particleList.get(4), PS);
		PS.addForce(f);
		
		f = new SpringForce2Particle(particleList.get(1), particleList.get(3), PS);
		PS.addForce(f);
		


		//bending?
		SpringForceBending ff = new SpringForceBending(particleList.get(0), particleList.get(4), particleList.get(2));
		PS.addForce(ff);
		
//		ff = new SpringForceBending(particleList.get(2), particleList.get(4), particleList.get(3));
//		PS.addForce(ff);


	}
	
	@Override
	public void animate(float dt)
	{
		//super.animate(dt);
		ArrayList<Point3f>controlPoints = new ArrayList<Point3f>();
		for (Particle p: particleList){
			Point3f point = new Point3f(p.x);
			System.out.println("BEFORE TRANSFORM " + point);
			System.out.println("AFTER TRANSFORM " + this.transformPointFromWorldSpace(point));
			controlPoints.add(this.transformPointFromWorldSpace(point));
		}
		this.mMeshes.clear();
		TreeLeave newtree = new TreeLeave(controlPoints);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(new UnshadedMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
		
	}
}
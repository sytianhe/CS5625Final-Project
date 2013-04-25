package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBending;


public class TrunckGeometry extends Geometry
{
	private int numSubdivisions = 0;
	ArrayList<Particle> particleList = new ArrayList<Particle>();
	public float topWid;
	public float botWid;
	
	public TrunckGeometry(ArrayList<Point3f>list, float topwidth, float bottomwidth){
		topWid = topwidth;
		botWid = bottomwidth;
		
		for (Point3f p : list){
			this.particleList.add(new Particle(new Point3d(p)));
		}
		TreeTrunk newTrunk = new TreeTrunk(list, topwidth, topwidth);
		newTrunk.subdivide(numSubdivisions);
		this.mMeshes.add( newTrunk );
		((Mesh) this.mMeshes.get(0)).setMaterial(new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f)));		
	}

	@Override
	public void addToParticleSystem(ParticleSystem PS){
		for (Particle p: particleList){
			PS.addParticle(p);
			p.setPin(false);
		}
		particleList.get(0).setPin(true);
		particleList.get(1).setPin(true);
		
		for (int i = 0; i<particleList.size() - 1; i++){
			SpringForce2Particle f = new SpringForce2Particle(particleList.get(i), particleList.get(i+1), PS);
			PS.addForce(f);
		}
		
		for (int i = 1; i<particleList.size() - 1; i++){
			SpringForceBending f = new SpringForceBending(particleList.get(i-1), particleList.get(i), particleList.get(i+1));
			f.setStiffness(1000.0);
			PS.addForce(f);
		}
		
		for (SceneObject child : this.mChildren)
		{
			child.addToParticleSystem(PS);
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
		TreeTrunk newtree = new TreeTrunk(controlPoints, topWid, botWid);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
		
		for(SceneObject child: this.getChildren()){
			child.animate(dt);
		}
		
	}
}
package cs5625.deferred.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce1Particle;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBending;
;


public class BranchGeometry extends SceneObject
{
	ArrayList<Point3f>list = new ArrayList<Point3f>();
	TrunckGeometry branch;
	ArrayList<LeaveGeometry>leaves = new ArrayList<LeaveGeometry>();
	public BranchGeometry(float length, float width){

		float wideLeaf = 0.5f;
		float narrowLeaf = 0.1f;
		float height = 0.75f*length;
		
		int num = (int) (height*2f/(wideLeaf + narrowLeaf));
		float diff = (wideLeaf - narrowLeaf)/(num + 1f);
		float currentHeight = 0.25f*length;
		float currentWide = wideLeaf;
		
		list.add(new Point3f(0f, 0f, 0f));
		for (int i = 0; i<num; i++){
			list.add(new Point3f(0f, currentHeight, 0f));
			
			LeaveGeometry newLeaf = new LeaveGeometry(currentWide*3, currentWide);
			newLeaf.setPosition(new Point3f(0f,currentHeight,0f));
			
			LeaveGeometry newLeaf2 = new LeaveGeometry(currentWide*3, currentWide);
			newLeaf2.setPosition(new Point3f(0f,currentHeight,0f));
			newLeaf2.setOrientation(new Quat4f(0f, (float) (Math.sqrt(2.0)/2f), 0f, (float) (Math.sqrt(2.0)/2f)));
			
			currentHeight = currentHeight + currentWide;
			currentWide -= diff;

			this.leaves.add(newLeaf);
			this.leaves.add(newLeaf2);
		}
		
		this.branch = new TrunckGeometry(list, 0.01f, 0.01f);
		
		try {
			for (LeaveGeometry leaf:leaves){
				this.branch.addChild(leaf);
			}
		} catch (ScenegraphException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.addChild(this.branch);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Override
	public void addToParticleSystem(ParticleSystem PS){
		

		for (SceneObject child : this.mChildren)
		{
			child.addToParticleSystem(PS);
		}
		
//		for (int i = list.size()-1; i<list.size(); i++){
//			SpringForce1Particle ff = new SpringForce1Particle(leaves.get(2*(i-1)).particleList.get(0), this.branch.particleList.get(i).x);
//			ff.setStiffness(10000.0);
//			PS.addForce(ff);
//			
//			ff = new SpringForce1Particle(leaves.get(2*(i-1)+1).particleList.get(0), this.branch.particleList.get(i).x);
//			ff.setStiffness(10000.0);
//			PS.addForce(ff);
//		}

	}
}
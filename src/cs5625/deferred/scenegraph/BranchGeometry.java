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
		
		Point3f point = new Point3f();
		point.set(3f,0f,0f);
		list.add(new Point3f(point));
		point.set(3f,1.148f,0f);
		list.add(new Point3f(point));
		point.set(2.121f,2.121f,-0.1f);
		list.add(new Point3f(point));
		point.set(1.148f,2.771f,-0.15f);
		list.add(new Point3f(point));
		point.set(0f,3f,-0.2f);
		list.add(new Point3f(point));
		
		//list.add(new Point3f(0f, 0f, 0f));
		//for (int i = 0; i<num; i++){
			//list.add(new Point3f(0f, currentHeight, 0f));
		for (Point3f ppp: list){	
			LeaveGeometry newLeaf = new LeaveGeometry(currentWide*3, currentWide);
			newLeaf.setPosition(new Point3f(ppp));
			
			LeaveGeometry newLeaf2 = new LeaveGeometry(currentWide*3, currentWide);
			newLeaf2.setPosition(new Point3f(ppp));
			newLeaf2.setOrientation(new Quat4f(0f, (float) (Math.sqrt(2.0)/2f), 0f, (float) (Math.sqrt(2.0)/2f)));
			
			currentHeight = currentHeight + currentWide;
			currentWide -= diff;

//			this.leaves.add(newLeaf);
//			this.leaves.add(newLeaf2);
		}


		
		this.branch = new TrunckGeometry(list, 0.01f, 0.01f);
        this.branch.setIsPinned(false);
		this.branch.setOrientation(new Quat4f(1f, 1f, 0f, (float) (Math.sqrt(2.0)/2f)));

		ArrayList<Point3f>list2 = new ArrayList<Point3f>();

		point.set(3f,0f,0f);
		list2.add(new Point3f(point));
		point.set(3f,-1.148f,0f);
		list2.add(new Point3f(point));
		point.set(2.121f,-2.121f,-0.1f);
		list2.add(new Point3f(point));
		point.set(1.148f,-2.771f,-0.15f);
		list2.add(new Point3f(point));
		point.set(0f,-3f,-0.2f);
		list2.add(new Point3f(point));
		TrunckGeometry newbrunh = new TrunckGeometry(list2, 0.01f, 0.01f);
		newbrunh.setOrientation(new Quat4f(1f, 1f, 0f, (float) (Math.sqrt(2.0)/2f)));
		
		try {
			this.addChild(this.branch);
			this.addChild(newbrunh);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			for (LeaveGeometry leaf:leaves){
				this.branch.addChild(leaf);
			}
		} catch (ScenegraphException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



	}
	@Override
	public void addToParticleSystem(ParticleSystem PS){
		

		for (SceneObject child : this.mChildren)
		{
			child.addToParticleSystem(PS);
		}
		
	}
	
}
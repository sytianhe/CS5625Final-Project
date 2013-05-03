package cs5625.deferred.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.LambertianMaterial;
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


public class Stem extends PhysicsGeometry
{
	private int numSubdivisions = 1;
	private Material material = new LambertianMaterial(new Color3f(110f/255f , 139f/255f, 61f/255f));
	private float bottomtopWidth  = 0.1f;
	private float topWidth = 0.01f;
	
	public Stem(ArrayList<Point3f>list){
		ArrayList<Point3f>newList = new ArrayList<Point3f>();
		for (Point3f p: list){
			newList.add(new Point3f(p.x, p.y, p.z));
		}
		this.addControlPoints(newList);
		Branchmesh branchmesh = new Branchmesh(newList, bottomtopWidth, topWidth);
		branchmesh.subdivide(numSubdivisions);
		this.mMeshes.add( branchmesh );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
		
//        for (Point3f pt :  newList ){
//        	if (! pt.equals(newList.get(0)) && ! pt.equals(newList.get(1))){
//        		Leaf leaf1 = new Leaf(4f, 0.35f);
//        		Leaf leaf2 = new Leaf(4f, 0.35f);
//        		leaf2.setOrientation(new Quat4f(0,1,0,0));
//        		this.pinToPhysicsGeometry(leaf1,pt);
//        		this.pinToPhysicsGeometry(leaf2,pt);
//        	}
//        }
        for (int i =0; i<newList.size(); i++ ){
        	//if (i>newList.size()/4){
        		double a = 0.9;
        		double b = 0.1;
        		double temp = (double)((i-4) * 1.0/(newList.size()/2.0) - 1.0);
        		float temp2 = (float) (Math.sqrt(1.0 - Math.pow(temp/a, 2))*b)*3;
        		Leaf leaf1 = new Leaf(4f*temp2, 0.5f*temp2);
        		Leaf leaf2 = new Leaf(4f*temp2, 0.5f*temp2);
        		leaf2.setOrientation(new Quat4f(0,1,0,0));
        		this.pinToPhysicsGeometry(leaf1,newList.get(i));
        		this.pinToPhysicsGeometry(leaf2,newList.get(i));
        	//}
        }
	}
	
	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for (Particle p: getControlParticles()){
			p.setPin(false);
			p.setRadius(topWidth);
		}
		getControlParticles().get(0).setPin(true);
		getControlParticles().get(1).setPin(true);
		
		for (int i = 0; i<getControlParticles().size() - 1; i++){
			SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(i), getControlParticles().get(i+1), PS);
			PS.addForce(f);
		}
		
		for (int i = 1; i<getControlParticles().size() - 1; i++){
			SpringForceBendingTheta f = new SpringForceBendingTheta(getControlParticles().get(i-1), getControlParticles().get(i), getControlParticles().get(i+1), new Vector3d(0,0,0) );
			f.setStiffness(2500.0);
			PS.addForce(f);
		}
	}
	
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);

		this.mMeshes.clear();
		Branchmesh newtree = new Branchmesh(this.getControlPoints(), bottomtopWidth, topWidth);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);		
	}
}
package cs5625.deferred.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import cs5625.deferred.materials.BarkMaterial;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;
import cs5625.deferred.physics.SpringForceBendingTheta;
import cs5625.deferred.physics.SpringForceParticleEdge;
import cs5625.deferred.scenegraph.Mesh;
;


public class Branch extends PhysicsGeometry
{
	private int numSubdivisions = 1;

	//private BlinnPhongMaterial material = new BlinnPhongMaterial(new Color3f( 205f/255f , 133f/255f, 63f/255f));
	//private BlinnPhongMaterial material = new BlinnPhongMaterial(new Color3f( 1f , 1f, 1f));
	private BarkMaterial material = new BarkMaterial(new Color3f( 1f , 1f, 1f));
	private float baseRadius  = 0.25f;
	private float tipRadius = 0.1f;
	public Particle topPoint;
	
	public Branch(ArrayList<Point3f>list,float baseRadius, float tipRadious, int numSubdivisions ){
		this.addControlPoints(list);
		this.baseRadius = baseRadius;
		this.tipRadius = tipRadius;
		this.numSubdivisions = numSubdivisions;
		Branchmesh branchmesh = new Branchmesh(list, baseRadius, tipRadius);		
		branchmesh.subdivide(numSubdivisions);
		branchmesh.calculateTangentVectors();
		this.mMeshes.add( branchmesh );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
	}
	
	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for (Particle p: getControlParticles()){
			p.setPin(false);
			p.setRadius(baseRadius);
		}
		//Make the branch rigid 
		getControlParticles().get(0).setPin(true);
		getControlParticles().get(1).setPin(true);
		
		for (int i = 0; i<getControlParticles().size() - 1; i++){
			SpringForce2Particle f = new SpringForce2Particle(getControlParticles().get(i), getControlParticles().get(i+1), PS);
			PS.addForce(f);
			for (PhysicsGeometry pg : getInteractsWith()){
				PS.addForce(new SpringForceParticleEdge(pg.getOriginParticle(),f ,PS) );
			}
		}
		
		for (int i = 1; i<getControlParticles().size() - 1; i++){
			SpringForceBendingTheta f = new SpringForceBendingTheta(getControlParticles().get(i-1), getControlParticles().get(i), getControlParticles().get(i+1), new Vector3d(-0.05 + Math.random()*0.1,-0.05 + Math.random()*0.1,-0.05 + Math.random()*0.1) );
			//SpringForceBending f = new SpringForceBending(getControlParticles().get(i-1), getControlParticles().get(i), getControlParticles().get(i+1));
			PS.addForce(f);
		}
		
		topPoint = getControlParticles().get(getControlParticles().size() - 1);
		
	}
	
	public Particle topPoint(){
		return topPoint;
	}
	
	public Texture2D getDiffuseTexture()
	{
		return material.getDiffuseTexture();
	}
	
	public void setDiffuseTexture(Texture2D texture)
	{
		material.setDiffuseTexture(texture);
	}
	
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);

		this.mMeshes.clear();
		Branchmesh newMesh = new Branchmesh(this.getControlPoints(), baseRadius, tipRadius);
		newMesh.subdivide(numSubdivisions);
		newMesh.calculateTangentVectors();
		this.mMeshes.add( newMesh );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);	
	}
}
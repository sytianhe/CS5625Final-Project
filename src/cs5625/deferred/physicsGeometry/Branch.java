package cs5625.deferred.physicsGeometry;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.Material;
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
	private int numSubdivisions = 3;

	//private Material material = new VertexLambertianMaterial(new Color3f( 205f/255f , 133f/255f, 63f/255f), (float)Math.random(), (float)Math.random());
	private float height  = 0.25f;
	private float width = 0.1f;
	private BlinnPhongMaterial material = new BlinnPhongMaterial(new Color3f( 205f/255f , 133f/255f, 63f/255f));
	private float bottomRadius  = 0.25f;
	private float topRadius = 0.1f;
	
	public Branch(ArrayList<Point3f>list){
		this.addControlPoints(list);
		Branchmesh branchmesh = new Branchmesh(list, bottomRadius, topRadius);		
		branchmesh.subdivide(numSubdivisions);
		this.mMeshes.add( branchmesh );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);
	}
	
	@Override
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for (Particle p: getControlParticles()){
			p.setPin(false);
			p.setRadius(bottomRadius);
		}
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
			f.setStiffness(10000.0);
			PS.addForce(f);
		}
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
		Branchmesh newtree = new Branchmesh(this.getControlPoints(), bottomRadius, topRadius);
		newtree.subdivide(numSubdivisions);
		this.mMeshes.add( newtree );
		((Mesh) this.mMeshes.get(0)).setMaterial(material);	
		//((VertexLambertianMaterial) material).setPhi((float)Math.random());
	}
}
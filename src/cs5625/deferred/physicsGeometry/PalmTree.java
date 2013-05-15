package cs5625.deferred.physicsGeometry;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.SpringForce2Particle;

/**
 * PalmTree class provides a simple wrapper for the numerous pieces of geometry that compose our palm PalmTree. 
 * 
 * @author homoflashmanicus, tianhe
 *
 */
public class PalmTree extends PhysicsGeometry {

//	private float height;
//	private float baseWidth;
//	private float topWidth;
//	private float curviness;
//	private float nFronds;
//	private float frondLength;
//	private int nLeavesPerFrond;
//	private int levelOfDetail; 
//	private Texture2D barkTexture;
	public Branch trunk;
	public SpringForce2Particle targetf;
	public boolean alreadyAdd = false;
	
	/**
	 * PalmTree constructor 
	 * @param height Height of trunk (not including fronds)
	 * @param baseWidth 
	 * @param topWidth
	 * @param curviness Amount of non linearity in transition from  the basewidth to top width of the trunk
	 * @param nFronds 
	 * @param nLeavesPerFrond
	 * @param barkTexture Swap in different bark textures to get different appearences 
	 */
	public PalmTree(float height, float baseWidth,  float topWidth, float curviness, float frondLength, int nFronds,  int nLeavesPerFrond, int levelOfDetail, Texture2D barkTexture){
		
//		this.height = height;
//		this.baseWidth = baseWidth;
//		this.topWidth = topWidth;
//		this.curviness = curviness ;
//		this.nFronds = nFronds ;
//		this.frondLength = frondLength ;
//		this.nLeavesPerFrond = nLeavesPerFrond ;
//		this.levelOfDetail = levelOfDetail ; 
		

		
        //ADD MAIN TRUNK
        trunk = new Branch((int)height, baseWidth, topWidth, levelOfDetail);
		trunk.setDiffuseTexture(barkTexture);
		this.pinToPhysicsGeometry(trunk, new Point3f(0.0f, 0.0f, 0.0f));
                
        Point3f topPoint = trunk.topPoint();
		
        //CREATE FRONDS
        
        for (int i = 0; i<nFronds; i++){
            Frond stem = new Frond(nLeavesPerFrond, levelOfDetail-1);
            // FIND THE RIGHT QUATERNION TO MAINTAIN THE PalmTree LEAVES FACING UP
            float rand1 = (float) Math.random();
            float rand2 = (float) Math.random()*0.35f;

            Quat4f rotY = new Quat4f(0,(float) Math.sin(rand1* Math.PI),0,(float) Math.cos(rand1* Math.PI));                
            Quat4f rotZ = new Quat4f(0,0,(float) Math.sin(rand2* Math.PI),(float) Math.cos(rand2* Math.PI));

            rotY.mul(rotZ);
        	stem.setOrientation(rotY);
            trunk.pinToPhysicsGeometry(stem, topPoint);
        }	
        
	}
	
	public void addForce(Particle p, ParticleSystem PS){
		if(!alreadyAdd){
			targetf = new SpringForce2Particle(trunk.topParticle(), p, 3.0, PS);
			targetf.setStiffness(100);
			PS.addForce(targetf);
			alreadyAdd = true;
		}
	}
	
	public void removeForce(Particle p, ParticleSystem PS){
		if (alreadyAdd){
			PS.removeForce(targetf);
			alreadyAdd = false;			
		}
	}
	
//	@Override 
//	public void animateHelper(){
//		
//	}
	
}

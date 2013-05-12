package cs5625.deferred.physics;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;



/**
 * Local repulsion force. 
 * @author homoflashmanicus
 *
 */
public class PenaltyForceSphere implements Force {
		
	/**Strength of repulsion force. */
	static double c =  Constants.STIFFNESS_STRETCH;
	
	/**Length scale of repuslion force. */
	static double h =   Constants.EDGE_COEFF;
	
	/** Allocate storage for force computation. */
	Vector3d sep = new Vector3d();
	Particle p1; 
	ParticleSystem PS;
	
	public PenaltyForceSphere(Particle p1 , ParticleSystem ps){
		this.p1=p1;
		this.PS=ps;		
	}

	
	//@Override
	public void applyForce() {
		for (Particle p2 : PS.P ){
			if ( ! p2.equals(p1) ){
				applyForce(p1,p2);
			}
		}
	}
	

	
	public void applyForce(Particle p1, Particle p2) {		
		double r = p1.x.distance(p2.x) - p1.getRadius() - p2.getRadius();
		if (r<h){
			sep.sub(p1.x,p2.x);
			sep.normalize();
			double f = c*Math.pow((h-r),2);
			p1.f.scaleAdd(f, sep, p1.f);
			p2.f.scaleAdd(-f, sep, p2.f);
		}
	}

	@Override
	public void display(GL2 gl) {

	}


	public ArrayList<Particle> getParticles() {
		// TODO Auto-generated method stub
		return PS.P;
	}

	

	public boolean contains(Particle p) {
		return PS.P.contains(p);
	}

}

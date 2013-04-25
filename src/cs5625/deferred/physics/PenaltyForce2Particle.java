package cs5625.deferred.physics;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;



/**
 * Local repulsion force. 
 * @author homoflashmanicus
 *
 */
public class PenaltyForce2Particle implements Force {
	
	ArrayList<Particle> PS;
	
	/**Strength of repulsion force. */
	static double c =  0.0001* Constants.STIFFNESS_STRETCH;
	
	/**Length scale of repuslion force. */
	static double h =   Constants.EDGE_COEFF;
	
	/** Allocate storage for force computation. */
	Vector3d sep = new Vector3d();
	Particle p1; 
	Particle p2; //apply penatly to p2
	
	public PenaltyForce2Particle(Particle p1 , Particle p2){
		this.p1=p1;
		this.p2=p2;		
	}

//	//@Override
//	public void applyForce() {
//		for (Particle p1 : particles1){
//			for (Particle p2 : particles2){
//				applyForce(p1,p2);
//			}
//		}
//	}
	

	
	public void applyForce() {		
		double r = p1.x.distance(p2.x) - p1.getRadius() - p2.getRadius();
		if (r<h ){
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
		return PS;
	}

	
	public void applyForce(Particle p1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Particle p) {
		return PS.contains(p);
	}

}

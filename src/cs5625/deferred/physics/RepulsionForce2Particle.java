package cs5625.deferred.physics;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;



/**
 * Local repulsion force. 
 * @author homoflashmanicus
 *
 */
public class RepulsionForce2Particle implements Force {
	
	ArrayList<Particle> PS;
	
	/**Strength of repulsion force. */
	static double c = Constants.STIFFNESS_STRETCH;
	
	/**Length scale of repuslion force. */
	static double h = Constants.EDGE_COEFF;
	
	/** Allocate storage for force computation. */
	Vector3d sep = new Vector3d();
	ArrayList<Particle> particles= new ArrayList<Particle>();

	
	public RepulsionForce2Particle(ArrayList<Particle> ps){
		PS=ps;
	}

	//@Override
	public void applyForce() {
		for (Particle p1 : PS){
			for (Particle p2 : PS){
			
			}
		}
	}
	

	
	public void applyForce(Particle p1, Particle p2) {
		double r = p1.x.distance(p2.x) - p1.getRadius() - p2.getRadius();
		if (r<h){
			sep.sub(p1.x,p2.x);
			sep.normalize();
			double f = c*Math.pow((h-r),1);
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

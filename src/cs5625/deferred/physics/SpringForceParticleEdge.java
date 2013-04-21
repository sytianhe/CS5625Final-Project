package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;


/** 
 * Spring force between two particles, with spring-spring overlap tests.
 * 
 * @author Doug James, January 2007 (Revised Feb 2009)
 */
public class SpringForceParticleEdge implements Force
{	
	/** Free Point */
	public Particle p1;
	
	/** Left point of edge. */
	public Particle p2;
	
	/** Right point of edge. */
	public Particle p3;
	
	/** SpringForce2Particle. */
	public SpringForce2Particle f;
	
	/** Soft edge thickness. */
	double h = Constants.EDGE_COEFF;
	
	/** Penalty force strength, relative to spring stiffness coefficient. */
	double REL_STRENGTH = 3;
	
	ParticleSystem PS;

	SpringForceParticleEdge(Particle p1, SpringForce2Particle ff)
	{
		if(p1==null || ff == null) throw new NullPointerException("p1="+p1+", p2="+p2 +"p3="+p3);
		this.f = ff;
		this.p1 = p1;
		this.p2 = ff.p1;
		this.p3 = ff.p2;

		this.PS = PS;
	}

	public void applyForce()
	{
		if(p1.isPinned() && p2.isPinned() && p3.isPinned()) return;/// no force
		
		//EVALUATE SEPERATION VECTORS
		Vector3d a = new Vector3d();
		Vector3d b = new Vector3d();
		
		a.sub(p3.x,p2.x);			//between first and second edge point
		b.sub(p1.x,p2.x);			//between particle and first edge point

		//COMPUTE NORMAL VECTOR IN THE DIRECTION OF P
		Vector3d n = new Vector3d();

		n.set(a);
		n.scaleAdd(-n.dot(b), a, b);
		n.scale(Math.signum(b.dot(n)));
		n.normalize();		
		
		//COMPUTE BARCENTRIC COORDINATE OF POINT ABOVE EDGE
		double alpha = a.dot(b)/a.dot(a);
		
		//THREE CASES:
		//CHECK IF PARTICLE IS ABOVE EDGE
		if(0<=alpha && alpha <=1 ){
			
			//COMPUTE RELATIVE VELOCITY AT POINT ABOVE EDGE
			Vector3d v = new Vector3d(p1.v);
			v.scaleAdd(-1+alpha, p2.v, v);
			v.scaleAdd(-alpha, p3.v,v);
			
			//COMPUTE TANGENT VELOCITY DIRECTION FOR FRICTION TERM
			Vector3d bn = new Vector3d(v);
			bn.scaleAdd(-v.dot(n), n, bn);
			if(bn.length()>0) bn.normalize();
			
			//SEPERATION FROM EDGE
			double d = b.dot(n);

			//IF CLOSE ENOUGH AND TRAVELING TOWARD, APPLY FORCES
			if(d<=h){
				if (v.dot(n)<0){
					//REPULSION FORCE
					p1.f.scaleAdd(REL_STRENGTH * Constants.STIFFNESS_STRETCH * (h-d) + Constants.DAMPING_MASS*v.dot(n), n , p1.f);
				}
				if (bn.length()>0 && p1.f.dot(n)<0){	
					//KINETIC FRICTION FORCE
					p1.f.scaleAdd(Constants.KINETIC_FRICTION * p1.f.dot(n) , bn, p1.f);			
				}
//				if (bn.length()==0 && p1.f.dot(n)<0){
//					//STATIC FRICTION FORCE
//					bn.set(p1.f);  //compute normal from force 
//					bn.scaleAdd(-p1.f.dot(n), n, bn);
//				}
			}			
		}
		//CHECK IF PARTICLE IS NEAR THE LEFT EDGE ENDPOINT
		else if(p1.x.distance(p2.x)<h){
			//COMPUTE NORMAL VECTOR AND RELATIVE VELOCITY OF THE TWO POINTS
			n.sub(p1.x,p2.x);
			Vector3d v = new Vector3d(p1.v);
			v.sub(p2.v);
			//SEPERATION BETWEEN POINTS
			double d = p1.x.distance(p2.x);
			if(v.dot(n)<0){
				p1.f.scaleAdd(REL_STRENGTH * Constants.STIFFNESS_STRETCH * (h-d) + Constants.DAMPING_MASS*v.dot(n) , n , p1.f);
			}
		}
		//CHECK IF PARTICLE IS NEAR THE RIGHT EDGE ENDPOINT
		else if(p1.x.distance(p3.x)<h){
			//COMPUTE NORMAL VECTOR AND RELATIVE VELOCITY OF THE TWO POINTS
			n.sub(p1.x,p3.x);
			Vector3d v = new Vector3d(p1.v);
			v.sub(p3.v);
			
			//SEPERATION BETWEEN POINTS
			double d = p1.x.distance(p3.x);
			if(v.dot(n)<0){
				p1.f.scaleAdd(REL_STRENGTH * Constants.STIFFNESS_STRETCH * (h-d) + Constants.DAMPING_MASS*v.dot(n) , n , p1.f);
			}
		}
	}

	public void display(GL2 gl)
	{

	}

	public boolean contains(Particle p)  { 
		return ((p==p1) || (p==p2));
	}
}

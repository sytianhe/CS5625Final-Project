package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;


/** 
 * Spring force between two particles, with spring-spring overlap tests.
 * 
 * 2d base code provided by
 * @author Doug James, January 2007 (Revised Feb 2009)
 * 
 * updated to 3d by
 * @author homoflashmanicus
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
	double REL_STRENGTH = 20;
	
	ParticleSystem PS;

	public SpringForceParticleEdge(Particle p1, SpringForce2Particle ff, ParticleSystem PS)
	{
		this.f = ff;
		this.p1 = p1;
		this.p2 = ff.p1;
		this.p3 = ff.p2;

		this.PS = PS;
	}

	public void applyForce()
	{
		
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
		
		//COMPUTE SEPERATION DISTANCES

		double d = b.dot(n) - ( p1.getRadius() + alpha * p2.getRadius() + (1-alpha) * p3.getRadius() );
		double dl = p1.x.distance(p2.x) - ( p1.getRadius() + p2.getRadius() );
		double dr = p1.x.distance(p3.x) - ( p1.getRadius() + p3.getRadius() );
		
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

			//IF CLOSE ENOUGH AND TRAVELING TOWARD, APPLY FORCES
			if(d<=h){
				if (v.dot(n)<0){
					//REPULSION FORCE
					Vector3d f = new Vector3d(n);
					n.scale(REL_STRENGTH * Constants.STIFFNESS_STRETCH * Math.pow((h-d),2));
					p1.f.add(f, p1.f);
					p2.f.scaleAdd(-alpha, f, p2.f);
					p3.f.scaleAdd(-(1-alpha),f, p3.f);
				}
				if (bn.length()>0 && p1.f.dot(n)<0){	
					//KINETIC FRICTION FORCE
					Vector3d f = new Vector3d(bn);
					f.scale(Constants.KINETIC_FRICTION * p1.f.dot(n));
					p1.f.add(f, p1.f);	
					p2.f.scaleAdd(-alpha , f, p1.f);
					p3.f.scaleAdd(-(1-alpha),f, p3.f);
				}
				
			}			
		}
		//CHECK IF PARTICLE IS NEAR THE LEFT EDGE ENDPOINT
		else if(dl<h){
			//COMPUTE NORMAL VECTOR AND RELATIVE VELOCITY OF THE TWO POINTS
			n.sub(p1.x,p2.x);
			Vector3d v = new Vector3d(p1.v);
			v.sub(p2.v);
			//SEPERATION BETWEEN POINTS
			if(v.dot(n)<0){
				Vector3d f = new Vector3d(n);
				f.scale(REL_STRENGTH * Constants.STIFFNESS_STRETCH * Math.pow((h-dl),2)+ Constants.DAMPING_MASS*v.dot(n));
				p1.f.add(f , p1.f);
				p2.f.scaleAdd(-1, f, p2.f);
			}
		}
		//CHECK IF PARTICLE IS NEAR THE RIGHT EDGE ENDPOINT
		else if(dr<h){
			//COMPUTE NORMAL VECTOR AND RELATIVE VELOCITY OF THE TWO POINTS
			n.sub(p1.x,p3.x);
			Vector3d v = new Vector3d(p1.v);
			v.sub(p3.v);
			
			//SEPERATION BETWEEN POINTS
			if(v.dot(n)<0){
				Vector3d f = new Vector3d(n);
				f.scale(REL_STRENGTH * Constants.STIFFNESS_STRETCH * Math.pow((h-dr),2)+ Constants.DAMPING_MASS*v.dot(n));
				p1.f.add(f , p1.f);
				p3.f.scaleAdd(-1, f, p3.f);

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

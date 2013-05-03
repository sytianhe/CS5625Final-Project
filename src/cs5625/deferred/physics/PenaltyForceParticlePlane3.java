package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;


/** 
 * Spring force between two particles, with spring-spring overlap tests.
 * 
 * @author Doug James, January 2007 (Revised Feb 2009)
 */
public class PenaltyForceParticlePlane3 implements Force
{	
	/** Free Point */
	public Particle p1;
	
	public Particle f1;
	public Particle f2;
	public Particle f3;
		
	/** Soft edge thickness. */
	double h = 0.1* Constants.EDGE_COEFF;
	
	/** Penalty force strength, relative to spring stiffness coefficient. */
	double REL_STRENGTH = 100;
	
	ParticleSystem PS;

	public PenaltyForceParticlePlane3(Particle p1, Particle f1, Particle f2, Particle f3, ParticleSystem ps)
	{
		this.p1 = p1;
		this.f1 = f1;
		this.f2 = f2;
		this.f3 = f3;
		this.PS = ps;
	}

	public void applyForce(){
		for (Particle p : PS.P){
			applyForce(p);
		}
	}
	
	public void applyForce(Particle p1)
	{
		if(p1.isPinned() && f1.isPinned() && f2.isPinned()) return;/// no force
		
		
		//EVALUATE SEPERATION VECTORS
		Vector3d p = new Vector3d();
		Vector3d a = new Vector3d();
		Vector3d b = new Vector3d();
		
		p.sub(p1.x,f1.x);			//between particle and first face point
		a.sub(f2.x,f1.x);			//between first and second face point
		b.sub(f3.x,f1.x);			//between first and third face point
		

		//COMPUTE NORMAL VECTOR IN THE DIRECTION OF P
		Vector3d n = new Vector3d();
		n.cross(a,b);
		n.normalize();
		if (n.dot(p) < 0) n.scale(-1);
		//PROJECT p ONTO THE PLANE
		Vector3d t = new Vector3d(); //position of particle projected onto the plane
		Vector3d pn = new Vector3d(); //seperation vector from plain to face
		pn.set(n);
		pn.normalize();
		pn.scale(pn.dot(p));
		t.sub(p,pn);
		
		//COMPUTE BARCENTRIC COORDINATE OF POINT ABOVE EDGE
		double adota = a.dot(a);
		double bdotb = b.dot(b);
		double adotb = a.dot(b);
		double det = adota * bdotb - 2 * adotb;
		double adott = a.dot(t);
		double bdott = b.dot(t);
		double alpha = (bdotb * adott - adotb * bdott)/det;
		double beta = (-adotb * adott + adota * bdott)/det;
		
//		double alpha = a.dot(b)/a.dot(a);
		
//		System.out.println("ALPHA: " + alpha + " BETA: " + beta );
		
		//THREE CASES:
		//CHECK IF PARTICLE IS ABOVE EDGE
		if(0<alpha && alpha <1 && 0<beta && beta < 1){
			
			//COMPUTE RELATIVE VELOCITY AT POINT ABOVE EDGE
			Vector3d v = new Vector3d(p1.v);
			v.scaleAdd(-1+alpha+beta, f1.v, v);
			v.scaleAdd(-alpha, f2.v,v);
			v.scaleAdd(-beta, f3.v,v);
			
			//COMPUTE TANGENT VELOCITY DIRECTION FOR FRICTION TERM
			Vector3d bn = new Vector3d(v);
			bn.scaleAdd(-v.dot(n), n, bn);
			if(bn.length()>0) bn.normalize();
			
			//SEPERATION FROM EDGE
			double d = pn.length() - p1.getRadius();
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
			}			
		}
	}

	public void display(GL2 gl)
	{

	}

	public ParticleSystem getParticleSystem() { return PS; }

	public boolean contains(Particle p)  { 
		return ((p==p1) || (p==f1));
	}
}

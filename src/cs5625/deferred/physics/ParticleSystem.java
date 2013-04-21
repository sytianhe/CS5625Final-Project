package cs5625.deferred.physics;

import java.util.*;

import javax.vecmath.*;
import javax.media.opengl.*;


/**
 * Maintains dynamic lists of Particle and Force objects, and provides
 * access to their state for numerical integration of dynamics.
 * <pre>
 * Symplectic-Euler integrator is hard-coded, with embedded collision
 * processing code.
 * </pre>
 * 
 * Starter code provided for CS5643
 * @author Doug James, January 2007 (revised Feb 2009)
 * 
 * Extended by
 * @author homoflashmanicus
 */
public class ParticleSystem 
{
	/** Current simulation time. */
	double time = 0;

	/** List of Particle objects. */
	public ArrayList<Particle>   P = new ArrayList<Particle>();

	/** List of Force objects. */
	public ArrayList<Force>      F = new ArrayList<Force>();
	
	
	/** Number of edges (springs). */
	int nEdges = 0;
		
	/** Sorting the particles. */
	static ParticleSort cmp = new ParticleSort();
	
	/** Basic constructor. */
	public ParticleSystem() {  }

	/** Adds a force object (until removed) */
	public synchronized void addForce(Force f) {
		F.add(f);
	}

	/** Useful for removing temporary forces, such as user-interaction
	 * spring forces. */
	public synchronized void removeForce(Force f) {
		F.remove(f);
	}
	
	
	

	/** Creates particle and adds it to the particle system. 
	 * @param p0 Undeformed/material position. 
	 * @return Reference to new Particle.
	 */
	int counterForPin = 0;
	public synchronized Particle createParticle(Point3d p0) 
	{
		Particle newP = new Particle(p0);
		P.add(newP);
		return newP;
	}

	/** Adds a particle object */
	public synchronized void addParticle(Particle p) {
		P.add(p);
	}

	
	/** Removes particle and any attached forces from the ParticleSystem.
	 * @param p Particle
	 */
	public void removeParticle(Particle p) 
	{
		P.remove(p);

		ArrayList<Force> removalList = new ArrayList<Force>();
		for(Force f : F) {/// REMOVE f IF p IS USED IN FORCE
			if(f.contains(p))  removalList.add(f);
		}

		F.removeAll(removalList);
	}

	/** 
	 * Helper-function that computes nearest particle to the specified
	 * (deformed) position.
	 * @return Nearest particle, or null if no particles. 
	 */
	public synchronized Particle getNearestParticle(Point3d x)
	{
		Particle minP      = null;
		double   minDistSq = Double.MAX_VALUE;
		for(Particle particle : P) {
			double distSq = x.distanceSquared(particle.x);
			if(distSq < minDistSq) {
				minDistSq = distSq;
				minP = particle;
			}
		}
		return minP;
	}

	/** 
	 * Helper-function that computes nearest particle to the specified
	 * (deformed) position.
	 * @return Nearest particle, or null if no particles. 
	 * @param pinned If true, returns pinned particles, and if false, returns unpinned
	 */
	public synchronized Particle getNearestPinnedParticle(Point3d x, boolean pinned)
	{
		Particle minP      = null;
		double   minDistSq = Double.MAX_VALUE;
		for(Particle particle : P) {
			if(particle.isPinned() == pinned) {
				double distSq = x.distanceSquared(particle.x);
				if(distSq < minDistSq) {
					minDistSq = distSq;
					minP = particle;
				}
			}
		}
		return minP;
	}

	/** Moves all particles to undeformed/materials positions, and
	 * sets all velocities to zero. Synchronized to avoid problems
	 * with simultaneous calls to advanceTime(). */
	public synchronized void reset()
	{
		for(Particle p : P)  {
			p.x.set(p.x0);
			p.v.set(0,0,0);
			p.f.set(0,0,0);
			p.setHighlight(false);
		}

		/// WORKAROUND FOR DANGLING MOUSE-SPRING FORCES AFTER PS-INTERNAL RESETS:
		ArrayList<Force> removeF = new ArrayList<Force>();
		for(Force f : F) {
			if(f instanceof SpringForce1Particle) removeF.add(f);
		}
		F.removeAll(removeF);

		time = 0;
	}


	/**
	 * MAIN FUNCTION TO IMPLEMENT YOUR ROBUST COLLISION PROCESSING ALGORITHM.
	 */
	public synchronized void advanceTime(double dt)
	{						

		{/// GATHER BASIC FORCES (NO NEED TO MODIFY):

			/// CLEAR FORCE ACCUMULATORS:
			for(Particle p : P)  p.f.set(0,0,0);

			/// APPLY FORCES:
			for(Force force : F) 
				force.applyForce();

			// GRAVITY:
			for(Particle p : P)   p.f.y -= p.m * 10;

			// ADD SOME MASS-PROPORTIONAL DAMPING (DEFAULT IS ZERO)
			for(Particle p : P) 
				Utils.acc(p.f,  -Constants.DAMPING_MASS * p.m * 0, p.v);
		}


		///////////////////////////////////////////////
		/// SYMPLECTIC-EULER TIME-STEP w/ COLLISIONS:
		///////////////////////////////////////////////
		///////////////////////////////////////////////
		/// 1. UPDATE PREDICTOR VELOCITY WITH FORCES
		///////////////////////////////////////////////
		for(Particle p : P) {
			/// APPLY PIN CONSTRAINTS (set p=p0, and zero out v):
			if(p.isPinned()) {
				p.v.set(0,0,0);
			}
			else {
				p.v.scaleAdd(dt/p.m, p.f, p.v); // v += dt * f/m;
			}

			/// CLEAR FORCE ACCUMULATOR
			p.f.set(0,0,0);
		}
		/////////////////////////////////////////////////////////
		/// 2.  APPLY PENALTY FORCES TO KEEP THINGS SEPERATED////
		/////////////////////////////////////////////////////////
//		for(Particle p : P){
//			for(Force f: F){
//				if( f instanceof SpringForce2Particle ){
//					SpringForce2Particle ff = (SpringForce2Particle) f ;
//					if(!ff.contains(p)){
//						SpringForceParticleEdge pe = new SpringForceParticleEdge(p,ff);
//						pe.applyForce();
//					}
//				}
//			}
//		}


		//////////////////////////////////////////////////////////
		/// 3. ADVANCE POSITIONS USING COLLISION-FEASIBLE VELOCITY
		//////////////////////////////////////////////////////////
		for(Particle p : P) {
			p.x.scaleAdd(dt, p.v, p.x); //p.x += dt * p.v;
		}

		time += dt;
	}

	/**
	 * Displays Particle and Force objects.
	 */
	public synchronized void display(GL2 gl) 
	{
		for(Force force : F) {
			force.display(gl);
		}

		for(Particle particle : P) {
			particle.display(gl);
		}
	}

}

/**
 * This class is for sorting particles.
 * @author tz249
 *
 */
class ParticleSort implements Comparator<Particle> {
	int index = 1;
	public ParticleSort(){ }
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int compare(Particle p1, Particle p2){
		double v1;
		double v2;
		if (index == 0){
			v1 = p1.x.x;
			v2 = p2.x.x;
		}
		else{
			v1 = p1.x.y;
			v2 = p2.x.y;
		}
		if(v1 < v2) return 1;
		if(v1 > v2) return -1;
		return 0;
	}
}
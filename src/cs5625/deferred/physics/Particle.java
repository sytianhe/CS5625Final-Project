package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;



/** 
 * Simple particle implementation.
 * @author Doug James, January 2007
 * 
 * Adapted to cs5625 by 
 * @author homoflashmanicus
 */
public class Particle
{
	/** Radius of particle's circle graphic. */
	private  double PARTICLE_RADIUS = 0.1;


	/** If true, then particle is pinned in space. */
	private boolean pin = true;
		
	/** Default mass. */
	double   m = Constants.PARTICLE_MASS;

	/** Deformed Position. */
	public Point3d  x = new Point3d();
	
	/** Undeformed/material Position. */
	public Point3d  x0 = new Point3d();

	/** Velocity. */
	public Vector3d v = new Vector3d();

	/** Force accumulator. */
	public Vector3d f = new Vector3d();
	
	/** Another particle to which this particle is pinned. */
	public Particle p2;

	/** 
	 * Constructs particle with the specified material/undeformed
	 * coordinate, p0.
	 */
	public Particle(Point3d x0) 
	{
		this.x0.set(x0);
		x.set(x0);
	}

	/** Specifies whether or not this particle is fixed in space via a
	 * pin constraint. (Should probably be elsewhere in a generic
	 * constraint list). */
	public void setPin(boolean fix) { pin = fix; }

	/** Returns true if currently pinned. */
	public boolean isPinned() { return pin; }

	public double getRadius() {return PARTICLE_RADIUS ; };

	public void setRadius(double r) {PARTICLE_RADIUS = r ; };	
	
	public void setMass(double m) {this.m = m ; };	
	

}

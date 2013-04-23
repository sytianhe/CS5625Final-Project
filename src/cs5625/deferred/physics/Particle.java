package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;



/** 
 * Simple particle implementation.
 * 
 * @author Doug James, January 2007
 */
public class Particle
{
	/** Radius of particle's circle graphic. */
	private static double PARTICLE_RADIUS = 1;/// (NOT USED)

	/** Display list index. */
	private static int PARTICLE_DISPLAY_LIST = -1;

	/** Highlighted appearance if true, otherwise white. */
	private boolean highlight = false;

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

	/** 
	 * Constructs particle with the specified material/undeformed
	 * coordinate, p0.
	 */
	public Particle(Point3d x0) 
	{
		this.x0.set(x0);
		x.set(x0);
	}
	
	/** Draws circular particle using a display list. */
	public void display(GL2 gl)
	{
		if(PARTICLE_DISPLAY_LIST < 0) {// MAKE DISPLAY LIST:
			int displayListIndex = gl.glGenLists(1);
			gl.glNewList(displayListIndex, GL2.GL_COMPILE);
			drawParticle(gl, new Point3d(), PARTICLE_RADIUS);///particle at origin
			gl.glEndList();
			//System.out.println("MADE LIST "+displayListIndex+" : "+gl.glIsList(displayListIndex));
			PARTICLE_DISPLAY_LIST = displayListIndex;
		}

		/// COLOR: DEFAULT WHITE; GREEN IF HIGHLIGHTED; ADD RED IF PINNED
		//Color3f c = new Color3f(1,1,1);//default: white
		float cx = 1f;
		float cy = 1f;
		float cz = 1f;
		if(pin)       { 
			cx = 1f;//add red
			cy *= 0.2f;
			cz = 0;
		}
		if(highlight) {
			cy = 1;
			cz = 0;
		}

		gl.glColor3f(cx, cy, cz);

		/// DRAW ORIGIN-CIRCLE TRANSLATED TO "p":
		gl.glPushMatrix();
		gl.glTranslated(x.x, x.y, 0);
		gl.glCallList(PARTICLE_DISPLAY_LIST);
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);

	}

	/** Specifies whether particle should be drawn highlighted. */
	public void setHighlight(boolean highlight) { 
		this.highlight = highlight;   
	}
	/** True if particle should be drawn highlighted. */
	public boolean getHighlight() { 
		return highlight; 
	}

	/** Specifies whether or not this particle is fixed in space via a
	 * pin constraint. (Should probably be elsewhere in a generic
	 * constraint list). */
	public void setPin(boolean fix) { pin = fix; }

	/** Returns true if currently pinned. */
	public boolean isPinned() { return pin; }

	public double getRadius() {return PARTICLE_RADIUS ; };
	
	/** 
	 * Draws a canonical circular particle.
	 */
	private static void drawParticle(GL2 gl, Point3d p, double PARTICLE_RADIUS)
	{
		if(PARTICLE_RADIUS < 0.01) {/// GL_POINT (doesn't really merit a display list ;P) 
			gl.glBegin(GL2.GL_POINTS);
			gl.glVertex2d(p.x,p.y);
			gl.glEnd();
		}
		else {/// TRIANGULATED CIRCLE:
			double radius = PARTICLE_RADIUS;

			double vectorY1 = p.y;
			double vectorX1 = p.x;

			gl.glBegin(GL2.GL_TRIANGLES);
			gl.glColor3d(0.0, 1.0, 1.0);
			int N = 45;
			for(int i=0; i<=N; i++)
			{
				double angle   = ((double)i) * 2. * Math.PI / (double)N;
				double vectorX = p.x + radius*Math.sin(angle);
				double vectorY = p.y + radius*Math.cos(angle);
				gl.glVertex2d(p.x,p.y);
				gl.glVertex2d(vectorX1,vectorY1);
				gl.glVertex2d(vectorX,vectorY);
				vectorY1 = vectorY;
				vectorX1 = vectorX;	
			}
			gl.glEnd();
		}
	}

}

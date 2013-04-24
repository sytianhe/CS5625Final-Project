package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Spring force between one particle and a proxy point. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForce1Particle implements Force
{
    Particle p1;
    Point3d  x2;
	Color4f color = new Color4f(0,1,0,1);
	double STIFFNESS = 100.0;

    public SpringForce1Particle(Particle p1, Point3d x2)
    {
	if(p1==null || x2==null) throw new NullPointerException("p1="+p1+", x2="+x2);

	this.p1 = p1;
	this.x2 = x2;
    }

    public void updatePoint(Point3f x) {
	x2.set(x);
    }

    public void applyForce()
    {
	{
	    Vector3d v = new Vector3d();
	    v.sub(x2, p1.x);
	    double L = v.length();

	    v.normalize();

	    double dvDot = - v.dot(p1.v);

	    double k = this.STIFFNESS * 0.5;
	    v.scale( (float) (k* ( L + 0.03*dvDot )) );
	    p1.f.add(v);

	    /// STRONGLY DAMP INTERACTION:
	    v.set(p1.v);
	    v.scale((float) (- 5. * p1.m));
	    p1.f.add(v);
	}
    }

    public void display(GL2 gl)
    {
	/// DRAW A LINE:
		gl.glColor4f(color.x, color.y, color.z, color.w);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2d(p1.x.x, p1.x.y);
		gl.glVertex2d(x2.x,   x2.y);
		gl.glEnd();	
    }

    public boolean contains(Particle p)  { return (p==p1);  }
    
    public void setColor(Color4f c){
    	this.color.set(c);
    }

	public void setStiffness(double d) {
		// TODO Auto-generated method stub
		this.STIFFNESS = d;
	}
}

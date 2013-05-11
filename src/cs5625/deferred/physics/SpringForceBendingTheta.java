package cs5625.deferred.physics;

import javax.vecmath.*;
import javax.media.opengl.*;

/** 
 * Bending spring force between three particles. 
 * 
 * @author Doug James, January 2007
 */
public class SpringForceBendingTheta implements Force
{
    Particle p0;
    Particle p1;
    Particle p2;
    Vector3d v;
    double STIFFNESS = Constants.STIFFNESS_BEND;

    /** 
     * Constructs a bending force affecting the angle implied by the
     * three-particle chain, p0-p1-p2.  
     */
    public SpringForceBendingTheta(Particle p0, Particle p1, Particle p2, Vector3d v)
    {
	if(p0==null || p1==null || p2==null) 
	    throw new NullPointerException("p0="+p0+", p1="+p1+", p2="+p2);

	this.p0 = p0;
	this.p1 = p1;
	this.p2 = p2;
	this.v = v;
    }

    public void applyForce()
    {
	{
	    /// VECTORS: a=x1-x0, b=x2-x1:
	    Vector3d a  = new Vector3d();  a.sub(p1.x, p0.x);
	    Vector3d b  = new Vector3d();  b.sub(p2.x, p1.x);
	    a.sub(v);

	    double L  = p1.x0.distance(p0.x0) + p2.x0.distance(p1.x0);

	    double A  = a.length();
	    double B  = b.length();
	    double a2 = A*A;
	    double b2 = B*B;

	    /// FORCE CONSTANT:
	    double a_b = a.dot(b) ;
	    double   k = this.STIFFNESS / L;
	    double   c = 0.5 * k / (A * B);

	    Vector3d f0 = new Vector3d();
	    Vector3d f1 = new Vector3d(); 
	    Vector3d f2 = new Vector3d();

	    /// f0 = c * ( b - a_b/a2 a ) 
	    Utils.acc(f0, c, b);
	    Utils.acc(f0, -c*a_b/a2, a);

	    /// f2 = c * ( -a + a_b/b2 b )
	    Utils.acc(f2, -c, a);
	    Utils.acc(f2, c*a_b/b2, b);

	    // f1 = -(f0 + f2)
	    f1.add(f0);
	    f1.add(f2);
	    f1.negate();

	    /// MINUS SIGN BUG: NEGATE ALL FORCES:
	    f0.negate();
	    f1.negate();
	    f2.negate();

	    /// ACCUMULATE FORCES:
	    p0.f.add(f0);
	    p1.f.add(f1);
	    p2.f.add(f2);
	    /// SKIP DAMPING (YOU MAY WANT TO ADD IT)
	}
    }

    public void display(GL2 gl)
    {
	///... 
    }

    public boolean contains(Particle p)  
    { 
	return ((p==p0) || (p==p1) || (p==p2));
    }
    
    public void setStiffness(double s){
    	this.STIFFNESS = s;
    }
}

package physics;

import java.util.ArrayList;

import javax.media.opengl.*;

import cs5625.deferred.scenegraph.SceneObject;


/**
 * Particle system force.
 * 
 * @author Doug James, January 2007
 */
public interface Force  
{
    /** 
     * Causes force to be applied to affected particles.
     */
    public void applyForce();

    /** Display any instructive force information, e.g., connecting spring. */
    public void display(GL2 gl);

    /** Reference to the Particles this force affects. */
    public ArrayList<SceneObject> getSceneObjects();

    /** Apply force to particle1.
     *  NOOP if not applicable.
     */
	public void applyForce(SceneObject s1);
    
    /** Apply force to particle1 given partical2.
     *  NOOP if not applicable.
     */
	public void applyForce(SceneObject s1, SceneObject s2);
}

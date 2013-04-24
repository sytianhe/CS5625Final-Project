package cs5625.deffered.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.scenegraph.Geometry;

public class PhysicsGeometry extends Geometry {
	/** Control points. Used fro creating phyiscs particles. */
	private ArrayList<Point3f> controlPoints = new ArrayList<Point3f>();	//List of control points 
	private ArrayList<Particle> controlParticles = new ArrayList<Particle>();  //List of particles to be dragged around by geometry 
	private ArrayList<Particle> interactsWith = new ArrayList<Particle>();	
	
	public ArrayList<Point3f> getControlPoints(){ return controlPoints; };
	public ArrayList<Particle> getControlParticles(){ return controlParticles; };

	public void addControlPoint(Point3f pt){
		controlPoints.add(pt);
	}
	
	public void addControlPoints(ArrayList<Point3f> pts){
		controlPoints.addAll(pts);
	}
	
	public void pinToPhysicsGeometry(Geometry pg, Point3f attachmentPoint){
		try {
			this.addChild(pg);
			pg.setPosition(attachmentPoint);
			pg.setIsPinned(true);
			addControlPoint(attachmentPoint);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void addInteractionWith(PhysicsGeometry pg){
		interactsWith.add(pg.getParticle());
	}
	
	public void addToParticleSystemHelper(ParticleSystem PS){
		for (Point3f cp : controlPoints){
			Particle p = new Particle( new Point3d(cp) );
			controlParticles.add(p);
			PS.addParticle(p);
		}
	}
	
	public void animateHelper(float dt){
		int n = controlPoints.size();
		for (int i = 0; i < n; i++){
			Particle cp = controlParticles.get(i);
			if (cp.isPinned() ){
				controlParticles.get(i).x.set(this.transformPointFromWorldSpace(controlPoints.get(i)));
			}
			else{
				controlPoints.get(i).set(this.transformPointToWorldSpace(new Point3f(cp.x)));
			}
		}
	}
	
}

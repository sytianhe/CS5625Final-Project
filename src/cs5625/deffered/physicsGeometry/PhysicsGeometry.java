package cs5625.deffered.physicsGeometry;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physics.PenaltyForce2Particle;
import cs5625.deferred.scenegraph.Geometry;

public class PhysicsGeometry extends Geometry {
	/** Control points. Used fro creating phyiscs particles. */
	private ArrayList<Point3f> controlPoints = new ArrayList<Point3f>();	//List of control points in model space
	private ArrayList<Particle> controlParticles = new ArrayList<Particle>();  //List of particles to be dragged around by geometry 
	private ArrayList<PhysicsGeometry> interactsWith = new ArrayList<PhysicsGeometry>();	
	
	public ArrayList<Point3f> getControlPoints(){ return controlPoints; };
	public ArrayList<Particle> getControlParticles(){ return controlParticles; };
	public ArrayList<PhysicsGeometry> getInteractsWith() { return interactsWith; };
	public void setInteractsWith(ArrayList<PhysicsGeometry> interactsWith) { this.interactsWith = interactsWith; };

	public void addControlPoint(Point3f pt){
		controlPoints.add(pt);
	}
	
	public void addControlPoints(ArrayList<Point3f> pts){
		controlPoints.addAll(pts);
	}
	
	public void pinToPhysicsGeometry(PhysicsGeometry pg, Point3f attachmentPoint){
		try {
			this.addChild(pg);
			pg.setPosition(attachmentPoint);
			pg.setIsPinned(true);
			//addControlPoint(attachmentPoint);
			addInteractionWith(pg);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void addInteractionWith(PhysicsGeometry pg){
		getInteractsWith().add(pg);
	}
	
	public void addToParticleSystemHelper(ParticleSystem PS){
		super.addToParticleSystemHelper(PS);
		for (Point3f cp : controlPoints){
			Particle p = new Particle( new Point3d(this.transformPointToWorldSpace(cp)));
			p.setRadius(0.1);
			controlParticles.add(p);
			PS.addParticle(p);
		}
		
		for (PhysicsGeometry pg: this.interactsWith){
			for (Particle p1 : pg.getControlParticles()){
				for (Particle p2 : this.getControlParticles()){
					//PS.addForce(new PenaltyForce2Particle(p1,p2));
				}
			}
		}
		
		
	}
	
	public void animateHelper(float dt){
		super.animateHelper(dt);
		int n = controlPoints.size();
		for (int i = 0; i < n; i++){
			Particle cp = controlParticles.get(i);
			if (cp.isPinned() ){
				controlParticles.get(i).x.set(this.transformPointToWorldSpace(controlPoints.get(i)));
			}
			else{
				controlPoints.get(i).set(this.transformPointFromWorldSpace(new Point3f(cp.x)));
			}
		}
	}
	
}

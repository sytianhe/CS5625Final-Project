package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.catmullclark.CCSubdiv;
import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.scenegraph.BranchGeometry;

import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.LeaveGeometry;
import cs5625.deferred.scenegraph.MengerSponge;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.Quadmesh;
import cs5625.deferred.scenegraph.TreeTrunk;
import cs5625.deferred.scenegraph.Trimesh;
import cs5625.deferred.scenegraph.TrunckGeometry;
import cs5625.deferred.physicsGeometry.Ground;
import cs5625.deferred.physicsGeometry.Leaf;
import cs5625.deferred.physicsGeometry.PhysicsGeometry;
import cs5625.deferred.physicsGeometry.Sphere;

public class SandDuneSceneController extends SceneController{

	
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Keeps track of shadow camera's orbit position. */
	private float mShadowCameraLongitude = -50.0f, mShadowCameraLatitude = -40.0f;
	private float mShadowCameraRadius = 15.f;
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	private Trimesh triMesh;
	private Geometry geo;
	
	/* Used to calculate subdivision surfaces. */
	private Quadmesh visibleMesh;
	
	private int numberofSub = 2;

	public void initializeScene() {
		try {
			ArrayList<Point3f>list = new ArrayList<Point3f>();
			Point3f point = new Point3f();
			point.set(3f,0f,0f);
			list.add(new Point3f(point));
			point.set(3f,1.148f,0f);
			list.add(new Point3f(point));
			point.set(2.121f,2.121f,-0.1f);
			list.add(new Point3f(point));
			point.set(1.148f,2.771f,-0.15f);
			list.add(new Point3f(point));
			point.set(0f,3f,-0.2f);
			list.add(new Point3f(point));

			ArrayList<Geometry> geoList = new ArrayList<Geometry>();

			Ground plane = new Ground();
			geoList.add(plane);

			
//            Geometry sphere =  Geometry.load("models/sphere.obj", true, false).get(0);
//            sphere.setPosition(new Point3f(-2.0f,2.0f,-2.0f));
//            
			Sphere sphere = new Sphere(new Point3f(-2.0f,2.0f,-2.0f));
            sphere.setIsPinned(false);
            sphere.getOriginParticle().v.set(5,5,5);
            plane.addInteractionWith(sphere);
        
									
//			TrunckGeometry newTrunk = new TrunckGeometry(list, 0.3f, 0.1f);
//			newTrunk.setPosition(new Point3f(-3.0f, 0.0f, 0.0f));
//			newTrunk.setIsPinned(false);
			
//			geoList.add(newTrunk);
            
            BranchGeometry newBranch = new BranchGeometry(3f, 1f);
            //newBranch.setIsPinned(false);
			mSceneRoot.addChild(newBranch);	
			newBranch.setPosition(new Point3f(0f,2f,0f));
			newBranch.setOrientation(new Quat4f(1f, 1f, 0f, (float) (Math.sqrt(2.0)/2f)));

            
//			LeaveGeometry newLeaf = new LeaveGeometry(3f, 0.5f);
//			newLeaf.setPosition(new Point3f(0f,1f,0f));
//			newLeaf.setIsPinned(false);
//			newTrunk.addChild(newLeaf);
//			mSceneRoot.addChild(newTrunk);

			//sphere.addChild(newLeaf);
		    mSceneRoot.addChild(sphere);

			//geoList.add(newLeaf);
			mSceneRoot.addGeometry(geoList);
			            
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();

			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);

			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);		
		}		 	
		catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.hasShadows = true;
		
		/* Initialize camera position. */
		updateCamera();
		updateShadowCamera();
	}

	/**
	 * Updates the camera position and orientation based on orbit parameters.
	 */
	protected void updateCamera()
	{
		/* Compose the "horizontal" and "vertical" rotations. */
		Quat4f longitudeQuat = new Quat4f();
		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mCameraLongitude * (float)Math.PI / 180.0f));
		
		Quat4f latitudeQuat = new Quat4f();
		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mCameraLatitude * (float)Math.PI / 180.0f));

		mCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
		
		/* Set the camera's position so that it looks towards the origin. */
		mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
		Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
		/* Zoom in and out by the scroll wheel. */
		mCameraRadius += mouseWheel.getUnitsToScroll();
		updateCamera();
		requiresRender();
	}

	@Override
	public void mousePressed(MouseEvent mouse)
	{
		/* Remember the starting point of a drag. */
		mLastMouseDrag = mouse.getPoint();
	}
	
	@Override
	public void mouseDragged(MouseEvent mouse)
	{
		/* Calculate dragged delta. */
		float deltaX = -(mouse.getPoint().x - mLastMouseDrag.x);
		float deltaY = -(mouse.getPoint().y - mLastMouseDrag.y);
		mLastMouseDrag = mouse.getPoint();
		
		/* Update longitude, wrapping as necessary. */
		mCameraLongitude += deltaX;
		
		if (mCameraLongitude > 360.0f)
		{
			mCameraLongitude -= 360.0f;
		}
		else if (mCameraLongitude < 0.0f)
		{
			mCameraLongitude += 360.0f;
		}
		
		/* Update latitude, clamping as necessary. */
		if (Math.abs(mCameraLatitude + deltaY) <= 180.0f)
		{
			mCameraLatitude += deltaY;
		}
		else
		{
			mCameraLatitude = 20.0f * Math.signum(mCameraLatitude);
		}
	
		updateCamera();
		requiresRender();
	}
	/**
	 * Updates the camera position and orientation based on orbit parameters.
	 */
	protected void updateShadowCamera()
	{
		/* Compose the "horizontal" and "vertical" rotations. */
		Quat4f longitudeQuat = new Quat4f();
		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mShadowCameraLongitude * (float)Math.PI / 180.0f));
		
		Quat4f latitudeQuat = new Quat4f();
		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mShadowCameraLatitude * (float)Math.PI / 180.0f));

		mShadowCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
		
		/* Set the camera's position so that it looks towards the origin. */
		mShadowCamera.setPosition(new Point3f(0.0f, 0.0f, mShadowCameraRadius));
		Util.rotateTuple(mShadowCamera.getOrientation(), mShadowCamera.getPosition());
		
		mSceneRoot.findDescendantByName("CameraLight").setPosition(mShadowCamera.getPosition());
	}
}

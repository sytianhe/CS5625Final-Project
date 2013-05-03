package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.Quadmesh;
import cs5625.deferred.scenegraph.Trimesh;
import cs5625.deferred.physicsGeometry.Branch;
import cs5625.deferred.physicsGeometry.Ground;
import cs5625.deferred.physicsGeometry.Leaf;
import cs5625.deferred.physicsGeometry.Sphere;
import cs5625.deferred.physicsGeometry.Stem;

public class TreeSceneController extends SceneController{

	
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Keeps track of shadow camera's orbit position. */
	private float mShadowCameraLongitude = -50.0f, mShadowCameraLatitude = -40.0f;
	private float mShadowCameraRadius = 50.f;
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	private Trimesh triMesh;
	private Geometry geo;
	
	/* Used to calculate subdivision surfaces. */
	private Quadmesh visibleMesh;
	
	private int numberofSub = 2;

	public void initializeScene() {
		try {
			//SETUP CONTROL POINT FOR LEAF AND STEM .... NEED TO BE SPACED OUT CURRENTLY
			ArrayList<Point3f>list = new ArrayList<Point3f>();
        	ArrayList<Point3f>list2 = new ArrayList<Point3f>();
//        	for(int j = 0; j<5; j++){
//				Point3f point = new Point3f();
//				point.set(0f,1.5f*i,0f);
//				list2.add(new Point3f(point));
//        	}
			for(int i=0; i< 10; i++){
				Point3f point = new Point3f();
				point.set(0f,1.5f*i,0f);
				list.add(new Point3f(point));
				//list2.add(new Point3f(point));

			}

			//ADD GROUND
			Ground plane = new Ground();
			mSceneRoot.addChild(plane);
			
			//ADD SPHERE
			Sphere sphere = new Sphere(new Point3f(4.0f,4.0f,4.0f));
            sphere.setIsPinned(false);
            sphere.getOriginParticle().v.set(-5,5,-5);
            mSceneRoot.addChild(sphere);    
            plane.addInteractionWith(sphere);

            //ADD MAIN TRUNK
            Branch branch = new Branch(list);
            branch.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
            branch.setIsPinned(true);
            mSceneRoot.addChild(branch);
            
            branch.addInteractionWith(sphere);
  
            //CREATE AND ATTACH LEAVES  UP STEM
//            for (Point3f pt :  list ){
//            	if (! pt.equals(list.get(0)) && ! pt.equals(list.get(1))){
//            		Leaf leaf1 = new Leaf(3f, 0.75f);
//            		Leaf leaf2 = new Leaf(3f, 0.75f);
//            		leaf2.setOrientation(new Quat4f(0,1,0,0));
//            		branch.pinToPhysicsGeometry(leaf1,pt);
//            		branch.pinToPhysicsGeometry(leaf2,pt);
//            	}
//            }
            
            Point3f topPoint = list.get(list.size()-1);
//            for (int i = 0; i<8; i++){
//            	Leaf leaf1 = new Leaf(3f, 0.75f);
//            	leaf1.setOrientation(new Quat4f(0,(float) Math.sin((float)i/8.0 *1.0* Math.PI),0,(float) Math.cos((float)i/8.0 *1.0* Math.PI)));
//            	branch.pinToPhysicsGeometry(leaf1,topPoint);
//            }

			for(int i=0; i< 15; i++){
				Point3f point = new Point3f();
				point.set(0f,1.5f*i/5f,0f);
				//list.add(new Point3f(point));
				list2.add(new Point3f(point));

			}
			
            //CREATE STEMS AND LEAVES
            for (int i = 0; i<20; i++){

                Stem stem = new Stem(list2);
                // FIND THE RIGHT QUATERNION TO MAINTAIN THE TREE LEAVES FACING UP
                float rand1 = (float) Math.random();
                float rand2 = (float) Math.random()*0.35f;

                Quat4f rotY = new Quat4f(0,(float) Math.sin(rand1* Math.PI),0,(float) Math.cos(rand1* Math.PI));                
                Quat4f rotZ = new Quat4f(0,0,(float) Math.sin(rand2* Math.PI),(float) Math.cos(rand2* Math.PI));

                rotY.mul(rotZ);
            	stem.setOrientation(rotY);
                branch.pinToPhysicsGeometry(stem, topPoint);
            }
			            
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();

			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);

			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			//light.setPosition(new Point3f(10f, 10f, 10f));
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

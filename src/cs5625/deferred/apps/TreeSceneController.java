package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.physics.ParticleSystem;
import cs5625.deferred.physicsGeometry.Branch;
import cs5625.deferred.physicsGeometry.Ground;
import cs5625.deferred.physicsGeometry.Sphere;
import cs5625.deferred.physicsGeometry.Stem;
import cs5625.deferred.scenegraph.PointLight;

public class TreeSceneController extends SceneController{

	
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Keeps track of shadow camera's orbit position. */
	private float mShadowCameraLongitude = -50.0f, mShadowCameraLatitude = -40.0f;
	private float mShadowCameraRadius = 50.f;
	
	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;
	
	public void initializeScene() {
		try {
			//ADD LIGHT
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();

			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);

			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);	

			//ADD GROUND
			Ground plane = new Ground();
			mSceneRoot.addChild(plane);
			
			//ADD SPHERE
			Sphere sphere = new Sphere(new Point3f(4.0f,4.0f,4.0f));
            sphere.setIsPinned(false);
            sphere.getOriginParticle().v.set(-5,5,-5);
            mSceneRoot.addChild(sphere);    
            plane.addInteractionWith(sphere);
            
			//SETUP CONTROL POINT FOR LEAF AND STEM .... NEED TO BE SPACED OUT CURRENTLY
			ArrayList<Point3f>list = new ArrayList<Point3f>();
        	ArrayList<Point3f>list2 = new ArrayList<Point3f>();

			for(int i=0; i< 10; i++){
				Point3f point = new Point3f();
				point.set(0f,1.5f*i,0f);
				list.add(new Point3f(point));
			}
			
			for(int i=0; i< 15; i++){
				Point3f point = new Point3f();
				point.set(0f,1.5f*i/5f,0f);
				list2.add(new Point3f(point));
			}
			
            //ADD MAIN TRUNK
            Branch branch = new Branch(list);
            branch.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
            branch.setIsPinned(true);
            mSceneRoot.addChild(branch);
            
            branch.addInteractionWith(sphere);
            
            Point3f topPoint = list.get(list.size()-1);
			
            //CREATE STEMS AND LEAVES
            for (int i = 0; i<15; i++){
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
		}		 	
		catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.hasShadows = true;
		try {
			mSceneRoot.addChild(mCamera);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	/**
	 * Override this in your SceneController subclass to respond to this type of user action.
	 * By default, the following keys will control the renderer in the specified way:
	 * '1', ..., '6': Specifies a gbuffer texture to preview.
	 * '7': Visualize the normals.
	 * '8': Visualize the tangents, only works for anisotropic ward objects.
	 * '9': Visualize the bitangents, only works for anisotropic ward objects.
	 * '0': Stop displaying a gbuffer texture or visualization.
	 * 't': Toggle toon shading.
	 * 'w': Toggle wireframes.
	 * 'b': Toggle bloom post-processing.
	 * 'v'/'V': Decrease/Increase the bloom variance.
	 * 'c'/'C': Decrease/Increase the bloom threshold.
	 * 'x'/'X': Decrease/Increase the bloom width.
	 */
	@Override
	public void keyPressed(KeyEvent key)
	{
		char c = key.getKeyChar();
		if (c == 'j')
		{
			System.out.println("pressing " + c);
			mCamera.keyUP = true;
//			mCameraRadius += 1.5;
//			updateCamera();
//			requiresRender();
			

		}
		else if (c == 'k')
		{
			mCameraRadius -= 1.5;
			updateCamera();
			requiresRender();
		}
		else if (c == 'h')
		{
			/* Zoom in and out by the scroll wheel. */
			mCameraLongitude += 1.5;
			updateCamera();
			requiresRender();
		}
		else if (c == 'l')
		{
			/* Zoom in and out by the scroll wheel. */
			mCameraLongitude -= 1.5;
			updateCamera();
			requiresRender();
		}
		
	}
	
	public void keyReleased(KeyEvent key) {
		char c = key.getKeyChar();
		if (c == 'j')
		{
			mCamera.keyUP = false;
		}
//		else if (c == 'k')
//		{
//			/* Zoom in and out by the scroll wheel. */
//			mCameraRadius -= 0.5;
//			updateCamera();
//			requiresRender();
//		}
//		else if (c == 'h')
//		{
//			/* Zoom in and out by the scroll wheel. */
//			mCameraLongitude += 0.5;
//			updateCamera();
//			requiresRender();
//		}
//		else if (c == 'l')
//		{
//			/* Zoom in and out by the scroll wheel. */
//			mCameraLongitude -= 0.5;
//			updateCamera();
//			requiresRender();
//		}
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

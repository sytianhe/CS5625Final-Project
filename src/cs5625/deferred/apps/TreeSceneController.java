package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.physicsGeometry.Ground;
import cs5625.deferred.physicsGeometry.PalmTree;
import cs5625.deferred.physicsGeometry.SkyBox;
import cs5625.deferred.physicsGeometry.Sphere;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.PointLight;

public class TreeSceneController extends SceneController{

	
	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
	private float mCameraRadius = 15.0f;
	
	/* Keeps track of shadow camera's orbit position. */
	private float mShadowCameraLongitude = -50.0f, mShadowCameraLatitude = -40.0f;
	private float mShadowCameraRadius = 40.f;
	
	private boolean mArbitraryMovement = false;
	
	public PalmTree tree;
	private ArrayList<Sphere> balls = new ArrayList<Sphere>();
	public Sphere target;
	public int targetIndex = 0;

	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
	private Point mLastMouseDrag;

	
	public void initializeScene() {
		try {
			
			GL2 gl = GLU.getCurrentGL().getGL2();
			
			mCamera.setPosition(new Point3f(10.0f, 8.0f,10.0f));


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
			Ground plane = new Ground(mRenderer);
			mSceneRoot.addChild(plane);
			
			//ADD TREE
            Texture2D barkTexture = Texture2D.load(gl, "textures/bark4.jpg",false);
            tree = new PalmTree(10f, 0.7f, 0.4f, 0f, 10f, 10, 10,2, barkTexture);
            tree.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
            mSceneRoot.addChild(tree);

            //ADD SKY BOX
			Texture2D skyTexture = Texture2D.load(gl, "textures/skybox.jpg",false);
			Texture2D starTexture = Texture2D.load(gl, "textures/star3.jpg",false);
			SkyBox skybox = new SkyBox(new Color3f(102.0f/256f, 1f, 1f), 205f, skyTexture, starTexture);

			skybox.setPosition(new Point3f(0f, 0f, 0f));
			//skybox.setOrientation(new Quat4f(0f,0f,(float) Math.sin(Math.PI/4.0),(float) Math.cos(Math.PI/4.0)));
			skybox.setScale(205);
			mSceneRoot.addChild(skybox);

			//ADD TARGET
			target = new Sphere(new Point3f(10.0f,10.0f,10.0f));
			target.setIsPinned(false);
			target.setIsSelected(true);

			mSceneRoot.addChild(target);
            balls.add(target);

            //INITIALIZE REPULSIVE GROUND
            plane.addInteraction(target,PS);      

			
         ;
		}		 	
		catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OpenGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
		
		if (mArbitraryMovement){
		/* Set the camera's position so that it looks towards the origin. */
			mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
			Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
		}
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
		int k = key.getKeyCode();

		if (k == KeyEvent.VK_UP){
			mCamera.keyUP = true;
		}
		else if (k == KeyEvent.VK_DOWN){
			mCamera.keyDOWN = true;
		}
		else if (k == KeyEvent.VK_LEFT){
			mCamera.keyLEFT = true;
		}
		else if (k == KeyEvent.VK_RIGHT){
			mCamera.keyRIGHT = true;
		}
		else if (c == '/')
		{
			tree.addForce(target.getOriginParticle(), PS);
		}	
		else if (c == 'n')
		{
			if (target.isPinned())
			{
				target.setIsPinned(false);
			}
			else target.setIsPinned(true);
		}
		else if (c == 'b'){
			//ADD SPHERE
            try {
            	//Sphere sphere = new Sphere(new Point3f( 50f* (float)Math.random()- 50f, 100f ,50f*(float)Math.random()- 50f));
            	Sphere sphere = new Sphere(new Point3f( (float)Math.random(), 80f , (float)Math.random() ));
				mSceneRoot.addChild(sphere);
            	sphere.setIsPinned(false);
            	sphere.addToParticleSystem(PS);
            	balls.add(sphere);
            	
			} catch (ScenegraphException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
		}
		else if (c =='B'){
			//TOGGLE THROUGH SPHERES
			target.setIsSelected(false);
			targetIndex = (targetIndex + 1) % balls.size();
			target=balls.get(targetIndex);
			target.setIsSelected(true);
		}
		
		updateCamera();
		requiresRender();
	}
	
	public void keyReleased(KeyEvent key) {
		char c = key.getKeyChar();
		int k = key.getKeyCode();

		if (k == KeyEvent.VK_UP){
			mCamera.keyUP = false;
		}
		else if (k == KeyEvent.VK_DOWN){
			mCamera.keyDOWN = false;
		}
		else if (k == KeyEvent.VK_LEFT){
			mCamera.keyLEFT = false;
		}
		else if (k == KeyEvent.VK_RIGHT){
			mCamera.keyRIGHT = false;
		}
		else if (c == '/'){
			tree.removeForce(target.getOriginParticle(), PS);
		}
		
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
		mShadowCamera.setPosition(new Point3f(0.0f, 0.0f, 4f*mShadowCameraRadius));
		Util.rotateTuple(mShadowCamera.getOrientation(), mShadowCamera.getPosition());
		
		mSceneRoot.findDescendantByName("CameraLight").setPosition(mShadowCamera.getPosition());
	}
}

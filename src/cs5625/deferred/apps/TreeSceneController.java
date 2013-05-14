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
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.materials.ParallaxMapMaterial;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.physics.Particle;
import cs5625.deferred.physicsGeometry.Ground;
import cs5625.deferred.physicsGeometry.PalmTree;
import cs5625.deferred.physicsGeometry.Sphere;
import cs5625.deferred.scenegraph.Geometry;
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
	public Sphere target;
	public PalmTree tree;
		
	public void initializeScene() {
		try {
			
			GL2 gl = GLU.getCurrentGL().getGL2();

			//ADD LIGHT
			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();

			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);

			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);	
			
			PointLight light2 = new PointLight();
			
			light2.setColor(new Color3f(1f,0f,0f));
			light2.setConstantAttenuation(1.0f);
			light2.setLinearAttenuation(0.0f);
			light2.setQuadraticAttenuation(0.0f);
			light2.setPosition(new Point3f(0f,50f,0f));
			//mSceneRoot.addChild(light2);
			
			//ADD GROUND
			Ground plane = new Ground(mRenderer);
			mSceneRoot.addChild(plane);
			
			//ADD SPHERE
			Sphere sphere = new Sphere(new Point3f(5.0f,5.0f,5.0f));
            sphere.setIsPinned(false);
            sphere.getOriginParticle().v.set(-5,5,-5);
            mSceneRoot.addChild(sphere);    
            plane.addInteractionWith(sphere);
            
            Texture2D barkTexture = Texture2D.load(gl, "textures/bark1.jpg",false);
            tree = new PalmTree(10f, 0.7f, 0.4f, 0f, 10f, 3, 10,2, barkTexture);
            tree.setPosition(new Point3f(0.0f, 0.0f, 0.0f));
            mSceneRoot.addChild(tree);
            
//            PalmTree tree2 = new PalmTree(10f, 0.5f, 0.4f, 0f, 10f, 15, 15,3, barkTexture);
//            tree2.setPosition(new Point3f(5.0f, 0.0f, 0.0f));
//            mSceneRoot.addChild(tree2);
            
//            PalmTree tree3 = new PalmTree(10f, 0.5f, 0.4f, 0f, 10f, 15, 15,3, barkTexture);
//            tree3.setPosition(new Point3f(0.0f, 0.0f, 5.0f));
//            mSceneRoot.addChild(tree3);

            //TESTING PARALLAX MAPPING:
		    ParallaxMapMaterial normalMaterial2 = new ParallaxMapMaterial();
			Texture2D brickTexture = Texture2D.load(gl, "textures/lion.jpg");
			Texture2D brickSpecularTexture = Texture2D.load(gl, "textures/Specular_example.jpg");
			Texture2D brickNormalTexture = Texture2D.load(gl, "textures/stoneBrickNormal.jpg");
			Texture2D brickHeightTexture = Texture2D.load(gl, "textures/lion_bump.jpg");
			normalMaterial2.setDiffuseTexture(brickTexture);
			normalMaterial2.setSpecularTexture(brickSpecularTexture);
			normalMaterial2.setNormalTexture(brickNormalTexture);
			normalMaterial2.setHeightTexture(brickHeightTexture);

			//mSceneRoot.addGeometry(Geometry.load("models/default-scene.obj", true, true));
			ArrayList<Geometry> temp = new ArrayList <Geometry>();
			temp.addAll( Geometry.load("models/cube.obj", true, true));
			mSceneRoot.addChild(temp.get(0));
			temp.get(0).setScale(100);
			temp.get(0).setPosition(new Point3f(0, 0, 0));
			temp.get(0).calculateTangentVectorsForAllGeometry();
			temp.get(0).getMeshes().get(0).setMaterial(new UnshadedMaterial(new Color3f(102.0f/256f, 1f, 1f)));

			target = new Sphere(new Point3f(1.0f,1.0f,0.0f));
			target.setIsPinned(true);
			target.setName("targetMark");
			target.getOriginParticle().v.set(0,0,0);
			
            plane.addInteractionWith(target);

			
            mSceneRoot.addChild(target);
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
		//int kk = key.getKeyCode();
		//System.out.println(kk);
//		if (kk == 38){
////			mCameraRadius -= 1.5;
//			mCamera.getPosition().x += 1;
//
//			updateCamera();
//			requiresRender();
//		}
//		else if (kk == 37){
//			mCamera.getPosition().x += 1;
//			updateCamera();
//			requiresRender();
//		}		
//		
		if (c == 'j')
		{
			System.out.println("pressing " + c);
			mCamera.keyUP = true;
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
		else if (c == 'u')
		{
			// move the target to the left
			Point3f currentPos = new Point3f(((Geometry)mSceneRoot.findDescendantByName("targetMark")).getPosition());
			currentPos.add(new Point3f(-1.5f, 0, 0));
			((Geometry)mSceneRoot.findDescendantByName("targetMark")).setPosition(currentPos);
			updateCamera();
			requiresRender();
		}
		else if (c == 'p')
		{
			// move the target to the right
			Point3f currentPos = new Point3f(((Geometry)mSceneRoot.findDescendantByName("targetMark")).getPosition());
			currentPos.add(new Point3f(1.5f, 0, 0));
			((Geometry)mSceneRoot.findDescendantByName("targetMark")).setPosition(currentPos);
			updateCamera();
			requiresRender();
		}
		else if (c == 'i')
		{
			// move the target to the down
			Point3f currentPos = new Point3f(((Geometry)mSceneRoot.findDescendantByName("targetMark")).getPosition());
			currentPos.add(new Point3f(0, 0, -1.5f));
			((Geometry)mSceneRoot.findDescendantByName("targetMark")).setPosition(currentPos);
			updateCamera();
			requiresRender();
		}
		else if (c == 'o')
		{
			// move the target to the up
			Point3f currentPos = new Point3f(((Geometry)mSceneRoot.findDescendantByName("targetMark")).getPosition());
			currentPos.add(new Point3f(0, 0, 1.5f));
			((Geometry)mSceneRoot.findDescendantByName("targetMark")).setPosition(currentPos);
			updateCamera();
			requiresRender();
		}
		else if (c == '/')
		{
			tree.addForce(target.getOriginParticle(), PS);
		}	
		else if (c == '!')
		{
			if (target.isPinned())
			{
				target.setIsPinned(false);
			}
			else target.setIsPinned(true);
		}
	}
	
	public void keyReleased(KeyEvent key) {
		char c = key.getKeyChar();
		if (c == 'j')
		{
			mCamera.keyUP = false;
		}
		else if (c == '/'){
			tree.removeForce(target.getOriginParticle(), PS);
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
		mShadowCamera.setPosition(new Point3f(0.0f, 0.0f, 1.6f*mShadowCameraRadius));
		Util.rotateTuple(mShadowCamera.getOrientation(), mShadowCamera.getPosition());
		
		mSceneRoot.findDescendantByName("CameraLight").setPosition(mShadowCamera.getPosition());
	}
}

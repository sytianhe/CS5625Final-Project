package cs5625.deferred.apps;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import cs5625.deferred.catmullclark.CCSubdiv;
import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.defaultGeometry.Tetrahedron;
import cs5625.deferred.loop.LoopSubdiv;
import cs5625.deferred.materials.BlinnPhongMaterial;
import cs5625.deferred.materials.LambertianMaterial;
import cs5625.deferred.materials.MandelbrotMaterial;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.MengerSponge;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.Quadmesh;
import cs5625.deferred.scenegraph.Trimesh;

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
	private Mesh visibleMesh;
	private Trimesh loopMesh;
	private Quadmesh ccMesh;
	private boolean isLoop;

	public void updateSceneGraph() {
		try {
			Geometry geom = new Geometry();
			geom.addMesh(visibleMesh);
			//geom.setPosition(new Point3f(0.0f,5.0f,0.0f));
			ArrayList<Geometry> geoList = new ArrayList<Geometry>();
			geoList.add(geom);

			Geometry plane = Geometry.load("models/plane.obj", false, false).get(0);					
			plane.getMeshes().get(0).setMaterial(new LambertianMaterial(new Color3f(0.00f, 0.70f, 0.70f)));
			geoList.add(plane);
			plane.setIsPinned(true);
			
			
			
			MengerSponge sponge = new MengerSponge(3);
			sponge.setMaterial(new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
			
			geo = new Geometry();
			geo.addMesh(sponge);
			geo.setPosition(new Point3f(4.0f, 1.0f, 0.0f));
			geoList.add(geo);
			
			geo = new Geometry();
			triMesh =  (Trimesh)Geometry.load("models/monkey.obj", true, true).get(0).getMeshes().get(0);
			triMesh.setMaterial(new BlinnPhongMaterial(new Color3f(0.30f, 0.00f, 0.30f)));
			triMesh.getMaterial();
			geo.addMesh(triMesh);
			geo.setPosition(new Point3f(-2.0f, 2.0f, 0.0f));
			geoList.add(geo);
						
			mSceneRoot.addGeometry(geoList);
			
            Geometry sphere =  Geometry.load("models/sphere.obj", false, false).get(0);
            sphere.setPosition(new Point3f(0.0f,2.0f,0.0f));
            sphere.setIsPinned(false);
            sphere.getMeshes().get(0).setMaterial(new MandelbrotMaterial());
            mSceneRoot.addChild(sphere);
            

			/* Add an unattenuated point light to provide overall illumination. */
			PointLight light = new PointLight();

			light.setConstantAttenuation(1.0f);
			light.setLinearAttenuation(0.0f);
			light.setQuadraticAttenuation(0.0f);

			//light.setPosition(new Point3f(50.0f, 180.0f, 100.0f));
			//mSceneRoot.addChild(light);
			
			light.setPosition(new Point3f(mShadowCamera.getPosition()));
			light.setName("CameraLight");
			mSceneRoot.addChild(light);		
			
		}		 	
		catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.hasShadows = true;
		
		/* Initialize camera position. */
		updateCamera();
		updateShadowCamera();
	}
	
	@Override
	public void initializeScene()
	{
		try
		{
			// TODO PA5: This is where you change the base meshes.
			//ccMesh = new FourQuadMesh().getTriMesh();
			//loopMesh = new Tetrahedron().getTriMesh();
			//loopMesh = new TwoTriangleMesh().getTriMesh();

			//ccMesh = new CubeQuadMesh().getQuadMesh();
//			MengerSponge sponge = new MengerSponge(1);
//			sponge.setMaterial(new BlinnPhongMaterial(new Color3f(0.10f, 0.70f, 0.10f)));
//			ccMesh = (Quadmesh)sponge;
			
			ccMesh = (Quadmesh)Geometry.load("models/cube.obj", true, true).get(0).getMeshes().get(0);
			loopMesh = (Trimesh)Geometry.load("models/lowpolysphere.obj", true, true).get(0).getMeshes().get(0);
			
			visibleMesh = loopMesh;
			isLoop = true;
			
			this.updateSceneGraph();
		}
		catch (Exception err)
		{
			/* If anything goes wrong, just die. */
			err.printStackTrace();
			System.exit(-1);
		}
		
		/* Initialize camera position. */
		updateCamera();
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
	public void keyTyped(KeyEvent key)
	{
		super.keyTyped(key);
		
		char c = key.getKeyChar();
		if (c == 'n')
		{
			if (isLoop)
			{
				System.out.println("Loop Subdivision!");
				EdgeDS edgeDS = new EdgeDS(loopMesh);
				LoopSubdiv loopSubdiv = new LoopSubdiv(edgeDS);
				loopMesh = (Trimesh)loopSubdiv.getNewMesh();
			}
			else
			{
				System.out.println("Catmull-Clark Subdivision!");
				EdgeDS edgeDS = new EdgeDS(ccMesh);
				CCSubdiv ccSubdiv = new CCSubdiv(edgeDS);
				ccMesh = (Quadmesh)ccSubdiv.getNewMesh();
			}
			
			visibleMesh = isLoop ? loopMesh : ccMesh;
			updateSceneGraph();
			requiresRender();
		}
		else if (c == 'm')
		{
			isLoop = !isLoop;
			
			visibleMesh = isLoop ? loopMesh : ccMesh;
			updateSceneGraph();
			requiresRender();
		}
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

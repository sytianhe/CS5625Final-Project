package cs5625.deferred.physicsGeometry;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import cs5625.deferred.materials.SkyMaterial;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.scenegraph.Mesh;

/**
 * SkyBox class creates a sky box into the scene 
 * 
 * @author Tianhe Zhang (tz249)
 *
 */
public class SkyBox extends PhysicsGeometry {
	
	public SkyMaterial skyMaterial;
	public float dayTime = 0f;
	
	public SkyBox(Color3f diffuseColor, Point3f SunPosition, Texture2D skyTexture){
		skyMaterial = new SkyMaterial(new Color3f(102.0f/256f, 1f, 1f));
		if (skyTexture != null){
			skyMaterial.setDiffuseTexture(skyTexture);
		}
		skyMaterial.setSunPosition(SunPosition);
		
		Sphere skySphere = new Sphere(new Point3f(0.0f,0.0f,0.0f));
		this.mMeshes.add( skySphere.getMeshes().get(0));
		((Mesh) this.mMeshes.get(0)).setMaterial(skyMaterial);
	}
	@Override
	public void animateHelper(float dt)
	{
		super.animateHelper(dt);

		dayTime += dt;
		if (dayTime > 24f){
			dayTime -= 24f;
		}
		skyMaterial.setDayTime(dayTime);
	}
}
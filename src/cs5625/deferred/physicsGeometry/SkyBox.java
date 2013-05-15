package cs5625.deferred.physicsGeometry;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import cs5625.deferred.materials.SkyMaterial;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.PointLight;

/**
 * SkyBox class creates a sky box into the scene 
 * 
 * @author Tianhe Zhang (tz249)
 *
 */
public class SkyBox extends PhysicsGeometry {
	
	public SkyMaterial skyMaterial;
	public float dayTime = 0f;
	public PointLight sunLight;
	public float skyRadius;
	
	public SkyBox(Color3f diffuseColor, float skyDomeRadius, Texture2D skyTexture, Texture2D starTexture){
		skyRadius = skyDomeRadius - 10f;//offsets
		skyMaterial = new SkyMaterial(new Color3f(102.0f/256f, 1f, 1f));
		skyMaterial.setSunPosition(new Point3f(skyRadius, 0f, 0f));
		if (skyTexture != null){
			skyMaterial.setDiffuseTexture(skyTexture);
		}
		if (starTexture != null){
			skyMaterial.setStarTexture(starTexture);
		}
		Sphere skySphere = new Sphere(new Point3f(0.0f,0.0f,0.0f));
		this.mMeshes.add( skySphere.getMeshes().get(0));
		((Mesh) this.mMeshes.get(0)).setMaterial(skyMaterial);
		
		// The Sun ...
		sunLight = new PointLight();
		
		sunLight.setColor(new Color3f(0f,0f,0f));
		sunLight.setConstantAttenuation(1.0f);
		sunLight.setLinearAttenuation(0.0f);
		sunLight.setQuadraticAttenuation(0.0f);
		sunLight.setPosition(new Point3f(0f,0f,0f));
		
		try {
			this.addChild(sunLight);
		} catch (ScenegraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if (dayTime>5f && dayTime < 21f) // day time
		{
			sunLight.setColor(new Color3f(1,0,0));
			float yAxis = (float) Math.sin((dayTime - 5f)/13f*Math.PI);
			float xAxis = (float) Math.cos((dayTime - 5f)/13f*Math.PI);
			sunLight.setPosition(new Point3f(skyRadius/1f*xAxis, skyRadius/1f*yAxis, 0f));
			
			skyMaterial.setSunPosition(new Point3f(xAxis, yAxis, 0f));
		}
		else {
			sunLight.setColor(new Color3f(0,0,0));
		}
	}
}
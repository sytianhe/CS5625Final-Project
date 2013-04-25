package cs5625.deferred.rendering;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import cs5625.deferred.materials.Material;
import cs5625.deferred.materials.Texture2D;
import cs5625.deferred.materials.Texture.Datatype;
import cs5625.deferred.materials.Texture.Format;
import cs5625.deferred.materials.UnshadedMaterial;
import cs5625.deferred.misc.OpenGLException;
import cs5625.deferred.misc.ScenegraphException;
import cs5625.deferred.misc.Util;
import cs5625.deferred.scenegraph.Geometry;
import cs5625.deferred.scenegraph.Light;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.PointLight;
import cs5625.deferred.scenegraph.SceneObject;

/**
 * Renderer.java
 * 
 * The Renderer class is in charge of rendering a scene using deferred shading. This happens in 4 stages, 
 * described below. In this description, numbers in {curly braces} indicate g-buffer texture indices.
 * 
 * 1. Render into gbuffer {0 = diffuse/normal.x, 1 = position/normal.y, 2&3 = material info} of each fragment.
 * 2. Render into gbuffer {4 = gradients} based on the positions and normals in 0&1, for edge detection.
 * 3. Render into gbuffer {5 = shaded scene} the final opaque scene, using all previous buffers.
 * 4. Output {5} to window.
 * 
 * Note that the eyespace normal is stored in a compressed form. The alpha values of the diffuse and position
 * buffers are the x and y values of the compressed normal. The algorithm used to do this is from Cry Engine 3.
 * Source: Mittring, M. "A bit more deferred - CryEngine3." Triangle Game Conference 2009.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Sean Ryan (ser99), Ivaylo Boyadzhiev (iib2), John DeCorato (jd537)
 * @date 2013-01-29
 */
public class Renderer
{
	/* Viewport attributes. */
	protected float mViewportWidth, mViewportHeight;
	
	/* The GBuffer FBO. */
	protected FramebufferObject mGBufferFBO;
	
	/* The shadow map FBOs */
	protected FramebufferObject mShadowMapFBO;
	
	/* The sanddune map FBO */
	protected FramebufferObject mSandDuneFBO;
	
	/* Name the indices in the GBuffer so code is easier to read. */
	protected final int GBuffer_DiffuseIndex = 0;
	protected final int GBuffer_PositionIndex = 1;
	protected final int GBuffer_MaterialIndex1 = 2;
	protected final int GBuffer_MaterialIndex2 = 3;
	protected final int GBuffer_GradientsIndex = 4;
	protected final int GBuffer_SSAOIndex = 5;
	protected final int GBuffer_FinalSceneIndex = 6;
	//protected final int GBuffer_SandDune1Index = 4;
	//protected final int GBuffer_SandDune2Index = 7;
	protected final int GBuffer_Count = 7;
	

	/* The index of the texture to preview in GBufferFBO, or -1 for no preview. */
	protected int mPreviewIndex = -1;
	
	/* List of lights in the scene, assembled every frame. */
	private ArrayList<Light> mLights = new ArrayList<Light>();
	
	/* Cache of shaders used by all the materials in the scene. Storing the shaders here instead of in 
	 * the Material classes themselves allows the shaders to be local to the renderer and the OpenGL 
	 * context, which is appropriate. */
	private HashMap<Class<? extends Material>, ShaderProgram> mShaderCache = new HashMap<Class<? extends Material>, ShaderProgram>();

	/* The "ubershader" used for performing deferred shading on the gbuffer, 
	 * and the silhouette shader to compute edges for toon rendering. */
	private ShaderProgram mUberShader, mSilhouetteShader, mSandDuneShader;
	private boolean mEnableToonShading = false;
	
	/* Material for rendering generic wireframes and crease edges, and flag to enable/disable that. */
	private Material mWireframeMaterial, mWireframeMarkedEdgeMaterial;
	private boolean mRenderWireframes = false;
	
	/* Used to control the bloom post-processing stage. */
	private ShaderProgram mBloomShader = null;
	private boolean mEnableBloom = false;
	private float mKernelVariance = 16.0f;
	private int mKernelWidth = 3;
	private float mThreshold = 0.80f;
	
	/* Used to control gbuffer data vizualization. */
	private ShaderProgram mVisShader = null;
	
	/* Locations of uniforms in the ubershader. */
	private int mLightPositionsUniformLocation = -1;
	private int mLightColorsUniformLocation = -1;
	private int mLightAttenuationsUniformLocation = -1;
	private int mNumLightsUniformLocation = -1;
	private int mEnableToonShadingUniformLocation = -1;
	
	// Shadow mapping uniforms
	private int mLightMatrixUniformLocation = -1;
	private int mInverseViewMatrixUniformLocation = -1;
	private int mShadowMapWidthUniformLocation = -1;
	private int mShadowMapHeightUniformLocation = -1;
	
	/* The shadow map ubershader locations */
	private int mHasShadowMapsUniformLocation = -1;
	private int mShadowTextureLocation = GBuffer_FinalSceneIndex + 1;
	private int mShadowModeUniformLocation = -1;
	
	/* SSAO modifications to the ubershader. */
	private int mEnableSSAOUniformLocation = -1;
	private boolean mEnableSSAO = false;
	
	/* All SSAO variables and uniform locations. */
	private ShaderProgram mSSAOShader;
	private int mNumRaysUniformLocation = -1;
	private int mSampleRaysUniformLocation = -1;
	private int mSampleRadiusUniformLocation = -1;
	private int mProjectionMatrixUniformLocation = -1;
	private int mScreenSizeUniformLocation = -1;
	
	/* The size of the sample rays array in the ssao shader. 
	 * This is queried directly from the shader file. */
	private int mMaxSSAORays;
	
	/* Local copies of the SSAO uniform data. */
	private Vector3f[] mSampleRays = null;
	private float mSampleRadius = 0.1f;
	
	/* The current shadow mode */
	private int mShadowMode = 0;
	
	/* All of the shadow mapping parameters */
	private float mBias =  0.000011f;
	private int mBiasUniformLocation = -1;
	
	private int mShadowSampleWidth = 4;
	private int mShadowSampleWidthUniformLocation = -1;
	
	private int mLightWidth = 16;
	private int mLightWidthUniformLocation = -1;
	
	/* The current sandDune texture */
	private boolean mWhichSandDune = true;
	private boolean mInitialize = false;
	private int mInitializeUniformLocation = -1;

	
	
	/* The size of the light uniform arrays in the ubershader. */
	private int mMaxLightsInUberShader = 40;
	
	
	/**
	 * Renders a single frame of the scene. This is the main method of the Renderer class.
	 * 
	 * @param drawable The drawable to render into.
	 * @param sceneRoot The root node of the scene to render.
	 * @param camera The camera describing the perspective to render from.
	 */
	public void render(GLAutoDrawable drawable, SceneObject sceneRoot, Camera camera, Camera shadowCamera)
	{
		GL2 gl = drawable.getGL().getGL2();		
				
		try
		{
			/* Reset lights array. It will be re-filled as the scene is traversed. */
			mLights.clear();
			
			if (shadowCamera != null) {
				fillGBuffer(gl, sceneRoot, shadowCamera);
			}
			
			/* 0. Cmpute sand dune buffer */
			//computeSandDuneBuffer(gl,mWhichSandDune);
			
			/* 1. Fill the gbuffer given this scene and camera. */ 
			fillGBuffer(gl, sceneRoot, camera);
			
			
			/* 2. Compute gradient buffer based on positions and normals, used for toon shading. */
			computeGradientBuffer(gl);
			computeSSAOBuffer(gl, camera);
			
			/* 3. Apply deferred lighting to the g-buffer. At this point, the opaque scene has been rendered. */
			lightGBuffer(gl, camera, shadowCamera);

			/* 4. If we're supposed to preview one gbuffer texture, do that now. 
			 *    Otherwise, envoke the final render pass (optional post-processing). */
//			if (mPreviewIndex == 2){
//				Util.renderTextureFullscreen(gl, mGBufferFBO.getColorTexture(GBuffer_SandDune1Index));
//				//Util.renderTextureFullscreen(gl, mSandDuneFBO.getColorTexture(SBuffer_SandDune1Index));
//			}
			if (mPreviewIndex >= 0 && mPreviewIndex < GBuffer_FinalSceneIndex)
			{
				Util.renderTextureFullscreen(gl, mGBufferFBO.getColorTexture(mPreviewIndex));
			}
			else
			{			
				finalPass(gl);					 								 
			}
		}
		catch (Exception err)
		{
			/* If an error occurs in all that, print it, but don't kill the whole program. */
			err.printStackTrace();
		}
	}
	
	
	/**
	 * All post-processing should be done in this method.
	 * If no post-processing is required it should display the final scene buffer.
	 * 
	 * @param gl The OpenGL state
	 */
	protected void finalPass(GL2 gl) throws OpenGLException
	{
		if(mEnableBloom)
		{
			/* Save state before we disable depth testing for blitting. */
			gl.glPushAttrib(GL2.GL_ENABLE_BIT);
			
			/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
			 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
			 * first. */
			gl.glDisable(GL2.GL_DEPTH_TEST);
			gl.glDisable(GL2.GL_BLEND);
			
			/* Bind the final scene texture for post-processing. */
			mGBufferFBO.getColorTexture(GBuffer_FinalSceneIndex).bind(gl, 0);
			
			/* Set all bloom shader uniforms. */
			mBloomShader.bind(gl);
			gl.glUniform1i(mBloomShader.getUniformLocation(gl, "KernelWidth"), mKernelWidth);
			gl.glUniform1f(mBloomShader.getUniformLocation(gl, "KernelVariance"), mKernelVariance);
			gl.glUniform1f(mBloomShader.getUniformLocation(gl, "Threshold"), mThreshold);
			
			/* Draw a full-screen quad to the framebuffer. */
			Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
			
			/* Unbind everything. */
			mBloomShader.unbind(gl);
			mGBufferFBO.getColorTexture(GBuffer_FinalSceneIndex).unbind(gl);

			/* Restore attributes (blending and depth-testing) to as they were before. */
			gl.glPopAttrib();
			
			/* Make sure nothing went wrong. */
			OpenGLException.checkOpenGLError(gl);
		}
		else if (mPreviewIndex >= 6 && mPreviewIndex <= 8)
		{
			/* The keys '7', '8', and '9' correspond to gbuffer data visualization. */
			/* Save state before we disable depth testing for blitting. */
			gl.glPushAttrib(GL2.GL_ENABLE_BIT);
			
			/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
			 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
			 * first. */
			gl.glDisable(GL2.GL_DEPTH_TEST);
			gl.glDisable(GL2.GL_BLEND);
			
			/* Bind the first four sections of the gbuffer. */
			mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).bind(gl, 0);
			mGBufferFBO.getColorTexture(GBuffer_PositionIndex).bind(gl, 1);
			mGBufferFBO.getColorTexture(GBuffer_MaterialIndex1).bind(gl, 2);
			mGBufferFBO.getColorTexture(GBuffer_MaterialIndex2).bind(gl, 3);
			
			/* Set the vis mode using the preview index. */
			mVisShader.bind(gl);
			gl.glUniform1i(mVisShader.getUniformLocation(gl, "VisMode"), mPreviewIndex - 6);
			
			/* Draw a full-screen quad to the framebuffer. */
			Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
			
			/* Unbind everything. */
			mVisShader.unbind(gl);
			mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).unbind(gl);
			mGBufferFBO.getColorTexture(GBuffer_PositionIndex).unbind(gl);
			mGBufferFBO.getColorTexture(GBuffer_MaterialIndex1).unbind(gl);
			mGBufferFBO.getColorTexture(GBuffer_MaterialIndex2).unbind(gl);

			/* Restore attributes (blending and depth-testing) to as they were before. */
			gl.glPopAttrib();
			
			/* Make sure nothing went wrong. */
			OpenGLException.checkOpenGLError(gl);
		}
		else
		{
			/* No post-processing is required; just display the unaltered scene. */
			Util.renderTextureFullscreen(gl, mGBufferFBO.getColorTexture(GBuffer_FinalSceneIndex));
		}
	}
	
	/**
	 * Clears the gbuffer and renders scene objects.
	 *
	 * @param gl The OpenGL state
	 * @param sceneRoot The root node of the scene to render.
	 * @param camera The camera describing the perspective to render from.
	 */
	private void fillGBuffer(GL2 gl, SceneObject sceneRoot, Camera camera) throws OpenGLException
	{
		/* First, bind and clear the gbuffer. */
		if (!camera.getIsShadowMapCamera()) 
		{
			mGBufferFBO.bindSome(gl, new int[]{GBuffer_DiffuseIndex, GBuffer_PositionIndex, GBuffer_MaterialIndex1, GBuffer_MaterialIndex2});
		}
		else 
		{		
			mShadowMapFBO.bindAll(gl);
		}
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		/* Update the projection matrix with this camera's projection matrix. */
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		GLU glu = GLU.createGLU(gl);
		glu.gluPerspective(camera.getFOV(), mViewportWidth / mViewportHeight, camera.getNear(), camera.getFar());
		
		/* Update the modelview matrix with this camera's eye transform. */
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		/* Find the inverse of the camera scale, position, and orientation in world space, accounting
		 * for the fact that the camera might be nested inside other objects in the scenegraph.*/
		float cameraScale = 1.0f / camera.transformDistanceToWorldSpace(1.0f);
		Point3f cameraPosition = camera.transformPointToWorldSpace(new Point3f(0.0f, 0.0f, 0.0f));
		AxisAngle4f cameraOrientation = new AxisAngle4f();
		cameraOrientation.set(camera.transformOrientationToWorldSpace(new Quat4f(0.0f, 0.0f, 0.0f, 1.0f)));
		
		/* Apply the camera transform to OpenGL. */
		gl.glScalef(cameraScale, cameraScale, cameraScale);
		gl.glRotatef(cameraOrientation.angle * 180.0f / (float)Math.PI, -cameraOrientation.x, -cameraOrientation.y, -cameraOrientation.z);
		gl.glTranslatef(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
		
		/* Check for errors before rendering, to help isolate. */
		OpenGLException.checkOpenGLError(gl);
		
		/* Render the scene. */
		renderObject(gl, camera, sceneRoot);

		/* GBuffer is filled, so unbind it. */
		if (!camera.getIsShadowMapCamera()) 
		{
			mGBufferFBO.unbind(gl);
		}	
		else 
		{
			mShadowMapFBO.unbind(gl);
		}
		
		/* Check for errors after rendering, to help isolate. */
		OpenGLException.checkOpenGLError(gl);
	}

	/**
	 * Computes position and normal gradients based on the position and normal textures of the GBuffer, for 
	 * use in edge detection (e.g. toon rendering). 
	 */
	private void computeGradientBuffer(GL2 gl) throws OpenGLException
	{
		/* Bind silhouette buffer as output. */
		mGBufferFBO.bindOne(gl, GBuffer_GradientsIndex);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		
		/* Save state before we disable depth testing for blitting. */
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		
		/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
		 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
		 * first. */
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
		
		/* Bind the diffuse and position textures so the edge-detection shader can read out position and normal data. */
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).bind(gl, 0);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).bind(gl, 1);
		
		/* Bind silhouette shader and render. */
		mSilhouetteShader.bind(gl);
		Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
		
		/* Unbind everything. */
		mSilhouetteShader.unbind(gl);
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).unbind(gl);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).unbind(gl);

		mGBufferFBO.unbind(gl);

		/* Restore attributes (blending and depth-testing) to as they were before. */
		gl.glPopAttrib();
	}
	
	/**
	 * Computes SSAO values based on the position and normal textures of the GBuffer. 
	 */
	private void computeSSAOBuffer(GL2 gl, Camera camera) throws OpenGLException
	{
		/* Bind the SSAO buffer as output. */
		mGBufferFBO.bindOne(gl, GBuffer_SSAOIndex);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		
		/* Save state before we disable depth testing for blitting. */
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		
		/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
		 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
		 * first. */
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
		
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).bind(gl, 0);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).bind(gl, 1);
		
		/* We need to disable interpolation on the g-buffer, otherwise we get ringing artifacts!
		 * This won't reduce the quality of our ssao because the g-buffer is full resolution. */
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).enableInterpolation(gl, false);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).enableInterpolation(gl, false);
		
		/* Bind the SSAO shader and update the uniforms. */
		mSSAOShader.bind(gl);
		
		float[] projMatrix = Util.fromMatrix4f(camera.getProjectionMatrix(mViewportWidth, mViewportHeight));
		gl.glUniform1f(mSampleRadiusUniformLocation, mSampleRadius);
		gl.glUniform2f(mScreenSizeUniformLocation, mViewportWidth, mViewportHeight);
		gl.glUniformMatrix4fv(mProjectionMatrixUniformLocation, 1, false, projMatrix, 0);
		
		if (mSampleRays != null) {
			if (mSampleRays.length > mMaxSSAORays) {
				throw new RuntimeException("MAX_RAYS is " + mMaxSSAORays + ". " + mSampleRays.length + " is too many!");
			}
			
			gl.glUniform1i(mNumRaysUniformLocation, mSampleRays.length);
			for (int i = 0; i < mSampleRays.length; i++) {
				gl.glUniform3f(mSampleRaysUniformLocation + i, mSampleRays[i].x, mSampleRays[i].y, mSampleRays[i].z);
			}
		}
		else {
			gl.glUniform1i(mNumRaysUniformLocation, 0);
		}
		
		
		/* Render. */
		Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
		
		/* Unbind everything. */
		mSSAOShader.unbind(gl);
		
		/* Re-enable interpolation. */
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).enableInterpolation(gl, true);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).enableInterpolation(gl, true);
		
		mGBufferFBO.getColorTexture(GBuffer_DiffuseIndex).unbind(gl);
		mGBufferFBO.getColorTexture(GBuffer_PositionIndex).unbind(gl);

		mGBufferFBO.unbind(gl);

		/* Restore attributes (blending and depth-testing) to as they were before. */
		gl.glPopAttrib();
	}
	
	
	/**
	 * Computes position and normal gradients based on the position and normal textures of the GBuffer, for 
	 * use in edge detection (e.g. toon rendering). 
	 */
//	private void computeSandDuneBuffer(GL2 gl, boolean swap) throws OpenGLException
//	{
//		/* Bind sanddune buffer as output. */
//		if (swap){ mGBufferFBO.bindOne(gl, GBuffer_SandDune1Index); }
//		else { mGBufferFBO.bindOne(gl, GBuffer_SandDune2Index); }
//
//		
//		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
//		
//		/* Save state before we disable depth testing for blitting. */
//		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
//		
//		/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
//		 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
//		 * first. */
//		gl.glDisable(GL2.GL_DEPTH_TEST);
//		gl.glDisable(GL2.GL_BLEND);
//		
//		mInitializeUniformLocation = mSandDuneShader.getUniformLocation(gl, "Initialize");
//		gl.glUniform1i(mInitializeUniformLocation, (mInitialize ? 1: 0) );
//
//		
//		/* Bind previous sandDune buffer. */
//		if (swap){ mGBufferFBO.getColorTexture(GBuffer_SandDune2Index).bind(gl, 0);}
//		else{ mGBufferFBO.getColorTexture(GBuffer_SandDune1Index).bind(gl, 0); }
//		
//		/* Bind sandDune shader and render. */
//		mSandDuneShader.bind(gl);
//		
//		//System.out.println(oldSandDuneTexture);
//		//if (oldSandDuneTexture != null) oldSandDuneTexture.bind(gl, 0);
//
//		Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
//
//		//Buffer nb =    mSandDuneFBO.getColorTexture(GBuffer_SandDune1Index).copyTextureImage(gl);
//		//this.oldSandDuneTexture = new Texture2D(gl, Format.RGBA, Datatype.FLOAT16, (int) mViewportWidth, (int) mViewportHeight,  nb);
//		
//		/* Unbind everything. */
//		mSandDuneShader.unbind(gl);		
//		if (swap){ mGBufferFBO.getColorTexture(GBuffer_SandDune2Index).unbind(gl);}
//		else{ mGBufferFBO.getColorTexture(GBuffer_SandDune1Index).unbind(gl); }
//
//	//	oldSandDuneTexture.unbind(gl);
//		mGBufferFBO.unbind(gl);
//
//		/* Restore attributes (blending and depth-testing) to as they were before. */
//		gl.glPopAttrib();
//		
//		mWhichSandDune = !mWhichSandDune;
//		mInitialize = true;
//	}
	
	/**
	 * Applies lighting to an already-filled gbuffer to produce the final scene. Output is sent 
	 * to the main framebuffer of the view/window.
	 * 
	 * @param gl The OpenGL state.
	 * @param camera Camera from whose perspective we are rendering.
	 */
	private void lightGBuffer(GL2 gl, Camera camera, Camera shadowCamera) throws OpenGLException, ScenegraphException
	{
		/* Need some lights, otherwise it will just be black! */
		if (mLights.size() == 0)
		{
			throw new ScenegraphException("Must have at least one light in the scene!");
		}
		
		/* Can't have more lights than the shader supports. */
		if (mLights.size() > mMaxLightsInUberShader)
		{
			throw new ScenegraphException(mLights.size() + " is too many lights; ubershader only supports " + mMaxLightsInUberShader + ".");
		}
		
		/* Bind final scene buffer as output target for this pass. */
		mGBufferFBO.bindOne(gl, GBuffer_FinalSceneIndex);
		
		/* Save state before we disable depth testing for blitting. */
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		
		/* Disable depth test and blend, since we just want to replace the contents of the framebuffer.
		 * Since we are rendering an opaque fullscreen quad here, we don't bother clearing the buffer
		 * first. */
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
		
		/* Bind all GBuffer source textures so the ubershader can read them. */
		for (int i = 0; i < GBuffer_FinalSceneIndex; ++i)
		{
			mGBufferFBO.getColorTexture(i).bind(gl, i);
		}
		
		/* Bind ubershader. */
		mUberShader.bind(gl);

		/* Update all the ubershader uniforms with up-to-date light information. */
		for (int i = 0; i < mLights.size(); ++i)
		{
			/* Transform each light position to eye space. */
			Light light = mLights.get(i);
			Point3f eyespacePosition = camera.transformPointFromWorldSpace(light.transformPointToWorldSpace(new Point3f()));
			
			/* Send light color and eyespace position to the ubershader. */
			gl.glUniform3f(mLightPositionsUniformLocation + i, eyespacePosition.x, eyespacePosition.y, eyespacePosition.z);
			gl.glUniform3f(mLightColorsUniformLocation + i, light.getColor().x, light.getColor().y, light.getColor().z);
			
			if (light instanceof PointLight)
			{
				gl.glUniform3f(mLightAttenuationsUniformLocation + i, 
						((PointLight)light).getConstantAttenuation(), 
						((PointLight)light).getLinearAttenuation(), 
						((PointLight)light).getQuadraticAttenuation());
			}
			else
			{
				gl.glUniform3f(mLightAttenuationsUniformLocation + i, 1.0f, 0.0f, 0.0f);
			}
		}
		
		/* Ubershader needs to know how many lights. */
		gl.glUniform1i(mNumLightsUniformLocation, mLights.size());	
		gl.glUniform1i(mEnableToonShadingUniformLocation, (mEnableToonShading ? 1 : 0));			
		gl.glUniform1i(mEnableSSAOUniformLocation, (mEnableSSAO ? 1 : 0));
		
		gl.glUniform1i(mHasShadowMapsUniformLocation, shadowCamera == null ? 0 : 1);
		gl.glUniform1i(mShadowModeUniformLocation, mShadowMode);
		
		if (shadowCamera != null) {
			// TODO PA3: Set the LightMatrix and InverseViewMatrix uniforms.
			
			/* Set LightMatrix, which sends points from world space into (light) camera clip coordinates space. */
			Matrix4f LightMatrix = new Matrix4f(); 
			LightMatrix.mul(shadowCamera.getProjectionMatrix(mViewportWidth, mViewportHeight), shadowCamera.getViewMatrix());
			float[] LightMatrixArray = new float[16];
			for (int i =0; i<4; i++){
				for (int j =0; j<4; j++){
					LightMatrixArray[4*i+j] = LightMatrix.getElement(i, j);
				}
			}
			gl.glUniformMatrix4fv(mLightMatrixUniformLocation, 1, true, LightMatrixArray,0);
			
			/* Set InverseViewMatrix, which sends points from the (eye) camera local space into world space. */
			
			Matrix4f InverseViewMatrix = new Matrix4f(); 
			InverseViewMatrix.set(camera.getViewMatrix());
			InverseViewMatrix.invert();
			float[] InverseViewMatrixArray = new float[16];
			for (int i =0; i<4; i++){
				for (int j =0; j<4; j++){
					InverseViewMatrixArray[4*i+j] = InverseViewMatrix.getElement(i, j);
				}
			}
			gl.glUniformMatrix4fv(mInverseViewMatrixUniformLocation, 1, true, InverseViewMatrixArray,0);
			
			gl.glUniform1f(mBiasUniformLocation, mBias);
			gl.glUniform1f(mShadowMapWidthUniformLocation, mViewportWidth);
			gl.glUniform1f(mShadowMapHeightUniformLocation, mViewportHeight);
			gl.glUniform1f(mShadowSampleWidthUniformLocation, (float) mShadowSampleWidth);
			gl.glUniform1f(mLightWidthUniformLocation, (float) mLightWidth);
			
			mShadowMapFBO.getDepthTexture().bind(gl, mShadowTextureLocation);
		}
		

		/* Let there be light! */
		Util.drawFullscreenQuad(gl, mViewportWidth, mViewportHeight);
		
		/* Unbind everything. */
		mUberShader.unbind(gl);
		
		if (shadowCamera != null) 
		{
			mShadowMapFBO.getDepthTexture().unbind(gl);
		}
		
		for (int i = 0; i < GBuffer_FinalSceneIndex; ++i)
		{
			mGBufferFBO.getColorTexture(i).unbind(gl);
		}

		/* Unbind rendering target. */
		mGBufferFBO.unbind(gl);

		/* Restore attributes (blending and depth-testing) to as they were before. */
		gl.glPopAttrib();
	}
	
	/**
	 * Renders a scenegraph node and its children.
	 * 
	 * @param gl The OpenGL state.
	 * @param camera The camera rendering the scene.
	 * @param obj The object to render. If this is a Geometry object, its meshes are rendered.
	 *        If this is a Light object, it is added to the list of lights. Other objects are ignored.
	 * @param dcm The dynamic cub map that we are rendering to. If it is not equal to null, we check to
	 *        see if the object being rendered is the same as the one stored in the DCM. If it is, then
	 *        we don't render it.
	 */
	private void renderObject(GL2 gl, Camera camera, SceneObject obj) throws OpenGLException
	{
		/* If the object is not visible, we skip the rendition of it and all its children */
		if (!obj.isVisible()) {
			return;
		}
		
		/* Save matrix before applying this object's transformation. */
		gl.glPushMatrix();
		
		/* Get this object's transformation. */
		float scale = obj.getScale();
		Point3f position = obj.getPosition();
		AxisAngle4f orientation = new AxisAngle4f();
		orientation.set(obj.getOrientation());
		
		/* Apply this object's transformation. */
		gl.glTranslatef(position.x, position.y, position.z);
		gl.glRotatef(orientation.angle * 180.0f / (float)Math.PI, orientation.x, orientation.y, orientation.z);
		gl.glScalef(scale, scale, scale);
		
		/* Render this object as appropriate for its type. */
		if (obj instanceof Geometry)
		{
			for (Mesh mesh : ((Geometry)obj).getMeshes())
			{
				renderMesh(gl, mesh);
			}
		}
		else if (obj instanceof Light)
		{
			mLights.add((Light)obj);
		}
		
		/* Render this object's children. */
		for (SceneObject child : obj.getChildren())
		{
			renderObject(gl, camera, child);
		}
		
		/* Restore transformation matrix and check for errors. */
		gl.glPopMatrix();
		OpenGLException.checkOpenGLError(gl);
	}

	/**
	 * Renders a single trimesh.
	 * 
	 * @param gl The OpenGL state.
	 * @param mesh The mesh to render.
	 */
	private void renderMesh(GL2 gl, Mesh mesh) throws OpenGLException
	{
		/* Save all state to isolate any changes made by this mesh's material. */
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glPushClientAttrib((int)GL2.GL_CLIENT_ALL_ATTRIB_BITS);
		
		/* Activate the material. */
		mesh.getMaterial().retrieveShader(gl, mShaderCache);
		mesh.getMaterial().bind(gl);
		
			
		/* Enable the required vertex arrays and send data. */
		if (mesh.getVertexData() == null)
		{
			throw new OpenGLException("Mesh must have non-null vertex data to render!");
		}
		else
		{
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, mesh.getVertexData());
		}

		if (mesh.getNormalData() == null)
		{
			gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		}
		else
		{
			gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
			gl.glNormalPointer(GL2.GL_FLOAT, 0, mesh.getNormalData());
		}
		
		if (mesh.getTexCoordData() == null)
		{
			gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		}
		else
		{
			gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, mesh.getTexCoordData());
		}

		/* Send custom vertex attributes (if any) to OpenGL. */
		bindRequiredMeshAttributes(gl, mesh);
		
		/* Render polygons. */
		gl.glDrawElements(getOpenGLPrimitiveType(mesh.getVerticesPerPolygon()), 
						  mesh.getVerticesPerPolygon() * mesh.getPolygonCount(), 
						  GL2.GL_UNSIGNED_INT, 
						  mesh.getPolygonData());
				
		/* Deactivate material and restore state. */
		mesh.getMaterial().unbind(gl);

		
		/* Render mesh wireframe if we're supposed to. */
		if (mRenderWireframes && mesh.getVerticesPerPolygon() > 2)
		{
			mWireframeMaterial.retrieveShader(gl, mShaderCache);
			mWireframeMaterial.bind(gl);

			gl.glLineWidth(1.0f);
			gl.glPolygonOffset(0.0f, 1.0f);
			gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);

			/* Render polygons. */
			gl.glDrawElements(getOpenGLPrimitiveType(mesh.getVerticesPerPolygon()), 
					  mesh.getVerticesPerPolygon() * mesh.getPolygonCount(), 
					  GL2.GL_UNSIGNED_INT, 
					  mesh.getPolygonData());					
			
			mWireframeMaterial.unbind(gl);
		}

		/* Render marked edges (e.g. for subdiv creases), if we're supposed to and if they exist. */
		if (mRenderWireframes && mesh.getEdgeData() != null)
		{
			mWireframeMarkedEdgeMaterial.retrieveShader(gl, mShaderCache);
			mWireframeMarkedEdgeMaterial.bind(gl);

			gl.glLineWidth(5.0f);
			gl.glPolygonOffset(0.0f, 1.0f);
			gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
			gl.glDrawElements(GL2.GL_LINES, mesh.getEdgeData().capacity(), GL2.GL_UNSIGNED_INT, mesh.getEdgeData());
			
			mWireframeMarkedEdgeMaterial.unbind(gl);
		}
		
		gl.glPopClientAttrib();
		gl.glPopAttrib();
		
		/* Check for errors. */
		OpenGLException.checkOpenGLError(gl);
	}
	
	/**
	 * Binds all custom vertex attributes required by a mesh's material to buffers provided
	 * by that mesh.
	 * 
	 * @param gl The OpenGL state.
	 * @param mesh All custom vertex attributes required by mesh's material and shader are bound to the 
	 *        correspondingly-named buffers in the mesh's `vertexAttribData` map.
	 *        
	 * @throws OpenGLException If a required attribute isn't supplied by the mesh.
	 */
	void bindRequiredMeshAttributes(GL2 gl, Mesh mesh) throws OpenGLException
	{
		ShaderProgram shader = mesh.getMaterial().getShaderProgram();
		
		for (String attrib : mesh.getMaterial().getRequiredVertexAttributes())
		{
			/* Ignore attributes which aren't actually used in the shader. */
			int location = shader.getAttribLocation(gl, attrib);
			if (location < 0)
			{
				continue;
			}
			
			/* Get data for this attribute from the mesh. */
			FloatBuffer attribData = mesh.vertexAttribData.get(attrib);
			
			/* This attribute is required, so throw an exception if the mesh doesn't supply it. */
			if (attribData == null)
			{
				throw new OpenGLException("Material requires vertex attribute '" + attrib + "' which is not present in mesh's vertexAttribData.");
			}
			else
			{
				gl.glEnableVertexAttribArray(location);
				gl.glVertexAttribPointer(location, attribData.capacity() / mesh.getVertexCount(), GL2.GL_FLOAT, false, 0, attribData);
			}
		}
	}

	/**
	 * Returns the OpenGL primitive type for the given size of polygon (e.g. GL_TRIANGLES for 3).
	 * @throws OpenGLException For values not in {1, 2, 3, 4}.
	 */
	private int getOpenGLPrimitiveType(int verticesPerPolygon) throws OpenGLException
	{
		switch (verticesPerPolygon)
		{
		case 1: return GL2.GL_POINTS;
		case 2: return GL2.GL_LINES;
		case 3: return GL2.GL_TRIANGLES;
		case 4: return GL2.GL_QUADS;
		default: throw new OpenGLException("Don't know how to render mesh with " + verticesPerPolygon + " vertices per polygon.");
		}
	}
	
	/**
	 * Requests that the renderer should render a preview of the indicated gbuffer texture, instead of the final shaded scene.
	 * 
	 * @param bufferIndex The index of the texture to preview. If `bufferIndex` is out of range (less than 0 or greater than
	 *        the index of the last gbuffer texture), the preview request will be ignored, and the renderer will render a 
	 *        shaded scene.
	 */
	public void previewGBuffer(int bufferIndex)
	{
		mPreviewIndex = bufferIndex;
	}

	/**
	 * Cancels a preview request made with `previewGBuffer()`, causing the renderer to render the final shaded scene when it renders.
	 */
	public void unpreviewGBuffer()
	{
		mPreviewIndex = -1;
	}
	
	/**
	 * Enables or disables toon shading.
	 */
	public void setToonShading(boolean toonShade)
	{
		mEnableToonShading = toonShade;
	}
	
	/**
	 * Returns true if toon shading is enabled.
	 */
	public boolean getToonShading()
	{
		return mEnableToonShading;
	}
	
	/**
	 * Enables or disables rendering of mesh edges.
	 * 
	 * All edges are rendered in thin grey wireframe, and marked edges (e.g. creases) are rendered in thick pink. 
	 */
	public void setRenderWireframes(boolean wireframe)
	{
		mRenderWireframes = wireframe;
	}
	
	/**
	 * Returns true if mesh edges are being rendered.
	 */
	public boolean getRenderWireframes()
	{
		return mRenderWireframes;
	}
	
	/**
	 * Enables or disables bloom.
	 */
	public void setBloom(boolean bloom)
	{
		mEnableBloom = bloom;
	}
	
	/**
	 * Returns true if bloom is enabled.
	 */
	public boolean getBloom()
	{
		return mEnableBloom;
	}
	
	/**
	 * Sets the cut-off threshold for the bloom algorithm.
	 */
	public void setBloomThreshold(float threshold)
	{
		mThreshold = threshold;
	}
	
	/**
	 * Gets the cut-off threshold for the bloom algorithm.
	 */
	public float getBloomThreshold()
	{
		return mThreshold;
	}
	
	/**
	 * Sets the variance of the Gaussian kernel used by bloom.
	 */
	public void setBloomVariance(float variance)
	{
		mKernelVariance = variance;
	}
	
	/**
	 * Gets the variance of the Gaussian kernel used by bloom.
	 */
	public float getBloomVariance()
	{
		return mKernelVariance;
	}
	
	/**
	 * Sets the half-width of the Gaussian kernel (in pixels).
	 * The end-to-end width of the kernel is actually 2*width + 1 pixels.
	 */
	public void setBloomWidth(int width)
	{
		mKernelWidth = width;
	}
	
	/**
	 * Gets the half-width of the Gaussian kernel (in pixels).
	 * The end-to-end width of the kernel is actually 2*width + 1 pixels.
	 */
	public int getBloomWidth()
	{
		return mKernelWidth;
	}
	
	/**
	 * Sets the shadow map bias
	 */
	public void setShadowMapBias(float bias)
	{
		mBias = bias;
	}
	
	/**
	 * Gets the shadow map bias
	 */
	public float getShadowMapBias()
	{
		return mBias;
	}
	
	/**
	 * Increments the shadow mode
	 */
	public void incrementShadowMode()
	{
		mShadowMode = (mShadowMode + 1) % 3;
	}
	
	public int getShadowMode() {
		return mShadowMode;
	}
	
	/** Getter and setter for shadow sample width */
	public void setShadowSampleWidth(int width)
	{
		mShadowSampleWidth = width;
	}
	
	public int getShadowSampleWidth()
	{
		return mShadowSampleWidth;
	}
	
	/** Getter and setter for the light width */
	public void setLightWidth(int width)
	{
		mLightWidth = width;
	}
	
	public int getLightWidth()
	{
		return mLightWidth;
	}
	
	/**
	 * Turns SSAO on/off in the final scene.
	 */
	public void setSSAOEnabled(boolean enable)
	{
		mEnableSSAO = enable;
	}
	
	/**
	 * Gets the current SSAO enabled state.
	 */
	public boolean getSSAOEnabled()
	{
		return mEnableSSAO;
	}
	
	/**
	 * Sets the SSAO sample radius (in pixels).
	 */
	public void setSSAORadius(float radius)
	{
		mSampleRadius = radius;
	}
	
	/**
	 * Gets the SSAO sample radius (in pixels).
	 */
	public float getSSAORadius()
	{
		return mSampleRadius;
	}
	
	/**
	 * This method should populate mSampleRays with 
	 */
	public void createNewSSAORays(int numRays)
	{
		// TODO PA4: Generate numRays random normalized vectors.	
		Random generator = new Random();
		
		mSampleRays = new Vector3f[numRays];
		for(int i = 0; i < numRays; i++) {
			// use equation 1 & 2 from http://mathword.wolfram.com/SpherePointPicking.html.
			
			double u = generator.nextDouble();
			double v = generator.nextDouble();

			double theta = 2* Math.PI*u;
			double phi = Math.acos(2*v- 1.0);
			
			mSampleRays[i] = new Vector3f(((float)Math.cos(theta)*(float)Math.sin(phi)), ((float)Math.sin(theta)*(float)Math.sin(phi)), ((float)Math.abs(Math.cos(phi))));     
			mSampleRays[i].normalize();
		}
	}
	
	/**
	 * Returns the current number of sample rays.
	 */
	public int getSSAORayCount() {
		return (mSampleRays == null) ? 0 : mSampleRays.length;
	}
	
	/**
	 * Returns the maximum number of sample rays supported by the SSAO shader.
	 */
	public int getMaxSSAORays()
	{
		return mMaxSSAORays;
	}

	
	/**
	 * Performs one-time initialization of OpenGL state and shaders used by this renderer.
	 * @param drawable The OpenGL drawable this renderer will be rendering to.
	 */
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		
		/* Enable depth testing. */
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);

		try
		{
			/* Load the ubershader. */
			mUberShader = new ShaderProgram(gl, "shaders/ubershader");

			/* Set material buffer indices once here, since they never have to change. */
			mUberShader.bind(gl);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "DiffuseBuffer"), 0);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "PositionBuffer"), 1);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "MaterialParams1Buffer"), 2);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "MaterialParams2Buffer"), 3);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "SilhouetteBuffer"), 4);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "SSAOBuffer"), 5);
			
//			gl.glUniform1i(mUberShader.getUniformLocation(gl, "SandDune1Buffer"), 4);
//			gl.glUniform1i(mUberShader.getUniformLocation(gl, "SandDune2Buffer"), 7);
			
			gl.glUniform3f(mUberShader.getUniformLocation(gl, "SkyColor"), 0.3f, 0.3f, 0.3f);
			//gl.glUniform3f(mUberShader.getUniformLocation(gl, "SkyColor"), 0.1f, 0.1f, 0.1f);
			gl.glUniform1i(mUberShader.getUniformLocation(gl, "ShadowMap"), mShadowTextureLocation);
			mUberShader.unbind(gl);			
			
			/* Get locations of the lighting uniforms, since these will have to be updated every frame. */
			mLightPositionsUniformLocation = mUberShader.getUniformLocation(gl, "LightPositions");
			mLightColorsUniformLocation = mUberShader.getUniformLocation(gl, "LightColors");
			mLightAttenuationsUniformLocation = mUberShader.getUniformLocation(gl, "LightAttenuations");
			mNumLightsUniformLocation = mUberShader.getUniformLocation(gl, "NumLights");
			mEnableToonShadingUniformLocation = mUberShader.getUniformLocation(gl, "EnableToonShading");
			mEnableSSAOUniformLocation = mUberShader.getUniformLocation(gl, "EnableSSAO");
			
			/* Shadow map uniforms */
			mHasShadowMapsUniformLocation = mUberShader.getUniformLocation(gl, "HasShadowMaps");
			mLightMatrixUniformLocation = mUberShader.getUniformLocation(gl, "LightMatrix");
			mInverseViewMatrixUniformLocation = mUberShader.getUniformLocation(gl, "InverseViewMatrix");
			mBiasUniformLocation = mUberShader.getUniformLocation(gl, "bias");
			mShadowModeUniformLocation = mUberShader.getUniformLocation(gl, "ShadowMode");
			mShadowMapWidthUniformLocation = mUberShader.getUniformLocation(gl, "ShadowMapWidth");
			mShadowMapHeightUniformLocation = mUberShader.getUniformLocation(gl, "ShadowMapHeight");
			mShadowSampleWidthUniformLocation = mUberShader.getUniformLocation(gl, "ShadowSampleWidth");
			mLightWidthUniformLocation = mUberShader.getUniformLocation(gl, "LightWidth");
			
			/* Get the maximum number of lights the shader supports. */
			mMaxLightsInUberShader = mUberShader.getUniformArraySize(gl, "LightPositions");
			int count[] = new int[1];
			int maxLen[] = new int[1];
			
			/* Start by figuring out how many uniforms there are and what the name buffer size should be. */
		    gl.glGetProgramiv(mUberShader.getHandle(), GL2.GL_ACTIVE_UNIFORMS, count, 0);
		    gl.glGetProgramiv(mUberShader.getHandle(), GL2.GL_ACTIVE_UNIFORM_MAX_LENGTH, maxLen, 0);
		    
		    int size[] = new int[1];
		    int type[] = new int[1];
		    int used[] = new int[1];
			byte name[] = new byte[maxLen[0]];

			/* Loop over the uniforms until we find "LightPositions" and grab its size. */
		    for(int i = 0; i < count[0]; ++i)
		    {
		    	/* We provide arrays for all fields (even if we don't use them) to prevent crashes. */
		    	gl.glGetActiveUniform(mUberShader.getHandle(), i, maxLen[0], used, 0, size, 0, type, 0, name, 0);
		    	
		    	String str = new String(name, 0, used[0]);
		    	if(str.equals("LightPositions"))
		    	{
		    		mMaxLightsInUberShader = size[0];
		    		break;
		    	}
		    }
			
			/* Load the silhouette (edge-detection) shader. */
			mSilhouetteShader = new ShaderProgram(gl, "shaders/silhouette");

			mSilhouetteShader.bind(gl);
			gl.glUniform1i(mSilhouetteShader.getUniformLocation(gl, "DiffuseBuffer"), 0);
			gl.glUniform1i(mSilhouetteShader.getUniformLocation(gl, "PositionBuffer"), 1);
			mSilhouetteShader.unbind(gl);

			/* Load the sandDune shader. */
//			mSandDuneShader = new ShaderProgram(gl, "shaders/sanddune");
//
//			mSandDuneShader.bind(gl);
//			gl.glUniform1i(mSandDuneShader.getUniformLocation(gl, "PreviousSandDuneBuffer"), 0);
//			mSandDuneShader.unbind(gl);
			
			/* Load the bloom shader. */
			mBloomShader = new ShaderProgram(gl, "shaders/bloom");
			
			mBloomShader.bind(gl);
			gl.glUniform1i(mBloomShader.getUniformLocation(gl, "FinalSceneBuffer"), 0);
			mBloomShader.unbind(gl);
			
			/* Load the visualization shader. */
			mVisShader = new ShaderProgram(gl, "shaders/visualize");
			
			mVisShader.bind(gl);
			gl.glUniform1i(mVisShader.getUniformLocation(gl, "DiffuseBuffer"), 0);
			gl.glUniform1i(mVisShader.getUniformLocation(gl, "PositionBuffer"), 1);
			gl.glUniform1i(mVisShader.getUniformLocation(gl, "MaterialParams1Buffer"), 2);
			gl.glUniform1i(mVisShader.getUniformLocation(gl, "MaterialParams2Buffer"), 3);
			mVisShader.unbind(gl);
			
			/* Load the SSAO shader. */
			mSSAOShader = new ShaderProgram(gl, "shaders/ssao");
			
			mSSAOShader.bind(gl);
			gl.glUniform1i(mSSAOShader.getUniformLocation(gl, "DiffuseBuffer"), 0);
			gl.glUniform1i(mSSAOShader.getUniformLocation(gl, "PositionBuffer"), 1);
			mSSAOShader.unbind(gl);
			
			mNumRaysUniformLocation = mSSAOShader.getUniformLocation(gl, "NumRays");
			mSampleRaysUniformLocation = mSSAOShader.getUniformLocation(gl, "SampleRays");
			mSampleRadiusUniformLocation = mSSAOShader.getUniformLocation(gl, "SampleRadius");
			mProjectionMatrixUniformLocation = mSSAOShader.getUniformLocation(gl, "ProjectionMatrix");
			mScreenSizeUniformLocation = mSSAOShader.getUniformLocation(gl, "ScreenSize");
			
			/* Query the max number of sample rays. */
		    mMaxSSAORays = mSSAOShader.getUniformArraySize(gl, "SampleRays");
		    
		    /* Start with half of the max number of rays. */
		    createNewSSAORays(mMaxSSAORays / 2);
		    
			/* Load the material used to render mesh edges (e.g. creases for subdivs). */
			mWireframeMaterial = new UnshadedMaterial(new Color3f(0.8f, 0.8f, 0.8f));
			mWireframeMarkedEdgeMaterial = new UnshadedMaterial(new Color3f(1.0f, 0.0f, 1.0f));
			
			/* Make sure nothing went wrong. */
			OpenGLException.checkOpenGLError(gl);
		}
		catch (Exception err)
		{
			/* If something did go wrong, we can't render - so just die. */
			err.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Called whenever the OpenGL context changes size. This renderer resizes the gbuffer 
	 * so it's always the same size as the viewport.
	 * 
	 * @param drawable The drawable being rendered to.
	 * @param width The new viewport width.
	 * @param height The new viewport height.
	 */
	public void resize(GLAutoDrawable drawable, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
		
		/* Store viewport size. */
		mViewportWidth = width;
		mViewportHeight = height;
		
		/* If we already had a gbuffer, release it. */
		if (mGBufferFBO != null)
		{
			mGBufferFBO.releaseGPUResources(gl);
			mShadowMapFBO.releaseGPUResources(gl);
			mSandDuneFBO.releaseGPUResources(gl);
		}
		
		/* Make a new gbuffer with the new size. */
		try
		{
			mGBufferFBO = new FramebufferObject(gl, Format.RGBA, Datatype.FLOAT16, width, height, GBuffer_Count, true, true);
			mShadowMapFBO = new FramebufferObject(gl, Format.RGBA, Datatype.FLOAT16, width, height, GBuffer_Count, true, false);
			if (mSandDuneFBO == null) mSandDuneFBO = new FramebufferObject(gl, Format.RGBA, Datatype.FLOAT16, width, height, GBuffer_Count, true, false);
		}
		catch (OpenGLException err)
		{
			/* If that fails, we can't render - so just die. */
			err.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Releases all OpenGL resources (shaders and FBOs) owned by this renderer.
	 */
	public void releaseGPUResources(GL2 gl)
	{
		mGBufferFBO.releaseGPUResources(gl);
		mUberShader.releaseGPUResources(gl);
		mSilhouetteShader.releaseGPUResources(gl);
		mBloomShader.releaseGPUResources(gl);
		mVisShader.releaseGPUResources(gl);
		mShadowMapFBO.releaseGPUResources(gl);
		mSandDuneShader.releaseGPUResources(gl);
		mSandDuneFBO.releaseGPUResources(gl);
	}
}

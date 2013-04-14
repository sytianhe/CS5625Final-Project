package cs5625.deferred.ui;

import java.awt.Dimension;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import cs5625.deferred.apps.SceneController;

/**
 * MainViewWindow.java
 * 
 * The MainViewWindow class is responsible for creating the OpenGL window and view, and
 * for forwarding user actions and OpenGL events to the controller.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-23
 */
public class MainViewWindow extends JFrame implements GLEventListener
{
	/* JFrame implements Serializable, so Eclipse wants us to define a version number. */
	private static final long serialVersionUID = 1L;
	
	/* Default viewport size. */
	private static int DEFAULT_VIEW_WIDTH = 800;
	private static int DEFAULT_VIEW_HEIGHT = 600;
	
	/* The OpenGL view/context object. */
	private GLCanvas mView;
	
	/* The controller which is running the show; events are forwarded here. */
	private SceneController mController;

	/**
	 * Creates a window containing an OpenGL view which sends its events to a controller.
	 *  
	 * @param title The window title.
	 * @param controller The controller to send user actions and OpenGL events to.
	 */
	public MainViewWindow(String title, SceneController controller)
	{
		/* Initialize generic JFrame stuff. */
		super(title);
		setPreferredSize(new Dimension(DEFAULT_VIEW_WIDTH, DEFAULT_VIEW_HEIGHT));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* Save the controller for later. */
		mController = controller;
		
		/* Create the OpenGL view, register listeners as appropriate, and add it to this window. */
		mView = new GLCanvas();
		mView.addGLEventListener(this);
		mView.addMouseListener(controller);
		mView.addMouseWheelListener(controller);
		mView.addMouseMotionListener(controller);
		mView.addKeyListener(controller);
		getContentPane().add(mView);

		/* Size window according to preferred sizes of its contents. */
		pack();

		/* Focus the view so keystrokes to there first. */
		mView.requestFocusInWindow();
	}
	
	@Override
	public void repaint()
	{
		super.repaint();
		mView.repaint();
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		mController.renderGL(drawable);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		mController.disposeGL(drawable);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		mController.initGL(drawable);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		mController.resizeGL(drawable, width, height);
	}
}

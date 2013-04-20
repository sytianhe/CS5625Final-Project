package physics;

/**
 * Dynamical System interface.
 * This dynamical systems interface defines basic functionality required for use by the OdeSolver interface. 
 * 
 * @author homoflashmanicus
 */
public interface DynamicalSystem {
	
	/**
	 * Get number of dimensions of dynamical system (in the dynamical systems sense).
	 * @return The number of dimensions of the dynamical system. 
	 */
	public int getDimensions();
	
	/**
	 * Get current time.
	 */
	public double getTime();
	
	/**
	 * Get current time.
	 */
	public void incrementTime(double dt);
	
	/**
	 * Get the dynamical system's state
	 * @param dst Save state to dst.
	 */
	public void getState(double[] dst);

	/**
	 * Set the dynamical system's state from src
	 */
	public void setState(double[] src);
		
	/**
	 * Calculate the dynamical system derivatives and save to dst
	 * include dt for impulse calculations  
	 */
	public void derivEval(double[] dst);
		
	/**
	 * Apply constraints.  
	 * This method performss impulse updates to particle systems 
	 */
	public void applyCollisionConstraints();
}

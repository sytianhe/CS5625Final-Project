package physics;

public class SymplecticEuler {
	
	int n = -1;
	double[] x0;
	double[] xf;

	/**
	 * Construct new SymplecticEuler OdeSolver
	*/
	public SymplecticEuler(){ }
	
	public void step(DynamicalSystem D, double dt) {
		//ensure that Solver has sufficient space for the computation
		if (n != D.getDimensions() ){
			n = D.getDimensions();
			x0 = new double[n]; xf = new double[n];
		}
		
		D.getState(xf);
		D.derivEval(x0);
		
		//perform symplectic euler step
		for (int i=0; i<n; i+=6) {
			//update velocity
			xf[i+3] += dt * x0[i+3];
			xf[i+4] += dt * x0[i+4];
			xf[i+5] += dt * x0[i+5];
			//update position from new velocity
			xf[i  ] += dt * xf[i+3];
			xf[i+1] += dt * xf[i+4];			
			xf[i+2] += dt * xf[i+5];			
		}
		
		//update system
		D.setState(xf);
		D.applyCollisionConstraints();
		D.incrementTime(dt);
	}
}

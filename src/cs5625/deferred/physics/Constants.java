package cs5625.deferred.physics;

/**
 * Default constants.
 * 
 * @author Doug James, tz249
 */
public class Constants
{

	/**Collision restitution coefficient. */
	public static double GRAVITY = 10.0;	
	
	/**Collision restitution coefficient. */
	public static double RESTITUTION_COEFF = 0.5;
	
    /** Mass-proportional damping. */
    public static double DAMPING_MASS      = 0.5; // default 1.0

    /** Mass of a particle. */
    public static double PARTICLE_MASS     = 1.0;

    /** Spring stretching stiffness. */
    //public static double STIFFNESS_STRETCH = 10000.0; 
    public static double STIFFNESS_STRETCH = 1000.0; 

    /** Spring bending stiffness. */
    public static double STIFFNESS_BEND    = 100000.0;//10000.0; 
    
    /** Edge thickness coefficient. */
    public static final double EDGE_COEFF = 0.02;
    
    /** Edge damping coefficient. */
    public static final double EDGE_DRAG = 1;
    
    /** Edge kinetic friction coefficient. */
    public static final double STATIC_FRICTION = 0.01;
    
    /** Edge kinetic friction coefficient. */
    public static final double KINETIC_FRICTION = 0.1;

	public static final double REPULSION_COEFF = 100;

	public static double WIND_STRENGTH = 0;
    
    /** RESTITUTION_COEFF */
    public static void set_RESTITUTION_COEFF(double num){
    	RESTITUTION_COEFF = num;
    }
    
    /** DAMPING_MASS */
    public static void set_DAMPING_MASS(double num){
    	DAMPING_MASS = num;
    }
    
    /** PARTICLE_MASS */
    public static void set_PARTICLE_MASS(double num){
    	PARTICLE_MASS = num;
    }
    
    /** STIFFNESS_STRETCH */
    public static void set_STIFFNESS_STRETCH(double num){
    	STIFFNESS_STRETCH = num;
    }
    
    /** STIFFNESS_BEND */
    public static void set_STIFFNESS_BEND(double num){
    	STIFFNESS_BEND = num;
    }
    
    /** WIND STRENGTH */
    public static void set_WIND_STRENGTH(double num){
    	WIND_STRENGTH = num;
    }
}

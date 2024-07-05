package prefuse.util.force;


/**
 * HVN: removed SpringFactory.  It was not thread safe.
 * 
 * Represents a spring in a force simulation.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class Spring {
    
    /**
     * Create a new Spring instance
     * @param fi1 the first ForceItem endpoint
     * @param fi2 the second ForceItem endpoint
     * @param k the spring tension co-efficient
     * @param len the spring's resting length
     */
    public Spring(ForceItem fi1, ForceItem fi2, float k, float len) {
        item1 = fi1;
        item2 = fi2;
        coeff = k;
        length = len;
    }
    
    /** The first ForceItem endpoint */
    public ForceItem item1;
    /** The second ForceItem endpoint */
    public ForceItem item2;
    /** The spring's resting length */
    public float length;
    /** The spring tension co-efficient */
    public float coeff;

} // end of class Spring

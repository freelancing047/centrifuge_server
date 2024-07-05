package csi.server.util;


public class IntegerUtil {
    
    // Throws an error if the string cannot be parsed as either an int or a float
    public static int valueOf(String stringIn) {
        
        if (-1 == stringIn.indexOf('.')) {
            
            return Integer.parseInt(stringIn);
            
        } else {
            
            return Math.round(Float.parseFloat(stringIn));
        }
    }
    
    // Returns the default value (which can be null)
    // if the string cannot be parsed as either an int or a float
    public static Integer valueOf(String stringIn, Integer defaultIn) {
        
        Integer myResult = defaultIn;
        
        if ((null != stringIn) && (0 < stringIn.length())) {
            
            try {
                if (-1 == stringIn.indexOf('.')) {
                    
                    myResult = Integer.parseInt(stringIn);
                    
                } else {
                    
                    myResult = Math.round(Float.parseFloat(stringIn));
                }
                
            } catch (Exception myException) {
                
            }
        }
        return myResult;
    }

}

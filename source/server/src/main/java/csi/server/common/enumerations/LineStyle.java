package csi.server.common.enumerations;


public enum LineStyle {
    
    SOLID,
    DASHED,
    DOTTED;
    
    public static LineStyle getLine(String line) {
        if(line == null)
            return null;

        line = line.toUpperCase();
        if (line.equals("SOLID")) {
            return SOLID;
        }
        if (line.equals("DOTTED")) {
            return DOTTED;
        }
        if (line.equals("DASHED")) {
            return DASHED;
        }
        
        return null;
    }
    
    
}


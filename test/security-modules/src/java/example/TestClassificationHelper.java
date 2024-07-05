package example;

import java.util.Collection;
import java.util.HashSet;

public class TestClassificationHelper
{
    public static class Parts
    {
        public Level              level;
        public Collection<String> compartments;
        public Collection<String> caveats;
        
        Parts() {
            level = Level.Unknown;
            compartments = new HashSet<String>();
            caveats = new HashSet<String>();
        }
        
        public void setLevel( Level other, boolean isHigh) {
            if( !isHigh ) {
                level = other;
            } else if( other.compareTo(level) > 0) {
                level = other;
            }
        }
    }

    public Parts parse(String value) {
        Parts parts = new Parts();
        
        value = (value == null ) ? "" : value.trim();
        int valLen = value.length();
        if( valLen == 0 ) {
            return parts;
        } else if( valLen >= 2 && value.charAt(0) == '(' && value.charAt(valLen - 1) == ')') {
            value = value.substring(1, valLen-1); 
        }
        
        String[] split = value.split("//");
        
        Level level = Level.lift(split[0]);
        parts.setLevel(level, true);
        
        if( split.length >= 2 ) {
            extractValues( parts.compartments, split[1]);
        }
        
        if( split.length >= 3 ) {
            extractValues( parts.caveats, split[2]);
        }
        
        return parts;
    }

    private void extractValues(Collection<String> collection, String value) {
        String[] vals = value.split("/");
        for (String s : vals) {
            collection.add( s );
        }
    }

    static public enum Level {
        Unknown("Unknown"), Unclassified("U"), Confidential("C"), Secret("S"), TopSecret("TS");

        String text;

        Level(String s) {
            text = s;
        }
        
        public String text() { return text; }
        
        public static Level lift( String val ) {
            try {
                return Level.valueOf(val);
            } catch (IllegalArgumentException iae) {
            }
            
            for( Level l : Level.values() ) {
                if( l.text.equals(val)) {
                    return l;
                }
            }
            
            return Unknown;
        }
    }
}

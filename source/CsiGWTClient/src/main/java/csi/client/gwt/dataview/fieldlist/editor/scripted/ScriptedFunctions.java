package csi.client.gwt.dataview.fieldlist.editor.scripted;

import csi.server.common.model.FunctionType;

/**
 * @author Centrifuge Systems, Inc.
 * Client side representation of FunctionType
 */
public enum ScriptedFunctions {

    CONCATENATE("Concatenate Fields"),
    SUBSTRING("Extract Partial Value"),
    CALCULATE_VALUE("Calculate Value"),
    CALCULATE_DURATION("Calculate Duration"),
    ADVANCED_FUNCTION("Advanced Function");

    private final String title;

    private ScriptedFunctions(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ScriptedFunctions fromTitle(String title){
        for(ScriptedFunctions df : ScriptedFunctions.values()){
            if(df.getTitle().equals(title)){
                return df;
            }
        }
        return null;
    }

    public static int getIndex(ScriptedFunctions functions) {
        ScriptedFunctions [] all = ScriptedFunctions.values();
        for(int i = 0, n = all.length; i < n; i++){
            if(all[i] == functions){
                return i;
            }
        }
        return -1;
    }

    public static FunctionType toFunctionType(ScriptedFunctions function) {
        if(function == null)
            return null;

        switch(function){
            case CONCATENATE:
                return FunctionType.CONCAT;
            case CALCULATE_DURATION:
                return FunctionType.DURATION;
            case CALCULATE_VALUE:
                return FunctionType.MATH;
            case SUBSTRING:
                return FunctionType.SUBSTRING;

        }
        return null;
    }

    public static ScriptedFunctions fromFunctionType(FunctionType function) {
        if(function == null)
            return null;

        switch(function){
            case CONCAT:
                return ScriptedFunctions.CONCATENATE;
            case DURATION:
                return ScriptedFunctions.CALCULATE_DURATION;
            case MATH:
                return ScriptedFunctions.CALCULATE_VALUE;
            case SUBSTRING:
                return ScriptedFunctions.SUBSTRING;

        }
        return null;
    }
}

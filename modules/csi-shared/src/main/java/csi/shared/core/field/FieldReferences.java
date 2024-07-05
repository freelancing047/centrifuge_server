package csi.shared.core.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 * Contains all the locations where a field is referenced from in a dataview.
 */
public class FieldReferences implements Serializable {
    private List<String> visualizationNames = new ArrayList<String>();
    private List<String> filterNames = new ArrayList<String>();
    private List<String> linkupNames = new ArrayList<String>();
    private List<String> fieldNames = new ArrayList<String>();

    public boolean hasReferences(){
        if(!visualizationNames.isEmpty()){
            return true;
        }
        if(!filterNames.isEmpty()){
            return true;
        }
        if(!linkupNames.isEmpty()){
            return true;
        }
        if(!fieldNames.isEmpty()){
            return true;
        }
        return false;
    }

    public void addVisualization(String name){
        visualizationNames.add(name);
    }

    public void addFilter(String name){
        filterNames.add(name);
    }

    public void addField(String name){
        fieldNames.add(name);
    }

    public void addLinkup(String name){
        linkupNames.add(name);
    }

    public List<String> getVisualizationNames() {
        return visualizationNames;
    }

    public List<String> getFilterNames() {
        return filterNames;
    }

    public List<String> getLinkupNames() {
        return linkupNames;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }
}

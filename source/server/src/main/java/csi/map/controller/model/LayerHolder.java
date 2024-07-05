package csi.map.controller.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class LayerHolder {
    private Set<String> selectedFeatures = new HashSet<>();
    private Set<String> combinedFeatures = new HashSet<>();
    private Set<String> newFeatures = new HashSet<>();
    private Set<String> updatedFeatures = new HashSet<>();
    private Set<TypeIdIconUrlPair> typeIdIconUrlPairs = new TreeSet<>();

    public Set<String> getSelectedFeatures() {
        return selectedFeatures;
    }

    public void setSelectedFeatures(Set<String> selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public void addSelectedFeature(String objectID) {
        selectedFeatures.add(objectID);
    }

    public Set<String> getCombinedFeatures() {
        return combinedFeatures;
    }

    public void setCombinedFeatures(Set<String> combinedFeatures) {
        this.combinedFeatures = combinedFeatures;
    }

    public void addCombinedFeature(String objectID) {
        combinedFeatures.add(objectID);
    }

    public Set<String> getNewFeatures() {
        return newFeatures;
    }

    public void setNewFeatures(Set<String> newFeatures) {
        this.newFeatures = newFeatures;
    }

    public void addNewFeature(String objectID) {
        newFeatures.add(objectID);
    }

    public Set<String> getUpdatedFeatures() {
        return updatedFeatures;
    }

    public void setUpdatedFeatures(Set<String> updatedFeatures) {
        this.updatedFeatures = updatedFeatures;
    }

    public void addUpdatedFeature(String objectID) {
        updatedFeatures.add(objectID);
    }

    public Set<TypeIdIconUrlPair> getTypeIdIconUrlPairs() {
        return typeIdIconUrlPairs;
    }

    public void setTypeIdIconUrlPairs(Set<TypeIdIconUrlPair> typeIdIconUrlPairs) {
        this.typeIdIconUrlPairs = typeIdIconUrlPairs;
    }
}
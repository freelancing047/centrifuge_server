package csi.map.controller.model;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private List<Feature> features = new ArrayList<>();
    private List<String> selectedFeatures = new ArrayList<>();
    private List<String> combinedFeatures = new ArrayList<>();
    private List<String> newFeatures = new ArrayList<>();
    private List<String> updatedFeatures = new ArrayList<>();
    private List<TypeIdIconUrlPair> typeIdIconUrlPairs = new ArrayList<>();
    private List<Association> associations = new ArrayList<>();

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<String> getSelectedFeatures() {
        return selectedFeatures;
    }

    public void setSelectedFeatures(List<String> selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    public List<String> getCombinedFeatures() {
        return combinedFeatures;
    }

    public void setCombinedFeatures(List<String> combinedFeatures) {
        this.combinedFeatures = combinedFeatures;
    }

    public List<String> getNewFeatures() {
        return newFeatures;
    }

    public void setNewFeatures(List<String> newFeatures) {
        this.newFeatures = newFeatures;
    }

    public List<String> getUpdatedFeatures() {
        return updatedFeatures;
    }

    public void setUpdatedFeatures(List<String> updatedFeatures) {
        this.updatedFeatures = updatedFeatures;
    }

    public List<TypeIdIconUrlPair> getTypeIdIconUrlPairs() {
        return typeIdIconUrlPairs;
    }

    public void setTypeIdIconUrlPairs(List<TypeIdIconUrlPair> typeIdIconUrlPairs) {
        this.typeIdIconUrlPairs = typeIdIconUrlPairs;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void setAssociations(List<Association> associations) {
        this.associations = associations;
    }

    public void clear() {
        features.clear();
        selectedFeatures.clear();
        combinedFeatures.clear();
        newFeatures.clear();
        updatedFeatures.clear();
        typeIdIconUrlPairs.clear();
        associations.clear();

    }
}
package csi.shared.gwt.viz.viewer.LensImage;

import java.util.List;

public class NodeLensImage implements LensImage {
    private String image;
    private List<String> Labels;
    private List<String> Types;
    private Integer occurrences;
    private Integer numberOfNeighbors;
    private Double Betweenness;
    private Double Closeness;
    private Double Eigenvector;
    private List<String> neighborLabels;
    private List<Integer> neighborLabelCounts;
    private List<String> neighborTypes;
    private List<Integer> neighborTypeCounts;
    private List<String> bundleLabels;
    private List<Integer> bundleLabelCounts;
    private List<String> bundleTypes;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getLabels() {
        return Labels;
    }

    public void setLabels(List<String> labels) {
        Labels = labels;
    }

    public List<String> getTypes() {
        return Types;
    }

    public void setTypes(List<String> types) {
        Types = types;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Integer getNumberOfNeighbors() {
        return numberOfNeighbors;
    }

    public void setNumberOfNeighbors(Integer numberOfNeighbors) {
        this.numberOfNeighbors = numberOfNeighbors;
    }

    public Double getBetweenness() {
        return Betweenness;
    }

    public void setBetweenness(Double betweenness) {
        Betweenness = betweenness;
    }

    public Double getCloseness() {
        return Closeness;
    }

    public void setCloseness(Double closeness) {
        Closeness = closeness;
    }

    public Double getEigenvector() {
        return Eigenvector;
    }

    public void setEigenvector(Double eigenvector) {
        Eigenvector = eigenvector;
    }

    public List<String> getNeighborLabels() {
        return neighborLabels;
    }

    public void setNeighborLabels(List<String> neighborLabels) {
        this.neighborLabels = neighborLabels;
    }

    public List<Integer> getNeighborLabelCounts() {
        return neighborLabelCounts;
    }

    public void setNeighborLabelCounts(List<Integer> neighborLabelCounts) {
        this.neighborLabelCounts = neighborLabelCounts;
    }

    public List<String> getNeighborTypes() {
        return neighborTypes;
    }

    public void setNeighborTypes(List<String> neighborTypes) {
        this.neighborTypes = neighborTypes;
    }

    public List<Integer> getNeighborTypeCounts() {
        return neighborTypeCounts;
    }

    public void setNeighborTypeCounts(List<Integer> neighborTypeCounts) {
        this.neighborTypeCounts = neighborTypeCounts;
    }

    public List<String> getBundleLabels() {
        return bundleLabels;
    }

    public void setBundleLabels(List<String> bundleLabels) {
        this.bundleLabels = bundleLabels;
    }

    public List<Integer> getBundleLabelCounts() {
        return bundleLabelCounts;
    }

    public void setBundleLabelCounts(List<Integer> bundleLabelCounts) {
        this.bundleLabelCounts = bundleLabelCounts;
    }

    public List<String> getBundleTypes() {
        return bundleTypes;
    }

    public void setBundleTypes(List<String> bundleTypes) {
        this.bundleTypes = bundleTypes;
    }

    public List<Integer> getBundleTypeCounts() {
        return BundleTypeCounts;
    }

    public void setBundleTypeCounts(List<Integer> bundleTypeCounts) {
        BundleTypeCounts = bundleTypeCounts;
    }

    private List<Integer> BundleTypeCounts;


    private String lensDef;
    @Override
    public String getLensDef() {
        return lensDef;
    }

    public void setLensDef(String lensDef) {
        this.lensDef = lensDef;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setLabel(String s) {

    }
}

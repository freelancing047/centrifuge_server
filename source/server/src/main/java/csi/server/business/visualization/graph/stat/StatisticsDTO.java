package csi.server.business.visualization.graph.stat;

public class StatisticsDTO {

    private String type;
    private Integer onGraph;
    private Integer inViewport;
    private Integer hidden;
    private Integer visible;
    private Integer selected;
    private Double id;


    public StatisticsDTO(String type, Integer onGraph, Integer inViewport, Integer hidden, Integer visible,
            Integer selected) {
        // Do I need a model key prodiver in StatisticsListProperties?
        this.id = Math.random();

        this.type = type;
        this.onGraph = onGraph;
        this.inViewport = inViewport;
        this.hidden = hidden;
        this.visible = visible;
        this.selected = selected;
    }


    public Integer getOnGraph() {
        return onGraph;
    }


    public void setOnGraph(Integer onGraph) {
        this.onGraph = onGraph;
    }


    public Integer getInViewport() {
        return inViewport;
    }


    public void setInViewport(Integer inViewport) {
        this.inViewport = inViewport;
    }


    public Integer getHidden() {
        return hidden;
    }


    public void setHidden(Integer hidden) {
        this.hidden = hidden;
    }


    public Integer getVisible() {
        return visible;
    }


    public void setVisible(Integer visible) {
        this.visible = visible;
    }


    public Integer getSelected() {
        return selected;
    }


    public void setSelected(Integer selected) {
        this.selected = selected;
    }


    public Double getId() {
        return id;
    }


    public void setId(Double id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }

}

package csi.shared.core.visualization.timeline;

public interface CommonTrack {

    String getLabel();

    boolean isVisible();

    void setCollapsed(boolean collapse);

    void setVisible(boolean visible);

    boolean isCollapsed();

}

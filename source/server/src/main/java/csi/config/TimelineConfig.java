package csi.config;

public class TimelineConfig
        extends AbstractConfigurationSettings {
   
    private static int eventLimit = 20000;
    private static int legendLimit = 1000;

    public int getEventLimit() {
        return eventLimit;
    }

    public void setEventLimit(int eventLimit) {
        TimelineConfig.eventLimit = eventLimit;
    }

    public static int getLegendLimit() {
        return legendLimit;
    }

    public void setLegendLimit(int legendLimit) {
        TimelineConfig.legendLimit = legendLimit;
    }


    
}

package csi.client.gwt.viz.chart.view;

import csi.client.gwt.validation.feedback.NoOpValidationFeedback;


public class ChartValidationFeedback extends NoOpValidationFeedback {
    public static final ChartValidationFeedback EMPTY_CATEGORY_FEEDBACK = new ChartValidationFeedback("Must choose a category");
    
    private final String error;

    public ChartValidationFeedback(String error){
        this.error = error;
    }

    @Override
    public String getError() {
        return error;
    }
}

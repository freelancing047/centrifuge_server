package csi.client.gwt.validation.feedback;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface ValidationFeedback {

    public void showValidationFeedback();

    public void hideValidationFeedback();

    public String getError();
}

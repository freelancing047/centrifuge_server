package csi.client.gwt.validation.feedback;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NoOpValidationFeedback implements ValidationFeedback {

    @Override
    public void showValidationFeedback() {
    }

    @Override
    public void hideValidationFeedback() {

    }

    @Override
    public String getError() {
        return ""; //$NON-NLS-1$
    }
}

package csi.client.gwt.widget.input_boxes;


public interface ValidityCheckCapable {
    
    public enum Mode {
        
        EXACT,
        LOWERCASE,
        UPPERCASE
    }

    public boolean isValid();
    public boolean isRequired();
    public boolean isConditionallyValid();
    public void setRequired(boolean isRequiredIn);
}

package csi.server.common.dto.config.connection;



import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.KeyValueItem;


public class ConfigItem implements IsSerializable {

    private String title;
    private String label;
    private String type;
    private String key;
    private String helpText;
    private boolean isAdvancedOption;
    private boolean isMultiline;
    private boolean allowCustomValue;
    private boolean required;
    private List<KeyValueItem> defaultValues;

    /**
     * Contains a list of validation operation key / value pairs this form item needs to pass. IE key = ValidationTypes.MAXCHARS : value = '25'. Or 
     * key = ValidationTypes.FORMAT : value = 'xx/xx/xxxx'.
     */
    private List<KeyValueItem> validationOperations = new ArrayList<KeyValueItem>();

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean getIsMultiline() {
        return isMultiline;
    }

    public void setIsMultiline(boolean isMultiline) {
        this.isMultiline = isMultiline;
    }

    public boolean getAllowCustomValue() {
        return allowCustomValue;
    }

    public void setAllowCustomValue(boolean allowCustomValue) {
        this.allowCustomValue = allowCustomValue;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<KeyValueItem> getValidationOperations() {
        return validationOperations;
    }

    public void setValidationOperations(List<KeyValueItem> validationOperations) {
        this.validationOperations = validationOperations;
    }

    public List<KeyValueItem> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(List<KeyValueItem> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getIsAdvancedOption() {
        return isAdvancedOption;
    }

    public void setIsAdvancedOption(boolean isAdvancedOption) {
        this.isAdvancedOption = isAdvancedOption;
    }
}

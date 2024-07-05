package csi.client.gwt.csiwizard.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.KeyValueItem;
import csi.server.common.enumerations.JdbcDriverParameterValidationType;
import csi.server.common.util.BitMask;



abstract class AbstractValidator {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private String _restrictionString = null;
    
    abstract public String validate(String valueIn, String defaultErrorIn);
    abstract public String getFormat();
    
    public AbstractValidator(String restrictionIn) {
        
        _restrictionString = restrictionIn;
    }

    public void addRestriction(String restrictionIn) {

        _restrictionString = restrictionIn;
    }
    
    public String getRestrictionString() {
       
        return _restrictionString;
    }
    
    public boolean isListValidator() {
        
        return false;
    }
    
    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }
}

abstract class AbstractIntValidator extends AbstractValidator {
    
    Integer _restrictionValue = null;

    abstract public void showConfigurationError();

    public AbstractIntValidator(String restrictionIn) {
        
        super(restrictionIn);
        _restrictionValue = Integer.decode(restrictionIn);
    }

    public int getRestrictionValue() {
       
        if (null == _restrictionValue) {
            
            try {
                
                _restrictionValue = Integer.decode(getRestrictionString());
                
            } catch (Exception myException) {
                
                showConfigurationError();
            }
        }
        
        return _restrictionValue;
    }
}

abstract class AbstractFloatValidator extends AbstractValidator {

    abstract public void showConfigurationError();

    Double _restrictionValue = null;
    
    public AbstractFloatValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public double getRestrictionValue() {
       
        if (null == _restrictionValue) {
            
            try {
                
                _restrictionValue = Double.parseDouble(getRestrictionString());
            
            } catch (Exception myException) {
                
                showConfigurationError();
            }
        }
        
        return _restrictionValue;
    }
}

abstract class AbstractListValidator extends AbstractIntValidator {
    
    public AbstractListValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public boolean isListValidator() {
        
        return true;
    }
    
    public String validate(String valueIn, String defaultErrorIn) {
        
        return null;
    }
}

class maxCharsValidator extends AbstractIntValidator {
    
    public maxCharsValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MaxCharacters(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MAXCHARS.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    @Override
    public String validate(String valueIn, String defaultErrorIn) {

        Integer myRestrictionValue = getRestrictionValue();
        
        return (null != myRestrictionValue)
                ? (valueIn.length() > getRestrictionValue())
                        ? getFormat()
                        : null
                : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MAXCHARS.getLabel());
    }
}

class minCharsValidator extends AbstractIntValidator {
    
    public minCharsValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MinCharacters(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MINCHARS.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {

        Integer myRestrictionValue = getRestrictionValue();
        
        return (null != myRestrictionValue)
                ? (valueIn.length() < myRestrictionValue)
                        ? getFormat()
                        : null
                : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MINCHARS.getLabel());
    }
}

class isNumberValidator extends AbstractValidator {
    
    public isNumberValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public static String getBasicFormat() {
        
        return i18n.parameterFormat_mustBeNumber();
    }

    public String getFormat() {
        
        return getBasicFormat();
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {
        
        String myError = null;
        
        if ((null != valueIn) && (0 < valueIn.length())) {

            char myFirstCharacter = valueIn.charAt(0);
            int myLimit = valueIn.length();
            int myBase = (('+' == myFirstCharacter) || ('-' == myFirstCharacter)) ? 1 : 0;

            if (myLimit > myBase) {

                for (int i = myBase; myLimit > i; i++) {

                    char myTest = valueIn.charAt(i);

                    if (('0' > myTest) || ('9' < myTest)) {

                        myError = getFormat();
                        break;
                    }
                }
            } else {

                myError = getFormat();
            }
        }

        return myError;
    }
}

class isValueValidator extends AbstractValidator {
    
    public isValueValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public static String getBasicFormat() {
        
        return i18n.parameterFormat_mustBeValue();
    }

    public String getFormat() {
        
        return getBasicFormat();
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {
        
        String myError = null;
        
        if ((null != valueIn) && (0 < valueIn.length())) {

            int myBase = 0;
            for (int myPass = 1, myMaxPass = 1; myMaxPass >= myPass; myPass++) {

                char myTest = valueIn.charAt(myBase);
                boolean myDotFound = ('.' == myTest);
                boolean myDigitFound = (('0' <= myTest) && ('9' >= myTest));

                if (myDotFound || ('+' == myTest) || ('-' == myTest)
                        || (('0' <= myTest) && ('9' >= myTest)) ) {

                    int myLimit = valueIn.length();
                    int i;

                    for (i = myBase + 1; myLimit > i; i++) {

                        myTest = valueIn.charAt(i);

                        if ('.' == myTest) {

                            if (myDotFound) {

                                myError = i18n.parameterValidatorError(); //$NON-NLS-1$
                                break;
                            }
                            myDotFound = true;
                        }
                        else if (('0' > myTest) || ('9' < myTest)) {

                            if (myDigitFound && (('e' == myTest) || ('E' == myTest))) {

                                myMaxPass = 2;
                                myBase = i + 1;

                            } else {

                                myError = getFormat();
                            }
                            break;

                        } else {

                            myDigitFound = true;
                        }
                    }

                } else {

                    myError = getFormat();
                }
                if (null != myError) {

                    break;
                }
            }
        }

        return myError;
    }
}

class minValueValidator extends AbstractFloatValidator {
    
    public minValueValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MinValue(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MINVALUE.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {

        try {

            Double myRestrictionValue = getRestrictionValue();
            
            return (null != myRestrictionValue)
                    ? (Double.parseDouble(valueIn) < getRestrictionValue())
                            ? getFormat()
                            : null
                    : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MINVALUE.getLabel());

        } catch (Exception myException) {
            
            return (null != defaultErrorIn) ? defaultErrorIn : i18n.parameterFormat_mustBeValue();
        }
    }
}

class maxValueValidator extends AbstractFloatValidator {
    
    public maxValueValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MaxValue(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MAXVALUE.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {

        try {

            Double myRestrictionValue = getRestrictionValue();
            
            return (null != myRestrictionValue)
                    ? (Double.parseDouble(valueIn) > getRestrictionValue())
                            ? getFormat()
                            : null
                    : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MAXVALUE.getLabel());
    
        } catch (Exception myException) {
            
            return (null != defaultErrorIn) ? defaultErrorIn : i18n.parameterFormat_mustBeValue();
        }
    }
}

class regExValidator extends AbstractValidator {
    
    private List<String> _expressionList = null;
    
    public regExValidator(String restrictionIn) {
        
        super(restrictionIn);
        _expressionList = new ArrayList<String>();
        _expressionList.add(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_regularExpression();
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {
        
        String myError = null;
        
        for (String myExpression : _expressionList) {
            
            if (!valueIn.matches(myExpression)) {
                
//                myError = "Must match " + Display.value(myExpression);
                myError = getFormat();
                break;
            }
        }

        return myError;
    }
    
    @Override
    public void addRestriction(String valueIn) {
        
        _expressionList.add(valueIn);
    }
}

class maxItemsValidator extends AbstractListValidator {
    
    public maxItemsValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MaxSelection(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MAXITEMS.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {

        Integer myRestrictionValue = getRestrictionValue();
        
        return (null != myRestrictionValue)
                ? (listIn.size() > getRestrictionValue())
                        ? getFormat()
                        : null
                : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MAXITEMS.getLabel());
    }
}

class minItemsValidator extends AbstractListValidator {
    
    public minItemsValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String getFormat() {
        
        return i18n.parameterFormat_MinSelection(getRestrictionString());
    }
    
    public void showConfigurationError() {
        
        Display.error(i18n.parameterFormat_ConfigurationProblem_Dialog(JdbcDriverParameterValidationType.MINITEMS.getLabel()));
    }

    public String validate(List<?> listIn, String defaultErrorIn) {

        Integer myRestrictionValue = getRestrictionValue();
        
        return (null != myRestrictionValue)
                ? (listIn.size() < getRestrictionValue())
                        ? getFormat()
                        : null
                : i18n.parameterFormat_ConfigurationProblem(JdbcDriverParameterValidationType.MINITEMS.getLabel());
    }
}

class fileTypeValidator extends AbstractValidator {
    
    String _txtExtensionFormat = null;
    
    Map<String, Integer> _extensionMap;
    
    public fileTypeValidator(String restrictionIn) {
        
        super("Valid extensions "); //$NON-NLS-1$
        
        _extensionMap = new HashMap<String, Integer>();
        addRestriction(restrictionIn);
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {
        
        String myExtension = getExtension(valueIn);

        return (null != myExtension) ? _extensionMap.containsKey(myExtension.toLowerCase()) ? null : getFormat() : getFormat();
    }
    
    @Override
    public void addRestriction(String valueIn) {
        
        String myExtension = getExtension(valueIn);
        
        if (null != myExtension) {
            
            _extensionMap.put(myExtension, 0);
        }
    }

    @Override
    public String getFormat() {

        if (null == _txtExtensionFormat) {
            
            StringBuilder myBuffer = new StringBuilder();
            
            for (String myExtension : _extensionMap.keySet()) {
                
                myBuffer.append(", *."); //$NON-NLS-1$
                myBuffer.append(myExtension);
            }
            
            _txtExtensionFormat = "[" + myBuffer.substring(2) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return _txtExtensionFormat;
    }
    
    String getExtension(String valueIn) {
        
        int myExtensionStart = valueIn.lastIndexOf('.');

        return ((1 < myExtensionStart) && ((valueIn.length() - 1) > myExtensionStart)) ? valueIn.substring(myExtensionStart + 1).toLowerCase() : null;
    }
}

class formatValidator extends AbstractValidator {
    
    public formatValidator(String restrictionIn) {
        
        super(restrictionIn);
    }

    public String validate(List<?> listIn, String defaultErrorIn) {
        
        return null;
    }

    public String validate(String valueIn, String defaultErrorIn) {

        return null;
    }

    @Override
    public String getFormat() {

        return getRestrictionString();
    }
}


public class ParameterValidator {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private String _format = null;
    private String _defaultError = null;
    private BitMask _validatorMask = new BitMask();
    private List<AbstractValidator> _validatorList = new ArrayList<AbstractValidator>();
    private Map<JdbcDriverParameterValidationType, AbstractValidator> _validatorMap
    = new HashMap<JdbcDriverParameterValidationType, AbstractValidator>();


    public ParameterValidator(JdbcDriverParameterValidationType keyIn, String valueIn) {

        registerValidator(keyIn, valueIn);
    }

    public ParameterValidator(KeyValueItem validationRuleIn) {

        registerValidator(JdbcDriverParameterValidationType.getValue(validationRuleIn.getKey()), validationRuleIn.getValue());
    }

    public ParameterValidator(List<KeyValueItem> validationRulesIn) {

        for (int i = 0; validationRulesIn.size() > i; i++) {
            
            KeyValueItem myPair = validationRulesIn.get(i);
            
            registerValidator(JdbcDriverParameterValidationType.getValue(myPair.getKey()), myPair.getValue());
        }
        
    }
    
    public void add(JdbcDriverParameterValidationType keyIn, String valueIn) {
        
        registerValidator(keyIn, valueIn);
    }
    
    public String getFormat() {
        
        if (null == _format) {
            
            if (_validatorMask.isSet(JdbcDriverParameterValidationType.FILETYPE)) {
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.REGEX)) {
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.MINCHARS)) {
                    
                if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXCHARS)) {
                    
                    _format = _constants.parameterFormat_CharacterLimits(
                            _validatorMap.get(JdbcDriverParameterValidationType.MINCHARS).getRestrictionString(),
                            _validatorMap.get(JdbcDriverParameterValidationType.MAXCHARS).getRestrictionString());
                    
                } else {
                    
                    _format = _validatorMap.get(JdbcDriverParameterValidationType.MINCHARS).getFormat();
                }
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXCHARS)) {
                
                _format = _validatorMap.get(JdbcDriverParameterValidationType.MAXCHARS).getFormat();
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.MINITEMS)) {
                    
                if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXITEMS)) {
                    
                    _format = _constants.parameterFormat_SelectionLimits(
                            _validatorMap.get(JdbcDriverParameterValidationType.MINITEMS).getRestrictionString(),
                            _validatorMap.get(JdbcDriverParameterValidationType.MAXITEMS).getRestrictionString());
                    
                } else {
                    
                    _format = _validatorMap.get(JdbcDriverParameterValidationType.MINITEMS).getFormat();
                }
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXITEMS)) {
                
                _format = _validatorMap.get(JdbcDriverParameterValidationType.MAXITEMS).getFormat();
                
            } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.ISNUMBER)
                    || _validatorMask.isSet(JdbcDriverParameterValidationType.ISVALUE)) {
                
                if (_validatorMask.isSet(JdbcDriverParameterValidationType.MINVALUE)) {
                    
                    if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXVALUE)) {
                        
                        _format = _constants.parameterFormat_valueLimits(
                                _validatorMap.get(JdbcDriverParameterValidationType.MINVALUE).getRestrictionString(),
                                _validatorMap.get(JdbcDriverParameterValidationType.MAXVALUE).getRestrictionString());
                        
                    } else {
                        
                        _format = _validatorMap.get(JdbcDriverParameterValidationType.MINVALUE).getFormat();
                    }
                    
                } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.MAXVALUE)) {
                    
                    _format = _validatorMap.get(JdbcDriverParameterValidationType.MAXVALUE).getFormat();
                    
                } else if (_validatorMask.isSet(JdbcDriverParameterValidationType.ISNUMBER)) {
                    
                    _format = isNumberValidator.getBasicFormat();
                    
                } else {
                    
                    _format = isValueValidator.getBasicFormat();
                }
            }
        }
        return _format;
    }
    
    public boolean isListValidator() {
        
        boolean mySuccess = false;
        
        for (AbstractValidator myValidator : _validatorList) {
            
            mySuccess = myValidator.isListValidator();
            
            if (mySuccess) {
                
                break;
            }
        }
        
        return mySuccess;
    }
    
    public String validateList(List<String> listIn) {
        
        String myMessage = validateList(JdbcDriverParameterValidationType.MINITEMS, listIn);

        if (null == myMessage) {
            
            myMessage = validateList(JdbcDriverParameterValidationType.MAXITEMS, listIn);
        }
        
        return myMessage;
    }
    
    public String checkInput(String dataIn) {
        
        String myMessage = null;
        
        for (AbstractValidator myValidator : _validatorList) {
            
            myMessage = myValidator.validate(dataIn, _defaultError);
            
            if (null != myMessage) {
                
                break;
            }
        }
        
        return myMessage;
    }
    
    private void registerValidator(JdbcDriverParameterValidationType keyIn, String valueIn) {
        
        _validatorMask.setBit(keyIn);
        
        if (_validatorMap.containsKey(keyIn)) {
            
            _validatorMap.get(keyIn).addRestriction(valueIn);
            
        } else {
            
            AbstractValidator myValidator = null;
            
            switch (keyIn) {
                
                case MAXCHARS :
                    
                    myValidator = new maxCharsValidator(valueIn);
                    break;
                    
                case MINCHARS :
                    
                    myValidator = new minCharsValidator(valueIn);
                    break;
                
                case ISNUMBER :
                    
                    myValidator = new isNumberValidator(valueIn);
                    _defaultError = _constants.parameterFormat_mustBeNumber();
                    break;
                
                case ISVALUE :
                    
                    myValidator = new isValueValidator(valueIn);
                    if (!_validatorMask.isSet(JdbcDriverParameterValidationType.ISNUMBER)) {
                        
                        _defaultError = _constants.parameterFormat_mustBeValue();
                    }
                    break;
                
                case MINVALUE :
                    
                    myValidator = new minValueValidator(valueIn);
                    if (!_validatorMask.isSet(JdbcDriverParameterValidationType.ISNUMBER)) {
                        
                        _defaultError = _constants.parameterFormat_mustBeValue();
                    }
                    break;
                
                case MAXVALUE :
                    
                    myValidator = new maxValueValidator(valueIn);
                    if (!_validatorMask.isSet(JdbcDriverParameterValidationType.ISNUMBER)) {
                        
                        _defaultError = _constants.parameterFormat_mustBeValue();
                    }
                    break;
                
                case REGEX :
                    
                    myValidator = new regExValidator(valueIn);
                    break;
                
                case MAXITEMS :
                    
                    myValidator = new maxItemsValidator(valueIn);
                    break;
                
                case MINITEMS :
                    
                    myValidator = new maxItemsValidator(valueIn);
                    break;
                
                case FILETYPE :
                    
                    myValidator = new fileTypeValidator(valueIn);
                    break;
                    
                case FORMAT :
                    
                    _format = valueIn;
                    break;
                    
                case UNSUPPORTED :
                    
                    break;
            }
            
            if (null != myValidator) {
                
                _validatorMap.put(keyIn, myValidator);
                _validatorList.add(myValidator);
            }
        }
    }
    
    private String validateList(JdbcDriverParameterValidationType keyIn, List<String> listIn) {
        
        String myMessage = null;
        AbstractValidator myValidator = _validatorMap.get(keyIn);
        
        if  (null != myValidator) {
            
            myMessage = myValidator.validate(listIn, null);
        }
        
        return myMessage;
    }
}

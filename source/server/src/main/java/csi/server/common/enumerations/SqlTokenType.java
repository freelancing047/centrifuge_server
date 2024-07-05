package csi.server.common.enumerations;

import java.io.Serializable;

/**
 * Created by centrifuge on 3/16/2015.
 */
public enum SqlTokenType implements Serializable {

    EXPRESSION,
    FUNCTION,
    DECISION_BRANCH,
    FIELD_WRAPPER,
    PARAMETER_WRAPPER,
    VALUE_WRAPPER,
    SYSTEM_VALUE,
    CONDITIONAL,
    CONDITIONAL_COMPONENT_1,
    CONDITIONAL_COMPONENT_2,
    CONDITIONAL_DEFAULT
}

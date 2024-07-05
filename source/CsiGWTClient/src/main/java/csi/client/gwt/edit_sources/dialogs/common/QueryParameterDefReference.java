/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.dialogs.common;

import java.util.Comparator;

import csi.server.common.model.query.QueryParameterDef;

/**
 * The grand idea: When editing a QueryParameterDef (henceforth param) in the grid, we wan't to maintain the 
 * modification as a change so that a cancel will not alter the underlying object. However, the edit cell takes the
 * whole object and sends it to the parameter editor dialog and gets back the edited object. At that point modifications
 * have already been made to the original. The ValueUpdater and commit mechanism only works with immutable objects. 
 * There are two options: create properties for every field and pass the propertyaccessor to the edit cell and 
 * individually update each value or treat the param itself as a field of a container object (this class). In using the
 * "container" model, we also pass a clone of the param to the param editor to simulate immutability of the param 
 * object.  
 * @author Centrifuge Systems, Inc.
 *
 */
public class QueryParameterDefReference {

    private static final Comparator<QueryParameterDef> comparator = new Comparator<QueryParameterDef>() {
        @Override
        public int compare(QueryParameterDef myParameter1, QueryParameterDef myParameter2) {

            String myName1 = (null != myParameter1) ? myParameter1.getName() : null;
            String myName2 = (null != myParameter2) ? myParameter2.getName() : null;

            if (null != myName1) {

                if (null != myName2) {

                    return myName1.compareTo(myName2);

                } else {

                    return 1;
                }

            } else if (null != myName2) {

                return -1;

            } else {

                return 0;
            }
        }
    };

    private QueryParameterDef parameter;
    private String name;
    private String prompt;
    private String type;

    public QueryParameterDefReference setParameter(QueryParameterDef parameterIn) {

        parameter = parameterIn;

        name = (null != parameter.getName()) ? parameter.getName() : ""; //$NON-NLS-1$
        prompt = (null != parameter.getPrompt()) ? parameter.getPrompt() : ""; //$NON-NLS-1$
        type = (null != parameter.getType()) ? parameter.getType().getLabel() : ""; //$NON-NLS-1$
        return this;
    }

    public QueryParameterDef getParameter() {

        return parameter;
    }

    public String getName() {

        return name;
    }

    public String getPrompt() {

        return prompt;
    }

    public String getType() {

        return type;
    }

    public static Comparator<QueryParameterDef> getComparator() {

        return comparator;
    }
}

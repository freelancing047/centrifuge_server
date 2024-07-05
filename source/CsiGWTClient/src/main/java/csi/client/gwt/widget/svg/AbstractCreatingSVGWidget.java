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
package csi.client.gwt.widget.svg;

import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.OMSVGTransform;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractCreatingSVGWidget extends AbstractSVGWidget {

    // Create helpers.

    public OMSVGPathElement createPath() {
        return getSVGDoc().createSVGPathElement();
    }

    public OMSVGTransform createTransform() {
        return getSVG().createSVGTransform();
    }

    public OMSVGRectElement createRect() {
        return getSVGDoc().createSVGRectElement();
    }
    
    public OMSVGTextElement createTextElement() {
        return getSVGDoc().createSVGTextElement();
    }
}

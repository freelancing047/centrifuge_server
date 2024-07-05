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

import org.vectomatic.dom.svg.OMNode;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.user.client.ui.HTML;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractSVGWidget extends HTML {

    private OMSVGSVGElement svg;
    private OMSVGDocument svgDoc;

    public AbstractSVGWidget() {
        super();
        svgDoc = OMSVGParser.currentDocument();
        svg = svgDoc.createSVGSVGElement();
        getElement().appendChild(svg.getElement());
    }

    public OMSVGSVGElement getSVG() {
        return svg;
    }

    public OMSVGDocument getSVGDoc() {
        return svgDoc;
    }

    public void append(OMNode element) {
        svg.appendChild(element);
    }

}

/*
 * Copyright Centrifuge Systems, Inc.  2012
 * 
 * @author Centrifuge Systems, Inc.
 */
package sample.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import csi.server.ws.rest.wire.RestDataView;
import csi.server.ws.rest.wire.RestRelGraph;

/**
 * The Class ResultData.
 */
@XmlRootElement(name = "resultData")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultData
{
    
    /** The graph. */
    @XmlElement
    public RestRelGraph graph;
    
    /** The data view. */
    @XmlElement
    public RestDataView dataView;
    
    /** The string. */
    @XmlElement
    public String       string;
    
    /** The operation status. */
    @XmlElement
    public String       operationStatus;
}

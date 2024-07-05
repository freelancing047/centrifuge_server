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
package csi.shared.core.visualization.matrix;


import com.google.common.eventbus.AllowConcurrentEvents;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
public class MatrixDataRequest extends AbstractMatrixData  {

    public static enum REQUEST_SUMMARIZATION_POLICY{
        ALLOW_SUMMARY, DISALLOW_SUMMARY, FORCE_SUMMARY;
    }

    private REQUEST_SUMMARIZATION_POLICY summarization_policy = REQUEST_SUMMARIZATION_POLICY.ALLOW_SUMMARY;
    private String dvUuid;
    private String vizUuid;

    public MatrixDataRequest() {
        super();
    }

    public MatrixDataRequest(int x1, int x2, int y1, int y2) {
        super(x1, x2, y1, y2);
    }

    public void setExtent(int x1, int x2, int y1, int y2){
        this.setStartX(x1);
        this.setStartY(y1);

        this.setEndX(x2);
        this.setEndY(y2);
    };


    public String getDvUuid() {
        return dvUuid;
    }

    public void setDvUuid(String dvUuid) {
        this.dvUuid = dvUuid;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }

    public boolean isSameRegion(MatrixDataRequest dr){
        if(dr.getStartX() == this.getStartX() && dr.getEndX() == this.getEndX() &&
                dr.getStartY() == this.getStartY() && dr.getEndY() == this.getEndY()){
            return true;
        }

        return false;

    }

    public REQUEST_SUMMARIZATION_POLICY getSummarizationPolicy() {
        return summarization_policy;
    }

    public void setSummarizationPolicy(REQUEST_SUMMARIZATION_POLICY summarization_policy) {
        this.summarization_policy = summarization_policy;
    }

    @Override
    public boolean equals(Object obj) {
        MatrixDataRequest req;
        if(obj instanceof MatrixDataRequest){
             req = (MatrixDataRequest) obj;
        }else{
            return false;
        }

        return req.getStartX() == this.getStartX() && req.getStartY() == this.getStartY() &&
                req.getEndX() == this.getEndX() && req.getEndY() == this.getEndY();
    }
}

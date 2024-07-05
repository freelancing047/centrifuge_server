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
package csi.server.common.model.visualization.chronos;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class TooltipEntry implements Serializable{

    private boolean hyperLink;
    private String title;
    private String value;

    public TooltipEntry() {
        super();
    }

    public TooltipEntry(boolean hyperLink, String title, String value) {
        super();
        this.hyperLink = hyperLink;
        this.title = title;
        this.value = value;
    }

    public boolean isHyperLink() {
        return hyperLink;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

}

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
package csi.client.gwt.widget.ui.surface;

import com.google.gwt.user.client.Timer;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class ZoomTimer extends Timer {

    private int zoom;
    private int mx, my;

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getMx() {
        return mx;
    }

    public void setMx(int mx) {
        this.mx = mx;
    }

    public int getMy() {
        return my;
    }

    public void setMy(int my) {
        this.my = my;
    }

}

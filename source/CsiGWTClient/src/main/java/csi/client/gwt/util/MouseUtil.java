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
package csi.client.gwt.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MouseUtil {

    /**
     * Gets the mouse x-position relative to a given element.
     * 
     * @param target the element whose coordinate system is to be used
     * @return the relative x-position
     */
    public static int getRelativeX(NativeEvent e, Element target) {
        return getRelativeX(e.getClientX(), target);
    }

    /**
     * @param clientX Client coordinate point
     * @param target
     * @return Location relative to element.
     */
    public static int getRelativeX(int clientX, Element target) {
        return clientX - target.getAbsoluteLeft() + target.getScrollLeft() + target.getOwnerDocument().getScrollLeft();
    }

    /**
     * Gets the mouse y-position relative to a given element.
     * 
     * @param target the element whose coordinate system is to be used
     * @return the relative y-position
     */
    public static int getRelativeY(NativeEvent e, Element target) {
        return getRelativeY(e.getClientY(), target);
    }
    
    /**
     * @param clientY Client coordinate point
     * @param target
     * @return Location relative to element.
     */
    public static int getRelativeY(int clientY, Element target) {
        return clientY - target.getAbsoluteTop() + target.getScrollTop()
                + target.getOwnerDocument().getScrollTop();
    }
}

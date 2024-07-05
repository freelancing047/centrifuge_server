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
package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.TabPane;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TabReferencingTabPane extends TabPane {

    private AbstractCsiTab tab;

    public TabReferencingTabPane(AbstractCsiTab tab) {
        super();
        this.tab = tab;
    }

    public AbstractCsiTab getTab() {
        return tab;
    }

}

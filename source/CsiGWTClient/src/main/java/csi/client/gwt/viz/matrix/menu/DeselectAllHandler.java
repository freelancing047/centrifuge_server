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
package csi.client.gwt.viz.matrix.menu;

import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DeselectAllHandler extends AbstractMatrixMenuEventHandler {

    public DeselectAllHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        getPresenter().deselectAll();
    }

}

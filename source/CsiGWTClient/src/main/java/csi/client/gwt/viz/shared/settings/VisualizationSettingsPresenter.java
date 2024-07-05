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
package csi.client.gwt.viz.shared.settings;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.model.dataview.DataView;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VisualizationSettingsPresenter {

    public void setWorksheetUuid(String worksheetUuid);

    public void setDataView(DataView dataView);
    
    public void setDataViewPresenter(AbstractDataViewPresenter abstractDataViewPresenter);

	public void setWorksheetPresenter(WorksheetPresenter worksheetPresenter);

    public void show();
}

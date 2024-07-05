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
package csi.shared.core;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface Constants {

    public interface UIConstants {

        String URL_RPC_ENDPOINT = "vortex";

        String RPC_HEADER_CSI_METHOD = "X-CSI-METHOD";

        int CHART_DIMENSION_DRILL_MAX_DRILL_CHARTS = 7;
        
        // Titles
        String TITLE_CHART_TAB_NAME_DIMENSION_DRILL = "Parallel Drill";
        
        // Styles (defined in less)
        public interface Styles {
            String CSI_TOOLTIP_CLASS = "csiTooltip";
            
            String WINDOW_LAYOUT_HEADER_BUTTON = "vizWindowHeaderButton";
            String WINDOW_LAYOUT_ACTIVATED = "windowLayoutActivated";
            String WINDOW_FULL_SCREEN_CONTAINER = "visualizationFullScreenContainer";
        }
    }

    public interface DataConstants {
        String FORMAT_DATE = "MM/dd/yyyy";
        String FORMAT_TIME = "hh:mm:ss";
        String FORMAT_DATE_TIME = "MM/dd/yyyy hh:mm:ss";
        String FORMAT_PICKER_DATE = "yyyy-mm-dd";
        String FORMAT_PICKER_TIME = "hh:ii:ss";
        String FORMAT_PICKER_DATE_TIME = "yyyy-mm-dd hh:ii:ss";
    }
    
    public interface FileConstants {
        String UPLOAD_INIT_PARAM_TOP_LEVEL_FOLDER = "topLevelFolder";
        String UPLOAD_INIT_PARAM_TEMP_FOLDER = "tempFolder";
        String UPLOAD_INIT_PARAM_MEMORY_THRESHOLD = "inMemoryThreshold";
        String UPLOAD_INIT_PARAM_MAX_FILE_SIZE = "maxFileSize";
        
        String UPLOAD_DEFAULT_TOP_LEVEL_FOLDER = "userfiles";
        String UPLOAD_DATAVIEW_FOLDER = "datafiles";
        String UPLOAD_THEME_FOLDER = "themefiles";
        String UPLOAD_DEFAULT_TEMP_FOLDER = "temp";
        int UPLOAD_DEFAULT_MEMORY_THRESHOLD = 1; // MB
        int UPLOAD_DEFAULT_MAX_FILE_SIZE = 1050; // MB
        String UPLOAD_FILE_MAPPING_PART = "fileMapping";
    }
}

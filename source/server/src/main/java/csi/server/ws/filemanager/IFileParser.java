package csi.server.ws.filemanager;

import java.io.File;
import java.util.HashMap;

public interface IFileParser {

    public String getUserDisplayName();

    public String getUserName();

    public String getUserID();

    public String getUserIDType();

    public String getGroupName();

    public String getGroupID();

    public String getDataViewName();

    public String getDataViewID();

    public String[] getInputParameters();

    public String getViewType();

    public String[] getChartDimensions() throws Exception;

    public String[] getAggregatingInfo();

    public String getRGLayout();

    public String getChartType();

    public String getTLLabelSwitch();

    public String[] getProcNames();

    public String[] getProcParameters(String procname);

    public HashMap parse(File f) throws Exception;

    public HashMap getParameterMap();
}

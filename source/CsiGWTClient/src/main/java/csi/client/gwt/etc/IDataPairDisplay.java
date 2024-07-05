package csi.client.gwt.etc;

import csi.server.common.dto.SelectionListData.ExtendedInfo;


public interface IDataPairDisplay extends ExtendedInfo {
    
    public String getKey();
    
    public String getGroupValueOne();
    
    public String getGroupValueTwo();
    
    public String getItemValueOne();
    
    public String getItemValueTwo();
}

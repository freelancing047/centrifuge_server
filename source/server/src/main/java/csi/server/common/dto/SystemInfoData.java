package csi.server.common.dto;



import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;


public class SystemInfoData implements IsSerializable {

    public ArrayList<String> servicePacks = new ArrayList<String>();
    public ArrayList<OrderedMapItem> systemInfo = new ArrayList<OrderedMapItem>();
    public ArrayList<OrderedMapItem> licenseInfo = new ArrayList<OrderedMapItem>();

    public void addMapToList(ArrayList<OrderedMapItem> list, String key, String value) {
        OrderedMapItem item = new OrderedMapItem();
        item.key = key;
        item.value = value;
        item.ordinal = list.size();
        list.add(item);
    }
}
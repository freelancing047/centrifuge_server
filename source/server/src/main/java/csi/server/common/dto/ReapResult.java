package csi.server.common.dto;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ReapResult implements IsSerializable {

    public String user;
    public int reapedCount;
    public int failedCount;
    public List<String> reaped = new ArrayList<String>();
    public List<String> failed = new ArrayList<String>();

    public void addReaped(String type, String name, String uuid) {
        reaped.add(name + " (" + uuid + ") " + type);
        reapedCount++;
    }

    public void addFailed(String type, String name, String uuid, String errormsg) {
        failed.add(name + " (" + uuid + ") " + type + " - " + errormsg);
        failedCount++;
    }
}

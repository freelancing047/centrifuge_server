package csi.server.common.dto;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class FileManagerResponseData implements IsSerializable {

    private String status;

    private FileManagerUserData userData;

    private FileManagerUserData list;

    private List<FileData> file;

    private List<DirectoryData> directory;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FileManagerUserData getUserData() {
        return userData;
    }

    public void setUserData(FileManagerUserData userData) {
        this.userData = userData;
    }

    public List<FileData> getFile() {
        if (file == null) {
            file = new ArrayList<FileData>();
        }
        return file;
    }

    public List<DirectoryData> getDirectory() {
        if (directory == null) {
            directory = new ArrayList<DirectoryData>();
        }
        return directory;
    }

    public FileManagerUserData getList() {
        return list;
    }

    public void setList(FileManagerUserData list) {
        this.list = list;
    }
}

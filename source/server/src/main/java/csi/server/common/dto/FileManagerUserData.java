package csi.server.common.dto;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


public class FileManagerUserData implements IsSerializable {

    String user;

    private FileManagerMessageData message;

    private List<FileData> file;

    private List<DirectoryData> directory;

    private String token;

    private String retrievalURL;

    private String processingURL;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRetrievalURL() {
        return retrievalURL;
    }

    public void setRetrievalURL(String retrievalURL) {
        this.retrievalURL = retrievalURL;
    }

    public String getProcessingURL() {
        return processingURL;
    }

    public void setProcessingURL(String processingURL) {
        this.processingURL = processingURL;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public FileManagerMessageData getMessage() {
        return message;
    }

    public void setMessage(FileManagerMessageData message) {
        this.message = message;
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

}

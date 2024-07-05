package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class FileData implements IsSerializable {

    private String name;

    private String fname;

    private String size;

    private String path;

    private String token;

    private String url;

    private String urltoken;

    private String lastmodified;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrltoken() {
        return urltoken;
    }

    public void setUrltoken(String urltoken) {
        this.urltoken = urltoken;
    }

    public String getLastmodified() {
        return lastmodified;
    }

    public void setLastmodified(String lastmodified) {
        this.lastmodified = lastmodified;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

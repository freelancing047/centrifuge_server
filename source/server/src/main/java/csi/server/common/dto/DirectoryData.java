package csi.server.common.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class DirectoryData implements IsSerializable {

    private String name;

    private String size;

    private String path;

    private String token;

    private String url;

    private String urltoken;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

package csi.config.advanced.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatternConfig implements IsSerializable {
    private boolean enabled;
    private String host;
    private boolean startWithServer;
    private int permutationLimit;
    private int combinationLimit;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isStartWithServer() {
        return startWithServer;
    }

    public void setStartWithServer(boolean startWithServer) {
        this.startWithServer = startWithServer;
    }

    public void setPermutationLimit(int permutationLimit) {
        this.permutationLimit = permutationLimit;
    }

    public int getPermutationLimit() {
        return permutationLimit;
    }

    public void setCombinationLimit(int combinationLimit) {
        this.combinationLimit = combinationLimit;
    }

    public int getCombinationLimit() {
        return combinationLimit;
    }
}

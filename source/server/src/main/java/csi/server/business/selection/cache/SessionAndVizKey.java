package csi.server.business.selection.cache;

import java.io.Serializable;

/**
 * A key for a value in the SelectionBroadcastCache.
 * The key contains a uuid for the visualization and the user name.
 * @author Centrifuge Systems, Inc.
 */
public class SessionAndVizKey implements Serializable {

    private final String sessionId;
    private final String vizUuid;

    public SessionAndVizKey(String sessionId, String vizUuid) {
        this.sessionId = sessionId;
        this.vizUuid = vizUuid;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionAndVizKey that = (SessionAndVizKey) o;

        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;
        return !(vizUuid != null ? !vizUuid.equals(that.vizUuid) : that.vizUuid != null);

    }

    @Override
    public int hashCode() {
        int result = sessionId != null ? sessionId.hashCode() : 0;
        result = 31 * result + (vizUuid != null ? vizUuid.hashCode() : 0);
        return result;
    }
}

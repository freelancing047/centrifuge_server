package csi.server.common.model.map;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Objects;

public class TrackidTracknameDuple implements Comparable<TrackidTracknameDuple>, IsSerializable, Serializable {
    private int trackId;
    private String trackName;

    public TrackidTracknameDuple() {
    }

    public TrackidTracknameDuple(int trackId, String trackName) {
        super();
        this.trackId = trackId;
        this.trackName = trackName;
    }

    public int getTrackid() {
        return trackId;
    }

    public void setTrackid(int trackId) {
        this.trackId = trackId;
    }

    public String getTrackname() {
        return trackName;
    }

    public void setTrackname(String trackName) {
        this.trackName = trackName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrackidTracknameDuple))
            return false;
        TrackidTracknameDuple o = (TrackidTracknameDuple) obj;
        if (trackNameEquals(o.trackName)) {
            return trackId == o.trackId;
        } else {
            return false;
        }
    }

    private boolean trackNameEquals(String otherTrackName) {
        boolean trackNameEquals = false;
        if (trackName == null && otherTrackName == null)
            trackNameEquals = true;
        else if (trackName != null && trackName.equals(otherTrackName))
            trackNameEquals = true;
        return trackNameEquals;
    }

    @Override
    public int compareTo(TrackidTracknameDuple arg0) {
        if (trackNameEquals(arg0.trackName)) {
            return Integer.compare(trackId, arg0.trackId);
        } else {
            return typenameCompare(arg0.trackName);
        }
    }

    private int typenameCompare(String otherTrackName) {
        int trackNameCompare;
        if (trackName == null) {
            if (otherTrackName == null) {
                trackNameCompare = 0;
            } else {
                trackNameCompare = -1;
            }
        } else {
            if (otherTrackName == null) {
                trackNameCompare = 1;
            } else {
                trackNameCompare = trackName.compareTo(otherTrackName);
            }
        }
        return trackNameCompare;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId, trackName);
    }
}
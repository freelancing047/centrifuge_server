package csi.server.business.selection;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class Pair implements Serializable{
    public final String x;
    public final String y;

    public Pair(String x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;
        return x.equals(pair.x) && y.equals(pair.y);
    }

    @Override
    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
}

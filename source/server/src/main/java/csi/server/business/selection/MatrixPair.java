package csi.server.business.selection;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MatrixPair implements Serializable{
    public final String x;
    public final String y;
    public final int row;

    public MatrixPair(String x, String y, int i) {
        this.x = x;
        this.y = y;
        this.row = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatrixPair pair = (MatrixPair) o;
        return x.equals(pair.x) && y.equals(pair.y);
    }

    @Override
    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
}

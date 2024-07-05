package csi.server.business.visualization.legend;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents an item on the Legend.
 * The legend can be used for charts and graphs at this time, each of them has a different implementation.
 */
public abstract class LegendItem implements IsSerializable {

    /**
     * The type name of this legend item. Could be the label of a node, or a functional field defined on a node/link.
     */
    public String typeName;

    /**
     * The number of visible items having the same typeName
     */
    public int count;

    /**
     * The internal key used to represent this legend item.
     */
    public String key;

    public LegendItem() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
         return true;
      }
        if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

        LegendItem that = (LegendItem) o;

        if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null) {
         return false;
      }

        return true;
    }

    @Override
    public int hashCode() {
        return typeName != null ? typeName.hashCode() : 0;
    }
}

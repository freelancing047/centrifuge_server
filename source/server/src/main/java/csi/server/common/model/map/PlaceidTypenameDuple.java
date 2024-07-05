package csi.server.common.model.map;

import java.io.Serializable;
import java.util.Objects;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PlaceidTypenameDuple implements Comparable<PlaceidTypenameDuple>, IsSerializable, Serializable {
    private Integer placeid;
    private String typename;

    public PlaceidTypenameDuple() {
    }

    public PlaceidTypenameDuple(Integer placeid, String typename) {
        super();
        this.placeid = placeid;
        this.typename = typename;
    }

    public Integer getPlaceid() {
        return placeid;
    }

    public void setPlaceid(Integer placeid) {
        this.placeid = placeid;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof PlaceidTypenameDuple)) {
         return false;
      }
        PlaceidTypenameDuple o = (PlaceidTypenameDuple) obj;
        if (typenameEquals(o.typename)) {
            return placeid == o.placeid;
        } else {
            return false;
        }
    }

    private boolean typenameEquals(String othertypename) {
        boolean typenameequals = false;
        if ((typename == null) && (othertypename == null)) {
         typenameequals = true;
      } else if ((typename != null) && (othertypename != null) && typename.equals(othertypename)) {
         typenameequals = true;
      }
        return typenameequals;
    }

    @Override
    public int compareTo(PlaceidTypenameDuple arg0) {
        if (typenameEquals(arg0.typename)) {
            return Integer.compare(placeid, arg0.placeid);
        } else {
            return typenameCompare(arg0.typename);
        }
    }

   private int typenameCompare(String othertypename) {
      int typenamecompare;

      if (typename == null) {
         typenamecompare = (othertypename == null) ? 0 : -1;
      } else {
         typenamecompare = (othertypename == null) ? 1 : typename.compareTo(othertypename);
      }
      return typenamecompare;
   }

   @Override
   public int hashCode() {
      return Objects.hash(placeid, typename);
   }
}

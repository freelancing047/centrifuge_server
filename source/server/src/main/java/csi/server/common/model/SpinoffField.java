package csi.server.common.model;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.user.client.rpc.IsSerializable;

//import csi.server.business.visualization.graph.base.GraphSupportingRows;

/**
 * <tt>SpinoffField</tt>.
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SpinoffField extends ModelObject implements Comparable<SpinoffField>, IsSerializable {

    /**
     * Usually the field name. For multi-dimensional cells, the first field
     * name.
     */
    public String name;

    /**
     * The DB column name or names used to define one of a cell's dimensions.
     * Dimensions are usually defined by a single field, but sometimes we may
     * want to define dimension "phone" from field "phone1" and "phone2".
     */
    @ElementCollection
    public List<String> fieldNames;

    public String value;

    public String type;

    public String bundleFunction;

    /**
     * Bundle-function parameters. FIXME: it would be more O-O to put bundle
     * function name and parameters into their own class.
     */
    @ElementCollection
    public List<String> params;

    public String valueType;

    public String cacheType;

    public SpinoffField() {
    	super();
    }

    // public String getFieldExpression() {
    // return GraphSupportingRows.getFieldExpression(bundleFunction, name, (String[]) params.toArray());
    // }
    //
    // public String getFieldExpression(int fieldInx) {
    // return GraphSupportingRows.getFieldExpression(bundleFunction, fieldNames.get(fieldInx), (String[]) params.toArray());
    // }
    //
    @Override
    public boolean equals(Object arg0) {
        boolean isEqual;

        if ((arg0 == null) || !(arg0 instanceof SpinoffField)) {
            isEqual = false;
        } else {
            SpinoffField that = (SpinoffField) arg0;

            isEqual = this.name.equals(that.name) && this.value.equals(that.value) && this.type.equals(that.type)
                    && this.bundleFunction.equals(that.bundleFunction);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * value.hashCode() * type.hashCode();
    }

   @Override
   public String toString() {
      return new StringBuilder(name).append(":").append(type).append(":").append(value).toString();
   }

    public int compareTo(SpinoffField that) {
        int compareVal;
        if (that == null) {
            compareVal = -1;
        } else {
            compareVal = this.name.compareTo(that.name);
            if (compareVal == 0) {
                compareVal = this.type.compareTo(that.type);
                if (compareVal == 0) {
                    compareVal = this.value.compareTo(that.value);
                }
            }
        }

        return compareVal;
    }
}

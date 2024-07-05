/**
 * 
 */
package csi.server.common.model;


import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("SortOrder")
public enum SortOrder implements Serializable {
    NONE(""),
    ASC("ASC"),
    DESC("DESC"),

    /*
   * Docs: https://www.postgresql.org/docs/8.3/static/queries-order.html
   *The NULLS FIRST and NULLS LAST options can be used to determine whether nulls appear before or after non-null values in the sort ordering.
   * By default, null values sort as if larger than any non-null value; that is, NULLS FIRST is the default for DESC order, and NULLS LAST otherwise.
   *
   */
    // Defaults - not really needed?
    //    DSC_NULLS_FIRST("DESC NULLS FIRST");
    //    ASC_NULLS_LAST("ASC NULLS LAST"),

    ASC_NULLS_FIRST("ASC NULLS FIRST"),
    DESC_NULLS_LAST("DESC NULLS LAST");

    private String sqlSyntax;

    private SortOrder(String sqlSyntax) {
        this.sqlSyntax = sqlSyntax;
    }

    public String getSQL() {
        return sqlSyntax;
    }

    public SortOrder toggle() {
        if (this == NONE) {
            return ASC;
        } else if (this == ASC) {
            return DESC;
        } else {
            return ASC;
        }
    }
    
    /**
     * @param value Comparator value (for ascending sort)
     * @return Modified per this sort order
     */
    public int compared(int value) {
        return (this == DESC) ? - value : value;
    }

}
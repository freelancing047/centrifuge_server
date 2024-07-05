/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.shared.core.color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class BrewerColorSet implements Serializable {

    private String name;
    private List<String> colors = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return List of color rgb codes in the format #rrggbb
     */
    public List<String> getColors() {
        return colors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof BrewerColorSet == false) {
            return false;
        } else {
            BrewerColorSet typed = (BrewerColorSet) obj;
            return Objects.equal(this.name, typed.name) && this.colors.equals(typed.colors);
        }
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this) //
                .add("name", getName()) //
                .add("colors", getColors()) //
                .toString();
    }
}

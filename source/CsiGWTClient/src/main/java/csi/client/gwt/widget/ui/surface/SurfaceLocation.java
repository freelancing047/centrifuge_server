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
package csi.client.gwt.widget.ui.surface;

import com.google.common.base.Objects;

import com.google.common.base.MoreObjects;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.surface.MatrixInsideOutSpiralVisitor.Direction;
import com.google.common.base.MoreObjects;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SurfaceLocation {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private int x, y;

    public SurfaceLocation(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SurfaceLocation other = (SurfaceLocation) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    /**
     * @param sizeX Total x indices
     * @param sizeY Total y indices.
     * @return true if current location is 0 to sizeX - 1 and 0 to sizeY - 1 
     */
    public boolean isWithinBounds(int sizeX, int sizeY) {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY;
    }

    public SurfaceLocation move(Direction dir) {
        switch (dir) {
            case DOWN:
                return new SurfaceLocation(x, y - 1);
            case LEFT:
                return new SurfaceLocation(x - 1, y);
            case RIGHT:
                return new SurfaceLocation(x + 1, y);
            case UP:
                return new SurfaceLocation(x, y + 1);
            default:
                throw new RuntimeException(i18n.surfaceLocationException() + dir); //$NON-NLS-1$
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("x", x) // //$NON-NLS-1$
                .add("y", y) // //$NON-NLS-1$
                .toString();
    }
}

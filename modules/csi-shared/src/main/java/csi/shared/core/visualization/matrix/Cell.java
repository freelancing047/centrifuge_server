/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.shared.core.visualization.matrix;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import csi.shared.core.util.TypedClone;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class Cell implements Comparable<Cell>, Serializable, TypedClone<Cell> {

    private int x, y;

    private Number value;

    public Cell() {
        super();
    }

    public Cell(int x, int y, double v) {
        this.x = x;
        this.y = y;
        value = v;
    }

    public Cell(Number value) {
        this.value = value;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return Index of this cell in the x category list.
     */
    public int getX() {
        return x;
    }

    /**
     * @return Index of this cell in the y category list.
     */
    public int getY() {
        return y;
    }

/*    public String getCategoryX() {
        return categoryX;
    }

    public void setCategoryX(String categoryX) {
        this.categoryX = categoryX;
    }

    public String getCategoryY() {
        return categoryY;
    }

    public void setCategoryY(String categoryY) {
        this.categoryY = categoryY;
    }*/

    public Number getValue() {
        return value;
    }

/*
    public Double getDoubleValue(){
        return value.doubleValue();
    }
*/

    public void setValue(Number value) {
        this.value = value;
    }

    @Override
    public Cell getClone() {
        Cell clone = new Cell();
        clone.setValue(getValue());
        clone.setX(getX());
        clone.setY(getY());
        return clone;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Cell) {
            Cell that = (Cell) obj;
            return this.x == that.x && this.y == that.y;
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
/*                .add("x", getCategoryX()) //
                .add("y", getCategoryY()) //*/
                .add("v", getValue()) //
                .toString();
    }

    public static int[] merge(int[] ids, int[] ids2) {
        int aLen = ids.length;
        int bLen = ids2.length;
        int[] result = new int[aLen + bLen];
        System.arraycopy(ids, 0, result, 0, aLen);
        System.arraycopy(ids2, 0, result, aLen, bLen);

        return result;
    }

    @Override
    public int compareTo(Cell o) {
        return ComparisonChain.start().compare(this.x, o.x).compare(this.y, o.y).result();
    }
}

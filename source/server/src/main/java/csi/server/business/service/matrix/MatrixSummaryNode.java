package csi.server.business.service.matrix;

import csi.shared.core.visualization.matrix.Cell;

/**
 * Created by Ivan on 10/11/2017.
 */
// this is what will be used to render the cells, and instead of ids associated with it,
// on the cell well have the range that the cell is responsible for, and then on server get(x,y) we will decide if we need to summirize again
public class MatrixSummaryNode{
     int startX, startY, endX, endY;

    private double value;

    MatrixSummaryNode(double value){
        this.value = value;
    }


    /**
     * First time this is called, we should set all of them to be the same, then each cell that we push will set the end coordinated to something else.
     * @param cell
     */
    public void setExtentsFromCell(Cell cell){
        if(startX == 0 && startY == 0) {
            startX = cell.getX();
            startY = cell.getY();
        }

        endX = cell.getX();
        endY = cell.getY();

    }


    public int getStartX() {
        return startX;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }



    //possibly add node type here.
    public void setMaxValue(double newVal){
        if(newVal > this.value){
            this.value = newVal;
        }
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }
}
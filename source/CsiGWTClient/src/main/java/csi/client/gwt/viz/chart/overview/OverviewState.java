package csi.client.gwt.viz.chart.overview;

/**
 * Data for the current state of the overview in pixels.
 * @author Centrifuge Systems, Inc.
 */
public class OverviewState {

    static final double ZOOM_FACTOR = .3;

    private int width;
    private int startPosition;
    private int endPosition;

    public OverviewState(int width){
        this.width = width;
        verifyWidth();
        this.startPosition = 0;
        this.endPosition = width;
    }

    public int zoomIn() {
        int halfOfDifference = getPositionDifference() / 2;
        int offset = (int)(halfOfDifference * (1 - ZOOM_FACTOR));
        int position = this.startPosition + halfOfDifference;
        modifyPositionByOffset(position, offset);
        return offset;
    }
    
    public int zoomOut(int maxView){
        int halfOfDifference = getPositionDifference() / 2;
        int offset = (int)(halfOfDifference * (1/(1- ZOOM_FACTOR)));
        //Always zoom out a little, otherwise we can get stuck.
        if(halfOfDifference == offset){
            offset++;
        }
        int position = this.startPosition + halfOfDifference;
        
        if(offset * 2 > maxView){
            offset = (int)Math.round(((double)maxView/2D)+.5);
        }
        
        modifyPositionByOffset(position, offset);
        if(this.startPosition == this.endPosition){
            this.endPosition++;
            verifyEndPosition();
        }
        
        if(this.startPosition == this.endPosition){
            this.startPosition--;
            verifyStartPosition();
        }
                
        return this.startPosition - this.endPosition;
    }

    public int zoomOut(){
        int halfOfDifference = getPositionDifference() / 2;
        int offset = (int)(halfOfDifference * (1/(1- ZOOM_FACTOR)));
        //Always zoom out a little, otherwise we can get stuck.
        if(halfOfDifference == offset){
            offset++;
        }
        int position = this.startPosition + halfOfDifference;
        modifyPositionByOffset(position, offset);
        return offset;
    }
    
    public void center(int position) {
        int offset = getPositionDifference() / 2;
        modifyPositionByOffset(position, offset);
    }

    public void pan(int panAmount) {
        if(startPosition + panAmount < 0)
            panAmount = -startPosition;
        if(endPosition + panAmount > width){
            panAmount = width - endPosition;
        }

        startPosition += panAmount;
        endPosition += panAmount;
    }

    public void scaleToNewWidth(int newWidth){
        double scaleFactor = (double)newWidth / (double)width;
        startPosition *= scaleFactor;
        endPosition *= scaleFactor;
        width = newWidth;
        verifyWidth();
        verifyBounds();
    }

    public void moveStart(int amount){
        startPosition += amount;
        verifyStartPosition();
    }

    public void moveEnd(int amount){
        endPosition += amount;
        verifyEndPosition();
    }

    public void reset() {
        startPosition = 0;
        endPosition = width;
    }

    public int getWidth() {
        return width;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public int getPositionDifference(){
        return this.endPosition - this.startPosition;
    }
    
    public void setRange(int startPosition, int endPosition){
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        verifyStartPosition();
        verifyEndPosition();
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
        verifyStartPosition();
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
        verifyEndPosition();
    }

    private void modifyPositionByOffset(int position, int offset) {
        this.startPosition = position - offset;
        this.endPosition = position + offset;
        verifyBounds();
    }

    private void verifyWidth() {
        if(width <= 0){
            width = 1;
        }
    }

    private void verifyBounds() {
        verifyEndPosition();
        verifyStartPosition();
    }

    private void verifyStartPosition() {
        if(startPosition < 0){
            startPosition = 0;
        }
        if(startPosition >= endPosition && startPosition > 0){
            startPosition = endPosition-1;
        }
    }

    private void verifyEndPosition() {
        if(endPosition > width){
            endPosition = width;
        }

        if(endPosition <= startPosition){
            endPosition = startPosition+1;
        }
    }

    public boolean validateDrag(int dragAmount, DragState dragState, int viewPortMax) {
        switch(dragState){
            case START_BAR:
                return (endPosition - (dragAmount + startPosition) > viewPortMax);
            case END_BAR:
                return (endPosition + dragAmount - (startPosition) > viewPortMax);
        }
        return false;
    }

    public OverviewState copy() {
        OverviewState copy = new OverviewState(width);
        copy.setStartPosition(startPosition);
        copy.setEndPosition(endPosition);
        return copy;
    }


}

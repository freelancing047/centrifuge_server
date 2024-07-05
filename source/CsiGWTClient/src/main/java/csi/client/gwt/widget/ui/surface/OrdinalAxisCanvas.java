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
package csi.client.gwt.widget.ui.surface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.aria.client.OrientationValue;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sencha.gxt.core.client.util.Rectangle;
import csi.client.gwt.WebMain;
import csi.config.Configuration;
import csi.shared.core.util.HasLabel;
import csi.shared.core.util.Native;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class OrdinalAxisCanvas extends Composite implements RequiresResize {

    private static final String RED = "red"; //$NON-NLS-1$
    private static final String WHITE = "white"; //$NON-NLS-1$
    private static final String BLACK = "black"; //$NON-NLS-1$
    private static final String PX_HELVETICA = "px Helvetica"; //$NON-NLS-1$
    private static final int FONT_SIZE_PIXELS = 9;
    private static final int MAX_LABEL_WIDTH = 45;
    private static final int OVERFLOW_RATIO = 10;


    private Canvas canvas;
    private OrientationValue orientation;
    private List<? extends HasLabel> domainValues = new ArrayList<HasLabel>();
    private OrdinalAxisExtent currentExtent;
    private AxisHighlightCallback axisHighlightCallback;
    private TitleDefinition axisTitle = new TitleDefinition();
    private Map<Integer, Integer> axisOrdinalLabelWidthByIndex = new HashMap<Integer, Integer>();
    private List<Integer> ordinalIndicesWithLabels = new ArrayList<Integer>();
    private OrdinalAxisHighlightTimer highlightTimer = new OrdinalAxisHighlightTimer();
    private AxisClickCallback axisClickCallback;
    private double zoom, lastZoom, lastZoomPoint;
    private double zeroLocation;
    private int maxIndex;
    private boolean leftEdgeBreach;
    private boolean rightEdgeBreach;
    private boolean axisInHighlight;

    public int getDataStart() {
        return dataStart;
    }

    public void setDataStart(int dataStart) {
        this.dataStart = dataStart;
    }

    public int getDataEnd() {
        return dataEnd;
    }

    public void setDataEnd(int dataEnd) {
        this.dataEnd = dataEnd;
    }

    private int dataStart, dataEnd;


    public OrdinalAxisCanvas(OrientationValue orientation) {
        canvas = Canvas.createIfSupported();
        canvas.setWidth("100%");
        canvas.setHeight("100%");

        initWidget(canvas);

        this.orientation = orientation;

        addHandlers();
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }

    /**
     *
     * @return determines how far we can move the first element off the screen.
     */
    public double getMaxOverflow(){
        return getDomainValuesSize() / (double)OVERFLOW_RATIO * lastZoom;
    }

    private void addHandlers() {
        canvas.addClickHandler(clickEvent -> {
            int mx = clickEvent.getRelativeX(canvas.getCanvasElement());
            int my = clickEvent.getRelativeY(canvas.getCanvasElement());

            Integer index = getClosestIndexWithLabel(mx, my);

            if (index > -1 && index < getDomainValuesSize()) {
                if (clickEvent.isShiftKeyDown() && clickEvent.isControlKeyDown()) {
                    axisClickCallback.onClickLabel(clickEvent.getClientX(), clickEvent.getClientY(), domainValues.get(index), index, getLocation(index), false);
                }
                if (!clickEvent.isShiftKeyDown() && clickEvent.isControlKeyDown()) {
                    axisClickCallback.onClickLabel(clickEvent.getClientX(), clickEvent.getClientY(), domainValues.get(index), index, getLocation(index), true);
                }
            }
        });
        canvas.addDomHandler(event -> {
            event.preventDefault();
            // Get the closest index to the mouse point. Get the bounding box for the text. If the mouse point is
            // within the bounding box, then we are over a label.
            int mx = event.getRelativeX(canvas.getCanvasElement());
            int my = event.getRelativeY(canvas.getCanvasElement());

            Integer index = getClosestIndexWithLabel(mx, my);
            boolean matchFailed = true;

            if (index != -1) {
                Integer width = axisOrdinalLabelWidthByIndex.get(index);
                if (width != null) {
                    Rectangle rect = null;
                    int l = (int) getLocation(index);
                    if (orientation == OrientationValue.HORIZONTAL) {
                        rect = new Rectangle(l - FONT_SIZE_PIXELS / 2, 10, FONT_SIZE_PIXELS, width);
                    } else {
                        rect = new Rectangle(canvas.getOffsetWidth() - width - 10, l - FONT_SIZE_PIXELS / 2, width,
                                FONT_SIZE_PIXELS);
                    }
                    if (rect.contains(mx, my)) {
                        if (!axisInHighlight) {
                            highlightTimer.cancel();
                            highlightTimer.setData(event.getClientX(), event.getClientY(), index);
                            highlightTimer.schedule(400);
                        }
                        matchFailed = false;
                    }
                }
            }

            if (matchFailed && axisInHighlight) {
                axisInHighlight = false;
                axisHighlightCallback.highlight(event.getClientX(), event.getClientY(), null, -1, -1, axisInHighlight);
            } else if (matchFailed) {
                // maybe we could request the data here
                highlightTimer.cancel();
            }
        }, MouseMoveEvent.getType());
    }

    private Integer getClosestIndexWithLabel(int x, int y) {
        Integer index = getClosestIndex(orientation == OrientationValue.HORIZONTAL ? x : canvas.getOffsetHeight() - y);

        int previous = -1;
        int largest = -1;

        for (Integer i : ordinalIndicesWithLabels) {
            if (i > index) {
                largest = i;
                break;
            }
            previous = i;
        }

        if (largest != -1) {

            if (previous == -1) {
                index = largest;
            } else {
                if ((largest - index) > (previous - index)) {
                    index = previous;
                } else {
                    index = largest;
                }
            }
            return index;
        }

        return -1;

    }

    public TitleDefinition getAxisTitle() {
        return axisTitle;
    }

    public void setAxisTitle(TitleDefinition axisTitle) {
        this.axisTitle = axisTitle;
    }

    public void setAxisClickCallback(AxisClickCallback axisClickCallback) {
        this.axisClickCallback = axisClickCallback;
    }

    public void setAxisHighlightCallback(AxisHighlightCallback callback) {
        this.axisHighlightCallback = callback;
    }

    public void setDomainValues(List<? extends HasLabel> values) {
        this.domainValues = values;
        if (getOffsetDimension() > 0 && currentExtent == null) {
            // This takes care of sizing the initial extent correctly
            setExtents(0, 50);
        } else if (getOffsetDimension() > 0) {
            display();
        }
    }

    public OrdinalAxisExtent getCurrentExtent() {
        return currentExtent;
    }

    /**
     * @return Pixel size of the axis
     */
    public int getOffsetDimension() {
        return orientation == OrientationValue.HORIZONTAL ? getOffsetWidth() : getOffsetHeight();
    }

    /**
     * @param start 0-based start index
     * @param end 0-based end indexo
     */
    public void setExtents(int start, int end) {
        if (end > domainValues.size() - 1) {
            end = domainValues.size() - 1;
        }

        // Compute zoom from start and end.
        int segments = end - start + 2;

        zoom = getOffsetDimension() / (double) segments;
        lastZoom = zoom;
        zeroLocation = -(start - 1) * zoom;
        display();
    }

    /**
     * @param distance Positive distance is moving to the left/up.
     */
    public void pan(int distance) {
       zeroLocation += distance;
       display();
    }

    /**
     * @param zoomPoint The point at which to zoom
     * @param zoomFactor Amount to zoom by. 1 = 10%, 2 = 10% of 10% etc. Negative implies zoom out. Positive is zoom in
     */
    public void zoom(int zoomPoint, int zoomFactor) {
        double scale = 1.0;

        for (int i = 0; i < Math.abs(zoomFactor); i++) {
            if (zoomFactor > 0) {
                scale *= 1.1;
            } else {
                scale *= 0.9;
            }
        }

        zeroLocation = (zeroLocation - zoomPoint) * scale + zoomPoint;
        lastZoom = zoom;
        lastZoomPoint = zoomPoint;
        zoom *= scale;
        display();
    }

    @Override
    public void onResize() {
        if (super.getOffsetWidth() != 0 && super.getOffsetHeight() != 0) {
            canvas.setCoordinateSpaceWidth(getOffsetWidth());
            canvas.setCoordinateSpaceHeight(getOffsetHeight());
            if (this.domainValues.size() > 0) {
                adjustAfterResize();
                fireEvent(new AxisResizeEvent());
            }
        }
    }

    public HandlerRegistration addResizeHandler(ResizeHandler handler) {
        return addHandler(handler, ResizeEvent.getType());
    }

    public int getStartIndex() {
        return (int) Math.floor(-zeroLocation / zoom);
    }

    public int getStartIndexForMoreData(boolean isSummary){
        adjust();
        return updateIndex(getDataStart(), isSummary);
    }

    public int getOffsetForDetail(){
        return 1;
    }

    // this works if its not summary
    public int getCategoryCount(){
        return getDomainValuesSize();
    }


    // pretty much a clone from matrixDataservice.
    public int getNumberOfCategories(int currentWindow){
        if(currentWindow <= (int) Math.sqrt(WebMain.getClientStartupInfo().getMatrixMaxCells() * 1.2)){
            return 1;
        }else{
            return currentWindow/(int) Math.sqrt(WebMain.getClientStartupInfo().getMatrixMaxCells() * 1.2);
        }
    }

    public int getIncrement(){
        double howMany = ((zeroLocation)) / getSegmentSize();
        int i = howMany < 0 ? (int)Math.floor(howMany) :  (int) Math.ceil(howMany);
        i = Math.abs(i);
        return i;
    }

    // interesting, but in oder for this to work - we have to get the axis state independent of the others..

    public int updateIndex(int currentX, boolean isSummary){

        int offset = isSummary ? getNumberOfCategories(dataEnd - dataStart) : getOffsetForDetail();

        int i = getIncrement();

        if(leftEdgeBreach && rightEdgeBreach){
            return currentX;
        }

        if(leftEdgeBreach){
            return isSummary ?  currentX - Math.abs(offset * i) : currentX - i;
        }

        if(rightEdgeBreach){
            return isSummary ?  currentX + Math.abs(offset * i) : currentX + i;
        }
        return currentX;

    }


    // possibly do a delta of x/y and then move it.. probs better
        // todo fix this bs
    public int getEndIndexForMoreData(boolean isSummary){
        return updateIndex(getDataEnd(), isSummary);
    }

    public int getEndIndex() {
        return getStartIndex() + (int) (getOffsetDimension() / zoom) + 1;
    }

    private void adjustAfterResize(){
        if(currentExtent != null){
            display(currentExtent.getStartIndex(), currentExtent.getEndIndex());
        }else{
            display();
        }
    }


    private void display() {

        adjust();

        int startIndex = getStartIndex();
        int endIndex = getEndIndex();

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex > domainValues.size() - 1) {
            endIndex = domainValues.size() - 1;
        }

        display(startIndex, endIndex);

        currentExtent = new OrdinalAxisExtent(startIndex, endIndex);
    }

    /**
     * This displays the categories requested
     * @param startIndex
     * @param endIndex
     */
    private void display(int startIndex, int endIndex) {
        Context2d ctx = canvas.getContext2d();
        ctx.clearRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());

        axisOrdinalLabelWidthByIndex.clear();
        ordinalIndicesWithLabels.clear();

        ctx.setFont(FONT_SIZE_PIXELS + PX_HELVETICA);
        ctx.setStrokeStyle(BLACK);
        ctx.setTextAlign(TextAlign.END);
        ctx.setTextBaseline(TextBaseline.MIDDLE);
        ctx.save();


        ctx.beginPath();

        switch (orientation) {
            case HORIZONTAL: {
                ctx.rotate(-Math.PI / 2.0);
                ctx.translate(-canvas.getOffsetHeight(), 0);
                int i = startIndex;

                double x = canvas.getOffsetHeight() - 10;
                double currentPosition = 0;
                int largest = -1;
                while (i <= endIndex) {
                    double pos = getLocation(i);

                    if (i == startIndex || Math.abs(pos - currentPosition) > FONT_SIZE_PIXELS) {
                        ctx.fillText(fitLabel(domainValues.get(i).getLabel()), x, pos);

                        axisOrdinalLabelWidthByIndex.put(i, (int) Math.ceil(ctx.measureText(domainValues.get(i).getLabel()).getWidth()));
                        ordinalIndicesWithLabels.add(i);

                        if (i > largest) largest = i;

                        currentPosition = pos;
                    }

                    if (zoom >= 2.0) {
                        ctx.moveTo(x + 5, pos);
                        ctx.lineTo(x + 10, pos);
                    }

                    i++;
                }

                ordinalIndicesWithLabels.add(largest + 1);

                if (zoom < 2.0) {
                    // The axis ticks are so close that they will resemble a solid black rectangle. We simply draw that
                    ctx.rect(x + 5, getLocation(startIndex), 5, getLocation(endIndex) - getLocation(startIndex));
                    ctx.fill();
                }

            }
            break;


            case VERTICAL: {
                int i = startIndex;
                double x = canvas.getOffsetWidth() - 10;
                double currentPosition = 0;
                int largest = -1;
                while (i <= endIndex) {
                    double pos = getLocation(i);
                    if (i == startIndex || Math.abs(pos - currentPosition) > 9)  {
                        ctx.fillText(fitLabel(domainValues.get(i).getLabel()), x, pos);
                        axisOrdinalLabelWidthByIndex.put(i, (int) Math.ceil(ctx.measureText(domainValues.get(i).getLabel()).getWidth()));
                        ordinalIndicesWithLabels.add(i);
                        if (i > largest) largest = i;
                        currentPosition = pos;
                    }
                    if (zoom >= 2.0) {
                        ctx.moveTo(x + 5, pos);
                        ctx.lineTo(x + 10, pos);
                    }
                    i++;
                }
                ordinalIndicesWithLabels.add(largest + 1);
                if (zoom < 2.0) {
                    // The axis ticks are so close that they will resemble a solid black rectangle. We simply draw that
                    ctx.rect(x + 5, getLocation(startIndex), 5, getLocation(endIndex) - getLocation(startIndex));
                    ctx.fill();
                }
            }
        } // end case

        ctx.stroke();
        ctx.restore();

        displayTitle();

    }

    /**
     * Renders the titles of axis.
     */
    private void displayTitle() {
        Context2d ctx = canvas.getContext2d();
        ctx.save();
        ctx.setFont(axisTitle.getFontSize() + PX_HELVETICA);
        ctx.setStrokeStyle(CssColor.make(WHITE));
        ctx.setLineWidth(12);
        ctx.setTextAlign(TextAlign.CENTER);
        ctx.setTextBaseline(TextBaseline.MIDDLE);

        ctx.setLineJoin(Context2d.LineJoin.MITER);
        ctx.setMiterLimit(2);

        switch (orientation) {
            case HORIZONTAL: {
                ctx.strokeText(axisTitle.getText(), canvas.getOffsetWidth() / 2.0, canvas.getOffsetHeight() - 10);
                ctx.fillText(axisTitle.getText(), canvas.getOffsetWidth() / 2.0, canvas.getOffsetHeight() - 10);
                break;
            }
            case VERTICAL: {
                ctx.rotate(-Math.PI / 2.0);
                ctx.translate(-canvas.getOffsetHeight(), 0);

                ctx.strokeText(axisTitle.getText(), canvas.getOffsetHeight() / 2.0, 10);
                ctx.fillText(axisTitle.getText(), canvas.getOffsetHeight() / 2.0, 10);
            }
        }
        ctx.restore();
    }

    /**
     * Ensure we don't go too far on either edge and that we are not zoomed out too much. We want to allow each edge
     * to go over by a 10th of the full size.
     */
    // so the zoom is also adjusted here.
    private void adjust() {

        double maxOverflow = getMaxOverflow();
        leftEdgeBreach = zeroLocation > maxOverflow + 5;
        rightEdgeBreach = getOffsetDimension() - zeroLocation - zoom * (domainValues.size() - 1) > maxOverflow + 5;

//        // if  the first index is further than 1.5 of curr distance between categories .
//        boolean leftEdgeBreach1 = zeroLocation > (getSegmentSize() * 1.5);
//        // if last index
//        boolean  rightEdgeBreach1 = getOffsetDimension() - zeroLocation - zoom * (domainValues.size() - 1) > (getSegmentSize() * 1.5);



        if (leftEdgeBreach && rightEdgeBreach) {
            // We've zoomed out too much. Adjust zoom level
            if(getCurrentExtent() != null){
                int segments = getCurrentExtent().getEndIndex() - getCurrentExtent().getStartIndex() + 1 ;
                zeroLocation = (getOffsetDimension()/segments)/2;
                zoom = getOffsetDimension() / (double)segments ;
            }else{
                zoom = lastZoom;
            }
        }
//        } else if (leftEdgeBreach) {
//            zeroLocation = maxOverflow;
//        } else if (rightEdgeBreach) {
//            zeroLocation = getOffsetDimension() - zoom * (domainValues.size() - 1) - maxOverflow;
//        }
    }

    /**
     * @param index 0-based index of axis ordinal.
     * @return Location of the axis ordinal in the axis. For vertical axis, it returns the location relative to the
     * top (i.e., the top is location 0 but index = 0 will be at the bottom (height).
     */
    public double getLocation(int index) {
        double rawLocation = index * zoom + zeroLocation;
        // either x or inverse of the proper location..
        rawLocation = orientation == OrientationValue.HORIZONTAL ?
                        rawLocation : getOffsetDimension() - rawLocation;

        return rawLocation;
    }

    /**
     * Reverse of getLocation.
     * @param location Location on screen. For vertical axis, this should be relative to the bottom.
     * @return category index closest to location.
     */
    public int getClosestIndex(int location) {
        return (int) Math.round((location - zeroLocation) / zoom);
}

    /**
     * Reverse of getLocation.
     * @param location Location on screen. For vertical axis, this should be relative to the bottom.
     * @return category index closest to location.
     */
    public int getClosestIndexCeiling(int location) {
        return (int) Math.ceil((location - zeroLocation) / zoom);
    }

    /**
     * Reverse of getLocation.
     * @param location Location on screen. For vertical axis, this should be relative to the bottom.
     * @return category index closest to location.
     */
    public int getClosestIndexFloor(int location) {
        return (int) Math.floor((location - zeroLocation) / zoom);
    }

    /**
     * @return Distance between two consecutive axis values.
     */
    public double getSegmentSize() {
        return zoom;
    }

    public int getDomainValuesSize() {
        return domainValues.size();
    }


    public void highlight(int index, boolean highlight) {

        //there is and we break the sync on the client which ends up with an error here, becaues the index passed in is not
        // in our data at that time.. This is just a haxx
        if(domainValues.size() < index){
            return;
        }

        if (highlight == false) {
            display();
        } else {
            Context2d ctx = canvas.getContext2d();
            ctx.save();

            ctx.setFont(FONT_SIZE_PIXELS + PX_HELVETICA);
            ctx.setFillStyle(RED);
            ctx.setTextAlign(TextAlign.END);
            ctx.setTextBaseline(TextBaseline.MIDDLE);
            ctx.beginPath();
            switch (orientation) {
                case HORIZONTAL: {
                    ctx.rotate(-Math.PI / 2.0);
                    ctx.translate(-canvas.getOffsetHeight(), 0);

                    double x = canvas.getOffsetHeight() - 10;
                    double pos = getLocation(index);
                    ctx.setStrokeStyle(WHITE);
                    ctx.setLineWidth(3.0);
                    ctx.strokeText(fitLabel(domainValues.get(index).getLabel()), x, pos);
                    ctx.fillText(fitLabel(domainValues.get(index).getLabel()), x, pos);
                    ctx.setStrokeStyle(RED);
                    if (zoom >= 2.0) {
                        ctx.moveTo(x + 5, pos);
                        ctx.lineTo(x + 10, pos);
                    }

                }
                break;
                case VERTICAL: {
                    double x = canvas.getOffsetWidth() - 10;

                    double pos = getLocation(index);
                    ctx.setStrokeStyle(WHITE);
                    ctx.setLineWidth(3.0);
                    ctx.strokeText(fitLabel(domainValues.get(index).getLabel()), x, pos);
                    ctx.fillText(fitLabel(domainValues.get(index).getLabel()), x, pos);
                    ctx.setStrokeStyle(RED);
                    if (zoom >= 2.0) {
                        ctx.moveTo(x + 5, pos);
                        ctx.lineTo(x + 10, pos);
                    }
                }
            } // end case
            ctx.stroke();
            ctx.restore();
        }
    }


    /**
     * Will start from the back of the string and remove one char at a time until the label fits, appends ellipsis at the end of the label.
     * @param lbl
     * @return
     */
    private String fitLabel(String lbl) {

        Context2d ctx = canvas.getContext2d();
        boolean adj = false;
        if (ctx.measureText(lbl).getWidth() > MAX_LABEL_WIDTH) {
            while (ctx.measureText(lbl + "...").getWidth() > MAX_LABEL_WIDTH) {
                lbl = lbl.substring(0, lbl.length() - 1);
                adj = true;
            }
        }

        return adj ? lbl + "..." : lbl;
    }

    public String toDataUrl(String imageType) {
        return canvas.toDataUrl(imageType);
    }

    public double getZeroLocation() {
        return zeroLocation;
    }

    public void setZeroLocation(double zeroLocation) {
        this.zeroLocation = zeroLocation;
    }

    public double getZoom() {
        return this.zoom;
    }

    public boolean isLeftEdgeBreach() {
        return leftEdgeBreach;
    }

    public boolean isRightEdgeBreach() {
        return rightEdgeBreach;
    }

    public class OrdinalAxisHighlightTimer extends Timer {
        private int x, y;

        private Integer index;

        public void setData(int x, int y, Integer index) {
            this.x = x;
            this.y = y;
            this.index = index;
        }
        @Override
        public void run() {
            try {
                axisInHighlight = true;
                axisHighlightCallback.highlight(x, y, domainValues.get(index), index, getLocation(index), axisInHighlight);
            }catch (Exception e){

            }
        }

    }


}

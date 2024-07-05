///**
// *  Copyright (c) 2008 Centrifuge Systems, Inc.
// *  All rights reserved.
// *
// *  This software is the confidential and proprietary information of
// *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
// *  not disclose such Confidential Information and shall use it only
// *  in accordance with the terms of the license agreement you entered
// *  into with Centrifuge Systems.
// *
// **/
//package csi.client.gwt.widget.ui.surface;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.vectomatic.dom.svg.OMSVGTextElement;
//import org.vectomatic.dom.svg.OMText;
//import org.vectomatic.dom.svg.utils.SVGConstants;
//
//import com.google.gwt.aria.client.OrientationValue;
//import com.google.gwt.core.client.JsArrayString;
//import com.google.gwt.dom.client.NativeEvent;
//import com.google.gwt.dom.client.Style.Unit;
//import com.google.gwt.user.client.DOM;
//import com.google.gwt.user.client.ui.RequiresResize;
//
//import csi.client.gwt.widget.svg.SVGWidget;
//import csi.shared.core.util.HasLabel;
//
///**
// * @author Centrifuge Systems, Inc.
// *
// */
////public class OrdinalAxis extends SVGWidget implements RequiresResize {
////
////    private static final String MIDDLE = "middle";
////
////	private static final String TEXT_ANCHOR = "text-anchor";
////
////	private static final String TRANSLATE = "translate(";
////
////	private static final String TRANSFORM = "transform";
////
////	private static final int FONT_SIZE_PIXELS = 9;
////
////    private String axisClass;
////    private OrientationValue orientation;
////    private List<? extends HasLabel> domainValues = new ArrayList<HasLabel>();
////    private double firstIndexOffset;
////    private double offsetSize = 1.0;
////    private double lastStartRatio;
////    private OrdinalAxisExtent currentExtent;
////    private ExtentCallback extentCallback;
////    private AxisHighlightCallback axisHighlightCallback;
////    private TitleDefinition axisTitle = new TitleDefinition();
////    private OMSVGTextElement titleElement;
////    private String highlightedAxisLabelClass = "axisLabelHighlighted";
////
////    public interface ExtentCallback {
////
////        public void setExtent(OrdinalAxisExtent extent);
////    }
////
////    public interface AxisClickCallback {
////        /**
////         * @param event Mouse click native event.
////         * @param value axis value that has been clicked
////         * @param i Index of the axis value
////         * @param location The x or y location of the highlight along the axis.
////         * @param enable true to select, false to unselect
////         */
////        public <T extends HasLabel> void onClickLabel(int clientX, int clientY, T value, int i, double location,
////                                                   boolean enable);
////    }
////
////    public interface AxisHighlightCallback {
////
////        /**
////         * @param event Mouse over/out native event.
////         * @param value Axis value that has been highlighted.
////         * @param i Index of the axis value
////         * @param location The x or y location of the highlight along the axis.
////         * @param enable true if the highlight has been enabled (mouse-over), false if highlight is now disabled (mouse-out)
////         */
////        public <T extends HasLabel> void highlight(int clientX, int clientY, T value, int i, double location,
////                boolean enable);
////    }
////
////    public OrdinalAxis(OrientationValue orientation, String axisClass, ExtentCallback callback) {
////        super();
////        getSVG().setId(DOM.createUniqueId());
////        getSVG().getWidth().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
////        getSVG().getHeight().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
////
////        this.axisClass = axisClass;
////        this.orientation = orientation;
////        this.extentCallback = callback;
////    }
////
////    @Override
////    public void onResize() {
////        if (super.getOffsetWidth() != 0 && super.getOffsetHeight() != 0) {
////            getSVG().setViewBox(0, 0, getOffsetWidth(), getOffsetHeight());
////            if (this.domainValues != null) {
////                refresh();
////            }
////            switch (orientation) {
////                case HORIZONTAL: {
////                    int height = getOffsetHeight() - getAxisTitle().getFontSize() / 2;
////                    int width = getOffsetWidth() / 2;
////                    titleElement.setAttribute(TRANSFORM, TRANSLATE + width + ", " + height + ")");
////                    break;
////                }
////                case VERTICAL: {
////                    int height = getOffsetHeight() / 2;
////                    int width = 15;
////                    titleElement.setAttribute(TRANSFORM, TRANSLATE + width + ", " + height + ")rotate(-90)");
////                    break;
////                }
////            }
////        }
////    }
////
////    @Override
////    protected void onAttach() {
////        super.onAttach();
////        setupTitle();
////    }
////
////    private void setupTitle() {
////        if (titleElement == null) {
////            titleElement = createTextElement();
////            getSVG().appendChild(titleElement);
////            titleElement.setAttribute(TEXT_ANCHOR, MIDDLE);
////        }
////        titleElement.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, getAxisTitle().getFontColor());
////        titleElement.getStyle().setFontSize(getAxisTitle().getFontSize(), Unit.PX);
////        if (titleElement.getFirstChild() != null) {
////            titleElement.removeChild(titleElement.getFirstChild());
////        }
////        titleElement.appendChild(new OMText(getAxisTitle().getText()));
////    }
////
////    public String getHighlightedAxisLabelClass() {
////        return highlightedAxisLabelClass;
////    }
////
////    public void setHighlightedAxisLabelClass(String highlightedAxisLabelClass) {
////        this.highlightedAxisLabelClass = highlightedAxisLabelClass;
////    }
////
////    public void setAxisHighlightCallback(AxisHighlightCallback axisHighlightCallback) {
////        this.axisHighlightCallback = axisHighlightCallback;
////    }
////
////    public TitleDefinition getAxisTitle() {
////        return axisTitle;
////    }
////
////    public void setAxisTitle(TitleDefinition title) {
////        this.axisTitle = title;
////        setupTitle();
////    }
////
////    public void setDomainValues(List<? extends HasLabel> values) {
////        this.domainValues = values;
////    }
////
////    public int getDomainValuesSize() {
////        return domainValues.size();
////    }
////
////    public void refresh() {
////        displayAtPosition(lastStartRatio, offsetSize);
////    }
////
////    /**
////     * @param categoryIndex Index within the categories
////     * @return Pixel location in the axis
////     */
////    public double getLocation(int categoryIndex) {
////        int delta = categoryIndex - currentExtent.getStartIndex();
////        double value = firstIndexOffset + (delta * offsetSize) + offsetSize / 2.0;
////        if (orientation == OrientationValue.VERTICAL) {
////            return getOffsetHeight() - value;
////        } else {
////            return value;
////        }
////    }
////
////    /**
////     * Reverse of getLocation.
////     * @param x Location on screen
////     * @return category index closest to location.
////     */
////    public int getClosestIndex(int location) {
////        return (int) Math.round((location - firstIndexOffset - offsetSize / 2.0) / offsetSize)
////                + currentExtent.getStartIndex();
////    }
////
////    /**
////     * @param startRatio A normalized ratio of display start location (the ratio of the left-edge of a slider to the full
////     * width that the slider can scroll).
////     * @param offsetSize Distance between each ordinal value.
////     */
////    public void displayAtPosition(double startRatio, double offsetSize) {
////        if (this.domainValues != null && this.domainValues.size() > 0) {
////            this.offsetSize = offsetSize;
////            this.lastStartRatio = startRatio;
////            double value = (startRatio * this.domainValues.size());
////            int firstIndex = (int) (Math.floor(value));
////            firstIndexOffset = value <= 0 ? 0 : 0 - (value - Math.floor(value)) * offsetSize;
////            if (firstIndex < 0) {
////                firstIndex = 0;
////            }
////
////            int size = orientation == OrientationValue.HORIZONTAL ? getOffsetWidth() : getOffsetHeight();
////            int numberToDisplay = (int) Math.ceil(size / offsetSize);
////
////            HasLabel[] valuesToDisplay;
////            if (firstIndex + numberToDisplay > this.domainValues.size()) {
////                valuesToDisplay = this.domainValues.subList(firstIndex, this.domainValues.size()).toArray(
////                        new HasLabel[0]);
////                currentExtent = new OrdinalAxisExtent(firstIndex, this.domainValues.size() - 1);
////                this.extentCallback.setExtent(currentExtent);
////            } else {
////                valuesToDisplay = this.domainValues.subList(firstIndex, firstIndex + numberToDisplay).toArray(
////                        new HasLabel[0]);
////                currentExtent = new OrdinalAxisExtent(firstIndex, firstIndex + numberToDisplay - 1);
////                this.extentCallback.setExtent(currentExtent);
////            }
////            display(firstIndex, valuesToDisplay);
////        }
////    }
////
////    public OrdinalAxisExtent getCurrentExtent() {
////        return currentExtent;
////    }
////
////    private void display(int firstIndex, HasLabel[] valuesToDisplay) {
////
////        JsArrayString jsStrings = (JsArrayString) JsArrayString.createArray();
////
////        // Doesn't matter what index we use here.
////        int index = firstIndex;
////        double currentPosition = 0;
////        for (HasLabel label : valuesToDisplay) {
////            String s = label.getLabel();
////            double newPos = getLocation(index);
////            if (index != firstIndex) {
////                double delta = Math.abs(newPos - currentPosition);
////                if (delta < FONT_SIZE_PIXELS) {
////                    jsStrings.push(" ");
////                } else {
////                    jsStrings.push(s);
////                    currentPosition = newPos;
////                }
////            } else {
////                currentPosition = newPos;
////                jsStrings.push(s);
////            }
////            index++;
////        }
////        jsSetDomainValues(getSVG().getId(), jsStrings, this.orientation == OrientationValue.HORIZONTAL);
////    }
////
////    private String getTransform(int i, boolean horizontal) {
////        double t = getLocation(i + currentExtent.getStartIndex());
////        if (horizontal) {
////            return TRANSLATE + t + ",2)rotate(-90)";
////        } else {
////            return TRANSLATE + (getOffsetWidth() - 2) + ", " + t + ")";
////        }
////    }
////
////    private void onMouseOverOut(NativeEvent event, int i, boolean over) {
////        int index = currentExtent.getStartIndex() + i;
////        highlight(index, over);
////        if (axisHighlightCallback != null) {
////            axisHighlightCallback.highlight(event.getClientX(), event.getClientY(), domainValues.get(index), index,
////                    getLocation(i), over);
////        }
////    }
////
////    private native void jsSetDomainValues(String svgId, JsArrayString values, boolean horizontalAxis) /*-{
////		var parentThis = this;
////		var gClass = this.@csi.client.gwt.widget.ui.surface.OrdinalAxis::axisClass;
////		var fontSizeComp = @csi.client.gwt.widget.ui.surface.OrdinalAxis::FONT_SIZE_PIXELS / 3.0;
////
////		var vals = $wnd.d3.select("#" + svgId).selectAll("." + gClass).data(
////				values);
////
////		vals
////				.attr(
////						"transform",
////						function(d, i) {
////							return parentThis.@csi.client.gwt.widget.ui.surface.OrdinalAxis::getTransform(IZ)(i, horizontalAxis);
////						}).select("text").text(function(d, i) {
////					return values[i]
////				});
////
////		var gElements = vals
////				.enter()
////				.append("g")
////				.on(
////						"mouseover",
////						function(d, i) {
////							parentThis.@csi.client.gwt.widget.ui.surface.OrdinalAxis::onMouseOverOut(Lcom/google/gwt/dom/client/NativeEvent;IZ)($wnd.d3.event, i, true);
////						})
////				.on(
////						"mouseout",
////						function(d, i) {
////							parentThis.@csi.client.gwt.widget.ui.surface.OrdinalAxis::onMouseOverOut(Lcom/google/gwt/dom/client/NativeEvent;IZ)($wnd.d3.event, i, false);
////						})
////				.attr("class", gClass)
////				.attr(
////						"transform",
////						function(d, i) {
////							return parentThis.@csi.client.gwt.widget.ui.surface.OrdinalAxis::getTransform(IZ)(i, horizontalAxis);
////						});
////		gElements
////				.append("text")
////				//
////				.attr("font-size",
////						@csi.client.gwt.widget.ui.surface.OrdinalAxis::FONT_SIZE_PIXELS)
////				.attr("transform", "translate(-5, " + fontSizeComp + ")").attr(
////						"text-anchor", "end").text(function(d, i) {
////					return values[i]
////				})
////		gElements.append("path").attr("d", "M2,0H-3").attr("style",
////				"stroke: black");
////		vals.exit().remove();
////    }-*/;
////
////    /**
////     * @param index Highlight element at index
////     * @param enable true to highlight, false to turn off highlight.
////     */
////    public void highlight(int index, boolean enable) {
////        jsHighlight(getSVG().getId(), index - currentExtent.getStartIndex(), enable);
////    }
////
////    private native void jsHighlight(String svgId, int index, boolean enable) /*-{
////		var gClass = this.@csi.client.gwt.widget.ui.surface.OrdinalAxis::axisClass;
////		var hClass = this.@csi.client.gwt.widget.ui.surface.OrdinalAxis::highlightedAxisLabelClass;
////		if (enable) {
////			$wnd.d3.select("#" + svgId).selectAll("." + gClass).select("text")
////					.classed(hClass, function(d, i) {
////						return i == index;
////					});
////			$wnd.d3.select("#" + svgId).selectAll("." + gClass).select("path")
////					.attr("style", function(d, i) {
////						if (i == index)
////							return "stroke:red;";
////						else
////							return "stroke:black";
////					});
////		} else {
////			$wnd.d3.select("#" + svgId).selectAll("." + hClass).classed(hClass,
////					false);
////			$wnd.d3.select("#" + svgId).selectAll("." + gClass).select("path")
////					.attr("style", "stroke:black");
////		}
////    }-*/;
//
//}

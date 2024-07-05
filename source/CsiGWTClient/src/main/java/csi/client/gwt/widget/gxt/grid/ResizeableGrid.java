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
package csi.client.gwt.widget.gxt.grid;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Scroll;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.table.CellHoverEvent;
import csi.client.gwt.widget.boot.Dialog;

import java.util.List;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ResizeableGrid<M> extends Grid<M> implements RequiresResize {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final double CSS_LIMIT = 5000000;

    private Cursor _mouseCursor = Cursor.DEFAULT;
    private boolean _autoResizeColumns = false;
    private boolean _initialResize = false;
    private int _minWidth = 0;

    private List<HTML> topElements = Lists.newArrayList();

    private List<HTML> bottomElements = Lists.newArrayList();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm, GridView<M> view) {
        super(store, cm, view);
        init();
    }

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm) {
        super(store, cm);
        init();
    }

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm, boolean showBorderIn) {
        super(store, cm);
        init();
        borderDisplay(showBorderIn);
    }

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm, GridView<M> view, int minWidthIn) {
        super(store, cm, view);
        _minWidth = minWidthIn;
        init();
    }

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm, int minWidthIn) {
        super(store, cm);
        init();
        _minWidth = minWidthIn;
    }

    public ResizeableGrid(ListStore<M> store, ColumnModel<M> cm, int minWidthIn, boolean showBorderIn) {
        super(store, cm);
        init();
        _minWidth = minWidthIn;
        borderDisplay(showBorderIn);
    }

    public void refreshKeepState() {
        Scroll scroll = getView().getScroller().getScroll();
        getView().refresh(false);
        getView().getScroller().setScrollTop(scroll.getScrollTop());
    }

    public void autoResizeColumns(boolean resizeIn) {

        _autoResizeColumns = resizeIn;
    }

    public void setCursor(Cursor mouseCursorIn) {

        _mouseCursor = mouseCursorIn;
    }

    @Override
    public void onResize() {

        DeferredCommand.add(new Command() {
            public void execute() {

                resizeToFit();
            }
        });
    }

    @Override
    public void setSelectionModel(GridSelectionModel<M> sm) {
        super.setSelectionModel(sm);

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected Cell<?> handleEventForCell(Event eventIn) {
        if (!Cursor.DEFAULT.equals(_mouseCursor)) {

            String myEvent = eventIn.getType();

            // Get the event target.
            EventTarget myEventTarget = eventIn.getEventTarget();
            if (Element.is(myEventTarget)) {

                final Element target = eventIn.getEventTarget().cast();
                forceMouseCursor(getView().findRowIndex(target));
            }
        }

        int type = eventIn.getTypeInt();
        if(type == Event.ONMOUSEMOVE) {
            Element target = Element.as(eventIn.getEventTarget());
            int rowIndex = view.findRowIndex(target);
            if (rowIndex >= 0) {
                int colIndex = view.findCellIndex(target, null);
                if (colIndex != -1) {
                    fireEvent(new CellHoverEvent(rowIndex, colIndex, eventIn));
                }
            }else{
                this.hideToolTip();
            }
        }

        return super.handleEventForCell(eventIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void init() {
        addStyleName("sencha-gxt-grid"); //$NON-NLS-1$
    }

    private void resizeToFit() {

        GridView myView = getView();

        if (null != myView) {

            if (isAttached()) {

                if (!_initialResize) {

                    _initialResize = true;

                    DeferredCommand.add(new Command() {
                        public void execute() {

                            resizeToFit();
                        }
                    });

                }

                int myNewWidth = Math.max(_minWidth, this.getParent().getOffsetWidth());

                setPixelSize(myNewWidth, this.getParent().getOffsetHeight());

                if (_autoResizeColumns) {

                    ColumnModel<M> myModel = getColumnModel();
                    int myCurrentWidth = 0;

                    if (null != myModel) {

                        List<ColumnConfig<M, ?>> myColumns = myModel.getColumns();

                        for (ColumnConfig<M, ?> myColumn : myColumns) {

                            if (myColumn.isResizable() && (!myColumn.isHidden())) {

                                myCurrentWidth += myColumn.getWidth();
                            }
                        }
                        if (0 < myCurrentWidth) {

                            for (ColumnConfig<M, ?> myColumn : myColumns) {

                                if (myColumn.isResizable() && (!myColumn.isHidden())) {

                                    myColumn.setWidth((myColumn.getWidth() * myNewWidth) / myCurrentWidth);
                                }
                            }
                        }
                    }
                }
                myView.refresh(true);
            }
        }
    }

    public void forceHeight(int height) {

        GridView myView = getView();

        if (null != myView) {
            myView.getBody().getStyle().setHeight(height, Unit.PX);
        }


    }

    public void forceTop(double top) {
        GridView myView = getView();

        if (null != myView) {
            XElement bodyNode = myView.getBody();
//
//            Node bodyParent = bodyNode.getParentNode();
//            while(topElements.size() > 0) {
//                //topElements.get(0).setHTML("");
//                bodyParent.removeChild(topElements.get(0).getElement());
//                topElements.remove(0);
//            }
//            
//            if(top > CSS_LIMIT) {
//                int divs = (int) (top/CSS_LIMIT);
//                //bodyParent.insertFirst((Node) DivElement.createObject());
//                while(divs > topElements.size()) {
//                    HTML divElement = new HTML("<div>&nbsp;</div>");
//                    topElements.add(divElement);
//                    divElement.getElement().getStyle().setHeight(CSS_LIMIT, Unit.PX);
//                    divElement.getElement().getStyle().setBorderWidth(0, Unit.PX);
//                    divElement.getElement().getStyle().setMargin(0, Unit.PX);
//                    divElement.getElement().getStyle().setPadding(0, Unit.PX);
//                    bodyParent.insertBefore(divElement.getElement(), bodyNode);
//                }
//                
//                top = top%CSS_LIMIT;
//            }

            bodyNode.getStyle().setMarginTop(top, Unit.PX);
        }
    }

    public void forceBottom(double bottom) {
        GridView myView = getView();

        if (null != myView) {
            XElement bodyNode = myView.getBody();
//
//            Node bodyParent = bodyNode.getParentNode();
//            while(bottomElements.size() > 0) {
//                //bottomElements.get(0).setHTML("");
//                bodyParent.removeChild(bottomElements.get(0).getElement());
//                bottomElements.remove(0);
//            }
//            if(bottom > CSS_LIMIT) {
//                int divs = (int) (bottom/CSS_LIMIT);
//                //bodyParent.insertFirst((Node) DivElement.createObject());
//                while(divs > bottomElements.size()) {
//                    HTML divElement = new HTML("<div>&nbsp;</div>");
//                    bottomElements.add(divElement);
//                    divElement.getElement().getStyle().setHeight(CSS_LIMIT, Unit.PX);
//                    divElement.getElement().getStyle().setBorderWidth(0, Unit.PX);
//                    divElement.getElement().getStyle().setMargin(0, Unit.PX);
//                    divElement.getElement().getStyle().setPadding(0, Unit.PX);
//                    bodyParent.insertAfter(divElement.getElement(), bodyNode);
//                }
//
//                bottom = bottom%CSS_LIMIT;
//            }

            bodyNode.getStyle().setMarginBottom(bottom, Unit.PX);
        }
    }

    public int getRowHeight() {
        GridView myView = getView();

        if (null != myView) {
            Element el = myView.getRow(0);
            if(el != null)
            return el.getOffsetHeight();
        }

        return 0;
    }

    private void forceMouseCursor(int rowIndexIn) {

        GridView<M> myView = getView();
        Element myRow = myView.getRow(rowIndexIn);

        if (null != myRow) {

            if (!Cursor.POINTER.equals(myRow.getStyle().getCursor())) {

                myRow.getStyle().setCursor(Cursor.POINTER);
            }
        }
    }

    private void borderDisplay(boolean showIn) {

        if (showIn) {

            getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
            getElement().getStyle().setBorderColor(Dialog.txtBorderColor);

        } else {

            getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        }
    }

}

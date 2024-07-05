/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.edit_sources.center_panel.shapes;

import java.util.ArrayList;
import java.util.List;

import com.emitrom.lienzo.client.core.event.NodeDragEndEvent;
import com.emitrom.lienzo.client.core.event.NodeDragEndHandler;
import com.emitrom.lienzo.client.core.event.NodeDragMoveEvent;
import com.emitrom.lienzo.client.core.event.NodeDragMoveHandler;
import com.emitrom.lienzo.client.core.event.NodeDragStartEvent;
import com.emitrom.lienzo.client.core.event.NodeDragStartHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseClickEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseClickHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseDoubleClickHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseEnterEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseEnterHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseExitEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseExitHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseOverEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseOverHandler;
import com.emitrom.lienzo.client.core.shape.Circle;
import com.emitrom.lienzo.client.core.shape.Group;
import com.emitrom.lienzo.client.core.shape.Node;
import com.emitrom.lienzo.client.core.shape.Rectangle;
import com.emitrom.lienzo.client.core.shape.Shape;
import com.emitrom.lienzo.client.core.shape.Text;
import com.emitrom.lienzo.client.widget.DragContext;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;

/**
 * Note: Wienzo = Lienzo graphics widget. Just a unique name to namespace-separate the elements that make up the 
 * dataview configuration.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class WienzoComposite extends Group {

    public abstract void launchEditDisplay();
    public abstract void updateInfo();

    private static final double VISIBLE_DIM_TITLE_OFFSET = 10.0;
    private static final double HIDDEN_DIM_TITLE_OFFSET = 5.0;
    private static final double VISIBLE_DIM_OFFSET = 5.0;
    private static final double HIDDEN_DIM_OFFSET = 0.0;
    private static final double FULL_LABEL_BOX_WIDTH = 120;
    private static final double MINI_LABEL_BOX_WIDTH = 20;
    private static final int FULL_MAX_TABLE_NAME_LENGTH = 20;
    private static final int MINI_MAX_TABLE_NAME_LENGTH = 2;

    protected static final double LABEL_BOX_HEIGHT = 30.0;
    protected static final double DIM_ICON = 14.0;

    protected static double LABEL_BOX_WIDTH = FULL_LABEL_BOX_WIDTH;
    protected static int MAX_TABLE_NAME_LENGTH = FULL_MAX_TABLE_NAME_LENGTH;

    protected static double DIM_TITLE_OFFSET = VISIBLE_DIM_TITLE_OFFSET;
    protected static double DIM_OFFSET = VISIBLE_DIM_OFFSET;
    protected static double TABLE_SPACING = 5.0;

    protected static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    protected static boolean displayConnectors = true;

    protected ConfigurationPresenter configurationPresenter;
    protected Rectangle borderRectangle;
    protected String backgroundFill;
    protected String attentionFill;
    protected String attentionStroke;
    protected String infoString = null;
    protected String messageString = null;

    private WienzoComposite parent;

    private ConnectionPoint cpLeft = new ConnectionPoint(PortType.JOIN_LEFT);
    private ConnectionPoint cpRight = new ConnectionPoint(PortType.JOIN_RIGHT);
    private ConnectionPoint cpTop = new ConnectionPoint(PortType.APPEND_TOP);
    private ConnectionPoint cpBottom = new ConnectionPoint(PortType.APPEND_BOTTOM);
    private List<ConnectionPoint> connectors;
    private ConnectionPoint hoveringPoint;

    private List<HandlerRegistration> _handlers = null;

    private DataSetOp dso;
    private CoreGroup coreGroup;
    private boolean dragging;

    private boolean _isOver = false;
    private boolean _isSelected = false;
    private boolean _isActive = false;

    protected Circle connectionCircle;
    protected boolean needsAttention = false;
    protected final WienzoComposite _this;

    protected double _lastX = 0;
    protected double _lastY = 0;

    protected Text title;

    public static void hideConnectors(boolean doHideIn) {

        displayConnectors = !doHideIn;
        DIM_TITLE_OFFSET = doHideIn ? HIDDEN_DIM_TITLE_OFFSET : VISIBLE_DIM_TITLE_OFFSET;
        DIM_OFFSET = doHideIn ? HIDDEN_DIM_OFFSET : VISIBLE_DIM_OFFSET;
    }

    public static void changeDisplayFormat(boolean isFullIn) {

        LABEL_BOX_WIDTH = (isFullIn) ? FULL_LABEL_BOX_WIDTH : MINI_LABEL_BOX_WIDTH;
        MAX_TABLE_NAME_LENGTH = (isFullIn) ? FULL_MAX_TABLE_NAME_LENGTH : MINI_MAX_TABLE_NAME_LENGTH;
    }

    public WienzoComposite(DataSetOp dsoIn, ConfigurationPresenter configurationPresenterIn,
                           String backgroundFillIn, String attentionFillIn, String attentionStrokeIn) {
        super();
        _this = this;
        dso = dsoIn;
        configurationPresenter = configurationPresenterIn;
        backgroundFill = backgroundFillIn;
        attentionFill = attentionFillIn;
        attentionStroke = attentionStrokeIn;
        coreGroup = new CoreGroup(this);
        connectors = Lists.newArrayList(cpLeft, cpRight, cpTop, cpBottom);

        connectionCircle = new Circle(ConnectionPoint.CIRCLE_RADIUS);
        connectionCircle.setFillColor(ColorValues.NO_FILL);
        _isActive = true;
    }

    public void setSelected() {

        _isSelected = true;
        setBackground();
        launchEditDisplay();
    }

    public void setDeselected() {

        _isSelected = false;
        setBackground();
    }

    public void destroy() {

        _isActive = false;
        if (null != _handlers) {

            List<HandlerRegistration> myHandlers = _handlers;

            _handlers = null;

            for (HandlerRegistration myRegistration : myHandlers) {

                myRegistration.removeHandler();
            }
        }
    }

    public Text getTitle() {

        if (null == title) {

            title = new Text("");
        }
        return title;
    }

    public void replaceTitle(String titleIn) {

        getTitle().setText(buildLabel(titleIn));
    }

    protected void setBackground() {

        if (null != borderRectangle) {

            if (_isSelected) {

                borderRectangle.setFillColor(ColorValues.SELECTION_FILL);

            } else if (_isOver && needsAttention) {

                borderRectangle.setFillColor(attentionFill);

            } else {

                borderRectangle.setFillColor(backgroundFill);
            }
        }

        if (null != connectionCircle) {

            if (needsAttention) {

                connectionCircle.setFillColor(attentionStroke);

            } else {

                connectionCircle.setFillColor(ColorValues.NO_FILL);
            }
        }

        if ((null != borderRectangle) || (null != connectionCircle)) {

            draw();
        }
    }

    protected void addHandlers() {

        DeferredCommand.add(new Command() {
            public void execute() {

                DeferredCommand.add(new Command() {
                    public void execute() {

                        buildHandlerList();
                    }
                });
            }
        });
    }

    private void buildHandlerList() {

        _handlers = new ArrayList<HandlerRegistration>();

        _handlers.add(this.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {

                if (_isActive && (null != configurationPresenter)) {

                    configurationPresenter.selectObject(_this);
                    launchEditDisplay();
                }
            }
        }));

        _handlers.add(this.addNodeMouseClickHandler(new NodeMouseClickHandler() {

            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {

                if (_isActive && (null != configurationPresenter)) {

                    configurationPresenter.selectObject(_this);
                    launchEditDisplay();
                }
            }

        }));

        _handlers.add(this.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            @Override
            public void onNodeDragMove (NodeDragMoveEvent eventIn){

                if (_isActive && (null != configurationPresenter)) {

                    configurationPresenter.selectObject(_this);
                    launchEditDisplay();
                }
            }
        }));

        _handlers.add(this.addNodeDragStartHandler(new NodeDragStartHandler() {

            @Override
            public void onNodeDragStart (NodeDragStartEvent event){

                if (_isActive) {

                    onDragStart(true);
                }
            }
        }));

        _handlers.add(this.addNodeDragMoveHandler(new NodeDragMoveHandler() {

            @Override
            public void onNodeDragMove (NodeDragMoveEvent event){

                if (_isActive) {

                    onDragMove(event);
                }
            }
        }));

        _handlers.add(this.addNodeDragEndHandler(new NodeDragEndHandler() {

            @Override
            public void onNodeDragEnd (NodeDragEndEvent eventIn){

                if (_isActive) {

                    onDragEnd(eventIn, true);
                }
            }
        }));

        _handlers.add(borderRectangle.addNodeMouseOverHandler(new NodeMouseOverHandler() {
            @Override
            public void onNodeMouseOver (NodeMouseOverEvent eventIn){

                if (_isActive) {

                    if (!_isOver) {

                        _isOver = true;
                        setBackground();
                        if (null != configurationPresenter) {

                            if (null != messageString) {

                                configurationPresenter.displayToolTip(messageString, attentionStroke); //$NON-NLS-1$

                            } else {

                                configurationPresenter.displayToolTip(infoString); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }));

        _handlers.add(borderRectangle.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {
            @Override
            public void onNodeMouseEnter (NodeMouseEnterEvent eventIn){

                if (_isActive) {

                    if (!_isOver) {

                        _isOver = true;
                        setBackground();

                        if (null != configurationPresenter) {

                            if (null != messageString) {

                                configurationPresenter.displayToolTip(messageString, attentionStroke); //$NON-NLS-1$

                            } else {

                                configurationPresenter.displayToolTip(infoString); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }));

        _handlers.add(borderRectangle.addNodeMouseExitHandler(new NodeMouseExitHandler() {
            @Override
            public void onNodeMouseExit (NodeMouseExitEvent eventIn){

                if (_isActive) {

                    if (_isOver) {

                        _isOver = false;
                        setBackground();
                        if (null != configurationPresenter) {

                            configurationPresenter.clearToolTip();
                        }
                    }
                }
            }
        }));
    }

    public DataSetOp getDso() {
        return dso;
    }

    public ConnectionPoint getCpLeft() {
        return cpLeft;
    }

    public ConnectionPoint getCpRight() {
        return cpRight;
    }

    public ConnectionPoint getCpTop() {
        return cpTop;
    }

    public ConnectionPoint getCpBottom() {
        return cpBottom;
    }

    public ConfigurationPresenter getConfigurationPresenter() {
        return configurationPresenter;
    }

    public WienzoComposite getWienzoParent() {
        return parent;
    }

    public void setWienzoParent(WienzoComposite parent) {
        this.parent = parent;
    }

    public CoreGroup getCoreGroup() {
        return coreGroup;
    }

    public boolean isDragging() {
        return dragging;
    }

    protected void addConnectors(boolean includeCloserIn) {
        cpLeft.setX(0);
        cpLeft.setY(coreGroup.getBoundingHeight() / 2.0 - cpLeft.getHeight() / 2.0 + cpTop.getHeight());

        cpRight.setX(cpLeft.getWidth() + coreGroup.getBoundingWidth());
        cpRight.setY(cpLeft.getY());

        cpTop.setX(cpLeft.getWidth() + coreGroup.getBoundingWidth() / 2.0 - cpTop.getWidth() / 2.0);
        cpTop.setY(0);

        cpBottom.setX(cpTop.getX());
        cpBottom.setY(cpTop.getHeight() + coreGroup.getBoundingHeight());

        coreGroup.setX(cpLeft.getWidth());
        coreGroup.setY(cpTop.getHeight());
        add(coreGroup);
        add(cpLeft);
        add(cpRight);
        add(cpTop);
        add(cpBottom);

        setDraggable(true);
    }

    public double getWidth() {
        return coreGroup.getBoundingWidth() + cpLeft.getWidth() + cpRight.getWidth();
    }

    public double getHeight() {
        return coreGroup.getBoundingHeight() + cpTop.getHeight() + cpBottom.getHeight();
    }

    /**
     * If you override this, call super before executing your own logic.
     * @param primary Set to true if this is the element that is directly being dragged. False if a parent is being
     * dragged.
     */
    public void onDragStart(boolean primary) {
        
        dragging = true;
        
        _lastX = this.getX();
        _lastY = this.getY();
        
        if (primary && getWienzoParent() == null) {
            // Turn off connectors.
            hideConnectors();
        }
    }

    public void onDragMove(NodeDragMoveEvent event) {
        // If parent is null, then this is an independent entity and we need to track the connectors.
        if (getWienzoParent() == null) {
            ConnectionPoint cp = findConnectablePointAt(event);
            if (cp != null && hoveringPoint != cp) {
                cp.highlight(true);
                if (hoveringPoint != null) {
                    hoveringPoint.highlight(false);
                }
                hoveringPoint = cp;
            } else if (cp == null && hoveringPoint != null) {
                hoveringPoint.highlight(false);
                hoveringPoint = null;
            }
        }
    }

    private ConnectionPoint findConnectablePointAt(NodeDragMoveEvent event) {
        DragContext dc = event.getDragContext();
        Shape<?> shape = getScene().findShapeAtPoint(dc.getEventX(), dc.getEventY());
        if (shape != null && shape.getParent() != null) {
            Node<?> node = shape.getParent();
            if (node instanceof ConnectionPoint && ((ConnectionPoint) node).isConnected() == false) {
                return ((ConnectionPoint) node);
            }
        }
        return null;
    }

    /**
     * NOTE: If you override this, call super after executing your own logic.
     * @param primary Set to true if this is the element that is directly being dragged. False if a parent is being
     * dragged.
     */
    public void onDragEnd(NodeDragEndEvent eventIn, boolean primary) {

        dragging = false;

        if (primary) {

            showConnectors();

            if (null != getWienzoParent()) {

                DragContext myContext = eventIn.getDragContext();

                if ((Math.abs(myContext.getDx()) > 40) || (Math.abs(myContext.getDy()) > 40)) {

                    WienzoComposite myParent = getWienzoParent();

                    configurationPresenter.destroyList();
                    myParent.handleDetach(getDso());
                    return;

                } else {

                    this.setX(_lastX);
                    this.setY(_lastY);
                    draw();
                }

            }else if (hoveringPoint != null) {

                hoveringPoint.highlight(false);
                getConfigurationPresenter().attachSibbling(hoveringPoint.getComposite().getDso(),
                        getDso(), hoveringPoint.getPortType());
                hoveringPoint = null;
            }
        }
    }

    protected boolean fullRedrawRequired(NodeDragEndEvent eventIn, boolean primary) {

        DragContext myContext = eventIn.getDragContext();

        return primary && (((null != getWienzoParent())
                            && ((Math.abs(myContext.getDx()) > 40) || (Math.abs(myContext.getDy()) > 40)))
                        || (hoveringPoint != null));
    }

    protected String buildLabel(String stringIn) {

        int endIndex = Math.min(stringIn.length() , MAX_TABLE_NAME_LENGTH);
        return stringIn.substring(0, endIndex);
    }

    protected String buildDsoLabel(DataSetOp dsoIn) {

        String myString = (null != dsoIn) ? getDso().getName() : null;
        return buildLabel((null != myString) ? myString : "");
    }

    protected String buildSourceLabel(DataSetOp dsoIn) {

        SqlTableDef myTable = (null != dsoIn) ? dsoIn.getTableDef() : null;
        DataSourceDef mySource = (null != myTable) ? myTable.getSource() : null;
        String myString = (null != mySource) ? mySource.getName(): null;

        return buildLabel((null != myString) ? myString : "");
    }
/*
    public void onDragEnd(NodeDragEndEvent eventIn, boolean primary) {
        dragging = false;
        if (primary && getWienzoParent() == null) {
            showConnectors();
            if (hoveringPoint != null) {
                hoveringPoint.highlight(false);
                getConfigurationPresenter().attachSibbling(hoveringPoint.getComposite().getDso(),
                        getDso(), hoveringPoint.getPortType());
                hoveringPoint = null;
            }
        }
    }
*/
    private void hideConnectors() {
        modifyConnectorVisibility(false);
    }

    private void showConnectors() {
        modifyConnectorVisibility(true);
    }

    private void modifyConnectorVisibility(boolean visible) {
        if (!visible) {
            for (ConnectionPoint cp : connectors) {
                remove(cp);
            }
        } else {
            for (ConnectionPoint cp : connectors) {
                add(cp);
            }
            draw();
        }
    }

    public void draw(){
        getLayer().draw();
    }
    
    /**
     * @param dso Given DSO wants to be detached from the relationship.
     */
    public void handleDetach(DataSetOp dso) {
        // noop
    }
}

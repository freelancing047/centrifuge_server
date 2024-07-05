package csi.client.gwt.viz.graph.surface.tooltip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Row;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.fx.client.DragMoveEvent;
import com.sencha.gxt.fx.client.DragMoveEvent.DragMoveHandler;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.Resizable;
import com.sencha.gxt.widget.core.client.Resizable.Dir;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.MoveEvent;
import com.sencha.gxt.widget.core.client.event.MoveEvent.MoveHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.graph.surface.tooltip.ToolTip.ToolTipView;
import csi.config.advanced.graph.TooltipAdvConfig;

public class ToolTipViewImpl implements ToolTipView {

    private static final String LABEL_SEPARATOR = ": ";
    private static final List<String> exemptList = new ArrayList<String>(Arrays.asList("csi.createIf","csi.postProcess","csi.initiallyHiddenIf"));
    private final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private final class CloseClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            toolTip.getSurface().getToolTipManager().removeToolTip(toolTip);
            toolTip.getSurface().getView().drawToolTipLines();
        }
    }

    interface MyUiBinder extends UiBinder<SimpleContainer, ToolTipViewImpl> {
    }

    protected static final int HEADING_HEIGHT = 16;

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    Icon closeIcon;
    @UiField
    FluidRow headingFluidRow;
    @UiField
    FluidRow bodyFluidRow;
    @UiField
    FluidContainer bodyFluidContainer;
    @UiField
    FluidContainer tooltipFluidContainer;
    @UiField
    Icon moveIcon;
    private ToolTip toolTip;
    @UiField
    Label toolTipHeadingLabel;
    @UiField
    SimpleContainer mySimpleContainer;
    @UiField
    Column headingColumn;

    private Draggable draggable;

    private boolean mouseOver;

    @Override
    public void setDragContainer(Widget w) {
        draggable.setContainer(w);
    }
    public ToolTipViewImpl(final ToolTip toolTip) {

        this.toolTip = toolTip;
        uiBinder.createAndBindUi(this);
        mySimpleContainer.addResizeHandler(new ResizeToolTipHandler(toolTip));

        Resizable resizable = new Resizable(mySimpleContainer, Dir.SE);
        TooltipAdvConfig tooltipConfig = WebMain.getClientStartupInfo().getGraphAdvConfig().getTooltips();
        resizable.setMinHeight(tooltipConfig.getMinHeight());
        resizable.setMinWidth(tooltipConfig.getMinWidth());
        resizable.setMaxHeight(tooltipConfig.getMaxHeight());
        resizable.setMaxWidth(tooltipConfig.getMaxWidth());

        draggable = new Draggable(mySimpleContainer, moveIcon);
        draggable.setUseProxy(false);
        draggable.addDragMoveHandler(new DragMoveHandler() {

            @Override
            public void onDragMove(DragMoveEvent event) {
                onHoverTooltip(null);
                //update tabdrawer to be ontop of all tooltips.
                toolTip.getSurface().getGraph().getChrome().getControlsLayer().getElement().getStyle().setZIndex(mySimpleContainer.getElement().getZIndex() + 1);
            }
        });

        //No moving of stationary tooltips, can still close and resize
        if(toolTip.isStationary()){
            //Can't null this due to other logic, just disable
            draggable.setEnabled(false);
            //Making this invisible screws up title positioning, just make practically invisible
            moveIcon.getElement().getStyle().setCursor(Cursor.DEFAULT);
            moveIcon.getElement().getStyle().setOpacity(.001);
        }
        
        closeIcon.addDomHandler(new CloseClickHandler(), ClickEvent.getType());
        
        mySimpleContainer.getElement().getStyle().setOverflow(Overflow.HIDDEN);

        mySimpleContainer.addMoveHandler(new MoveHandler() {

            @Override
            public void onMove(MoveEvent event) {
                onExitTooltip(null);
                onHoverTooltip(null);
            }
        });

        mySimpleContainer.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                setMouseOver(false);
                onHoverTooltip(null);
            }
        }, MouseOutEvent.getType());
        mySimpleContainer.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                setMouseOver(true);
                onHoverTooltip(event);

            }
        }, MouseOverEvent.getType());
        // This forces the long or wide tooltips to have scroll bars initially.
        Scheduler.get().scheduleFinally(new ScheduledCommand() {

            @Override
            public void execute() {
                if (mySimpleContainer.getElement().getWidth(false) > 300) {
                    mySimpleContainer.setWidth(300);
                }
                if (mySimpleContainer.getElement().getHeight(false) > 200) {
                    mySimpleContainer.setHeight(200);
                }
                updateBodyHeight();

            }
        });
    }

    public void addItemWithButtons(ToolTipItem item, List<Button> buttons) {

        try {
            if (item != null) {
                String label = item.getLabel();
                String value = item.getValue(toolTip.getModel());
                if (value != null) {

                    addRowWithButtons(label, value, buttons);
                }
            }
        } catch (NullPointerException e) {
            // do nothing
        }

    }

    @Override
    public void addItem(ToolTipItem item) {
        try {
            if (item != null ) {
                String label = item.getLabel();
                
                String value = item.getValue(toolTip.getModel());
                if (value != null && !exemptList.contains(label)) {
                    addRow(label, value);
                }
            }
        } catch (NullPointerException e) {
            // do nothing
        }
    }

    private void addRowWithButtons(String label, String value, List<Button> buttons) {

        boolean containsHtml = label.equals(i18n.tooltipLabel_comments());
        FluidRow idRow0 = new FluidRow();
        InlineLabel label2 = new InlineLabel(label + LABEL_SEPARATOR);
        label2.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label2.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        idRow0.add(label2);

        for (Button button : buttons) {
            idRow0.add(button);
        }

        try {

            addValuesToRow(value, idRow0, false, containsHtml);

        } catch (Exception ignore) {

            try {

                addValuesToRow(value, idRow0, containsHtml);

            } catch (Exception ignoreAlso) {}
        }
    }

    @Override
    public void addRow(String label, String value) {
        // FIXME:
        if(label.equals(i18n.tooltipLabel_contains()) && (value == null || value.trim().isEmpty())){
            return;
        }
        boolean containsHtml = label.equals(i18n.tooltipLabel_comments());
        FluidRow idRow0 = new FluidRow();
        InlineLabel label2 = new InlineLabel(label + LABEL_SEPARATOR);
        label2.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        //label2.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        idRow0.add(label2);

        try {

            addValuesToRow(value, idRow0, false, containsHtml);

        } catch (Exception ignore) {

            try {

                addValuesToRow(value, idRow0, containsHtml);

            } catch (Exception ignoreAlso) {}
        }
    }

    private void addValuesToRow(String value, FluidRow fluidRow, boolean containsHtml) {

        FluidContainer valuesContainer = new FluidContainer();
        ArrayList<String> values = Lists.newArrayList(value.split("\n"));
        for (String s : values) {

            if (!containsHtml) {

                s = s.replaceAll("&", "&amp;");
                s = s.replaceAll("<", "&lt;");
                s = s.replaceAll(">", "&gt;");
            }
            newValueRow(false, valuesContainer, s);
        }
        fluidRow.add(valuesContainer);
        bodyFluidContainer.add(fluidRow);
    }

    private void addValuesToRow(String value, FluidRow fluidRow, boolean wrap, boolean containsHtml) {
        FluidContainer valuesContainer = new FluidContainer();
        ArrayList<String> values = Lists.newArrayList(value.split("\n"));
        for (String s : values) {
            s=s.replaceAll("&lt;EMPTY&gt;", "<EMPTY>");
            if (s.contains(";") && !containsHtml) {
                ArrayList<String> subvalues = Lists.newArrayList(s.split(";"));
                for (String subvalue : subvalues) {
                    if (subvalue.contains("=")) {
                        subvalue= subvalue.replaceAll("<EMPTY>", "&lt;EMPTY&gt;");
                        ArrayList<String> keyvalue = Lists.newArrayList(subvalue.split("="));
                        FluidRow idRow0 = new FluidRow();
                        InlineLabel label2 = new InlineLabel(keyvalue.get(0) + LABEL_SEPARATOR);
                        label2.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                        label2.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
                        idRow0.add(label2);

                        FluidContainer valuesContainer2 = new FluidContainer();
                        if (keyvalue.get(1).startsWith("[") && keyvalue.get(1).endsWith("]")) {
                            String listString = keyvalue.get(1).substring(1, keyvalue.get(1).length() - 1);
                            Map<String, Integer> valueHash = new HashMap<>();
                            for (String listStringValue : listString.split(",")) {
                                if (valueHash.containsKey(listStringValue.trim())) {
                                    valueHash.put(listStringValue.trim(), valueHash.get(listStringValue.trim()) + 1);
                                } else {
                                    valueHash.put(listStringValue.trim(), 1);
                                }
                            }
                            List<String> list = new ArrayList<>(valueHash.keySet());
                            Collections.sort(list);
                            for (String listStringValue : list) {
                                if (valueHash.get(listStringValue) > 1) {
                                    newValueRow(wrap, valuesContainer2, listStringValue + "(" + valueHash.get(listStringValue) + ")");
                                } else {
                                    newValueRow(wrap, valuesContainer2, listStringValue);
                                }
                            }
                        } else {
                            newValueRow(wrap, valuesContainer2, keyvalue.get(1));
                        }
                        idRow0.add(valuesContainer2);
                        valuesContainer.add(idRow0);
                    } else {
                        newValueRow(wrap, valuesContainer, subvalue);
                    }
                }
            } else {
                s= s.replaceAll("<EMPTY>", "&lt;EMPTY&gt;");
                newValueRow(wrap, valuesContainer, s);
            }
        }
        fluidRow.add(valuesContainer);
        bodyFluidContainer.add(fluidRow);
    }

    private void newValueRow(boolean wrap, FluidContainer valuesContainer, String s) {
        Row row = new Row();
        InlineLabel ilabel = new InlineLabel();
        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
        safeHtmlBuilder.appendEscaped(s);
        //FIXME: not safe
        ilabel.getElement().setInnerHTML(s);
        row.add(ilabel);
        valuesContainer.add(row);
        if (!wrap) {
            ilabel.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        }
    }

    @Override
    public Widget asWidget() {
        return mySimpleContainer;
    }

    @Override
    public Widget getMoveHandle() {
        return moveIcon;
    }

    public void onExitTooltip(MouseOutEvent e) {
        toolTip.getSurface().getView().clearForeground();
    }

    public void onHoverTooltip(MouseOverEvent e) {
        toolTip.getSurface().getView().drawToolTipLines();
    }

    @Override
    public void setHeading(String heading) {
        if (heading == null) {
            // TODO: warn?
        }
        toolTipHeadingLabel.setText(heading);
    }

    @Override
    public void updateBodyHeight() {
        int totalWidgetHeight = mySimpleContainer.getElement().getHeight(true);
        int newBodyHeight = totalWidgetHeight - ToolTipViewImpl.HEADING_HEIGHT;
        Style bodyStyle = bodyFluidContainer.getElement().getStyle();
        bodyStyle.setHeight(newBodyHeight, Unit.PX);
    }

    @Override
    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    @Override
    public void addMoreLink(boolean more) {
        FluidRow moreLink = new FluidRow();
        Anchor link = new Anchor();
        if(more) {
            link.setText(i18n.tooltipMoreDetails());
        } else {
            link.setText(i18n.pin());
        }
        moreLink.addStyleName("tooltip-more-link");
        moreLink.add(link);
        bodyFluidContainer.add(moreLink);
        //if(toolTip.isMoreDetails())
        moreLink.addDomHandler(toolTip.getMoreDetailsHandler(), ClickEvent.getType());
    }
}

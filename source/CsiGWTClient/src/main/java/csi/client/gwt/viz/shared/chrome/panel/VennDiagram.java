package csi.client.gwt.viz.shared.chrome.panel;

import com.github.gwtbootstrap.client.ui.Row;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.DrawingPanel;
import csi.client.gwt.widget.drawing.Layer;
import csi.client.gwt.widget.drawing.Rectangle;
import csi.client.gwt.widget.drawing.Renderable;
import csi.server.common.model.broadcast.BroadcastSet;

import java.util.Set;

class VennDiagram extends DrawingPanel {
    private static final CssColor SELECT_COLOR = CssColor.make("rgb(251,180,80)");
    private static final CssColor SELECT_HOVER_COLOR = CssColor.make("rgb(225, 133, 5)");
    private static final CssColor DESELECT_COLOR = CssColor.make("white");
    private static final CssColor HOVER_COLOR = CssColor.make("rgb(217,217,217)");
    private static final CssColor TEXT_BORDER_COLOR = CssColor.make("black");
    private static final CssColor TEXT_BG_COLOR = CssColor.make("white");
    private static final String TEXT_FONT = "18px Arial";
    private static final CssColor TEXT_COLOR = CssColor.make("black");
    //    private static final CssColor OLD_CIRLCE_BEHIND_NEW_CIRCLE_COLOR = CssColor.make("rgba(126, 152, 169,.4)");
    private static final CssColor OLD_CIRLCE_BEHIND_NEW_CIRCLE_COLOR = CssColor.make("rgb(33,104,147)");
    private static final CssColor OLD_CIRCLE_COLOR = CssColor.make("rgb(33,104,147)");
    private static final CssColor EVERYTHING_ELSE_STROKE_COLOR = CssColor.make("rgb(33,104,147)");
    private static final CssColor NEW_CIRCLE_COLOR = CssColor.make("rgb(33,104,147)");
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static final int TEXT_AREA_HEIGHT = 28;
    private final Row container;
    private Renderable circle1;
    private Layer mainLayer;
    private Rectangle allRect;
    private BaseRenderable circle2;
    private String displayText = i18n.vennDiagram_chooseSet();
    private BaseRenderable overlapArea;

    VennDiagram(Row container) {
        this.container = container;
        setWidth("100%");
        setHeight("100%");
        makeMainLayer();


        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                render();
                return false;
            }
        }, 25);
    }

    private BaseRenderable makeTextArea() {
        return new BaseRenderable() {
            @Override
            public void render(Context2d context2d) {
                if (!Strings.isNullOrEmpty(displayText)) {
                    int width = mainLayer.getWidth();
                    context2d.setStrokeStyle(TEXT_BORDER_COLOR);
                    context2d.setFillStyle(TEXT_BG_COLOR);
                    context2d.beginPath();
                    context2d.rect(width / 4, 0, width / 2, 24);
                    context2d.closePath();
                    context2d.fill();
//                    context2d.stroke();

                    context2d.setFillStyle(TEXT_COLOR);
                    context2d.setFont(TEXT_FONT);
                    double textW = context2d.measureText(displayText).getWidth();
                    context2d.fillText(displayText, width / 4 + (width / 2 - textW) / 2, 20, width / 2);
                }

            }

            @Override
            public boolean hitTest(double x, double y) {
                return false;
            }

            @Override
            public void bind(Layer layer) {

            }

            @Override
            public boolean isDirty() {
                return false;
            }
        };
    }

    static native void setLineDash(Context2d ctx, String dash) /*-{
        ctx.setLineDash([6, 8]);
    }-*/;
    static native void setLineNoDash(Context2d ctx, String dash) /*-{
        ctx.setLineDash([]);
    }-*/;

    private BaseRenderable makeOverlapArea() {
        return new BaseRenderable() {
            private Layer layer;
            private boolean fill;
            Canvas scratchPad = Canvas.createIfSupported();

            @Override
            public void render(Context2d context2d) {
                {
                    Context2d ctx = scratchPad.getContext2d();
                    ctx.save();

                    scratchPad.setCoordinateSpaceHeight(layer.getHeight());
                    scratchPad.setCoordinateSpaceWidth(layer.getWidth());
                    ctx.clearRect(0, 0, layer.getWidth(), layer.getHeight());

                    ctx.setGlobalCompositeOperation(Context2d.Composite.SOURCE_OVER);
                    {
                        int width = layer.getWidth();
                        int height = layer.getHeight();

             /*           double r = ((height-TEXT_AREA_HEIGHT) / 2.6);
                        double x = width / 2 - .625 * r;
                        double y = height / 2.0;
*/
                        {
                            double r = (getCircleRadius(height));
                            double x = width / 2 - .625 * r;
                            double y = getCircleCenterY(r);
                            if (fill) {
                                if(clicked.contains(overlapArea)){
                                    ctx.setFillStyle(SELECT_HOVER_COLOR);
                                }
                                else {
                                    ctx.setFillStyle(HOVER_COLOR);
                                }
                            } else if (clicked.contains(overlapArea)) {
                                ctx.setFillStyle(SELECT_COLOR);
                            } else {
                                ctx.setFillStyle(DESELECT_COLOR);
                            }
                            ctx.setStrokeStyle(OLD_CIRLCE_BEHIND_NEW_CIRCLE_COLOR);
//                                        ctx.setStrokeStyle(CssColor.make("#7e98a9"));
//                                        ctx.setStrokeStyle("");

                            ctx.beginPath();
                            setLineDash(ctx, "[5, 5]");
                            ctx.arc(x, y, r, -Math.PI / 2.0, Math.PI / 2.0);
                            ctx.closePath();
                            ctx.setLineWidth(6);
                            ctx.fill();
                            ctx.stroke();
                        }
                        {
                            double r = (getCircleRadius(height));
                            double x = width / 2 + .625 * r;
                            double y = getCircleCenterY(r);
                            if (fill) {
                                if(clicked.contains(overlapArea)){
                                    ctx.setFillStyle(SELECT_HOVER_COLOR);
                                }
                                else {
                                    ctx.setFillStyle(HOVER_COLOR);
                                }
                            } else if (clicked.contains(overlapArea)) {
                                ctx.setFillStyle(SELECT_COLOR);
                            } else {
                                ctx.setFillStyle(DESELECT_COLOR);
                            }
                            ctx.setStrokeStyle(OLD_CIRLCE_BEHIND_NEW_CIRCLE_COLOR);
//                                        ctx.setStrokeStyle(CssColor.make("#7e98a9"));
//                                        ctx.setStrokeStyle("");

                            ctx.moveTo(x, y+r);
                            ctx.beginPath();
                            setLineNoDash(ctx, "[5, 5]");
                            ctx.arc(x, y, r, 0, Math.PI*2);
                            ctx.closePath();
                            ctx.setLineWidth(6);
                            ctx.stroke();

                        }
//                        setLineDash("123456789", "[1]");
                    }
                    ctx.setGlobalCompositeOperation(Context2d.Composite.DESTINATION_IN);
                    {
                        int width = layer.getWidth();
                        int height = layer.getHeight();

     /*                   double r = ((height-TEXT_AREA_HEIGHT) / 2.6);
                        double x = width / 2 + .625 * r;
                        double y = height / 2.0;*/
                        double r = (getCircleRadius(height));
                        double x = width / 2 + .625 * r;
                        double y = getCircleCenterY(r);
                        ctx.setFillStyle(CssColor.make("black"));//NOTE:Color shouldn't matter...
                        ctx.beginPath();
                        ctx.arc(x, y, r, 0, 360);
                        ctx.closePath();
                        ctx.fill();
/*                                        x = width / 2 - .625 * r;
                                        ctx.beginPath();
                                        ctx.arc(x, y, r, 0, 360);
                                        ctx.closePath();
                                        ctx.fill();*/
                    }
                    ctx.restore();
                }
                context2d.drawImage(scratchPad.getCanvasElement(), 0, 0, layer.getWidth(), layer.getHeight(),
                        0, 0, layer.getWidth(), layer.getHeight());
            }

            @Override
            public boolean hitTest(double x, double y) {
                return circle1.hitTest(x, y) && circle2.hitTest(x, y);
            }

            @Override
            public void bind(Layer layer) {

                this.layer = layer;

                addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        fill = false;
                        layer.getDrawingPanel().render();
                    }
                });
                addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        fill = true;
                        displayText = i18n.vennDiagram_circle_inCommon();
                        layer.getDrawingPanel().render();
                    }
                });
                addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (clicked.contains(overlapArea)) {
                            clicked.remove(overlapArea);
                        }
                        else {
                            clicked.add(overlapArea);
                        }
                        layer.getDrawingPanel().render();
                    }
                });
            }

            @Override
            public boolean isDirty() {
                return false;
            }
        };
    }

    public double getCircleCenterY(double r) {
        return TEXT_AREA_HEIGHT + 1.3 * r;
    }

    private BaseRenderable makeCircle2() {
        return new BaseRenderable() {
            private Layer layer;
            private boolean fill;

            @Override
            public void render(Context2d context2d) {
                int width = layer.getWidth();
                int height = layer.getHeight();

                double r = (getCircleRadius(height));
                double x = width / 2 + .625 * r;
                double y = getCircleCenterY(r);
                context2d.setStrokeStyle(NEW_CIRCLE_COLOR);
                if (fill) {
                    if(clicked.contains(circle2)){
                        context2d.setFillStyle(SELECT_HOVER_COLOR);
                    }
                    else {
                        context2d.setFillStyle(HOVER_COLOR);
                    }
                } else if (clicked.contains(circle2)) {
                    context2d.setFillStyle(SELECT_COLOR);
//                                    context2d.setFillStyle(CssColor.make("DarkGray"));
                } else {
                    context2d.setFillStyle(DESELECT_COLOR);
                }
                context2d.beginPath();
                context2d.arc(x, y, r, 0, Math.PI*2);
                context2d.closePath();
                context2d.setLineWidth(6);
                context2d.fill();
                context2d.stroke();
            }

            @Override
            public boolean hitTest(double x, double y) {
                int width = layer.getWidth();
                int height = layer.getHeight();

                double r = (getCircleRadius(height));
                double circ_x = width / 2 + .625 * r;
                double circ_y = height / 2.0;
                return Math.sqrt(Math.pow(circ_x - x, 2) + Math.pow(circ_y - y, 2)) < r;
            }

            @Override
            public void bind(Layer layer) {

                this.layer = layer;
                addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        fill = true;
                        displayText = i18n.vennDiagram_circle_new();
                        layer.getDrawingPanel().render();
                    }
                });
                addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        fill = false;

                        layer.getDrawingPanel().render();
                    }
                });
                addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (clicked.contains(circle2)) {
                            clicked.remove(circle2);
                        }
                        else {
                            clicked.add(circle2);
                        }
                        layer.getDrawingPanel().render();
                    }
                });
            }

            @Override
            public boolean isDirty() {
                return false;
            }
        };
    }

    private void makeMainLayer() {
        mainLayer = new Layer();
        addLayer(mainLayer);
        makeAllRect();
        mainLayer.addItem(allRect);
        circle1 = makeCircle1();
        mainLayer.addItem(circle1);
        circle2 = makeCircle2();
        mainLayer.addItem(circle2);
        overlapArea = makeOverlapArea();
        mainLayer.addItem(overlapArea);
        BaseRenderable textBox = makeTextArea();
        mainLayer.addItem(textBox);
        clicked.add(circle2);
        clicked.add(overlapArea);
    }

    private BaseRenderable makeCircle1() {
        return new BaseRenderable() {


            private Layer layer;
            private boolean fill;

            @Override
            public void render(Context2d context2d) {
                int width = layer.getWidth();
                int height = layer.getHeight();

/*                double r = ((height-TEXT_AREA_HEIGHT) / 2.6);
                double x = width / 2 - .625 * r;
                double y = height / 2.0;*/
                double r = getCircleRadius(height);
                double x = width / 2 - .625 * r;
                double y = getCircleCenterY(r);
                context2d.setStrokeStyle(OLD_CIRCLE_COLOR);
                if (fill) {
                    if(clicked.contains(circle1)){
                        context2d.setFillStyle(SELECT_HOVER_COLOR);
                    }
                    else {
                        context2d.setFillStyle(HOVER_COLOR);
                    }
                } else if (clicked.contains(circle1)) {
                    context2d.setFillStyle(SELECT_COLOR);
                } else {
                    context2d.setFillStyle(DESELECT_COLOR);
                }
                context2d.beginPath();
                context2d.arc(x, y, r, 0, 360);
                context2d.closePath();
                context2d.setLineWidth(6);
                context2d.fill();
                context2d.stroke();
            }

            @Override
            public boolean hitTest(double x, double y) {
                int width = layer.getWidth();
                int height = layer.getHeight();

                double r = (getCircleRadius(height));
                double circ_x = width / 2 - .625 * r;
                double circ_y = height / 2.0;
                return Math.sqrt(Math.pow(circ_x - x, 2) + Math.pow(circ_y - y, 2)) < r;
            }

            @Override
            public void bind(Layer layer) {

                this.layer = layer;
                {
                    addMouseOverHandler(new MouseOverHandler() {
                        @Override
                        public void onMouseOver(MouseOverEvent event) {
                            fill = true;
                            displayText = i18n.vennDiagram_circle_existing();
                            layer.getDrawingPanel().render();

                        }
                    });
                    addMouseOutHandler(new MouseOutHandler() {
                        @Override
                        public void onMouseOut(MouseOutEvent event) {
                            fill = false;
                            layer.getDrawingPanel().render();
                        }
                    });
                    addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (clicked.contains(circle1)) {
                                clicked.remove(circle1);
                            }
                            else {
                                clicked.add(circle1);
                            }

                            layer.getDrawingPanel().render();
                        }
                    });
                }
            }

            @Override
            public boolean isDirty() {
                return false;
            }
        };
    }

    public double getCircleRadius(int height) {
        return (height-TEXT_AREA_HEIGHT-6) / 2.6;
    }

    private void makeAllRect() {
        //                                    setFillStyle(CssColor.make("white"));
        allRect = new Rectangle(3, TEXT_AREA_HEIGHT, 1, 1) {
            boolean fill;

            @Override
            public void render(Context2d context2d) {
                setW(getLayer().getWidth() - 6);
                setH(getLayer().getHeight() - 36);
                if (fill) {
                    if(clicked.contains(allRect)){
                        setFillStyle(SELECT_HOVER_COLOR);
                    }
                    else {
                        setFillStyle(HOVER_COLOR);
                    }
                } else if (clicked.contains(allRect)) {
                    setFillStyle(SELECT_COLOR);
                } else {
                    setFillStyle(DESELECT_COLOR);
                }
//                                    setFillStyle(CssColor.make("white"));

                setStrokeStyle(EVERYTHING_ELSE_STROKE_COLOR);
                context2d.setLineWidth(6);
                super.render(context2d);
            }

            @Override
            public void bind(Layer layer) {
                super.bind(layer);
                addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent event) {
                        fill = true;
                        displayText = i18n.vennDiagram_circle_everythingElse();
                        layer.getDrawingPanel().render();

                    }
                });
                addMouseOutHandler(new MouseOutHandler() {
                    @Override
                    public void onMouseOut(MouseOutEvent event) {
                        fill = false;
                        displayText = "";
                        layer.getDrawingPanel().render();
                    }
                });
                addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (clicked.contains(allRect)) {
                            clicked.remove(allRect);
                        }
                        else {
                            clicked.add(allRect);
                        }
                        layer.getDrawingPanel().render();
                    }
                });
            }

        };

    }

    public Set<Object> clicked = Sets.newHashSet();

    @Override
    public void render() {
        setHeight(container.getOffsetHeight() + "px");
        setWidth(container.getOffsetWidth() + "px");
        super.render();
    }

    public BroadcastSet toBroadCastSet() {
        if (clicked.size() == 4) {
            return BroadcastSet.TRUE;
        }
        if (clicked.isEmpty()) {
            return BroadcastSet.FALSE;
        }
        if (clicked.contains(allRect)) {
            if (clicked.contains(circle1)) {//all and c1

                if (clicked.contains(circle2)) {
                    return BroadcastSet.NOT_BOTH; //all, c1, c2, not overlap
                } else if (clicked.contains(overlapArea)) {
                    return BroadcastSet.IF_B_THEN_A; //all, c1, not c2, and overlap
                } else {
                    return BroadcastSet.NOT_B;//all, c1, not c2, not overlap
                }
            } else if (clicked.contains(circle2)) { //all and not c1 and c2
                if (clicked.contains(overlapArea)) {
                    return BroadcastSet.IF_A_THEN_B;

                } else {
                    return BroadcastSet.NOT_A;

                }
            } else if (clicked.contains(overlapArea)) {//all and not c1, not c2 and overlap
                return BroadcastSet.A_IF_AND_ONLY_IF_B;

            } else {
                return BroadcastSet.NEITHER_A_NOR_B;

            }

        } else {
            if (clicked.contains(circle1)) {//not all and c1

                if (clicked.contains(circle2)) {//and c2
                    if (clicked.contains(overlapArea)) {//and overlap
                        return BroadcastSet.A_OR_B;
                    } else {
                        return BroadcastSet.EITHER_A_OR_B_BUT_NOT_BOTH;//not overlap
                    }
                } else {//and not c2
                    if (clicked.contains(overlapArea)) {//and overlap
                        return BroadcastSet.A;
                    } else {
                        return BroadcastSet.A_AND_NOT_B;//and not overlap
                    }
                }
            } else { //not all and not c1
                if (clicked.contains(circle2)) { // and c2
                    if (clicked.contains(overlapArea)) { // and overlap
                        return BroadcastSet.B;

                    } else {
                        return BroadcastSet.B_AND_NOT_A;

                    }
                } else {//not all and not c1, not c2
                    if (clicked.contains(overlapArea)) {//and overlap
                        return BroadcastSet.AND;
                    }
                }
            }
        }
        return BroadcastSet.FALSE;
    }
}

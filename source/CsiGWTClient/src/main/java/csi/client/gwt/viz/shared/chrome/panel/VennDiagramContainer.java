package csi.client.gwt.viz.shared.chrome.panel;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RequiresResize;

import csi.client.gwt.dataview.broadcast.BroadcastManager;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.server.common.model.broadcast.BroadcastSet;

public class VennDiagramContainer extends FluidContainer implements RequiresResize{
    
    private VennDiagram canvas;
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();


    public VennDiagramContainer(BroadcastManager broadcastManager, Visualization senderViz, Visualization viz) {
        this.setHeight("50%");
        this.setWidth("50%");
        this.getElement().getStyle().setProperty("margin", "auto");
        this.getElement().getStyle().setProperty("maxHeight", "250px");
        this.getElement().getStyle().setProperty("maxWidth", "300px");
        this.getElement().getStyle().setProperty("minHeight", "200px");
        this.getElement().getStyle().setProperty("minWidth", "200px");
        this.getElement().getStyle().setProperty("borderRadius", "4px");
        this.getElement().getStyle().setProperty("boxShadow", "0 0px 0px 10px rgba(0, 0, 0, 0.2)");
        this.getElement().getStyle().setBackgroundColor("rgba(255,255,255,.9");
        this.getElement().getStyle().setPadding(8, Unit.PX);

        {
            Row rowOne = new FluidRow();
            rowOne.getElement().getStyle().setProperty("height", "calc(80% - 4px)");

                Column col = new Column(12);
//                col.getElement().getStyle().setBackgroundColor("blue");

            canvas = new VennDiagram(rowOne);

                    col.add(canvas);


                rowOne.add(col);


            this.add(rowOne);
            Row spacer = new FluidRow();
            spacer.setHeight("8px");
            this.add(spacer);
            Row rowTwo = new FluidRow();
            rowTwo.getElement().getStyle().setProperty("height", "calc(20% - 4px)");
            {
                Column w = new Column(4);
                w.setHeight("100%");
                w.getElement().getStyle().setBackgroundColor("blue");
                {
                    Button button = new Button(i18n.vennDiagram_button_filter());
                    button.setHeight("100%");
                    button.setSize(ButtonSize.LARGE);
                    button.getElement().getStyle().setDisplay(Style.Display.FLEX);
                    button.getElement().getStyle().setProperty("alignItems", "center");
                    button.getElement().getStyle().setProperty("justifyContent", "center");
                    button.setBlock(true);
                    button.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            BroadcastSet set = canvas.toBroadCastSet();
                            broadcastManager.broadcastFilter(set, senderViz, viz);
                            broadcastManager.endSendTo();
                        }
                    });
                    w.add(button);
                }

                rowTwo.add(w);
            }
            {
                Column w = new Column(4);
                w.setHeight("100%");
                Button button = new Button(i18n.vennDiagram_button_select());
                button.setHeight("100%");
                button.setSize(ButtonSize.LARGE);
                button.getElement().getStyle().setDisplay(Style.Display.FLEX);
                button.getElement().getStyle().setProperty("alignItems", "center");
                button.getElement().getStyle().setProperty("justifyContent", "center");
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        BroadcastSet set = canvas.toBroadCastSet();
                        broadcastManager.broadcastSelection(set, senderViz, viz);
                        broadcastManager.endSendTo();
                    }
                });
                button.setBlock(true);
                w.add(button);
                rowTwo.add(w);
            }
            {
                Column w = new Column(4);
                w.setHeight("100%");
                Button button = new Button(i18n.vennDiagram_button_cancel());
                button.setHeight("100%");
                button.setSize(ButtonSize.LARGE);
                button.getElement().getStyle().setDisplay(Style.Display.FLEX);
                button.getElement().getStyle().setProperty("alignItems", "center");
                button.getElement().getStyle().setProperty("justifyContent", "center");
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        broadcastManager.endSendTo();
                    }
                });
                button.setBlock(true);
                w.add(button);
                rowTwo.add(w);
            }
            this.add(rowTwo);
        }
    }

    @Override
    public void onResize() {
        canvas.render();
    }

}

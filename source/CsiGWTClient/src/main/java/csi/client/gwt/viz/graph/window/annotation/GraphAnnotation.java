package csi.client.gwt.viz.graph.window.annotation;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.CollapseEvent;
import com.sencha.gxt.widget.core.client.event.CollapseEvent.CollapseHandler;
import com.sencha.gxt.widget.core.client.event.ExpandEvent;
import com.sencha.gxt.widget.core.client.event.ExpandEvent.ExpandHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.MenuKey;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.graph.gwt.AnnotationDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.Annotation;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class GraphAnnotation implements IsWidget {

    private static final String ANNOTATION_WINDOW_HEADER = CentrifugeConstantsLocator.get().graphAnnotationDialog_Heading();
    private static final String CLOSE_BUTTON_TEXT = CentrifugeConstantsLocator.get().graphAnnotationDialog_closeButton();
    private static final String EDIT_BUTTON_TEXT = CentrifugeConstantsLocator.get().graphAnnotationDialog_EditButton();
    private static final String GRAPH_ANNOTATION_ID = "-1";
    private Graph graph;
    private String html;
    private GraphAnnotationView view;
    private GraphAnnotationDialog dialog;
    private ContentPanel display;
    private boolean loaded = false;
    private boolean forcedClosed = false;
    private ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            display.setVisible(false);
        }
    };
    private GraphAnnotationCallback graphAnnotationCallback;

    public static GraphAnnotation create(Graph graph) {
        return new GraphAnnotation(graph);
    }


    public GraphAnnotation(Graph graph1) {
        this.graph = graph1;
        display = new ContentPanel();
        view = new GraphAnnotationView(this);
        dialog = new GraphAnnotationDialog(this);

        display.setWidget(view);
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        closeButton.setTitle(CLOSE_BUTTON_TEXT);
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());

        display.addTool(closeButton);

        Button editButton = new Button();
        editButton.setTitle(EDIT_BUTTON_TEXT);
        editButton.setIcon(IconType.PENCIL);
        editButton.setType(ButtonType.LINK);
        editButton.getElement().getStyle().setMargin(0, Unit.PX);
        editButton.getElement().getStyle().setPadding(0, Unit.PX);
        editButton.getElement().getStyle().setPosition(Position.ABSOLUTE);
        editButton.getElement().getStyle().setLeft(70, Unit.PX);
        editButton.getElement().getStyle().setTop(-1, Unit.PX);
        editButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                editAnnotation();
            }

        });

        display.getHeader().addTool(editButton);

        display.setHeading(ANNOTATION_WINDOW_HEADER);
        display.setCollapsible(true);
        display.setStyleName("legend");//NON-NLS
        display.setBodyBorder(false);
        display.setBodyStyle("background:none;");//NON-NLS
        // attempt to remove the background bellow is not honored
        display.setBodyStyleName("legend-body");//NON-NLS
        display.getHeader().addStyleName("legend-header");//NON-NLS
        display.getHeader().setBorders(false);
        display.setHeading(ANNOTATION_WINDOW_HEADER);
        // display.getHeader().setStyleName("background:rba(180,180,180,.4)");
        display.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");//NON-NLS
        display.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS
        // this is basically an infinite loop..
//        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
//
//            @Override
//            public boolean execute() {
//                try {
//                    if(display.getHeader().getTools().size()>=1) {
//                        Widget collapseButton = display.getHeader().getTool(1);
//                        collapseButton.setTitle("Hide");
//                    }else{
//                        return true;
//                    }
//                } catch (Exception e) {
//                    return true;
//                }
//                return false;
//            }
//        }, 1000);

        display.addCollapseHandler(new CollapseHandler() {

            @Override
            public void onCollapse(CollapseEvent event) {
                Widget collapseButton = display.getHeader().getTool(1);
                collapseButton.setTitle((CentrifugeConstantsLocator.get().graphAnnotation_expandTooltip()));
            }

        });

        display.addExpandHandler(new ExpandHandler() {

            @Override
            public void onExpand(ExpandEvent event) {

                Widget collapseButton = display.getHeader().getTool(1);
                collapseButton.setTitle(CentrifugeConstantsLocator.get().graphAnnotation_collapseTooltip());
            }
        });

        display.addHideHandler(new HideEvent.HideHandler() {
            @Override
            public void onHide(HideEvent event) {
                setForcedClosed(true);
                graph.getMenuManager().hide(MenuKey.HIDE_ANNOTATION);
                graph.getMenuManager().enable(MenuKey.SHOW_ANNOTATION);
            }
        });


    }

    public String getHtml() {
        return html;
    }

    public VortexFuture<Annotation> addAnnotation(String htmlString) {
        this.html = htmlString;
        VortexFuture<Annotation> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Annotation>() {
            @Override
            public void onSuccess(Annotation result) {
                view.populateAnnotation(result.getHtmlString());
            }
        });
        //I believe -1 will never be an ID assigned to nodes and links, so it'll represent the graph
        AnnotationDTO annotationDTO = AnnotationDTO.create(graph.getUuid(), htmlString, GRAPH_ANNOTATION_ID);
        view.populateAnnotation("");
        try {
            future.execute(GraphActionServiceProtocol.class).addAnnotation(annotationDTO);
        } catch (CentrifugeException e) {
            e.printStackTrace();
        }
        return future;
    }

    public VortexFuture<Annotation> load() {

        VortexFuture<Annotation> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Annotation>() {
            @Override
            public void onSuccess(Annotation result) {
                if (result != null) {
                    html = result.getHtmlString();
                    view.populateAnnotation(html);
                }
                setLoaded(true);

                graphAnnotationCallback.callback();
            }

            @Override
            public boolean onError(Throwable t) {
                setLoaded(true);
                return loaded;
            }
        });
        //I believe -1 will never be an ID assigned to nodes and links, so it'll represent the graph
        AnnotationDTO annotationDTO = AnnotationDTO.create(graph.getUuid(), null, GRAPH_ANNOTATION_ID);
        try {
            future.execute(GraphActionServiceProtocol.class).retrieveAnnotation(annotationDTO);
        } catch (Exception e) {

        }
        return future;
    }

    private void editAnnotation() {
        dialog.show();
    }

    @Override
    public Widget asWidget() {
        return display;
    }

    public ContentPanel getDisplay() {
        return display;
    }


    public void hide() {
        display.hide();
    }


    public void show() {
        if (html == null) {
            load();
        }
        display.show();
    }

    public boolean isVisible() {
        return display.isVisible();
    }


    public boolean isLoaded() {
        return loaded;
    }


    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }


    public boolean isForcedClosed() {
        return forcedClosed;
    }


    public void setForcedClosed(boolean forcedClosed) {
        this.forcedClosed = forcedClosed;
    }


    public void addLoadCallback(GraphAnnotationCallback graphAnnotationCallback) {
        this.graphAnnotationCallback = graphAnnotationCallback;
    }

}

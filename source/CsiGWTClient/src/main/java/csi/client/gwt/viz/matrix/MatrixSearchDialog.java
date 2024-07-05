package csi.client.gwt.viz.matrix;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.fx.client.Draggable;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.MatrixCategoryResponse;
import csi.shared.core.visualization.matrix.MatrixSearchRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

/**
 *
 * //TODO @p figure out a way to default this view to something betyter.
 * Control that will be doing searchy search on matrix
 */
public class MatrixSearchDialog extends ContentPanel {
    MatrixPresenter presenter;
    ContentPanel searchView;
    Draggable searchViewDraggable;
    ComboBox xCatToSearch;
    ComboBox yCatToSearch;
    TextField valueToSearch;
    Button search;
    Integer xPos;
    Integer yPos;

    private boolean searchingX= false, searchingY = false;

    private String prevX = "", prevY= "";
    private boolean searchDisabled = false;


    MatrixSearchDialog(MatrixPresenter presenter){
        this.presenter = presenter;
        buildView();
    }

    public void show(int buttonTop, int buttonLeft){
        int vizWidth = presenter.getView().getElement().getOffsetWidth();
        int vizHeight = presenter.getView().getElement().getOffsetHeight();
        if (xPos != null && yPos !=null) {
            searchView.setPosition(xPos, yPos);
        } else {
            searchView.setHeading(presenter.getName());
            searchView.setPosition(vizWidth-350, vizHeight - 170);
        }

        show();
    }

    public void updateDialogPosition(int buttonTop, int buttonLeft) {
        int vizWidth = presenter.getView().getElement().getOffsetWidth();
        int vizLeft = presenter.getView().getAbsoluteLeft();
        int vizHeight = presenter.getView().getElement().getOffsetHeight();
        int vizTop = presenter.getView().getAbsoluteTop();
        int searchViewAbsoluteLeft = searchView.getAbsoluteLeft();
        int searchViewAbsoluteTop = searchView.getAbsoluteTop();

        if (vizLeft + vizWidth < searchViewAbsoluteLeft + 280 && yPos == null) {
            searchView.setPosition(vizWidth-350, buttonTop);
        } else if (vizLeft + vizWidth < searchViewAbsoluteLeft + 280 && yPos != null) {
            searchView.setPosition(vizWidth-350, yPos);
        }

        if (vizTop + vizHeight < searchViewAbsoluteTop + 150 && xPos == null) {
            searchView.setPosition(buttonLeft, vizHeight-220);
        } else if (vizTop + vizHeight < searchViewAbsoluteTop + 150 && xPos != null) {
            searchView.setPosition(xPos, vizHeight-220);
        }
    }

    public void show(){
        searchView.setVisible(true);
        searchView.setSize("290px", "150px");
        xCatLabel.setText(presenter.getModel().getXAxisTitle());
        yCatLabel.setText(presenter.getModel().getYAxisTitle());
    }

    FieldLabel xCatLabel;
    FieldLabel yCatLabel;


    UpdateTimer timer;

    public class UpdateTimer extends Timer {
        @Override
        public void run() {
            presenter.getView().fetchData();
        }
    }


    private void buildView() {
        searchView = new ContentPanel();
        searchView.setHeading("Search");

        addCloseButton();

        BoxLayoutContainer.BoxLayoutData innerLayout = new BoxLayoutContainer.BoxLayoutData(new Margins(2,5,0,15));
        BoxLayoutContainer.BoxLayoutData data = new BoxLayoutContainer.BoxLayoutData(new Margins(4,0,0,0));
        BoxLayoutContainer.BoxLayoutData labelPad = new BoxLayoutContainer.BoxLayoutData(new Margins(3,0,0,15));

        VBoxLayoutContainer view = new VBoxLayoutContainer();


        HBoxLayoutContainer xCatContent = new HBoxLayoutContainer();
        xCatContent.setPack(BoxLayoutContainer.BoxLayoutPack.START);
        xCatContent.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);
        xCatLabel = new FieldLabel();
        xCatLabel.setWidth("100px");

        xCatLabel.setText(presenter.getModel().getXAxisTitle());

        LabelProvider<AxisLabel> lblProvider = new LabelProvider<AxisLabel>() {
            @Override
            public String getLabel(AxisLabel axisLabel) {
                return axisLabel.getLabel();
            }
        };

        ListStore<AxisLabel> store = new ListStore<AxisLabel>(new ModelKeyProvider<AxisLabel>() {
            @Override
            public String getKey(AxisLabel axisLabel) {
                return axisLabel.getLabel();
            }
        });
        ListStore<AxisLabel> ytore = new ListStore<AxisLabel>(new ModelKeyProvider<AxisLabel>() {
            @Override
            public String getKey(AxisLabel axisLabel) {
                return axisLabel.getLabel();
            }
        });


        xCatToSearch =  new ComboBox(store, lblProvider);
        xCatContent.addStyleName("string-combo-style");;
        xCatToSearch.setWidth("150px");
        xCatContent.add(xCatLabel, labelPad);
        xCatContent.add(xCatToSearch, innerLayout);

        xCatToSearch.addKeyUpHandler(event ->{
            String xQuery = xCatToSearch.getText();
            if(xQuery.length() > 1){ // make them type in a number

                Vortex vortex = presenter.getVortex();
                VortexFuture<MatrixCategoryResponse> vortexFuture = vortex.createFuture();
                MatrixSearchRequest req = new MatrixSearchRequest(presenter.getDataViewUuid(), presenter.getVisualizationDef().getUuid());
                req.setxQuery(xQuery);
                req.setyQuery("");

                vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixCategoryResponse>() {
                    @Override
                    public void onSuccess(MatrixCategoryResponse result) {
                        searchingX = false;
                        if(result.getCategoryX().size() > 0){
                            xCatToSearch.getStore().clear();
//                            xCatToSearch.getStore().addAll(prevX);
                            if(result.getCategoryY().size() > 20){
                                xCatToSearch.getStore().addAll(result.getCategoryX().subList(0, 19));
                            }else{
                                xCatToSearch.getStore().addAll(result.getCategoryX());
                            }
                            xCatToSearch.expand();
                        }
                    }
                });
                if(!searchingX && !prevX.equals(xQuery)){
                    prevX = xQuery;
                    searchingX = true;
                    vortexFuture.execute(MatrixActionsServiceProtocol.class).getAxisCategoriesForSearch(req, Axis.X);
                }
            }
        });

        HBoxLayoutContainer yCatContent = new HBoxLayoutContainer();

        yCatContent.setPack(BoxLayoutContainer.BoxLayoutPack.START);
        yCatContent.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);

        yCatToSearch = new ComboBox(ytore, lblProvider);
        yCatToSearch.addStyleName("string-combo-style");

        yCatToSearch.addKeyUpHandler(event ->{
            String yQuery = yCatToSearch.getText();
            if(yQuery.length() > 1){ // make them type in a number
                Vortex vortex = presenter.getVortex();
                VortexFuture<MatrixCategoryResponse> vortexFuture = vortex.createFuture();
                MatrixSearchRequest req = new MatrixSearchRequest(presenter.getDataViewUuid(), presenter.getVisualizationDef().getUuid());
                req.setxQuery("");
                req.setyQuery(yQuery);

                vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixCategoryResponse>() {
                    @Override
                    public void onSuccess(MatrixCategoryResponse result) {
                        searchingY = false;
                        if(result.getCategoryY().size()>0){
                            yCatToSearch.getStore().clear();
                            if(result.getCategoryY().size() > 20){
                                yCatToSearch.getStore().addAll(result.getCategoryY().subList(0,19));
                            }else {
                                yCatToSearch.getStore().addAll(result.getCategoryY());
                            }

                            yCatToSearch.expand();
                        }
                    }
                });

                if(!searchingY & !prevY.equals(yQuery)) {
                    prevY = yQuery;
                    searchingY = true;
                    vortexFuture.execute(MatrixActionsServiceProtocol.class).getAxisCategoriesForSearch(req, Axis.Y);
                }
            }
        });
        yCatToSearch.setWidth("150px");
        yCatLabel = new FieldLabel();
        yCatLabel.setWidth("100px");
        yCatLabel.setText(presenter.getModel().getYAxisTitle());
        yCatContent.add(yCatLabel,labelPad);
        yCatContent.add(yCatToSearch, innerLayout);


        HBoxLayoutContainer valueContent = new HBoxLayoutContainer();
        valueContent.setPack(BoxLayoutContainer.BoxLayoutPack.START);
        valueContent.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);
        valueToSearch = new TextField();
        FieldLabel valueLabel = new FieldLabel();
        valueLabel.setWidth("61px");
        valueLabel.setText("Value");
        valueContent.add(valueLabel, labelPad);
        valueContent.add(valueToSearch, innerLayout);

        HBoxLayoutContainer control = new HBoxLayoutContainer();
        control.setPack(BoxLayoutContainer.BoxLayoutPack.CENTER);
        control.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.STRETCHMAX);
        search = new Button("Search");
        search.addClickHandler(getSearchHandler());
        search.setWidth("244px");
        control.add(search, innerLayout);

        view.add(xCatContent, new BoxLayoutContainer.BoxLayoutData(new Margins(10,0,0,0)));
        view.add(yCatContent, data);
        view.add(control, data);

        xCatToSearch.setEmptyText("Type to search");
        yCatToSearch.setEmptyText("Type to search");

        searchView.add(view);

        searchViewDraggable = presenter.getChrome().addWindowAndReturnDraggable(searchView);
        searchViewDraggable.addDragEndHandler(dragEndEvent -> {
            xPos = searchView.getElement().getLeft();
            yPos = searchView.getElement().getTop();
        });
        searchView.setVisible(false);

    }

    // TODO: ADD VALIDATORS
    private ClickHandler getSearchHandler(){
        ClickHandler handle = event -> {
            MatrixSearchRequest req = new MatrixSearchRequest(presenter.getDataViewUuid(), presenter.getVisualizationDef().getUuid());
            req.setxQuery(xCatToSearch.getText());
            req.setyQuery(yCatToSearch.getText());

            // don't search for nothing.
            if(req.getxQuery().isEmpty() && req.getyQuery().isEmpty()){
                return;
            }

            String numb = valueToSearch.getText().isEmpty() ? "0" : valueToSearch.getText();
            //
            req.setValueQuery(Integer.parseInt(numb));

            Vortex vortex = presenter.getVortex();
            VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

            // TODO Change this to MatrixSearchResults
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
                @Override
                public void onSuccess(MatrixWrapper result) {
                    searchDisabled = false;
                    presenter.getView().setLoadingIndicator(false);
                    if(result.isEmpty()){
                        search.setText("No results found");
                    }else {
                        search.setText("Search");
                        int startX = result.getData().getStartX();
                        int startY = result.getData().getStartY();

                        int width = result.getData().getEndX() - result.getData().getStartX() + 1;
                        int height = result.getData().getEndY() - result.getData().getStartY() + 1;


                        presenter.getModel().setCurrentView(startX, startY, width, height);
//                        presenter.getModel().setX(startX);
//                        presenter.getModel().setY(startY);
//                        presenter.getModel().setWidth(width);
//                        presenter.getModel().setHeight(height);
//
                        if(timer == null){
                            timer = new UpdateTimer();
                        }else{
                            timer.cancel();
                        }
                        timer.schedule(1000);
                    }
                }
            });
            search.setText("Searching...");
            presenter.getView().setLoadingIndicator(true);

            if(!searchDisabled) {
                vortexFuture.execute(MatrixActionsServiceProtocol.class).search(req);
                searchDisabled = true;
            }
        };

        return handle;
    }

    private void addCloseButton(){
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        ClickHandler closeHandler = event -> searchView.setVisible(false);

        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        searchView.addTool(closeButton);

    }


    public void resetAndHide(){
        searchView.setVisible(false);
        buildView();
    }

}

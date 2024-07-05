package csi.client.gwt.mainapp;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.dom.XDOM;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.dataview.directed.FitView;
import csi.client.gwt.dataview.directed.SelectView;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.Response;
import csi.server.common.model.dataview.AnnotationCardDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MessagesDialog {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final int MAX_TEXTAREA_HEIGHT = 150;
    private MainPresenter _mainPresenter = null;
    private SimplePanel dialog;
    private InlineLabel closeButton;
    private InlineLabel titleLabel;
    private com.github.gwtbootstrap.client.ui.Button informationMouseover;
    private HorizontalPanel textBoxAndAdd;
//    private TabBar messageTypePanel;
    private FluidContainer annotationPanel;
    private TextArea annotationTextArea;
    private Button addButton;
    private AnnotationCard annotationCard = null;
    private FluidRow annotationRow;
    private FluidRow emptyAnnotationRow = null;
    private AnnotationBadge annotationBadge;
    private List<AnnotationCardDef> cardDefList = new ArrayList<AnnotationCardDef>();
    private FluidContainer messagesPanel;

    public MessagesDialog(DataView dataViewIn, AbstractDataViewPresenter dataViewPresenter) {
        dialog = new SimplePanel();
        styleDialog(dataViewPresenter);

        dialog.sinkEvents(Event.ONCLICK);
        dialog.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            }
        }, ClickEvent.getType());


        messagesPanel = new FluidContainer();
        {
            Style style = messagesPanel.getElement().getStyle();
            style.setPaddingLeft(12, Style.Unit.PX);
            style.setPaddingRight(12, Style.Unit.PX);
        }
        {
            FluidRow headerRow = new FluidRow();
            messagesPanel.add(headerRow);
            {
                Style style = headerRow.getElement().getStyle();
                style.setMarginTop(12, Style.Unit.PX);
            }
            //Title
            {
                titleLabel = new InlineLabel();
                titleLabel.setText("Annotations");
                titleLabel.getElement().getStyle().setFontSize(22.0, Style.Unit.PX);
                titleLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
//            titleLabel.getElement().getStyle().setLineHeight(55, Style.Unit.PX);
                Column headerTextColumn = new Column(8);
                headerRow.add(headerTextColumn);
                headerTextColumn.add(titleLabel);
            }

            //Close Button
            {
                closeButton = new InlineLabel();
                styleCloseButton();
                closeButton.addClickHandler(event -> {
                    hide();
                });
//            closeButton.getElement().getStyle().setMarginTop(20, Style.Unit.PX);
                Column headerCloseColumn = new Column(1);
                headerCloseColumn.getElement().getStyle().setFloat(Style.Float.RIGHT);
                headerRow.add(headerCloseColumn);
                headerCloseColumn.add(closeButton);
            }

            //Information (mouseover tooltip)
            {
                informationMouseover = new com.github.gwtbootstrap.client.ui.Button();
                informationMouseover.setIcon(IconType.INFO_SIGN);
                informationMouseover.setTitle(i18n.messagesPanel_informationTooltip());
                styleInformationButton();
//                informationMouseover.addMouseOverHandler(event -> {
//                   handleInformationMouseover();
//                });

                Column headerInfoColumn = new Column(1);
                headerInfoColumn.getElement().getStyle().setFloat(Style.Float.RIGHT);
                headerInfoColumn.getElement().getStyle().setPosition(Style.Position.RELATIVE);
                headerInfoColumn.getElement().getStyle().setTop(-3, Style.Unit.PX);
                headerInfoColumn.getElement().getStyle().setLeft(10, Style.Unit.PX);
                headerInfoColumn.add(informationMouseover);
                headerRow.add(headerInfoColumn);


            }
        }
        //Actions, Annotations, Tags
//        messageTypePanel = new TabBar();
//        messageTypePanel.getElement().getStyle().setBackgroundColor("blue");
//        Label annotationsTabLabel = new Label();
//        messageTypePanel.addTab("Actions");
//        messageTypePanel.addTab("Annotations");
//        messageTypePanel.addTab("Tags");


//        annotationsTabLabel.setText("Annotations");
//        messageTypePanel.add(annotationsTabLabel, "Annotations");

        //annotations listed
        annotationPanel = new FluidContainer();
        annotationPanel.getElement().getStyle().setBackgroundColor("#f2f7f4");
        annotationPanel.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
        annotationPanel.getElement().getStyle().setOverflowX(Style.Overflow.VISIBLE);
        {
            Style style = annotationPanel.getElement().getStyle();
            style.setPaddingLeft(5, Style.Unit.PX);
            style.setPaddingRight(8, Style.Unit.PX);
            style.setPaddingBottom(20, Style.Unit.PX);
        }
        cardDefList = dataViewIn.getMeta().getModelDef().getAnnotationCardDefs();
        if (cardDefList.size() > 0) {
            for (AnnotationCardDef annotationCardDef : dataViewIn.getMeta().getModelDef().getAnnotationCardDefs().stream().sorted((o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime())).collect(Collectors.toList())) {
                FluidRow newAnnotationRow = new FluidRow();
                AnnotationCard newAnnotationCard = new AnnotationCard(annotationCardDef, newAnnotationRow, annotationPanel, dataViewPresenter.isReadOnly(), dataViewPresenter.getOwnership());
                if (!dataViewPresenter.isReadOnly()) {
                    addHandlers(dataViewIn, newAnnotationCard);
                }
                newAnnotationRow.add(newAnnotationCard);
                annotationPanel.add(newAnnotationRow);
            }
        } else {
            addEmptyRowMessage();
        }


        //TextBox & add button
        textBoxAndAdd = new HorizontalPanel();
        annotationTextArea = new TextArea();
        addButton = new Button();
        addButton.setText(i18n.messagesPanel_addButton());
        styleAddButton();

        annotationTextArea.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        annotationTextArea.getElement().getStyle().setProperty("resize", "none");


        annotationTextArea.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (!(annotationTextArea.getText().trim().isEmpty())) {

                    int textAreaHeight = annotationTextArea.getOffsetHeight();
                    if (textAreaHeight < MAX_TEXTAREA_HEIGHT) {
                        if (annotationTextArea.getElement().getScrollHeight() < MAX_TEXTAREA_HEIGHT) {
                            annotationTextArea.setHeight("auto");
                            annotationTextArea.setHeight(annotationTextArea.getElement().getScrollHeight() + "px");
                        } else {
                            annotationTextArea.setHeight(MAX_TEXTAREA_HEIGHT + "px");
                        }
                    }

                    if (event.getNativeKeyCode() == 13) {
                        handleAdd(dataViewPresenter, dataViewIn);
                    }
                }
            }
        });
        addButton.addClickHandler(event -> {
            if (!(annotationTextArea.getText().trim().isEmpty())) {
                handleAdd(dataViewPresenter, dataViewIn);
            }
        });

        if (!dataViewPresenter.isReadOnly()) {
            textBoxAndAdd.add(annotationTextArea);
            textBoxAndAdd.add(addButton);
        }

        annotationBadge = new AnnotationBadge(String.valueOf(cardDefList.size()));

        if (cardDefList.size() > 0) {
//            updateBadge();
        }

        annotationBadge.getElement().getStyle().setTop(70, Style.Unit.PX);
        annotationBadge.getElement().getStyle().setProperty("userSelect", "none");

//        messagesPanel.add(messageTypePanel);



        if (emptyAnnotationRow != null) {
            messagesPanel.add(emptyAnnotationRow);
            emptyAnnotationRow.getElement().getStyle().setMarginTop(20, Style.Unit.PX);
        }
        messagesPanel.add(annotationPanel);
        annotationPanel.getElement().getStyle().setMarginTop( 10, Style.Unit.PX);
        annotationPanel.getElement().getStyle().setProperty("maxHeight", Window.getClientHeight() * .65 + "px");

        if (!dataViewPresenter.isReadOnly()) {
            messagesPanel.add(textBoxAndAdd);
            textBoxAndAdd.getElement().getStyle().setMarginTop(15, Style.Unit.PX);
            textBoxAndAdd.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
            textBoxAndAdd.getElement().getStyle().setMarginBottom(10, Style.Unit.PX);
        }

        dialog.add(messagesPanel);
        attach();
        hide();

        if (dataViewPresenter instanceof DirectedPresenter) {
            if (!Strings.isNullOrEmpty(getMainPresenter().getAnnotationMode())) {
                if (getMainPresenter().getAnnotationMode().equals("true") && cardDefList.size() > 0) {
                    show();
                }
            } else {
                show();
            }
        }
    }

    private void handleAdd(AbstractDataViewPresenter dataViewPresenter, DataView dataViewIn) {
        annotationRow = new FluidRow();
        AnnotationCardDef newAnnotationCardDef = new AnnotationCardDef();
        newAnnotationCardDef.setCreatorUserName(getMainPresenter().getUserName());
        newAnnotationCardDef.setCreateTime(new Date());
        newAnnotationCardDef.setContent(annotationTextArea.getText());
        newAnnotationCardDef.setEdited(false);
        annotationCard = new AnnotationCard(newAnnotationCardDef, annotationRow, annotationPanel, dataViewPresenter.isReadOnly(), dataViewPresenter.getOwnership());
        dataViewIn.getMeta().getModelDef().getAnnotationCardDefs().add(newAnnotationCardDef);
        VortexFuture<Response<String, DataView>> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {
            vortexFuture.execute(DataViewActionServiceProtocol.class).save(dataViewIn);
        } catch (Exception e) {
            Display.error("AbstractDataViewPresenter", 11, e);
        }

        if (emptyAnnotationRow != null) {
            emptyAnnotationRow.removeFromParent();
        }
        annotationRow.add(annotationCard);
        annotationPanel.add(annotationRow);
        //TODO Dynamic positioning for annotations and chat box
        annotationTextArea.setValue("");
//                cardDefList.add(newAnnotationCardDef);
//                updateBadge();
        if (!dataViewPresenter.isReadOnly()) {
            addHandlers(dataViewIn, annotationCard);
        }
        annotationTextArea.setHeight("auto");
    }

    private void addEmptyRowMessage() {
        emptyAnnotationRow = new FluidRow();
        Label emptyAnnotationNotification = new Label();
        emptyAnnotationNotification.setText(i18n.messagesPanel_emptyAnnotationNotification());
        emptyAnnotationNotification.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
        emptyAnnotationNotification.getElement().getStyle().setFontStyle(Style.FontStyle.ITALIC);
        emptyAnnotationRow.add(emptyAnnotationNotification);
        annotationPanel.add(emptyAnnotationRow);
    }

    private void updateBadge() {
        if (cardDefList.size() > 0) {
            messagesPanel.add(annotationBadge);
        } else {
            messagesPanel.remove(annotationBadge);
        }
//        annotationBadge.setCount(String.valueOf(cardDefList.size()));
    }

    private void addHandlers(DataView dataView, AnnotationCard annotationCard) {
        annotationCard.addDeleteHandler(new DeleteAnnotationEventHandler() {
            @Override
            public void onDelete(AnnotationCard annotationCard) {
                AnnotationCardDef acd = annotationCard.getAnnotationCardDef();
                dataView.getMeta().getModelDef().getAnnotationCardDefs().remove(acd);
//                cardDefList.remove(acd);
//                updateBadge();
                VortexFuture<Response<String, DataView>> vortexFuture = WebMain.injector.getVortex().createFuture();
                try {
                    vortexFuture.execute(DataViewActionServiceProtocol.class).save(dataView);
                } catch (Exception e) {
                    Display.error("AbstractDataViewPresenter", 11, e);
                }
                if (dataView.getMeta().getModelDef().getAnnotationCardDefs().size() == 0) {
                    addEmptyRowMessage();
                }
                //TODO Dynamic position on annotation and add box
            }
        });

        annotationCard.addSaveEditHandler(new SaveEditAnnotationEventHandler() {
            @Override
            public void onSaveEdit(AnnotationCard annotationCard) {
                for (AnnotationCardDef annotationCardDef : dataView.getMeta().getModelDef().getAnnotationCardDefs()) {
                    if(annotationCardDef.equals(annotationCard.getAnnotationCardDef())) {
                        annotationCardDef.setContent(annotationCard.getContent());
                        annotationCardDef.setEdited(true);
                    }
                }
                VortexFuture<Response<String, DataView>> vortexFuture = WebMain.injector.getVortex().createFuture();
                try {
                    vortexFuture.execute(DataViewActionServiceProtocol.class).save(dataView);
                } catch (Exception e) {
                    Display.error("AbstractDataViewPresenter", 11, e);
                }
            }
        });
    }

    public void show() {
        dialog.getElement().getStyle().setDisplay(Style.Display.INITIAL);
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                dialog.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
                return false;
            }
        }, 150);
        dialog.setVisible(true);
    }


    public void attach() {
        if (!dialog.isAttached()) {
            RootPanel.get().add(dialog);
        }
    }

    private void styleDialog(AbstractDataViewPresenter dataViewPresenter) {
        dialog.setWidth("350px");
        dialog.getElement().getStyle().setProperty("minHeight", "200px");
        dialog.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        if (dataViewPresenter instanceof DataViewPresenter) {
            dialog.getElement().getStyle().setTop(30, Style.Unit.PX);
        } else if (dataViewPresenter instanceof DirectedPresenter) {
            dialog.getElement().getStyle().setTop(0, Style.Unit.PX);
        }
        dialog.getElement().getStyle().setRight(0, Style.Unit.PX);
        dialog.getElement().getStyle().setProperty("boxShadow", "none");
        dialog.getElement().getStyle().setBackgroundColor("white");
        dialog.getElement().getStyle().setProperty("border", "groove");
    }

    private void styleCloseButton() {
        closeButton.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        closeButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        closeButton.getElement().getStyle().setColor("#000000");
        closeButton.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        closeButton.setText(" X ");
    }

    private void styleInformationButton() {
        informationMouseover.getElement().getStyle().setProperty("background", "none");
        informationMouseover.getElement().getStyle().setProperty("border", "none");
        informationMouseover.getElement().getStyle().setProperty("boxShadow", "none");
        informationMouseover.setSize(ButtonSize.LARGE);

    }


    private void styleAddButton() {
        addButton.setWidth("60px");
        addButton.setHeight("30px");
        addButton.getElement().getStyle().setBackgroundColor("#216893");
        addButton.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);
        addButton.getElement().getStyle().setColor("white");
        addButton.getElement().getStyle().setProperty("borderRadius", "9px");
    }

    private void handleInformationMouseover() {
        informationMouseover.setTitle("lol");


    }



    public void hide() {
        dialog.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}

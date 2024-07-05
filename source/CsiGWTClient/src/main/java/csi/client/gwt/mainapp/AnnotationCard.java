package csi.client.gwt.mainapp;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.dataview.AnnotationCardDef;

import java.util.Date;

public class AnnotationCard extends FluidContainer {
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static final DateTimeFormat TIME_OF_ANNOTATION_FORMAT = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

    private FluidRow annotationRow;
    private FluidRow nameTimeMenuRow;
    private FluidRow contentRow;
    private Column nameTimeColumn;
    private Column menuColumn;
    private NavLink editMenuItem = null;
    private NavLink deleteMenuItem = null;
    private String content;
    private Label contentLabel;
    private Label editedLabel;
    private DropdownButton menuButton;
    private Button editCancelButton;
    private Button editSaveButton;
    private TextBox editTextBox;
    private Date createdDate;
    private AnnotationCardDef annotationCardDef;
    private HandlerManager _handlerManager;
    private Widget annotationPanelt;
    private boolean readOnly;
    private boolean isDataViewOwner;
    private boolean isOriginalPoster;
    private boolean isEdited;
    private boolean editLabelAdded = false;


    public AnnotationCard(String userName, String contentIn, Date dateIn, boolean isEditedIn, FluidRow parentRow, Widget annotationPanel, boolean readOnlyIn, boolean isDataViewOwnerIn) {
        _handlerManager = new HandlerManager(this);
        annotationRow = parentRow;
        content = contentIn;
        isEdited = isEditedIn;
        createdDate = dateIn;
        readOnly = readOnlyIn;
        isDataViewOwner = isDataViewOwnerIn;
        annotationPanelt = annotationPanel;
        String viewingUserName = WebMain.injector.getMainPresenter().getUserName();
        if (viewingUserName.equals(userName)) {
            isOriginalPoster = true;
        } else {
            isOriginalPoster = false;
        }
        init(userName, contentIn);
    }

    public AnnotationCard(AnnotationCardDef annotationCardDefIn, FluidRow annotationRow, Widget annotationPanel, boolean readOnlyIn, boolean isDataViewOwnerIn) {
        this(annotationCardDefIn.getCreatorUserName(), annotationCardDefIn.getContent(), annotationCardDefIn.getCreateTime(), annotationCardDefIn.isEdited(), annotationRow, annotationPanel, readOnlyIn, isDataViewOwnerIn);
        annotationCardDef = annotationCardDefIn;
    }

    public void init(String userName, String content) {
        this.getElement().getStyle().setPaddingLeft(0, Style.Unit.PX);
        this.getElement().getStyle().setPaddingRight(0, Style.Unit.PX);
        nameTimeMenuRow = new FluidRow();
        {
            Style style = nameTimeMenuRow.getElement().getStyle();
            style.setMarginTop(5.0, Style.Unit.PX);

        }
        nameTimeColumn = new Column(11);
        menuColumn = new Column(1);

        nameTimeColumn.getElement().getStyle().setBackgroundColor("#f2f7f4");
        nameTimeColumn.getElement().getStyle().setProperty("minHeight", "0");
        menuColumn.getElement().getStyle().setProperty("minHeight", "0");

        nameTimeMenuRow.add(nameTimeColumn);
        nameTimeMenuRow.add(menuColumn);

        addName(userName);
        addTime();
        if (!readOnly && (isOriginalPoster || isDataViewOwner)) {
            addMenuButton();
        }
        editSaveButton = new Button(i18n.messagesPanel_editSaveButton());
        if (!readOnly && (isOriginalPoster || isDataViewOwner)) {
            createMenuItems();
        }


        contentRow = new FluidRow();
        contentRow.getElement().getStyle().setMarginLeft(6, Style.Unit.PX);
        contentRow.getElement().getStyle().setWidth(95, Style.Unit.PCT);
        contentRow.getElement().getStyle().setMarginBottom(5, Style.Unit.PX);
        addContent(content);

//        if (isEdited) {
//            addEditLabel();
//        }
        add(nameTimeMenuRow);
        add(contentRow);


    }

    public void addContent(String content) {
        contentLabel = new Label(content);
        Style style = contentLabel.getElement().getStyle();
        style.setProperty("fontFamily", "\"Helvetica Neue\",Helvetica,Arial,sans-serif");
        style.setColor("black");
        style.setBackgroundColor("#f2f7f4");
        style.setFontWeight(Style.FontWeight.NORMAL);
        style.setFontSize(9, Style.Unit.PT);
        style.setWhiteSpace(Style.WhiteSpace.NORMAL);
        contentRow.add(contentLabel);
    }

    public void addName(String userName) {
        Label userNameLabel = new Label(userName);
        userNameLabel.getElement().getStyle().setProperty("fontFamily", "\"Helvetica Neue\",Helvetica,Arial,sans-serif");
        userNameLabel.getElement().getStyle().setColor("#216893");
        userNameLabel.getElement().getStyle().setBackgroundColor("#f2f7f4");
        userNameLabel.getElement().getStyle().setFontSize(9, Style.Unit.PT);
        nameTimeColumn.add(userNameLabel);
    }

    public void addTime() {
        Label currentTime = new Label();
        DateTimeFormat dtf = TIME_OF_ANNOTATION_FORMAT;
        String formattedDate = dtf.format(createdDate);
        currentTime.setText("    " + formattedDate);
        Style style = currentTime.getElement().getStyle();
        style.setProperty("fontFamily", "\"Helvetica Neue\",Helvetica,Arial,sans-serif");
        style.setColor("#767676");
        style.setBackgroundColor("#f2f7f4");
        style.setFontSize(9, Style.Unit.PT);
        style.setFontWeight(Style.FontWeight.NORMAL);
        nameTimeColumn.add(currentTime);
    }

    public void addMenuButton() {
        menuButton = new DropdownButton();
        menuButton.getTriggerWidget().getElement().getStyle().setProperty("background", "#f2f7f4");
        menuButton.getTriggerWidget().getElement().getStyle().setProperty("border", "none");
        menuButton.getTriggerWidget().getElement().getStyle().setProperty("boxShadow", "none");
        menuButton.getTriggerWidget().getElement().getStyle().setTop(-5, Style.Unit.PX);
        menuButton.getTriggerWidget().getElement().getStyle().setHeight(14, Style.Unit.PX);
        menuButton.setRightDropdown(true);
        menuButton.getMenuWiget().getElement().getStyle().setMarginTop(-16, Style.Unit.PX);
        menuButton.getMenuWiget().getElement().getStyle().setProperty("minWidth", "50px");

//        menuButton.getTriggerWidget().addMouseDownHandler(new MouseDownHandler() {
//            @Override
//            public void onMouseDown(MouseDownEvent event) {
//                int test = menuButton.getMenuWiget().getElement().getAbsoluteTop() - annotationPanelt.getElement().getAbsoluteTop();
//                annotationPanelt.getElement().setScrollTop(test);
//            }
//        });

        menuColumn.add(menuButton);
    }

    public void startEditContent() {
        contentRow.remove(contentLabel);
        editTextBox = new TextBox();
        editTextBox.setText(content);
        editTextBox.setHeight("20px");
        editTextBox.setWidth("200px");
        editCancelButton = new Button(i18n.messagesPanel_editCancelButton());
        editCancelButton.setHeight("20px");
        editCancelButton.setWidth("40px");
        editSaveButton.setHeight("20px");
        editSaveButton.setWidth("40px");
        contentRow.add(editTextBox);
        contentRow.add(editCancelButton);
        contentRow.add(editSaveButton);


        {
            editCancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    contentRow.remove(editTextBox);
                    contentRow.remove(editCancelButton);
                    contentRow.remove(editSaveButton);
                    addContent(content);
                }
            });
        }
    }

    private void createMenuItems() {
        {
            editMenuItem = new NavLink(i18n.messagesMenuItem_edit());
            editMenuItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    startEditContent();
                }
            });
            menuButton.add(editMenuItem);
            if (!isOriginalPoster) {
                editMenuItem.setDisabled(true);
            }
        }
        {
            deleteMenuItem = new NavLink(i18n.messagesMenuItem_delete());
            deleteMenuItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    DeleteAnnotationEvent dae = new DeleteAnnotationEvent();
                    dae.setAnnotationCard(AnnotationCard.this);
                    fireEvent(dae);
                    annotationRow.removeFromParent();
                }
            });

            menuButton.add(deleteMenuItem);

            editSaveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    content = editTextBox.getText();
                    SaveEditAnnotationEvent seae = new SaveEditAnnotationEvent();
                    seae.setAnnotationCard(AnnotationCard.this);
                    fireEvent(seae);
                    contentRow.remove(editTextBox);
                    contentRow.remove(editCancelButton);
                    contentRow.remove(editSaveButton);

                    isEdited = true;
                    addContent(content);
//                    addEditLabel();
                }
            });
        }
    }

    public void addEditLabel() {
        if (!editLabelAdded) {
            editedLabel = new Label(content);
            editedLabel.getElement().getStyle().setProperty("fontFamily", "\"Helvetica Neue\",Helvetica,Arial,sans-serif");
            editedLabel.getElement().getStyle().setColor("#C0C0C0");
            editedLabel.getElement().getStyle().setBackgroundColor("white");
            editedLabel.getElement().getStyle().setFontWeight(Style.FontWeight.NORMAL);
            editedLabel.setText(" (" + i18n.messagesPanel_editedMessage() + ")");
            editLabelAdded = true;
            contentRow.add(editedLabel);
        }
    }

    public String getContent() {
        return content;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setReadOnly() {
        readOnly = true;
        menuButton.setVisible(false);

    }

    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public AnnotationCardDef getAnnotationCardDef() {
        return annotationCardDef;
    }

    public HandlerRegistration addDeleteHandler(DeleteAnnotationEventHandler handler) {
        return _handlerManager.addHandler(DeleteAnnotationEvent.type, handler);
    }

    public HandlerRegistration addSaveEditHandler(SaveEditAnnotationEventHandler handler) {
        return _handlerManager.addHandler(SaveEditAnnotationEvent.type, handler);
    }

}

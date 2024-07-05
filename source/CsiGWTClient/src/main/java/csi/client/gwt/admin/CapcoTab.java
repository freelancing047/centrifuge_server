package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.buttons.RedButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;

/**
 * Created by centrifuge on 5/14/2015.
 */
public class CapcoTab extends AdminTab {

    interface CapcoTabUiBinder extends UiBinder<Widget, CapcoTab> {
    }

    @UiField
    LayoutPanel container;

    @UiField
    GridContainer gridContainer;
    @UiField
    RadioButton searchCheckBox;
    @UiField
    TextBox searchTextBox;
    @UiField
    RadioButton allCheckBox;
    @UiField
    Label filterLabel;
    @UiField
    CsiStringListBox filterListBox;
    @UiField
    CyanButton getButton;
    @UiField
    BlueButton newButton;
    @UiField
    BlueButton editButton;
    @UiField
    RedButton deleteButton;
    @UiField
    HorizontalPanel topContainer;
    @UiField
    HorizontalPanel bottomContainer;
    @UiField
    CyanButton clearButton;
    @UiField
    RadioButton replaceButton;
    @UiField
    RadioButton augmentButton;
    @UiField
    Label itemsReturnedLabel;

    private static CapcoTabUiBinder uiBinder = GWT.create(CapcoTabUiBinder.class);

    private ClickHandler _externalHandler = null;

    CapcoTab(ClickHandler handlerIn) {
        super(3);
        add(uiBinder.createAndBindUi(this));
        _externalHandler = handlerIn;
        initialize();
    }

    @Override
    public TextBox getSearchTextBox() {
        return searchTextBox;
    }

    @Override
    public RadioButton getAllRadioButton() {
        return allCheckBox;
    }

    @Override
    public RadioButton getSearchRadioButton() {
        return searchCheckBox;
    }

    @Override
    public CyanButton getRetrievalButton() {
        return getButton;
    }

    @Override
    public BlueButton getNewButton() {
        return newButton;
    }

    @Override
    protected void initialize() {

        super.initialize();

        container.setWidgetTopHeight(topContainer, 0, Style.Unit.PX, 75, Style.Unit.PX);
        container.setWidgetTopBottom(gridContainer, 75, Style.Unit.PX, 60, Style.Unit.PX);
        container.setWidgetBottomHeight(bottomContainer, 0, Style.Unit.PX, 60, Style.Unit.PX);
    }

    @Override
    protected void wireInHandlers() {

        getButton.addClickHandler(_externalHandler);
        searchCheckBox.addClickHandler(radioClickHandler);
        allCheckBox.addClickHandler(radioClickHandler);
        searchTextBox.addKeyUpHandler(keyboardHandler);
        searchTextBox.addDropHandler(dropHandler);

        onShow();
    }

    public void resetFilterList() {

        filterListBox.setSelectedIndex(0);
    }
}

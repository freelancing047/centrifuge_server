package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.server.common.dto.user.UserSecurityInfo;

public class ReportsTab extends AdminTab {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ReportsTabUiBinder extends UiBinder<Widget, ReportsTab> {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    LayoutPanel container;
    @UiField
    GridContainer reportsContainer;
    @UiField
    GridContainer eventsContainer;
    @UiField
    HorizontalPanel topContainer;
    @UiField
    HorizontalPanel bottomContainer;
    @UiField
    Label maxConcurrencyLabel;

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static ReportsTabUiBinder uiBinder = GWT.create(ReportsTabUiBinder.class);

    private ClickHandler _externalHandler = null;

    UserSecurityInfo _userInfo;

    ReportsTab(UserSecurityInfo info, ClickHandler handler) {
        super(4);

        _userInfo = info;
        add(uiBinder.createAndBindUi(this));
        _externalHandler = handler;
        initialize();
    }

    @Override
    public TextBox getSearchTextBox() { return new TextBox(); }
    @Override
    public RadioButton getAllRadioButton() { return new RadioButton(""); }
    @Override
    public RadioButton getSearchRadioButton() { return new RadioButton(""); }
    @Override
    public CyanButton getRetrievalButton() { return new CyanButton(""); }
    @Override
    public BlueButton getNewButton() { return new BlueButton(); }

    @Override
    protected void initialize() {
        super.initialize();

        container.setWidgetTopHeight(topContainer, 0, Style.Unit.PX, 60, Style.Unit.PX);
        container.setWidgetTopHeight(bottomContainer, 60, Style.Unit.PX, 200, Style.Unit.PX);

        container.setWidgetTopBottom(reportsContainer, 40, Style.Unit.PX, 60, Style.Unit.PX);
        container.setWidgetTopBottom(eventsContainer, 100, Style.Unit.PX, 60, Style.Unit.PX);

    }

    @Override
    protected void wireInHandlers() { }

}

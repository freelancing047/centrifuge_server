package csi.client.gwt.mainapp;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavHeader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.etc.ApplicationInjector;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.theme.editor.ThemeEditorManager;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.model.dataview.DataView;

abstract public class AbstractApplicationToolbar extends Composite {
    public static final ApplicationInjector injector = GWT.create(ApplicationInjector.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField
    protected Button tMessengerButton;

    MessagesDialog messagesDialog;

    @UiField
    NavHeader header;
    protected AbstractDataViewPresenter dataViewPresenter;
    protected ThemeEditorPresenter themeEditorPresenter = new ThemeEditorPresenter();
    ClickHandler editThemeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                ThemeEditorManager themeManager = new ThemeEditorManager(themeEditorPresenter, dataViewPresenter);
                themeManager.show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };

    private ImageResource logo;

    public void finalizeMenus(UserSecurityInfo userInfoIn) {}

    protected abstract void createMainCentrifugeDropdown(UserSecurityInfo userInfoIn);

    protected void createMessagesDialog(DataView dataViewIn) {
        messagesDialog = new MessagesDialog(dataViewIn, dataViewPresenter);
    }

    public void enableMessagesMenus(DataView dataViewIn) {
        if (null != dataViewIn) {
            createMessagesDialog(dataViewIn);
        }
    }

    public void toggleMessagesMenu(MainView.Mode mode) {
        if (!(mode.equals(MainView.Mode.ANALYSIS))) {
            tMessengerButton.setVisible(false);
        } else {
            tMessengerButton.setVisible(true);
        }
    }

    public void setHeaderText(String textIn) {
        header.setText(textIn);
    }

    public abstract void abortDataView();

    public abstract void enableMenus();

    public abstract void enableDataMenus(DataView dataViewIn);

    public abstract void disableMenus();

    public abstract void setDataView(AbstractDataViewPresenter presenter);

    public abstract void setNewMessageAlertVisible(boolean b);
}

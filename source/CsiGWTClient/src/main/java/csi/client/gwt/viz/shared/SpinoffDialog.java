package csi.client.gwt.viz.shared;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewInNewTab;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.dto.Response;
import csi.server.common.dto.SpinoffRequestV2;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.linkup.LinkupResponse;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.DataViewActionServiceProtocol;

public class SpinoffDialog {

    Dialog dialog;
    private String dataviewUuid;
    private String vizUUID;
    private TextBox spinoffNameTextBox;
    private CheckBox newWindowCheckbox;
    private Label helpTextLabel = new Label();

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public SpinoffDialog(final String dataviewUuid, final String vizUUID, final Selection selection) {
        this.dataviewUuid = dataviewUuid;
        this.vizUUID = vizUUID;
        dialog = new Dialog();
        dialog.add(new Label(i18n.spinoffDialog_dataviewNameLabel()));
        spinoffNameTextBox = new TextBox();
        spinoffNameTextBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                spinoffNameTextBox.getElement().getStyle().setColor(null);
                helpTextLabel.setVisible(false);

            }
        });
        dialog.hideOnCancel();
        dialog.add(spinoffNameTextBox);
        dialog.setTitle(i18n.spinoffDialog_createSpinoff());

        helpTextLabel.getElement().getStyle().setColor("red");
        helpTextLabel.setVisible(false);
        dialog.add(helpTextLabel);
        newWindowCheckbox = new CheckBox();
        newWindowCheckbox.setText(i18n.spinoffDialog_openSpinoffNow());
        newWindowCheckbox.setValue(true);

        Button actionButton = dialog.getActionButton();
        actionButton.setText(i18n.spinoffDialog_spinoffButton());
        actionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final String spinoffName = spinoffNameTextBox.getValue();
                // validate new DV name

                VortexFuture<Boolean> nameFuture = WebMain.injector.getVortex().createFuture();
                try {
                    nameFuture.execute(DataViewActionServiceProtocol.class).dataviewNameExists(spinoffName);
                } catch (CentrifugeException e1) {
                    e1.printStackTrace();
                }

                nameFuture.addEventHandler(new AbstractVortexEventHandler<Boolean>() {

                    @Override
                    public boolean onError(Throwable exceptionIn) {
                        Dialog.showException(i18n.serverMessage_CaughtException(), exceptionIn);
                        return true;
                    }
                    @Override
                    public void onSuccess(Boolean result) {
                        if (spinoffNameTextBox.getValue() == null || spinoffNameTextBox.getValue().equals("")) {
                            helpTextLabel.setText(i18n.spinoffDialog_dataviewMustHaveName());
                            helpTextLabel.setVisible(true);
                        } else if (result) {
                            spinoffNameTextBox.getElement().getStyle().setColor("#FF0000");
                            helpTextLabel.setText(i18n.spinoffDialog_dataviewNameExists());
                            helpTextLabel.setVisible(true);
                        } else {
                            doSpinoff(selection);
                            dialog.hide();
                        }
                    }
                });
            }
        });
    }

    public void show() {
        dialog.show();
    }

    private void doSpinoff(Selection selection) {
        final MaskDialog mask = new MaskDialog(i18n.spinoffDialog_creatingSpinoff());
        mask.show();
        String spinoffName = spinoffNameTextBox.getValue();
        SpinoffRequestV2 request = new SpinoffRequestV2();
        request.setSelection(selection);
        request.setDataViewUuid(dataviewUuid);
        request.setVisualizationUuid(vizUUID);
        VortexFuture<Response<String, DataView>> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Response<String, DataView>>() {

            @Override
            public void onSuccess(Response<String, DataView> resultIn) {

                mask.hide();
                if(resultIn.isSuccess()){

                    DataView myDataView = resultIn.getResult();
                    List<ButtonDef> buttonsIn = Lists.newArrayList();

                    buttonsIn.add(new ButtonDef(i18n.dialog_NoButton()));
                    buttonsIn.add(new ButtonDef(i18n.dialog_YesButton()));
                    ChoiceMadeEventHandler handlerIn = new ChoiceMadeEventHandler() {

                        @Override
                        public void onChoiceMade(ChoiceMadeEvent event) {

                            int i = event.getChoice();

                            if (i == 2) {

                                DataViewInNewTab.open(myDataView.getUuid(), myDataView.getName());
                            }
                        }
                    };
                    DecisionDialog decisionDialog = new DecisionDialog(i18n.spinoffDialog_openSpinoffTitle(),
                                                                        i18n.spinoffDialog_openSpinoffMessage(),
                                                                        buttonsIn, handlerIn, 40);
                    decisionDialog.getCancelButton().setVisible(false);
                    decisionDialog.show();

                } else {

                    Display.error("SpinoffDialog 1", resultIn.getException());
                }
            }

            @Override
            public boolean onError(Throwable myException) {
                
                mask.hide();
                Display.error("SpinoffDialog", 2, myException);
                return true;
            }
        });
        try {

            future.execute(DataViewActionServiceProtocol.class).spinoff(request, spinoffName);

        } catch (CentrifugeException myException) {

            mask.hide();
            Display.error("SpinoffDialog", 3, myException);
            myException.printStackTrace();
        }
    }
}

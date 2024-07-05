package csi.client.gwt.mainapp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.system.ReleaseInfo;

@SuppressWarnings("static-access")
public class HelpTopicsDialog{

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private static String HELP_TOPICS = null;

    @UiField
    Dialog dialog;
    
    @UiField
    VerticalPanel verticalPanel;

    private static HelpTopicsDialogUiBinder uiBinder = GWT
            .create(HelpTopicsDialogUiBinder.class);

    interface HelpTopicsDialogUiBinder extends
            UiBinder<Widget, HelpTopicsDialog> {
    }

    public HelpTopicsDialog() {

        try {

            uiBinder.createAndBindUi(this);

            buildUi();

        } catch (Exception myException) {

            Dialog.showException("HelpTopicsDialog", myException);
        }
    }

    private void buildUi() {

        dialog.getCancelButton().setText(Dialog.txtCloseButton);
        dialog.hideOnCancel();
        dialog.getActionButton().setEnabled(false);
        dialog.getActionButton().setVisible(false);
        dialog.hideOnAction();

        verticalPanel.add(createTitle());
        verticalPanel.add(createHelp());
        verticalPanel.add(createContacts());

    }

    private Panel createContacts() {
        
        Element ul = DOM.createElement("ul");
        
        addEmail(ul);
        addWebsite(ul);
        addPhone(ul);
        //addGettingStarted(ul);
        //addTutorial(ul);
        
        SimplePanel simplePanel = new SimplePanel();
        simplePanel.getElement().appendChild(ul);
        return simplePanel;
    }

    private void addEmail(Element ul) {
        Element li = DOM.createElement("li");
        li.setInnerText(i18n.Help_Analytics_Dialog_Email());
        ul.appendChild(li);
    }
    
    private void addWebsite(Element ul) {
        Element li = DOM.createElement("li");
        String label = i18n.Help_Analytics_Dialog_Website_Label();
        Element a = DOM.createAnchor();
        a.setInnerText(i18n.Help_Analytics_Dialog_Website_Value());
        a.setAttribute("href", WebMain.getClientStartupInfo().getExternalLinkConfig().getCentrifugeCompanyHomeUrl());
        a.setAttribute("target", "_blank");
        Element span = DOM.createSpan();
        span.setInnerHTML(label);
        span.appendChild(a);

        
        li.appendChild(span);
        ul.appendChild(li);
    }
    
    private void addPhone(Element ul) {
        Element li = DOM.createElement("li");
        li.setInnerText(i18n.Help_Analytics_Dialog_Phone());
        ul.appendChild(li);
    }
    
    private void addGettingStarted(Element ul) {
        Element li = DOM.createElement("li");
        Element a = DOM.createAnchor();
        
        a.setInnerText(i18n.Help_Analytics_Dialog_Getting_Started_Text());
        a.setAttribute("href", WebMain.getClientStartupInfo().getExternalLinkConfig().getGettingStartedGuideUrl());
        a.setAttribute("target", "_blank");
        li.appendChild(a);
        ul.appendChild(li);
    }
    
    private void addTutorial(Element ul) {
        Element li = DOM.createElement("li");
        Element a = DOM.createAnchor();
        
        a.setInnerText(i18n.Help_Analytics_Dialog_Tutorial_Text());
        a.setAttribute("href", WebMain.getClientStartupInfo().getExternalLinkConfig().getTutorialUrl());
        a.setAttribute("target", "_blank");
        li.appendChild(a);
        ul.appendChild(li);
    }

    private Widget createHelp() {

        HTML html = new HTML(i18n.Help_Analytics_Dialog_Info());

        return html;
    }

    private Label createTitle() {
        Label label = new Label(getHelpTopics());
        label.getElement().getStyle().setFontSize(16, Style.Unit.PX);
        label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLDER);
        label.setHeight("40px");
        return label;
    }

    public void show(){
        dialog.show();
    }

    private String getHelpTopics() {

        if (null == HELP_TOPICS) {

            HELP_TOPICS = i18n.Help_Analytics_Dialog_Title(ReleaseInfo.version);
        }
        return HELP_TOPICS;
    }
}

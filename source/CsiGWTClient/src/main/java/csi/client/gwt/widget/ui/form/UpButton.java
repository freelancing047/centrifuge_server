package csi.client.gwt.widget.ui.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.FlowPanel;
import csi.client.gwt.resources.SortResource;

/**
 * Created by centrifuge on 10/11/2018.
 */
public class UpButton extends FlowPanel {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"text-align: left;display:inline;\"><img src=\"{0}\"></img></div>")
        SafeHtml templateOrder(SafeUri uri);
    }

    private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    private ClickHandler _handler = null;
    private UpButton _this;

    public UpButton(ClickHandler handlerIn) {
        super();
        _this = this;
        _handler = handlerIn;
        getElement().setInnerSafeHtml(cellTemplate.templateOrder(SortResource.IMPL.upButtonIcon().getSafeUri()));
        this.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
                if (null != _handler) {

                    _handler.onClick(null);
                }
                DeferredCommand.add(new Command() {
                    public void execute() {
                        DeferredCommand.add(new Command() {
                            public void execute() {
                                DeferredCommand.add(new Command() {
                                    public void execute() {
                                        _this.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, ClickEvent.getType());
    }
}

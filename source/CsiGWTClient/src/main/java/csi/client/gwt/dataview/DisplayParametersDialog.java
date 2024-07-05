package csi.client.gwt.dataview;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.FieldDefResource;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ScrollingDialog;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.query.QueryParameterDef;

import java.util.List;

/**
 * Created by centrifuge on 2/11/2019.
 */
public class DisplayParametersDialog extends ScrollingDialog {

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private CheckBox limitParameters;

    private AbstractDataViewPresenter _parent;
    private boolean _displayNulls = true;

    public DisplayParametersDialog(AbstractDataViewPresenter parentIn) {

        super("Parameter Values", true);

        _parent = parentIn;

        limitParameters = new CheckBox("Show non-null parameters only.");
        limitParameters.setValue(!_displayNulls);
        limitParameters.addClickHandler(handleNullProcessingClick);
        addLeftControl(limitParameters);
        getCancelButton().setText(Dialog.txtCloseButton);
        hideOnCancel();
        getActionButton().setEnabled(false);
        getActionButton().setVisible(false);
        hideOnAction();

        handleNullProcessingClick.onClick(null);
    }

    private ClickHandler handleNullProcessingClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            DataView myDataView = _parent.getDataView();
            List<QueryParameterDef> myList = myDataView.getParameterList();

            _displayNulls = !limitParameters.getValue();
            clearPanel();

            for (QueryParameterDef myParameter : myList) {

                String myParameterName = myParameter.getName();
                CsiDataType myDataType = myParameter.getType();
                List<String> myValues = myParameter.getValues();
                HorizontalPanel myPanel = new HorizontalPanel();
                ImageResource myImageResource = FieldDefUtils.getDataTypeImage(myDataType);
                Image myImage = new Image(myImageResource);
                SimplePanel myIcon = new SimplePanel(myImage);
                InlineLabel myName = new InlineLabel(myParameterName);
                InlineLabel myEquals = new InlineLabel(" = ");
                InlineLabel myValue = null;

                if ((null != myValues) && (0 < myValues.size())) {

                    if (1 < myValues.size()) {

                        StringBuilder myBuffer = new StringBuilder();

                        myBuffer.append('{');
                        for (int i = 0; myValues.size() > i; i++) {

                            myBuffer.append(formatValue(myValues.get(i), myDataType));
                            myBuffer.append(", ");
                        }
                        myBuffer.setLength(myBuffer.length() - 2);
                        myBuffer.append('}');

                        myValue = new InlineLabel(myBuffer.toString());

                    } else {

                        myValue = new InlineLabel(formatValue(myValues.get(0), myDataType));
                    }
                }
                if ((null == myValue) && _displayNulls) {

                    myValue = new InlineLabel("");
                }
                if (null != myValue) {

                    myIcon.setPixelSize(20,20);
                    myName.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
                    myEquals.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
                    myValue.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);

                    myPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
                    myPanel.add(myIcon);
                    myPanel.add(myName);
                    myPanel.add(myEquals);
                    myPanel.add(myValue);
                    addWidget(myPanel);
                }
            }
        }
    };

    private String formatValue(String valueIn, CsiDataType dataTypeIn) {

        String myValue;

        if (CsiDataType.Number.equals(dataTypeIn)
                || CsiDataType.Integer.equals(dataTypeIn)
                || CsiDataType.Boolean.equals(dataTypeIn)) {

            myValue = valueIn;

        } else {

            myValue = "\"" + valueIn + "\"";
        }
        return myValue;
    }
}

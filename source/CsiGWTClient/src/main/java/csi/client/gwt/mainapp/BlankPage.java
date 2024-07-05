package csi.client.gwt.mainapp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.dto.user.UserSecurityInfo;

/**
 * Created by centrifuge on 5/22/2019.
 */
public class BlankPage extends Composite implements CsiLandingPage {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface MyUiBinder extends UiBinder<Widget, BlankPage> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Widget widget = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);



    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public BlankPage() {

    }

    @Override
    public Widget asWidget() {

        if (null == widget) {

            widget = uiBinder.createAndBindUi(this);
        }
        return widget;
    }

    @Override
    public void saveState() {

    }

    @Override
    public void restoreState() {

    }

    @Override
    public void forceExit() {

    }

    @Override
    public void finalizeWidget(UserSecurityInfo userSecurityInfoIn) {

    }

    @Override
    public void reloadData() {

    }
}

/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.util;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.TextInputDialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DownloadHelper {

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final int LENGTH_OF_H5 = 4;
    private static final String H5_SERVLET_NAME = "h5/"; //$NON-NLS-1$
    private ClickHandler _cancelCallBack = null;
    private TextInputDialog _dialog = null;
    private String _underscoredFilename = null;
    private String _filenameWithDateAppended = null;
    private String _fullFilename = null;
    private String _hostUrl = null;
    private String _token = null;

    private ClickHandler executeDownload = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            _fullFilename = _dialog.getResult();

            WebMain.getDownloadFrame().setUrl(_hostUrl + "download?filename=" + _fullFilename + "&token=" + _token);
            _dialog.destroy();
        }
    };

    private ClickHandler cancelDownload = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if (null != _cancelCallBack) {

                _cancelCallBack.onClick(eventIn);
            }
            _dialog.destroy();
        }
    };

    public DownloadHelper(String filenameIn, String suffixIn, String tokenIn) {

        _underscoredFilename = ((null != filenameIn) && (0 < filenameIn.length()))
                                        ?  filenameIn.replaceAll(" ", "_") : "Collection";
        _filenameWithDateAppended = _underscoredFilename;
        _fullFilename = URL.encode(_filenameWithDateAppended + suffixIn);
        _hostUrl = hostPageURLWithoutH5();
        _token = tokenIn;
    }

    public void execute(ClickHandler cancelCallBackIn) {

        _dialog = new TextInputDialog(_constants.downloadHelper_CallbackTitle(), _constants.downloadHelper_CallbackPrompt(),
                                        _fullFilename, executeDownload, cancelDownload);
        _cancelCallBack = cancelCallBackIn;
        _dialog.show();
    }


    // This does the name
    public static void download(String filename, String suffix, String token) {
        String underscoredFilename = filename.replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        String filenameWithDateAppended = underscoredFilename;
        String fullFilename = URL.encode(filenameWithDateAppended + suffix);
        String hostUrl = hostPageURLWithoutH5();
        WebMain.getDownloadFrame().setUrl(hostUrl + "download?filename=" + fullFilename + "&token=" + token); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String hostPageURLWithoutH5() {
        String hostUrl = GWT.getHostPageBaseURL();

        if(hostUrl.endsWith(H5_SERVLET_NAME)){
            hostUrl = hostUrl.substring(0, hostUrl.length()- LENGTH_OF_H5) + "/"; //$NON-NLS-1$
        }
        return hostUrl;
    }
}

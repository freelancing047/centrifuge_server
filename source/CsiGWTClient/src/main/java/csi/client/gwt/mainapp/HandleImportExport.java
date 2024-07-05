package csi.client.gwt.mainapp;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.csi_resource.ExportDialog;
import csi.client.gwt.csi_resource.ImportDialog;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Created by Ivan on 10/2/2017.
 */
public class HandleImportExport {

    ClickHandler importClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ImportDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };

    ClickHandler exportClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            try {

                (new ExportDialog()).show();

            } catch (Exception myException) {

                Dialog.showException("ApplicationToolbar", myException);
            }
        }
    };
}

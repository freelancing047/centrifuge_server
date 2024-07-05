package csi.client.gwt.widget.ui.uploader.wizards.components;

import org.vectomatic.file.FileUploadExt;

/**
 * Created by centrifuge on 7/2/2015.
 */
public class CsiFileSelector extends FileUploadExt {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiFileSelector() {
        super(true);
    }

    public CsiFileSelector(boolean multiple, boolean isVisibleIn) {
        super(multiple);
        super.setVisible(isVisibleIn);
    }

    public void activate() {

        super.click();
    }
}

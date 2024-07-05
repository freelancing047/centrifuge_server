package csi.client.gwt.widget.ui.uploader;

import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 10/26/2015.
 */
public class UnsupportedUpload extends CentrifugeException {

    public UnsupportedUpload() {

        super();
    }

    public UnsupportedUpload(String reasonIn) {

        super(reasonIn);
    }
}

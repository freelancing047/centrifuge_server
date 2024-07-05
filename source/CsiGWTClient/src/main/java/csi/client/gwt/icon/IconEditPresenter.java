package csi.client.gwt.icon;

import csi.client.gwt.util.Display;
import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.icon.ui.IconSelector;
import csi.client.gwt.icon.ui.IconUtil;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.IconActionsServiceProtocol;

public class IconEditPresenter {
    private static final double MAX_IMAGE_SIZE = 1024 * 24;

    private IconSelector panel;

    private VortexEventHandler<String> handler;

    public AbsolutePanel getView(boolean editAccess) {
        if (panel == null) {
            try {
                panel = new IconSelector();
                if (editAccess) {
                    panel.addSelectionHandler(handleFileSelection);
                    panel.enable();
                } else {
                    panel.disable();
                }
            } catch (CentrifugeException e) {

                Display.error(e);
            }
        }
        return panel.getView();
    }

    private ChoiceMadeEventHandler handleFileSelection = new ChoiceMadeEventHandler() {

        public void onChoiceMade(ChoiceMadeEvent eventIn) {
            File file = panel.getFile();
            final FileReader fileReader = new FileReader();

            if (null != file) {
                long size = file.getSize();

                Blob myBlock = file.slice(0L, size);
                if (verifySize(size)) {
                    fileReader.readAsDataURL(myBlock);

                    fileReader.addLoadEndHandler(event -> {
                        String base64 = fileReader.getStringResult();
                        Image image = new Image();
                        image.setUrl(base64);

                        verifyDimensions(image);

                        if (base64.length() > 0) {
                            editIcon(base64);
                        }
                    });
                } else {
                    ErrorDialog dialog = new ErrorDialog("Image is too large to upload");
                    dialog.show();
                }
            }
        }
    };

    private String uuid;

    private boolean verifySize(long size) {
        return !(size > MAX_IMAGE_SIZE);
    }

    private void verifyDimensions(Image image) {
//        int width = image.getElement().getPropertyInt("naturalWidth");
//        int height = image.getElement().getPropertyInt("naturalHeight");
    }

    private void editIcon(String base64) {
        VortexFuture<String> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            if (handler != null) {
                myVortexFuture.addEventHandler(handler);
            }
            myVortexFuture.execute(IconActionsServiceProtocol.class).editIconData(base64, uuid);
        } catch (Exception myException) {

            Display.error(myException);
        }
    }

    public void setLoadHandler(VortexEventHandler<String> handler) {
        this.handler = handler;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void attach(Image image, boolean editAccess) {
        IconUtil.addImage(getView(editAccess), image);
        image.addClickHandler(panel.getHandleSelectButtonClick());
    }

}

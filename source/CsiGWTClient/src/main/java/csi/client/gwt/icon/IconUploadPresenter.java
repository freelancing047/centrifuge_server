package csi.client.gwt.icon;

import csi.client.gwt.util.Display;
import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.icon.ui.IconSelectorButton;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.IconActionsServiceProtocol;

public class IconUploadPresenter {
    
    //private static final double MAX_IMAGE_SIZE = IconActionsService.MAX_IMAGE_SIZE;

    private IconSelectorButton button;

    VortexEventHandler<String> handler;
    
    protected void cancelUpload() {
//
//        removeFile(_serverFileName);
//        resetDataValues();
    }
    
    public Panel getView(){
        if(button == null){
            try {
                button = new IconSelectorButton();
                button.addSelectionHandler(handleFileSelection);
            } catch (CentrifugeException e) {

                Display.error(e);
            }
        }
        return button.getView();
    }
    
    private ChoiceMadeEventHandler handleFileSelection = new ChoiceMadeEventHandler() {

        public void onChoiceMade(ChoiceMadeEvent eventin) {
        
            eventin.getChoice();
        
            File file = button.getFile();
            final FileReader fileReader = new FileReader();
            
            if (null != file) {
        
                long size = file.getSize();
                final String fileName = file.getName();

                Blob myBlock = file.slice(0L, size);
                if(true){//verifySize(size)){
                    fileReader.readAsDataURL(myBlock);
                    
                    fileReader.addLoadEndHandler(new LoadEndHandler(){
    
                        @Override
                        public void onLoadEnd(LoadEndEvent event) {
                            String base64 = fileReader.getStringResult();
                            Image image = new Image();
                            image.setUrl(base64);
                            
                            verifyDimensions(image);
                            
                            if(base64 != null && base64.length() > 0){
                                uploadIcon(base64, fileName);
                            }
                        }
                    });
                } else {
                    Display.error("Image is too large to upload");
                }
            }
        }
    };
//
//    private boolean verifySize(long size) {
//        if(size > MAX_IMAGE_SIZE){    
//            return false;
//        }
//        return true;
//    }
    
    private void verifyDimensions(Image image) {

        int width = image.getElement().getPropertyInt("naturalWidth");
        int height = image.getElement().getPropertyInt("naturalHeight");
        
    }
    
    protected void uploadIcon(String base64, String fileName) {
        VortexFuture<String> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            if(handler != null){
                myVortexFuture.addEventHandler(handler);
            }
            myVortexFuture.execute(IconActionsServiceProtocol.class).uploadIcon(base64, fileName);
        } catch (Exception myException) {

            Display.error(myException);
        }
    }
    
    public void setLoadHandler(VortexEventHandler<String> handler){
        this.handler = handler;
    }

}

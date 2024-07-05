package csi.client.gwt.widget.buttons;

import java.util.List;
import java.util.Map;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.server.common.dto.SelectionListData.SelectorBasics;

/**
 * Created by centrifuge on 7/7/2015.
 */
public interface CsiBrowseButton<T extends SelectorBasics> {

    public void setPixelSize(int widthIn, int heightIn);
    public void setEnabled(boolean enabledIn);
    public HandlerRegistration addTransferCompleteEventHandler(TransferCompleteEventHandler handleUploadCompleteIn);
    public void showSuccessPopup(boolean showIn);
    public void setMaxFileSize(Long sizeIn);
    public void initialize(List<Map<String, T>> rejectionListIn, String fileTypesIn);
    public Widget asWidget();
}

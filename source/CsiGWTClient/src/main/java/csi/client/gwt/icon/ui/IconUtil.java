package csi.client.gwt.icon.ui;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class IconUtil {
    public static void addImage(AbsolutePanel panel, Image image) {
        int offsetWidth = panel.getOffsetWidth();
        int offsetHeight = panel.getOffsetHeight();
        int naturalWidth = image.getElement().getPropertyInt("naturalWidth");
        int naturalHeight = image.getElement().getPropertyInt("naturalHeight");
        int newWidth = offsetWidth;
        int newHeight = offsetHeight;
        if (naturalWidth > offsetWidth || naturalHeight > offsetHeight) {
            if (naturalWidth > naturalHeight) {
                newHeight = naturalHeight * offsetWidth / naturalWidth;
            } else {
                newWidth = naturalWidth * offsetHeight / naturalHeight;
            }
            image.setPixelSize(newWidth, newHeight);
        }
        int left = 0;
        int top = 0;
        if (newHeight < offsetHeight) {
            top = (offsetHeight - newHeight) / 2;
        }
        if (newWidth < offsetWidth) {
            left = (offsetWidth - newWidth) / 2;
        }
        panel.add(image, left, top);
    }
}

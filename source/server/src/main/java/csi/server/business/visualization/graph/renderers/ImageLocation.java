package csi.server.business.visualization.graph.renderers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Created by Patrick on 5/7/2014.
 */
public class ImageLocation {

    private String imageLocationString;

    public ImageLocation(String icon) {
        imageLocationString = getImageLocationString(icon);
    }
    private String getImageLocationString(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        try {
            File f = new File("webapps" + url.replace('\\', '/'));
            return f.getCanonicalFile().toURL().toString();
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public Image read() {

        try {
            BufferedImage image = ImageIO.read( new URL(imageLocationString));
            return image;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return imageLocationString;
    }
}

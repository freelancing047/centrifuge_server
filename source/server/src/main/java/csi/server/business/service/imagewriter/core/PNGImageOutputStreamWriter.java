package csi.server.business.service.imagewriter.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import csi.server.business.service.imagewriter.api.ImageOutputStreamWriter;

public class PNGImageOutputStreamWriter implements ImageOutputStreamWriter {

    public void writeImage(BufferedImage img, OutputStream outputStream) throws IOException {
        ImageIO.write(img, "PNG", outputStream);
    }
}

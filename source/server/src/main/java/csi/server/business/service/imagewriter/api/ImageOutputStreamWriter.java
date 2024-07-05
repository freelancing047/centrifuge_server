package csi.server.business.service.imagewriter.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public interface ImageOutputStreamWriter {

    void writeImage(BufferedImage img, OutputStream outputStream) throws IOException;
}

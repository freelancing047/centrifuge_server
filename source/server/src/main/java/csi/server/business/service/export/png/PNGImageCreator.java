package csi.server.business.service.export.png;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import csi.server.util.ImageUtil;
import csi.shared.core.imaging.ImageComponent;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.imaging.SVGImageComponent;
import org.w3c.dom.DOMException;

/**
 * Creates a PNG image.
 * @author Centrifuge Systems, Inc.
 */
public class PNGImageCreator {

    public BufferedImage createImage(ImagingRequest request) {
        BufferedImage finalImage = new BufferedImage(request.getWidth(), request.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = initializeGraphics(request, finalImage);
        for (ImageComponent component : request.getComponents()) {
            drawImageComponent(graphics, component);
        }
        graphics.dispose();
        return finalImage;
    }

    private void drawImageComponent(Graphics2D graphics, ImageComponent component) {
        if (component instanceof SVGImageComponent) {
            drawSVGImage(graphics, component);
        } else if (component instanceof PNGImageComponent) {
            drawPNGImage(graphics, component);
        }
    }

    private void drawPNGImage(Graphics2D graphics, ImageComponent component) {
        if (component.getWidth() == 0 || component.getHeight() == 0) {
            // just fit the image at full scale.
            graphics.drawImage(ImageUtil.fromBase64String(component.getData()), component.getX(),
                    component.getY(), null);
        } else {
            graphics.drawImage(ImageUtil.fromBase64String(component.getData()), component.getX(),
                    component.getY(), component.getWidth(), component.getHeight(), null);
        }
    }

    private void drawSVGImage(Graphics2D graphics, ImageComponent component) {
        BufferedImage svgImage = createImageFromSVG((SVGImageComponent) component);
        graphics.drawImage(svgImage, component.getX(), component.getY(), null);
    }

    private Graphics2D initializeGraphics(ImagingRequest request, BufferedImage finalImage) {
        Graphics2D graphics = finalImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, request.getWidth(), request.getHeight());
        return graphics;
    }


    // BE CAREFUL, tested with fill/stroke as attribute
    private String replaceRGBAWithRGBandOpacity(String source, String attribute){
        Pattern rgbaRegex = Pattern.compile(attribute+"=\"rgba\\((\\s*\\d*),(\\s*\\d*),(\\s*\\d*),(\\s*\\d*.\\d*)\\)\"");
        String cString = source;
        Matcher rgbaMatcher = rgbaRegex.matcher(cString);

        while(rgbaMatcher.find()) {
            // gets the full matched string
            String fullRGBA= rgbaMatcher.group(0);

            // lets build the rgb
            String rgbString = attribute + "=\"rgb(" + rgbaMatcher.group(1) + "," + rgbaMatcher.group(2) +","+ rgbaMatcher.group(3) + ")\"";
            String opacityValue = rgbaMatcher.group(4) == null ? "1" : rgbaMatcher.group(4);
            String attributeOpacity = " " + attribute + "-opacity=\"" + opacityValue + "\"";

            String newProp = rgbString + attributeOpacity;
            cString = cString.replace(fullRGBA, newProp);
        }
        return cString;
    }

    private BufferedImage createImageFromSVG(SVGImageComponent component) {
        String cString = component.getData();
        String noBadFill = replaceRGBAWithRGBandOpacity(cString, "fill");
        String noBadStroke = replaceRGBAWithRGBandOpacity(noBadFill, "stroke");


        Reader reader = new BufferedReader(new StringReader(noBadStroke));
        TranscoderInput svgImage = new TranscoderInput(reader);
        BufferedImageTranscoder transcoder = transcodeImage(component, svgImage);

        return transcoder.getImage();
    }

    private BufferedImageTranscoder transcodeImage(SVGImageComponent component, TranscoderInput svgImage) {
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) component.getWidth());
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) component.getHeight());
        try {
            transcoder.transcode(svgImage, null);
        } catch (TranscoderException e) {
        }
        return transcoder;
    }
}

package csi.server.business.visualization.graph.renderers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;

import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.ImageFactory;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.optionset.OptionSetManager;

/**
 * @author Centrifuge Systems, Inc.
 */
public class PlunkedNodeDrawer {
   private static final int ORIGINAL_ICON_SIZE = 105;
   private static final double ICON_SIZE = 15d;
   private static final double NODE_POSITIONING_SCALE_FACTOR = 8d;
   private final ImageFactory imageFactory;

   public PlunkedNodeDrawer(ImageFactory imageFactory) {
      this.imageFactory = imageFactory;
   }

   public void renderIconIfPlunked(Graphics2D graphics, VisualItem item, NodeStore details) {
      if (details.isPlunked()) {
         ImageLocation imageLocation = new ImageLocation(OptionSetManager.toResourceUrl("/plunked.png"));
         Image image = imageFactory.getImage(imageLocation, ORIGINAL_ICON_SIZE, ORIGINAL_ICON_SIZE);
         int desiredSize = (int) (ICON_SIZE * item.getSize());
         BufferedImage resizedImage = new BufferedImage(desiredSize, desiredSize, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = resizedImage.createGraphics();

         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (details.getTransparency() / 100D)));
         g.drawImage(image, 0, 0, desiredSize, desiredSize, null);
         g.dispose();
         renderImage(graphics, resizedImage, item);
      }
   }

   public void renderIconIfPlunked(Graphics2D graphics, VisualItem item, LinkStore details) {
      boolean multiType = (details.getTypes() != null) && (details.getTypes().size() > 1);

      if (multiType) {
         ImageLocation imageLocation = new ImageLocation(OptionSetManager.toResourceUrl("/plunked.png"));
         Image image = imageFactory.getImage(imageLocation, ORIGINAL_ICON_SIZE, ORIGINAL_ICON_SIZE);
         int desiredSize = (int) (ICON_SIZE * item.getSize());
         BufferedImage resizedImage = new BufferedImage(desiredSize, desiredSize, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = resizedImage.createGraphics();

         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(details.getTransparency()/100d)));
         g.drawImage(image, 0, 0, desiredSize, desiredSize, null);
         g.dispose();
         renderEdgeImage(graphics, resizedImage, item);
      }
   }

   private static void renderImage(Graphics2D graphics, BufferedImage buffImg, VisualItem item) {
      double height = buffImg.getHeight(null);
      double width = buffImg.getWidth(null);

      if ((BigDecimal.valueOf(height).compareTo(BigDecimal.ZERO) != 0) &&
          (BigDecimal.valueOf(width).compareTo(BigDecimal.ZERO) != 0)) {
         double nodeSize = item.getSize() * NodeRenderer.DEFAULT_BASE_SIZE;
         double itemCenterPositionX = item.getX();
         double itemCenterPositionY = item.getY();
         double imageTopLeftX = itemCenterPositionX + (nodeSize / NODE_POSITIONING_SCALE_FACTOR);
         double imageTopLeftY = itemCenterPositionY + (nodeSize / NODE_POSITIONING_SCALE_FACTOR);

         graphics.drawImage(buffImg, null, (int) imageTopLeftX, (int) imageTopLeftY);
      }
   }

   private static void renderEdgeImage(Graphics2D graphics, BufferedImage buffImg, VisualItem item) {
      double height = buffImg.getHeight(null);
      double width = buffImg.getWidth(null);

      if ((BigDecimal.valueOf(height).compareTo(BigDecimal.ZERO) != 0) &&
          (BigDecimal.valueOf(width).compareTo(BigDecimal.ZERO) != 0)) {
         double nodeSize = item.getSize() * NodeRenderer.DEFAULT_BASE_SIZE;
         double itemCenterPositionX = item.getX();
         double itemCenterPositionY = item.getY();
         double imageTopLeftX = itemCenterPositionX + (nodeSize / NODE_POSITIONING_SCALE_FACTOR);
         double imageTopLeftY = itemCenterPositionY + (nodeSize / NODE_POSITIONING_SCALE_FACTOR);

         graphics.drawImage(buffImg, null, (int) imageTopLeftX, (int) imageTopLeftY);
      }
   }
}

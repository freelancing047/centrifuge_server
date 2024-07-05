package csi.server.business.visualization.viewer.lens;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.util.GraphicsLib;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.viewer.dto.ViewerGridConfig;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.model.visualization.selection.SelectionModel;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.NodeObjective;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.util.ImageUtil;
import csi.shared.gwt.viz.viewer.LensImage.ImageLensImage;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

public class ContextImageLens implements Lens {
    @Override
    public LensImage focus(LensDef lensDef, Objective objective) {
        ImageLensImage imageLensImage = new ImageLensImage();
        String base64String = null;
        try {
            GraphContext gc = GraphServiceUtil.getGraphContext(objective.getVisualizationUuid());
            if (gc == null) {
                throw new CentrifugeException();
            }
            SelectionModel selection = gc.getSelection(GraphManager.DEFAULT_SELECTION);
            Selection backupSelection = selection.copy();
            selection.clearSelection();
            FindItemDTO findItemDTO = ((NodeObjective) objective).getFindItemDTO();
            Integer id = findItemDTO.getID();
            selection.nodes.add(id);
            Node node = gc.getVisibleGraph().getNode(id);
            NodeStore nodeDetails = GraphManager.getNodeDetails(node);
            Visualization vis = gc.getVisualization();
            Integer color = nodeDetails.getColor();
//            nodeDetails.setColor(16745738);
            Display display = vis.getDisplay(0);
            int height = display.getHeight();
            int width = display.getWidth();
            AffineTransform transform = (AffineTransform) display.getTransform().clone();
display.setSize(260,260);
            zoomToRegion(lensDef,((NodeObjective) objective));
            display = vis.getDisplay(0);
            BufferedImage displayImage = getDisplayImage(objective.getVisualizationUuid(), 260 + "", 260 + "");
            Graphics2D graphics = (Graphics2D) displayImage.getGraphics();
            graphics.setColor(new Color(0x595959));
            graphics.setStroke(new BasicStroke(1));
//            graphics.drawArc(findItemDTO.getClickX().intValue()-70,findItemDTO.getClickY().intValue()-70,140,140,0,500);
//            graphics.drawLine(findItemDTO.getClickX().intValue(),0,findItemDTO.getClickX().intValue(),display.getHeight());
//            graphics.drawLine(0,findItemDTO.getClickY().intValue(),display.getWidth(),findItemDTO.getClickY().intValue());
            base64String = ImageUtil.toBase64String(displayImage);
            imageLensImage.setValue(base64String);
            nodeDetails.setColor(color);
            display.setTransform(transform);
            display.setSize(new Dimension(width,height));

            selection.setFromSelection(backupSelection);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CentrifugeException e) {
            e.printStackTrace();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return imageLensImage;

    }

    private void zoomToRegion(LensDef lensDef, NodeObjective objective) {
        String visualizationUuid = objective.getVisualizationUuid();

        int x = (int) (objective.getFindItemDTO().getX() - (40 * objective.getFindItemDTO().getSize() * 2));
        int y = (int) (objective.getFindItemDTO().getY() - (40 * objective.getFindItemDTO().getSize() * 2));
        int w = (int) (40 * objective.getFindItemDTO().getSize() * 4);
        int h = w;

        GraphContext context = GraphServiceUtil.getGraphContext(visualizationUuid);
        if (context == null) {
            return;
        }
        synchronized (context) {
            Rectangle2D bounds = new Rectangle(x - 25, y - 25, w + 50, h + 50);
            // context.computeSoftMinimumBounds(bounds);
            // expand with 15% border to ensure nothing gets clipped on the
            // edges of the display
            double dim = Math.max(bounds.getWidth(), bounds.getHeight());
            // dim = Math.max(dim, 50);
            GraphicsLib.expand(bounds, dim * .15);
            context.fitToRegion(bounds);
        }
    }

    public BufferedImage getDisplayImage(String vizuuid, String viewWidth, String viewHeight)
            throws IOException, CentrifugeException {

        Dimension vdim = new Dimension(720, 720);
        if ((viewWidth != null) && (viewHeight != null)) {
            int w = Integer.parseInt(viewWidth);
            int h = Integer.parseInt(viewHeight);
            if ((w > 0) && (h > 0)) {
                vdim.width = w;
                vdim.height = h;
            }
        }

        try {
            GraphContext gc = GraphServiceUtil.getGraphContext(vizuuid);
            if (gc == null) {
                throw new CentrifugeException();
            }

            BufferedImage img = GraphManager.getInstance().renderGraph(gc, vdim);

            return img;
        } catch (Exception exception) {
            return null;
        }
    }


    @Override
    public List<List<?>> focus(LensDef lensDef, Objective objective, String token) {
        return null;
    }

    @Override
    public ViewerGridConfig getGridConfig() {
        return null;
    }
}

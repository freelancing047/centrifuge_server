package csi.server.business.visualization.graph.layout;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.config.RelGraphConfig;
import csi.server.business.visualization.graph.io.DotPositionReader;
import csi.server.business.visualization.graph.io.DotWriter;
import csi.server.common.model.visualization.graph.GraphConstants;

/**
 * Instances of this class copy out the results of a layout from the current graph and update the
 * corresponding 'root' graph.  In the process of copying out the positions, a transform is applied
 * to ensure that the positions are transformed into the graph's patchwork region.
 * <p>
 * This transform involves translation and scaling from the original positions into the patchwork
 * regions coordinate system.  A standard transformation translates and scales the coordinates; one additional
 * step that this class takes is to perform one additional translation to ensure that the resulting positions
 * are properly centered within the destination region.
 *
 *
 */
public class CopyPositionLayout extends DelegatedLayout {
    public static final String ROOT_GRAPH = "rootGraph";

    public File DOT_FILE = new File("C:/Program Files/Graphviz2.26.3/bin/dot.exe");

    private boolean ranDotLayout = false;

    public CopyPositionLayout(Layout delegate) {
        super(delegate);
    }

    @Override
    public void run(double frac) {
        if ((layout instanceof NodeLinkTreeLayout) && hasDotLayout()) {
            if (!doDotLayout()) {
                super.run(frac);
            } else {
                ranDotLayout = true;
            }
        } else {
            super.run(frac);
        }

        Visualization vis = getVisualization();
        TupleSet localGraph = vis.getGroup(getGroup());
        VisualGraph rootGraph = (VisualGraph) localGraph.getClientProperty(ROOT_GRAPH);

        String nodeGroupName = PrefuseLib.getGroupName(getGroup(), Graph.NODES);

        AffineTransform transform = getTransform();

        @SuppressWarnings("unchecked")
        Iterator<VisualItem> items = vis.visibleItems(nodeGroupName);
        Point2D.Double src = new Point2D.Double();
        Point2D.Double target = new Point2D.Double();

        while (items.hasNext()) {
            VisualItem item = items.next();
            int rootRowid = item.getInt(GraphConstants.ORIG_NODE_ID);
            if (rootRowid != -1) {
                VisualItem rootItem = (VisualItem) rootGraph.getNode(item.getInt(GraphConstants.ORIG_NODE_ID));

                src.x = item.getX();
                src.y = item.getY();

                transform.transform(src, target);

                setX(rootItem, null, target.x);
                setY(rootItem, null, target.y);

            }
        }

    }

    private boolean hasDotLayout() {
        boolean status = false;
        RelGraphConfig config = Configuration.getInstance().getGraphConfig();
        if ((config != null) && (config.getDotPath() != null)) {
            String dotPath = config.getDotPath();
            File file = new File(dotPath);
            status = file.exists();
        }

        return status;

    }

    private boolean doDotLayout() {

        boolean status = false;
        RelGraphConfig config = Configuration.getInstance().getGraphConfig();

        File path = (config.getDotPath() != null) ? new File(config.getDotPath()) : DOT_FILE;

        DotWriter dotWriter = new DotWriter();
        Visualization vis = getVisualization();
//        String nodeGroupName = PrefuseLib.getGroupName(getGroup(), Graph.NODES);
        TupleSet localGraph = vis.getGroup(getGroup());
//        VisualGraph rootGraph = (VisualGraph) localGraph.getClientProperty(ROOT_GRAPH);

        File tempFile = null;

        try {
            tempFile = File.createTempFile("csiGV", ".dot");

            try (PrintWriter pw = new PrintWriter(tempFile)) {
               dotWriter.write((Graph) localGraph, pw);
               pw.flush();
            }
            List<String> l = new ArrayList<String>();

            l.add(path.getAbsolutePath());
            l.add("-Tdot"); // dot format
            l.add("-y"); // invert y-axis--ensures roots are at the 'top' of the page
            l.add("-Gratio=\"1.0\""); // increase aspect ratio to 1.5 -- appears to apply vertical stretch to the resulting layout
            l.add(tempFile.getAbsolutePath());

            Process dot = Runtime.getRuntime().exec(l.toArray(new String[0]));
            InputStream istream = dot.getInputStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(istream))) {
               DotPositionReader dotReader = new DotPositionReader();

               dotReader.read((Graph) localGraph, reader);
            }
            status = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            status = false;
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        return status;
    }

   private AffineTransform getTransform() {
      AffineTransform P = new AffineTransform();
      String nodeGroupName = PrefuseLib.getGroupName(getGroup(), Graph.NODES);
      Rectangle2D region = getComputedBounds(nodeGroupName);

      if (region != null) {
         Rectangle2D patchRegion = getLayoutBounds();
         double scaleX = (patchRegion.getWidth() / region.getWidth());
         double scaleY = (patchRegion.getHeight() / region.getHeight());
         double minScale = Math.min(scaleX, scaleY);

        // steps for constructing our transform are:
        // P ( S ( T( pos ) ) )
        // where T( ) is translation of point into patchwork
        // S( ) applies proper scaling into the patchwork
        // P( ) performs translation to ensure point is properly centered in patchwork

         AffineTransform T = new AffineTransform();
         T.translate(-region.getMinX(), -region.getMinY());

         AffineTransform S = new AffineTransform();
         S.scale(minScale, minScale);

         if (ranDotLayout) {
            S.scale(2, 2);
         }
         double Px = (patchRegion.getWidth() - (region.getWidth() * minScale)) / 2.0d;
         Px += patchRegion.getX();

         double Py = (patchRegion.getHeight() - (region.getHeight() * minScale)) / 2.0d;
         Py += patchRegion.getY();

         P.translate(Px, Py);
         S.concatenate(T);
         P.concatenate(S);
      }
      return P;
   }

    @SuppressWarnings("unchecked")
    private Rectangle2D getComputedBounds(String nodeGroupName) {
        Rectangle2D.Double region = null;

        Visualization vis = getVisualization();
        Iterator<VisualItem> items = vis.visibleItems(nodeGroupName);
        while (items.hasNext()) {

            VisualItem item = items.next();
            if (region == null) {
                region = new Rectangle2D.Double();
                region.setRect(item.getBounds());
            } else {
                region.add(item.getX(), item.getY());
            }
        }

        return region;
    }

}

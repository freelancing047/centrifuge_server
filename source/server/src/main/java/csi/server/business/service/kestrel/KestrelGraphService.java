package csi.server.business.service.kestrel;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import prefuse.data.Graph;
import csi.server.business.service.AbstractService;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.RequestStreamParam;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.kestrel.reader.TransientGraphCodec;
import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.visualization.graph.GraphConstants.eLayoutAlgorithms;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskSession;
import csi.server.util.CsiUtil;

@Service(path = "/tac/graph")
public class KestrelGraphService extends AbstractService {

    @Operation
    public String createSessionGraph(@RequestStreamParam InputStream graphStream, @QueryParam("testData") String useTestData) throws CentrifugeException {
        TaskSession session = TaskHelper.getCurrentSession();
        String graphId = (String) session.getAttribute("sessionGraphId");
        if (graphId == null) {
            graphId = UUID.randomUUID().toString();
            session.setAttribute("sessionGraphId", graphId);
        }
        System.out.println("sessionGraphId " + graphId);

        InputStream fin = null;
        try {
            if (useTestData != null && useTestData.equalsIgnoreCase("true")) {
                File f = new File("webapps/Centrifuge/samples/kestrel.testdata.xml");
                try {
                    fin = new FileInputStream(f);
                    graphStream = fin;
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            TransientGraphCodec reader = new TransientGraphCodec(graphId);
            GraphContext context = reader.loadGraph(graphStream);

            runComponentizedLayout(context);

            GraphServiceUtil.removeGraphContext(graphId);
            GraphServiceUtil.setGraphContext(context);

            return graphId;
        } finally {
            CsiUtil.quietClose(fin);
        }
    }

    private void runComponentizedLayout(GraphContext context) {
        GraphManager manager = GraphManager.getInstance();
        Graph graph = context.getGraphData();
        manager.computeComponents(graph);
        manager.computeComponentRegions(context);
        manager.runPlacement(graph, eLayoutAlgorithms.forceDirected, context);

        manager.fitToSize(context.getVisualization());
    }

    private Point2D getTranslated(AffineTransform transform, double x, double y) {

        Point2D orig = new Point2D.Double(x, y);

        Point2D translated = transform.transform(orig, null);
        return translated;
    }
}

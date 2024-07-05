package csi.server.business.visualization.graph.base;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.data.Graph;

import csi.config.Configuration;
import csi.config.RelGraphConfig;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.Theme;

public class ConditionalBundleOperation extends BundleByObject {
   private static final Logger LOG = LogManager.getLogger(ConditionalBundleOperation.class);

    static final long DEFAULT_THRESHOLD = 1000L;

    protected long threshold = DEFAULT_THRESHOLD;

    public ConditionalBundleOperation(String dvUuid, String vizUuid, Graph graph) throws SQLException, CentrifugeException {
        super(dvUuid, vizUuid, graph);
    }

    @Override
    protected void initialize() {
        super.initialize();

        if (theme != null) {
            extractThreshold(theme);
        } else {
            RelGraphConfig graphConfig = Configuration.getInstance().getGraphConfig();
            threshold = graphConfig.getAutoBundleThreshold();
        }

    }

    @Override
    protected void execute() {
        if (!evaluateCondition()) {
           LOG.debug("Automatic bundling skipped, threshold not exceeded");
            return;
        }

        super.execute();
    }

    protected boolean evaluateCondition() {
        return graph.getNodeCount() > threshold;
    }

    private void extractThreshold(Theme theme) {
//        if (optionSet != null) {
//            try {
//                threshold = Integer.parseInt(optionSet.bundleThreshold);
//                return;
//            } catch (NumberFormatException e) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Invalid formatting encountered for bundlng threshold in " + graphDef.getOptionSetName());
//                }
//            }
//        }

        // fail-safe to ensure that we don't get mixed up!
        threshold = DEFAULT_THRESHOLD;
    }

}

package csi.server.business.visualization.graph;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.google.common.collect.Maps;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.task.api.TaskController;
import csi.server.task.api.TaskHelper;
import csi.server.task.api.TaskSession;
import csi.server.task.exception.TaskAbortedException;
import csi.server.ws.async.HttpTaskSession;

public class GraphServiceUtil {

    //
    // Helper methods for old style RestletActions
    //

    @Deprecated
    public static GraphContext getGraphContext(HttpSession httpSession, String vizUuid) {
        return getGraphContext(new HttpTaskSession(httpSession), vizUuid);
    }

    public static void setGraphContext(HttpSession httpSession, GraphContext gc) {
        setGraphContext(new HttpTaskSession(httpSession), gc);
    }

    public static GraphContext removeGraphContext(HttpSession httpSession, String vizUuid) {
        return removeGraphContext(new HttpTaskSession(httpSession), vizUuid);
    }

    // 
    // Helper methods for task based services
    //

    public static GraphContext getGraphContext(String vizUuid) {
        TaskSession taskSession = TaskHelper.getCurrentSession();
        GraphContext graphContext = getGraphContext(taskSession, vizUuid);
        if (graphContext == null) {
            for(TaskSession oneTaskSession : TaskController.getInstance().getUserSessions()) {
                try {
                graphContext = getGraphContext(oneTaskSession, vizUuid);
                } catch (TaskAbortedException tae) {
                    graphContext = null;
                }
                if (graphContext != null) {
                    break;
                }
            }
        }
        GraphContext.Current.set(graphContext);
//      return getGraphContext(taskSession, vizUuid); 
        return graphContext;
    }
    
    public static void setGraphContext(GraphContext gc) {
      TaskSession taskSession = TaskHelper.getCurrentSession();
      setGraphContext(taskSession, gc);
  }

  public static GraphContext removeGraphContext(String vizUuid) {
      TaskSession taskSession = TaskHelper.getCurrentSession();
      GraphContext graphContext = removeGraphContext(taskSession, vizUuid);
      for(TaskSession onetaskSession : TaskController.getInstance().getUserSessions()) {
          removeGraphContext(onetaskSession, vizUuid);
      }
      return graphContext;
  }

    //
    // base methods
    //

    private static GraphContext getGraphContext(TaskSession taskSession, String vizUuid) {
        Map<String, GraphContext> vizMaps = getGraphContextMap(taskSession);

        synchronized (vizMaps) {
            GraphContext gc = vizMaps.get(vizUuid);
            if (gc != null && gc.isInvalidated()) {
                // this throws a runtime exception
                vizMaps.remove(gc);
                TaskHelper.abortTask("Graph context is invalidated");
            }
            return gc;
        }
    }

    private static GraphContext removeGraphContext(TaskSession taskSession, String vizUuid) {
        Map<String, GraphContext> vizMaps = getGraphContextMap(taskSession);

        synchronized (vizMaps) {
            GraphContext gc = vizMaps.get(vizUuid);
            if (gc != null) {
                gc.setInvalidated(true);
                return vizMaps.remove(vizUuid);
            } else {
                return null;
            }
        }
    }

    private static void setGraphContext(TaskSession taskSession, GraphContext gc) {
        Map<String, GraphContext> vizMaps = getGraphContextMap(taskSession);

        synchronized (vizMaps) {
            String vizUuid = gc.getVizUuid();
            vizMaps.put(vizUuid, gc);
        }
    }

    private static Map<String, GraphContext> getGraphContextMap(TaskSession taskSession) {
        Map<String, GraphContext> vizMaps = (Map<String, GraphContext>) taskSession.getAttribute(GraphConstants.KEY_CONTEXTS_GRAPH);
        if (vizMaps == null) {
            vizMaps = taskSession.setAttributeIfAbsent(GraphConstants.KEY_CONTEXTS_GRAPH, Maps.newConcurrentMap());
        }
        return vizMaps;
    }
}

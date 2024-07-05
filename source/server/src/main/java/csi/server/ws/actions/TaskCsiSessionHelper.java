/*
* @(#) TaskCsiSessionHelper.java,  01.04.2010
*
*/
package csi.server.ws.actions;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import csi.server.business.visualization.deprecated.timeline.TemporalEvent;
import csi.server.business.visualization.graph.GraphServiceUtil;
import csi.server.business.visualization.map.MapCacheUtil;
import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.task.api.TaskContext;
import csi.server.task.api.TaskSession;
import csi.server.ws.async.HttpTaskSession;

public class TaskCsiSessionHelper {
    public static final String TIMELINE_SUFFIX = ".session.timeline";

    private TaskSession taskSession;

    public TaskCsiSessionHelper(TaskContext taskContext) {
        this.taskSession = taskContext.getTaskSession();
    }

    public TaskCsiSessionHelper(HttpSession httpSession) {
        this.taskSession = new HttpTaskSession(httpSession);
    }

    public Object getSessionAttribute(String attrname) {
        return taskSession.getAttribute(attrname);
    }

    public void setSessionAttribute(String attrname, Object obj) {
        taskSession.setAttribute(attrname, obj);
    }

    public void removeSessionAttribute(String attrname) {
        if (taskSession != null) {
            taskSession.removeAttribute(attrname);
        }
    }

    public void cleanUp(String dvUuid) {
        cleanUp(CsiPersistenceManager.findForDelete(DataView.class, dvUuid));
    }

    public void cleanUp(DataView dataView) {
        if (dataView == null) {
            return;
        }

        DataModelDef modelDef = dataView.getMeta().getModelDef();
        List<VisualizationDef> visualizations = modelDef.getVisualizations();
        for (VisualizationDef viewDef : visualizations) {
            if (viewDef instanceof RelGraphViewDef) {
                GraphServiceUtil.removeGraphContext(viewDef.getUuid());
            } 
            //No longer use this visualization
//            else if (viewDef instanceof TimelineViewDef_V1) {
//                removeTimeLineData(viewDef.getUuid());
//            } 
            else if (viewDef instanceof MapViewDef) {
            	MapServiceUtil.removeMapContext(viewDef.getUuid());
            	MapServiceUtil.removePlaceDynamicTypeInfo(viewDef.getUuid());
                MapCacheUtil.removeMapCacheInfo(viewDef.getUuid());
                MapCacheUtil.invalidate(viewDef.getUuid());
            }
        }
    }

    private String getTimeLineAttributeName(String dvUuid) {
        return dvUuid + TIMELINE_SUFFIX;
    }

    @SuppressWarnings("unchecked")
    public Map<String, TemporalEvent> getTimeLineData(String id) {
        String s = getTimeLineAttributeName(id);
        return (Map<String, TemporalEvent>) getSessionAttribute(s);
    }

    public void setTimeLineData(String id, Map<String, TemporalEvent> obj) {
        String s = getTimeLineAttributeName(id);
        setSessionAttribute(s, obj);
    }

    public void removeTimeLineData(String vizUuid) {
        String name = getTimeLineAttributeName(vizUuid);
        removeSessionAttribute(name);
    }
}

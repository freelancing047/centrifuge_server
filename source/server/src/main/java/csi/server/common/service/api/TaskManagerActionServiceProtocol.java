package csi.server.common.service.api;

import csi.server.common.exception.CentrifugeException;
import csi.shared.gwt.vortex.VortexService;

import java.util.List;

/**
 * Created by centrifuge on 4/19/2018.
 */
public interface TaskManagerActionServiceProtocol extends VortexService {

    public String cancelTask(String taskId) throws CentrifugeException;

    public void cancelGroupTasks(String groupTaskId, String clientId) throws CentrifugeException;

    public List<String> listGroupTasksInfo(String groupTaskId, String clientId) throws CentrifugeException;
}

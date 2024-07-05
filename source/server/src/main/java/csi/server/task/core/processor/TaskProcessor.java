package csi.server.task.core.processor;

import csi.server.task.api.TaskContext;



public interface TaskProcessor extends Processor {

    void processTaskSynchronously(TaskContext taskContext);
    
    void processTaskAsynchronously(TaskContext taskContext);
    
    TaskContext getCurrentContext();
    
    TaskThreadPoolExecutor getExecutor();
    
}

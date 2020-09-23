package org.lucas.component.common.core.idcenter.support.worker;

import org.apache.commons.lang3.RandomUtils;
import org.lucas.component.common.core.idcenter.support.worker.entity.WorkerNodeEntity;
import org.lucas.component.common.util.DockerUtils;
import org.lucas.component.common.util.NetUtils;

public abstract class AbstractWorkerIdAssigner implements WorkerIdAssigner {

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNodeEntity buildWorkerNode() throws Exception {
        WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();
        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());
        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalIp());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(0, 100000));
        }
        return workerNodeEntity;
    }

}

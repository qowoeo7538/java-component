package org.lucas.component.common.core.idcenter.support.worker;

@FunctionalInterface
public interface WorkerIdAssigner {

    /**
     * @return assigned worker id
     */
    long assignWorkerId();

}

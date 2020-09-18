package org.lucas.component.common.core.idcenter.support;

public enum BitsAllocator {

    DEFAULT(22);

    private final int workerIdBits;

    BitsAllocator(int workerIdBits) {
        this.workerIdBits = workerIdBits;
    }

}

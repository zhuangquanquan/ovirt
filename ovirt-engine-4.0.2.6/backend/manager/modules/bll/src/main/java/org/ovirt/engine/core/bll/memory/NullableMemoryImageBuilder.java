package org.ovirt.engine.core.bll.memory;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Guid;

/**
 * This builder is used when no memory image should be created
 */
public class NullableMemoryImageBuilder implements MemoryImageBuilder {

    @Override
    public void build() {
        //no op
    }

    @Override
    public String getVolumeStringRepresentation() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isCreateTasks() {
        return false;
    }

    @Override
    public Guid getMemoryDiskId() {
        return null;
    }

    @Override
    public Guid getMetadataDiskId() {
        return null;
    }
}

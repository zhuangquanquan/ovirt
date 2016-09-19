package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -2684524270498397962L;

    private Guid snapshotId;

    private boolean needsLocking = true;

    public RemoveSnapshotParameters(Guid snapshotId, Guid vmGuid) {
        super(vmGuid);
        this.snapshotId = snapshotId;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid value) {
        snapshotId = value;
    }

    public RemoveSnapshotParameters() {
        snapshotId = Guid.Empty;
    }

    public boolean isNeedsLocking() {
        return needsLocking;
    }

    public void setNeedsLocking(boolean needsLocking) {
        this.needsLocking = needsLocking;
    }
}

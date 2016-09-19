package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class AttachDetachVmDiskParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = 5640716432695539552L;
    private boolean isPlugUnPlug;
    private boolean isReadOnly;
    private Guid snapshotId;

    public AttachDetachVmDiskParameters() {
    }

    public AttachDetachVmDiskParameters(DiskVmElement diskVmElement) {
        this(diskVmElement, true, false);
    }

    public AttachDetachVmDiskParameters(DiskVmElement diskVmElement, boolean isPlugUnPlug, boolean isReadOnly) {
        super(diskVmElement);
        setEntityInfo(new EntityInfo(VdcObjectType.Disk, diskVmElement.getDiskId()));
        this.isPlugUnPlug = isPlugUnPlug;
        this.isReadOnly = isReadOnly;
    }

    public void setPlugUnPlug(boolean isPlugUnPlug) {
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public boolean isPlugUnPlug() {
        return isPlugUnPlug;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }
}

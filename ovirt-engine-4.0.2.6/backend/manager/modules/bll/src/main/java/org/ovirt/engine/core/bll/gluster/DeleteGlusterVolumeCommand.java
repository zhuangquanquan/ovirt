package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * BLL command to delete a Gluster volume
 */
@NonTransactiveCommandAttribute
public class DeleteGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeParameters> {

    public DeleteGlusterVolumeCommand(GlusterVolumeParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (volume.isOnline()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_UP);
            addValidationMessageVariable("volumeName", volume.getName());
            return false;
        }

        if (volume.getSnapshotsCount() > 0) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_HAS_SNAPSHOTS);
            addValidationMessageVariable("volumeName", volume.getName());
            addValidationMessageVariable("noOfSnapshots", volume.getSnapshotsCount());
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.DeleteGlusterVolume,
                        new GlusterVolumeVDSParameters(upServer.getId(),
                                getGlusterVolumeName()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            updateVolumeStatusInDb(getParameters().getVolumeId());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_DELETE_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_DELETE;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_DELETE_FAILED : errorType;
        }
    }

    private void updateVolumeStatusInDb(Guid volumeId) {
        getGlusterVolumeDao().remove(volumeId);
    }
}

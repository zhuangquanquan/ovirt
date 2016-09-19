package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveAllCinderSnapshotDisksCommand<T extends RemoveAllVmCinderDisksParameters> extends VmCommand<T> {

    public RemoveAllCinderSnapshotDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        List<CinderDisk> cinderDisks = getParameters().getCinderDisks();
        for (final CinderDisk cinderDisk : cinderDisks) {
            Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                    VdcActionType.RemoveCinderSnapshotDisk,
                    getCinderDiskSnapshotParameter(cinderDisk),
                    cloneContextAndDetachFromParent());
            try {
                VdcReturnValueBase vdcReturnValueBase = future.get();
                if (!vdcReturnValueBase.getSucceeded()) {
                    log.error("Error removing snapshot for Cinder disk '{}'", cinderDisk.getDiskAlias());
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error removing snapshot for Cinder disk '{}': {}", cinderDisk.getDiskAlias(), e.getMessage());
            }
        }
        setSucceeded(true);
    }

    private ImagesContainterParametersBase getCinderDiskSnapshotParameter(CinderDisk cinderDisk) {
        ImagesContainterParametersBase removeCinderSnapshotParams =
                new ImagesContainterParametersBase(cinderDisk.getImageId());
        removeCinderSnapshotParams.setDestinationImageId(cinderDisk.getImageId());
        removeCinderSnapshotParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        removeCinderSnapshotParams.setParentCommand(getActionType());
        removeCinderSnapshotParams.setParentParameters(getParameters());
        removeCinderSnapshotParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return removeCinderSnapshotParams;
    }

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public class GlusterMultipleActionsRunner extends PrevalidatingMultipleActionsRunner {

    public GlusterMultipleActionsRunner(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected VdcReturnValueBase runValidateOnly(final int currentValidateId, final int totalSize) {
        try {
            return super.runValidateOnly(currentValidateId, totalSize);
        } finally {
            // free the lock so that validateOnly() on next command doesn't block
            getCommands().get(currentValidateId).freeLock();
        }
    }

    @Override
    protected void executeValidatedCommand(CommandBase<?> command) {
        // Since we had released the lock at the end of Validate,
        // it must be acquired back just before execution of the command
        command.acquireLock();
        super.executeValidatedCommand(command);
    }
}

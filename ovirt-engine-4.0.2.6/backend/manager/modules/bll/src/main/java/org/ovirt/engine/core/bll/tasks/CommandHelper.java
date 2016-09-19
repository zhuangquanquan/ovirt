package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.CommandStatus;

public class CommandHelper {

    public static VdcReturnValueBase canDoAction(VdcActionType actionType,
                                                 VdcActionParametersBase parameters,
                                                 ExecutionContext executionContext,
                                                 boolean isInternal) {
        CommandBase<?> command = buildCommand(actionType, parameters, executionContext, CommandStatus.NOT_STARTED);
        command.setInternalExecution(isInternal);
        return command.validateOnly();
    }

    public static CommandBase<?> buildCommand(VdcActionType actionType,
                                              VdcActionParametersBase parameters,
                                              ExecutionContext executionContext,
                                              CommandStatus cmdStatus) {
        ExecutionContext cmdExecutionContext = executionContext == null ? new ExecutionContext() : executionContext;
        CommandBase<?> command = CommandsFactory.createCommand(actionType,
                parameters,
                new CommandContext(new EngineContext()).withExecutionContext(cmdExecutionContext));
        command.setCommandStatus(cmdStatus, false);
        return command;
    }
}

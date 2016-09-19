package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;

public class ClearExternalJobCommand <T extends VdcActionParametersBase> extends CommandBase<T>{

    private Job job;

    public ClearExternalJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue = true;
        if (getParameters().getJobId() != null) {
            job = getJobDao().get(getParameters().getJobId());
            if (job == null) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NO_JOB);
            }
            else if (! job.isExternal()) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NOT_EXTERNAL);
            }
        }
        else {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_NO_JOB);
        }
        if (!retValue) {
            addValidationMessage(EngineMessage.VAR__ACTION__CLEAR);
            addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_JOB);
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        job = getJobDao().get(getParameters().getJobId());
        job.setAutoCleared(true);
        getJobDao().update(job);
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject>  permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_TASKS));
        return permissionList;
    }

    public JobDao getJobDao() {
        return DbFacade.getInstance().getJobDao();
    }
}

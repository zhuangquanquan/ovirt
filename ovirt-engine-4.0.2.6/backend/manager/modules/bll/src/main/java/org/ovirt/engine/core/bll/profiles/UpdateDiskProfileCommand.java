package org.ovirt.engine.core.bll.profiles;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;

public class UpdateDiskProfileCommand extends UpdateProfileCommandBase<DiskProfileParameters, DiskProfile, DiskProfileValidator> {

    @Inject
    private VmSlaPolicyUtils vmSlaPolicyUtils;

    public UpdateDiskProfileCommand(DiskProfileParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected DiskProfileValidator getProfileValidator() {
        return new DiskProfileValidator(getProfile());
    }

    @Override
    protected ProfilesDao<DiskProfile> getProfileDao() {
        return getDiskProfileDao();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getProfileId(),
                VdcObjectType.DiskProfile, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__DISK_PROFILE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_DISK_PROFILE
                : AuditLogType.USER_FAILED_TO_UPDATE_DISK_PROFILE;
    }

    @Override
    protected void executeCommand() {
        // Chcek if qos has changed
        Guid oldQos = getDiskProfileDao().get(getProfileId()).getQosId();
        Guid newQos = getProfile().getQosId();

        super.executeCommand();

        // QoS did not change
        if (Objects.equals(oldQos, newQos)) {
            return;
        }

        // Update policies of all running vms
        // Profile changes are already persisted in the database
        if (getSucceeded()) {
            vmSlaPolicyUtils.refreshRunningVmsWithDiskProfile(getProfileId());
        }
    }
}

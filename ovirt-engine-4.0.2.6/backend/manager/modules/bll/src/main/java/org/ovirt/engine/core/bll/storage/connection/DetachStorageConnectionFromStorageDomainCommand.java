package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StorageConnectionValidator;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class DetachStorageConnectionFromStorageDomainCommand<T extends AttachDetachStorageConnectionParameters>
        extends StorageDomainCommandBase<T> {

    public DetachStorageConnectionFromStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        StorageConnectionValidator storageConnectionValidator = createStorageConnectionValidator();

        if (!validate(storageConnectionValidator.isConnectionExists())
                || !validate(storageConnectionValidator.isDomainOfConnectionExistsAndInactive(getStorageDomain()))
                || !validate(storageConnectionValidator.isISCSIConnectionAndDomain(getStorageDomain()))) {
            return false;
        }
        if(!storageConnectionValidator.isConnectionForISCSIDomainAttached(getStorageDomain())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_FOR_DOMAIN_NOT_EXIST);
        }
        return true;
    }

    protected StorageConnectionValidator createStorageConnectionValidator() {
        String connectionId = getParameters().getStorageConnectionId();
        StorageServerConnections connection = getStorageServerConnectionDao().get(connectionId);

        return new StorageConnectionValidator(connection);
    }

    @Override
    protected void executeCommand() {
        String connectionId = getParameters().getStorageConnectionId();
        List<LUNs> lunsForConnection = getLunDao().getAllForStorageServerConnection(connectionId);
        List<LUNs> lunsForVG = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());
        Collection<LUNs> lunsToRemove = (Collection<LUNs>) CollectionUtils.intersection(lunsForConnection, lunsForVG);

        for (LUNs lun : lunsToRemove) {
            if (getLunDao().get(lun.getLUNId()) != null) {
                getLunDao().remove(lun.getLUNId());
            }
        }

        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }

}

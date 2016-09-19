package org.ovirt.engine.core.common.businessentities;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public interface IVdsEventListener {
    void vdsNotResponding(VDS vds); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, Guid domainId); // BLL

    void vdsNonOperational(Guid vdsId, NonOperationalReason type, boolean logCommand, Guid domainId,
            Map<String, String> customLogValues); // BLL

    void vdsMovedToMaintenance(VDS vds); // BLL

    EventResult storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId); // BLL

    EventResult masterDomainNotOperational(Guid storageDomainId, Guid storagePoolId, boolean isReconstructToInactiveDomains, boolean canReconstructToCurrentMaster); // BLL

    void processOnVmStop(Collection<Guid> vmIds, Guid hostId);

    boolean vdsUpEvent(VDS vds);

    boolean connectHostToDomainsInActiveOrUnknownStatus(VDS vds);

    void processOnCpuFlagsChange(Guid vdsId);

    void processOnVmPoweringUp(Guid vmId);

    void handleVdsVersion(Guid vdsId);

    void rerun(Guid vmId);

    void runningSucceded(Guid vmId);

    void removeAsyncRunningCommand(Guid vmId);

    void storagePoolUpEvent(StoragePool storagePool);


    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            EngineError error);

    void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            EngineError error, TransactionScopeOption transactionScopeOption);

    void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status);

    void runFailedAutoStartVMs(List<Guid> vmIds);

    void runColdRebootVms(List<Guid> vmIds);

    void addUnmanagedVms(Guid hostId, List<Guid> unmanagedVmIds);

    void handleVdsMaintenanceTimeout(Guid vdsId);

    /**
     * update host's scheduling related properties
     */
    void updateSchedulingStats(VDS vds); // BLL

    void syncLunsInfoForBlockStorageDomain(final Guid storageDomainId, final Guid vdsId);

    /**
     * Updates VMs QoS
     */
    void updateSlaPolicies(List<Guid> vmIds, Guid vdsId);

    void refreshHostIfAnyVmHasHostDevices(List<Guid> vmIds, Guid hostId);

    void refreshHostCapabilities(Guid hostId);

    boolean isUpdateAvailable(VDS host);

    void importHostedEngineVm(VM vm);
}

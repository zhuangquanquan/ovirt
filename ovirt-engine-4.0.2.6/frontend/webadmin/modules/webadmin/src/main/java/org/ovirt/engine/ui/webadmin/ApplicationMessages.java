package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {
    String hostHasUpgradeAlert();

    String hostInSupportedStatusHasUpgradeAlert();

    String hostHasReinstallAlertNonResponsive();

    String hostHasReinstallAlertInstallFailed();

    String hostHasReinstallAlertMaintenance();

    String hostHasNICsAlert();

    String hostHasManualFenceAlert();

    String hostHasNoPowerManagementAlert();

    String hostGlusterDisconnectedAlert();

    String migrateHostDisabledVMsInServerClusters();

    String migrateSomeVmsAlreadyRunningOnHost();

    String migrateNoAvailableHost();

    String manaulFencePopupMessageLabel(String hostName);

    String storageDestroyPopupMessageLabel(String storageName);

    String detaCenterForceRemovePopupMessageLabel(String dcName);

    String clusterPopupMemoryOptimizationForServerLabel(String a);

    String clusterPopupMemoryOptimizationForDesktopLabel(String a);

    String clusterPopupMemoryOptimizationCustomLabel(String a);

    String theNetworkWillBeAddedToTheDataCenterAsWell(String dcName);

    String vlanNetwork(int vlanId);

    String sameVmNameExists(String vmName);

    String unlimitedMemConsumption(long mem);

    String unlimitedVcpuConsumption(int vcpu);

    String limitedMemConsumption(long mem, long limit);

    String limitedVcpuConsumption(int vcpu, int limit);

    String unlimitedStorageConsumption(String storage);

    String limitedStorageConsumption(String storage, double limit);

    String clusterHasNewGlusterHosts();

    String vmsWithTotalMigrations(String vmCount, String vmMigrating);

    String vmsWithInOutMigrations(String vmCount, String incomingMigrations, String outgoingMigrations);

    String rebalanceStatusMessage(JobExecutionStatus jobStatus);

    String moveDisplayNetworkWarning(String networkOperationMessage);

    String defaultMtu(int mtu);

    String glusterCapacityInfo(String freeSize, String usedSize, String totalSize);

    String geoRepSlaveVolumeToolTip(String mastervolName, String clusterName);

    String maxVfs(int maxVfs);

    String nameId(String name, String id);

    String percentWithValueInGB(int percent, int value);

    String stripSizeInfoForGlusterBricks(int stripeSize, String raidType);

    String getStorageDeviceSelectionInfo(String raidType);

    String onlyAvailableInCompatibilityVersions(String versions);

    String unSyncedEntriesPresent(int unSyncedEntries);

    String brickStatusWithUnSyncedEntriesPresent(String brickStatus, int unSyncedEntries);

    String needsGlusterHealingWithVolumeStatus(String volumeStatus);
}


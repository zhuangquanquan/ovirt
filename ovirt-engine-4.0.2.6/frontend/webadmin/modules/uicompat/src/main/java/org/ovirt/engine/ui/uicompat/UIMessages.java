package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.Messages;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.common.utils.SizeConverter;

public interface UIMessages extends Messages {
    String customPropertyOneOfTheParamsIsntSupported(String parameters);

    String customPropertiesValuesShouldBeInFormatReason(String format);

    String keyValueFormat();

    String emptyOrValidKeyValueFormatMessage(String format);

    String customPropertyValueShouldBeInFormatReason(String parameter, String format);

    String createOperationFailedDcGuideMsg(String storageName);

    String nameCanContainOnlyMsg(int maxNameLength);

    String detachNote(String localStoragesFormattedString);

    String youAreAboutToDisconnectHostInterfaceMsg(String nicName);

    String connectingToGuestWithNotResponsiveAgentMsg();

    String hostNameMsg(int hostNameMaxLength);

    String naturalNumber();

    String realNumber();

    String thisFieldMustContainTypeNumberInvalidReason(String type);

    String numberValidationNumberBetweenInvalidReason(String prefixMsg, String min, String max);

    String numberValidationNumberGreaterInvalidReason(String prefixMsg, String min);

    String numberValidationNumberLessInvalidReason(String prefixMsg, String max);

    String integerValidationNumberBetweenInvalidReason(String prefixMsg, int min, int max);

    String integerValidationNumberGreaterInvalidReason(String prefixMsg, int min);

    String integerValidationNumberLessInvalidReason(String prefixMsg, int max);

    String lenValidationFieldMusnotExceed(int maxLength);

    String vmStorageDomainIsNotAccessible();

    String noActiveStorageDomain();

    String alreadyAssignedClonedVmName();

    String suffixCauseToClonedVmNameCollision(String vmName);

    String alreadyAssignedClonedTemplateName();

    String suffixCauseToClonedTemplateNameCollision(String templateName);

    String createFailedDomainAlreadyExistStorageMsg(String storageName);

    String importFailedDomainAlreadyExistStorageMsg(String storageName);

    String memSizeBetween(int minMemSize, int maxMemSize);

    String maxMemSizeIs(int maxMemSize);

    String minMemSizeIs(int minMemSize);

    String memSizeMultipleOf(String architectureName, int multiplier);

    String nameMustConataionOnlyAlphanumericChars(int maxLen);

    String newNameWithSuffixCannotContainBlankOrSpecialChars(int maxLen);

    String importProcessHasBegunForVms(String importedVms);

    String storageDomainIsNotActive(String storageName);

    String importProcessHasBegunForTemplates(String importedTemplates);

    String templatesAlreadyExistonTargetExportDomain(String existingTemplates);

    String vmsAlreadyExistOnTargetExportDomain(String existingVMs);

    String templatesWithDependentVMs(String template, String vms);

    String sharedDisksWillNotBePartOfTheExport(String diskList);

    String directLUNDisksWillNotBePartOfTheExport(String diskList);

    String snapshotDisksWillNotBePartOfTheExport(String diskList);

    String noExportableDisksFoundForTheExport();

    String sharedDisksWillNotBePartOfTheSnapshot(String diskList);

    String directLUNDisksWillNotBePartOfTheSnapshot(String diskList);

    String snapshotDisksWillNotBePartOfTheSnapshot(String diskList);

    String noExportableDisksFoundForTheSnapshot();

    String sharedDisksWillNotBePartOfTheTemplate(String diskList);

    String directLUNDisksWillNotBePartOfTheTemplate(String diskList);

    String snapshotDisksWillNotBePartOfTheTemplate(String diskList);

    String noExportableDisksFoundForTheTemplate();

    String diskAlignment(String alignment, String lastScanDate);

    String errConnectingVmUsingSpiceMsg(Object errCode);

    String errConnectingVmUsingRdpMsg(Object errCode);

    String areYouSureYouWantToDeleteSanpshot(String from, Object description);

    String editBondInterfaceTitle(String name);

    String editHostNicVfsConfigTitle(String name);

    String editManagementNetworkTitle(String networkName);

    String editNetworkTitle(String name);

    String setupHostNetworksTitle(String hostName);

    String noOfBricksSelected(int brickCount);

    String vncInfoMessage(String hostIp, int port, String password, int seconds);

    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    String lunUsedByDiskWarning(String diskAlias);

    String lunUsedByVG(String vgID);

    String usedLunIdReason(String id, String reason);

    String removeBricksReplicateVolumeMessage(int oldReplicaCount, int newReplicaCount);

    String breakBond(String bondName);

    String detachNetwork(String networkName);

    String removeNetwork(String networkName);

    String attachTo(String name);

    String bondWith(String name);

    String addToBond(String name);

    String extendBond(String name);

    String removeFromBond(String name);

    String label(String label);

    String unlabel(String label);

    String suggestDetachNetwork(String networkName);

    String labelInUse(String label, String ifaceName);

    String incorrectVCPUNumber();

    String poolNameLengthInvalid(int maxLength, int vmsInPool);

    String poolNameWithQuestionMarksLengthInvalid(int maxLength, int vmsInPool, int numberOfQuestionMarks);

    String numOfVmsInPoolInvalid(int maxNumOfVms, int poolNameLength);

    String numOfVmsInPoolInvalidWithQuestionMarks(int maxNumOfVms, int poolNameLength, int numberOfQuestionMarks);

    String refreshInterval(int intervalSec);

    String importClusterHostNameEmpty(String address);

    String importClusterHostPasswordEmpty(String address);

    String importClusterHostFingerprintEmpty(String address);

    String unreachableGlusterHosts(List<String> hosts);

    String networkDcDescription(String networkName, String dcName, String description);

    String networkDc(String networkName, String dcName);

    String vnicFromVm(String vnic, String vm);

    String vnicProfileFromNetwork(String vnicProfile, String network);

    String vnicFromTemplate(String vnic, String template);

    String bridlessNetworkNotSupported(String version);

    String mtuOverrideNotSupported(String version);

    String numberOfVmsForHostsLoad(int numberOfVms);

    String cpuInfoLabel(int numberOfCpus, int numberOfSockets, int numberOfCpusPerSocket, int numberOfThreadsPerCore);

    String templateDiskDescription(String diskAlias, String storageDomainName);

    String interfaceIsRequiredToBootFromNetwork();

    String bootableDiskIsRequiredToBootFromDisk();

    String disklessVmCannotRunAsStateless();

    String urlSchemeMustBeEmpty(String passedScheme);

    String urlSchemeMustNotBeEmpty(String allowedSchemes);

    String urlSchemeInvalidScheme(String passedScheme, String allowedSchemes);

    String providerUrlWarningText(String providedEntities);

    String nicHotPlugNotSupported(String clusterVersion);

    String customSpmPriority(int priority);

    String brickDetailsNotSupportedInClusterCompatibilityVersion(String version);

    String hostNumberOfRunningVms(String hostName, int runningVms);

    String commonMessageWithBrackets(String subject, String inBrackets);

    String removeNetworkQoSMessage(int numOfProfiles);

    String removeStorageQoSMessage(int numOfProfiles);

    String removeStorageQoSItem(String qosName, String diskProfileNames);

    String removeCpuQoSMessage(int numOfProfiles);

    String removeHostNetworkQosMessage(int numOfNetworks);

    String cpuInfoMessage(int numOfCpus, int sockets, int coresPerSocket, int threadsPerSocket);

    String numaTopologyTitle(String hostName);

    String rebalanceStatusFailed(String name);

    String volumeProfileStatisticsFailed(String volName);

    String removeBrickStatusFailed(String name);

    String confirmStopVolumeRebalance(String name);

    String cannotMoveDisks(String disks);

    String cannotCopyDisks(String disks);

    String moveDisksPreallocatedWarning(String disks);

    String moveDisksWhileVmRunning(String disks);

    String errorConnectingToConsole(String name, String s);

    String errorConnectingToConsoleNoProtocol(String name);

    String cannotConnectToTheConsole(String vmName);

    String schedulerOptimizationInfo(int numOfRequests);

    String schedulerAllowOverbookingInfo(int numOfRequests);

    String vmTemplateWithCloneProvisioning(String templateName);

    String vmTemplateWithThinProvisioning(String templateName);

    String youAreAboutChangeDcCompatibilityVersionWithUpgradeMsg(String version);

    String haActive(int score);

    String volumeProfilingStatsTitle(String volumeName);

    String networkLabelConflict(String nicName, String labelName);

    String labeledNetworkNotAttached(String nicName, String labelName);

    String bootMenuNotSupported(String clusterVersion);

    String diskSnapshotLabel(String diskAlias, String snapshotDescription);

    String optionNotSupportedClusterVersionTooOld(String clusterVersion);

    String optionRequiresSpiceEnabled();

    String rngSourceNotSupportedByCluster(String source);

    String glusterVolumeCurrentProfileRunTime(int currentRunTime, String currentRunTimeUnit, int totalRunTime, String totalRunTimeUnit);

    String bytesReadInCurrentProfileInterval(String currentBytesRead, String currentBytesReadUnit, String totalBytes, String totalBytesUnit);

    String bytesWrittenInCurrentProfileInterval(String currentBytesWritten, String currentBytesWrittenUnit, String totalBytes, String totalBytesUnit);

    String defaultMtu(int mtu);

    String threadsAsCoresPerSocket(int cores, int threads);

    String approveCertificateTrust(String subject, String issuer, String sha1Fingerprint);

    String approveRootCertificateTrust(String subject, String sha1Fingerprint);

    String geoRepForceTitle(String action);

    String geoRepActionConfirmationMessage(String action);

    String iconDimensionsTooLarge(int width, int height, int maxWidht, int maxHeight);

    String iconFileTooLarge(int maxSize);

    String invalidIconFormat(String s);

    String clusterSnapshotOptionValueEmpty(String option);

    String volumeSnapshotOptionValueEmpty(String option);

    String vmDialogDisk(String name, String sizeInGb, String type, String boot);

    String confirmRestoreSnapshot(String volumeName);

    String confirmRemoveSnapshot(String volumeName);

    String confirmRemoveAllSnapshots(String volumeName);

    String confirmActivateSnapshot(String volumeName);

    String confirmDeactivateSnapshot(String volumeName);

    String confirmVolumeSnapshotDeleteMessage(String snapshotNames);

    @Messages.AlternateMessage(value = { "UNKNOWN" , "None" , "INTERVAL" , "Minute" , "HOURLY" , "Hourly" , "DAILY" , "Daily" , "WEEKLY" , "Weekly" , "MONTHLY" , "Monthly" })
    String recurrenceType(@Messages.Select
    GlusterVolumeSnapshotScheduleRecurrence recurrence);

    @Messages.AlternateMessage(value = { "BYTES" , "{0} B" , "KiB" , "{0} KiB" , "MiB" , "{0} MiB" , "GiB" , "{0} GiB" , "TiB" , "{0} TiB" })
    String sizeUnitString(String size, @Messages.Select
    SizeConverter.SizeUnit sizeUnit);

    String userSessionRow(long sessionId, String UserName);

    @Messages.AlternateMessage(value = { "SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER" , "Destination and master volumes should not be from same cluster." , "SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE" , "Capacity of destination volume should be greater than or equal to that of master volume." , "SLAVE_CLUSTER_AND_MASTER_CLUSTER_COMPATIBILITY_VERSIONS_DO_NOT_MATCH" , "Cluster Compatibility version of destination and master volumes should be same." , "SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION" , "Destination volume is already a part of another geo replication session." , "SLAVE_VOLUME_SHOULD_BE_UP" , "Destination volume should be up." , "SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE" , "Capacity information of the destination volume is not available." , "MASTER_VOLUME_SIZE_TO_BE_AVAILABLE" , "Capacity information of the master volume is not available." , "SLAVE_VOLUME_TO_BE_EMPTY" , "Destination volume should be empty." , "NO_UP_SLAVE_SERVER" , "No up server in the destination volume" })
    String geoRepEligibilityViolations(@Messages.Select
    GlusterGeoRepNonEligibilityReason reason);

    String testSuccessfulWithPowerStatus(String powerStatus);

    String testFailedWithErrorMsg(String errorMessage);

    String uiCommonRunActionPartitialyFailed(String reason);

    String vnicTypeDoesntMatchPassthroughProfile(String type);

    String vnicTypeDoesntMatchNonPassthroughProfile(String type);

    String guestOSVersionOptional(String optional);

    String guestOSVersionLinux(String distribution, String version, String codeName);

    String guestOSVersionWindows(String version, String build);

    String guestOSVersionWindowsServer(String version, String build);

    String positiveTimezoneOffset(String name, String hours, String minutes);

    String negativeTimezoneOffset(String name, String hours, String minutes);

    String bracketsWithGB(int value);

    String confirmDeleteFenceAgent(String agentDisplayString);

    String confirmDeleteAgentGroup(String agents);

    String failedToLoadOva(String ovaPath);

    String errataForHost(String hostName);

    String errataForVm(String vmName);

    String uploadImageFailedToStartMessage(String reason);

    String uploadImageFailedToResumeMessage(String reason);

    String uploadImageFailedToResumeSizeMessage(long priorFileBytes, long newFileBytes);

    String providerFailure();
}


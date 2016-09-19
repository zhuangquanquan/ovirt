package org.ovirt.engine.core.common.businessentities;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.common.validation.annotation.ValidCluster;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.annotation.ValidSerialNumberPolicy;
import org.ovirt.engine.core.common.validation.annotation.ValidUri;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@ValidCluster(groups = { CreateEntity.class })
@ValidSerialNumberPolicy(groups = {CreateEntity.class, UpdateEntity.class})
public class Cluster implements IVdcQueryable, BusinessEntity<Guid>, HasStoragePool<Guid>,
        Nameable, Commented, HasSerialNumberPolicy, HasMigrationOptions {

    private static final long serialVersionUID = 5659359762655478095L;

    private Guid id;

    @NotNull(message = "VALIDATION_CLUSTER_NAME_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.CLUSTER_NAME_SIZE, message = "VALIDATION_CLUSTER_NAME_MAX",
            groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "VALIDATION_CLUSTER_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private String comment;

    @Size(max = BusinessEntitiesDefinitions.CLUSTER_CPU_NAME_SIZE)
    private String cpuName;

    private Guid storagePoolId;

    @Size(max = BusinessEntitiesDefinitions.DATACENTER_NAME_SIZE)
    private String storagePoolName;

    private int maxVdsMemoryOverCommit;

    private boolean enableBallooning;

    private boolean enableKsm;

    private boolean countThreadsAsCores;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    private String compatibilityVersion;

    private Version compatVersion;

    private boolean transparentHugepages;

    private MigrateOnErrorOptions migrateOnError;

    private boolean virtService;

    private boolean glusterService;

    private boolean glusterCliBasedSchedulingOn;

    private boolean tunnelMigration;

    private String emulatedMachine;

    private boolean trustedService;

    private boolean haReservation;

    private boolean optionalReasonRequired;

    private boolean maintenanceReasonRequired;

    private Guid clusterPolicyId;

    private String clusterPolicyName;

    private Set<SupportedAdditionalClusterFeature> addtionalFeaturesSupported;

    /**
     * Currently we want all networks of sole cluster to have same switch type.
     */
    private SwitchType requiredSwitchTypeForCluster;

    @ValidUri(message = "VALIDATION_CLUSTER_SPICE_PROXY_HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.SPICE_PROXY_ADDR_SIZE)
    private String spiceProxy;

    private Map<String, String> clusterPolicyProperties;
    private boolean detectEmulatedMachine;

    private ArchitectureType architecture;
    private OptimizationType optimizationType;

    private SerialNumberPolicy serialNumberPolicy;
    private ClusterHostsAndVMs clusterHostsAndVms;

    @Size(max = BusinessEntitiesDefinitions.VM_SERIAL_NUMBER_SIZE)
    private String customSerialNumber;

    private Set<VmRngDevice.Source> requiredRngSources;

    private FencingPolicy fencingPolicy;

    private Boolean autoConverge;

    private Boolean migrateCompressed;

    private String glusterTunedProfile;

    private boolean ksmMergeAcrossNumaNodes;

    /**
     * How max sum of bandwidths of both outgoing and incoming migrations on one host are limited
     */
    @NotNull
    private MigrationBandwidthLimitType migrationBandwidthLimitType;

    /**
     * Maximum of sum of bandwidths of both outgoing and incoming migrations on one host. <br/>
     * Relevant only if {@link #migrationBandwidthLimitType} is {@link MigrationBandwidthLimitType#CUSTOM}.
     * In that case, it may not be null. <br/>
     * Unit: Mbps
     */
    @Min(1)
    private Integer customMigrationNetworkBandwidth;

    private Guid migrationPolicyId;

    public Cluster() {
        migrateOnError = MigrateOnErrorOptions.YES;
        name = "";
        virtService = true;
        optimizationType = OptimizationType.NONE;
        requiredRngSources = new HashSet<>();
        fencingPolicy = new FencingPolicy();
        addtionalFeaturesSupported = new HashSet<>();
        ksmMergeAcrossNumaNodes = true;
        migrationBandwidthLimitType = MigrationBandwidthLimitType.DEFAULT;
        requiredSwitchTypeForCluster = SwitchType.LEGACY;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid value) {
        id = value;
    }

    public void setClusterId(Guid value) {
        setId(value);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String value) {
        comment = value;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String value) {
        cpuName = value;
    }

    @Override
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String value) {
        storagePoolName = value;
    }

    public int getMaxVdsMemoryOverCommit() {
        return maxVdsMemoryOverCommit;
    }

    public void setMaxVdsMemoryOverCommit(int value) {
        maxVdsMemoryOverCommit = value;
    }

    public boolean getCountThreadsAsCores() {
        return countThreadsAsCores;
    }

    public void setCountThreadsAsCores(boolean value) {
        countThreadsAsCores = value;
    }

    public Version getCompatibilityVersion() {
        return compatVersion;
    }

    public void setCompatibilityVersion(Version value) {
        compatibilityVersion = value.getValue();
        compatVersion = value;
    }

    public boolean getTransparentHugepages() {
        return transparentHugepages;
    }

    public void setTransparentHugepages(boolean value) {
        transparentHugepages = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setMigrateOnError(MigrateOnErrorOptions migrateOnError) {
        this.migrateOnError = migrateOnError;
    }

    public MigrateOnErrorOptions getMigrateOnError() {
        return migrateOnError;
    }

    public void setVirtService(boolean virtService) {
        this.virtService = virtService;
    }

    public boolean supportsVirtService() {
        return virtService;
    }

    public void setGlusterService(boolean glusterService) {
        this.glusterService = glusterService;
    }

    public boolean supportsGlusterService() {
        return glusterService;
    }

    public void setGlusterCliBasedSchedulingOn(boolean glusterCliBasedSchedulingOn) {
        this.glusterCliBasedSchedulingOn = glusterCliBasedSchedulingOn;
    }

    public boolean isGlusterCliBasedSchedulingOn() {
        return this.glusterCliBasedSchedulingOn;
    }

    public boolean isTunnelMigration() {
        return tunnelMigration;
    }

    public void setTunnelMigration(boolean value) {
        tunnelMigration = value;
    }

    public String getEmulatedMachine() {
        return emulatedMachine;
    }

    public void setEmulatedMachine(String emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }

    public void setTrustedService(boolean trustedService) {
        this.trustedService = trustedService;
    }

    public boolean supportsTrustedService() {
        return trustedService;
    }

    public boolean supportsHaReservation() {
        return haReservation;
    }

    public void setHaReservation(boolean haReservation) {
        this.haReservation = haReservation;
    }

    public boolean isOptionalReasonRequired() {
        return optionalReasonRequired;
    }

    public void setOptionalReasonRequired(boolean optionalReasonRequired) {
        this.optionalReasonRequired = optionalReasonRequired;
    }

    public boolean isMaintenanceReasonRequired() {
        return maintenanceReasonRequired;
    }

    public void setMaintenanceReasonRequired(boolean maintenanceReasonRequired) {
        this.maintenanceReasonRequired = maintenanceReasonRequired;
    }

    public boolean isInUpgradeMode(){
        return ClusterPolicy.UPGRADE_POLICY_GUID.equals(clusterPolicyId);
    }

    public Guid getClusterPolicyId() {
        return clusterPolicyId;
    }

    public void setClusterPolicyId(Guid clusterPolicyId) {
        this.clusterPolicyId = clusterPolicyId;
    }

    public String getClusterPolicyName() {
        return clusterPolicyName;
    }

    public void setClusterPolicyName(String clusterPolicyName) {
        this.clusterPolicyName = clusterPolicyName;
    }

    public Map<String, String> getClusterPolicyProperties() {
        return clusterPolicyProperties;
    }

    public void setClusterPolicyProperties(Map<String, String> clusterPolicyProperties) {
        this.clusterPolicyProperties = clusterPolicyProperties;
    }

    public boolean isEnableKsm() {
        return enableKsm;
    }

    public void setEnableKsm(boolean enableKsm) {
        this.enableKsm = enableKsm;
    }

    public boolean isEnableBallooning() {
        return enableBallooning;
    }

    public void setEnableBallooning(boolean enableBallooning) {
        this.enableBallooning = enableBallooning;
    }

    public void setDetectEmulatedMachine(boolean detectEmulatedMachine) {
        this.detectEmulatedMachine = detectEmulatedMachine;
    }

    public boolean isDetectEmulatedMachine() {
        return detectEmulatedMachine;
    }

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    public void setArchitecture (ArchitectureType architecture) {
        this.architecture = architecture;
    }

    public OptimizationType getOptimizationType() {
        return optimizationType;
    }

    public void setOptimizationType(OptimizationType optimizationType) {
        this.optimizationType = optimizationType;
    }

    public String getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(String spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    @Override
    public String getCustomSerialNumber() {
        return customSerialNumber;
    }

    @Override
    public void setCustomSerialNumber(String customSerialNumber) {
        this.customSerialNumber = customSerialNumber;
    }

    @Override
    public SerialNumberPolicy getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    @Override
    public void setSerialNumberPolicy(SerialNumberPolicy serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
    }

    public Set<VmRngDevice.Source> getRequiredRngSources() {
        return requiredRngSources;
    }

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    public void setFencingPolicy(FencingPolicy fencingPolicy) {
        this.fencingPolicy = fencingPolicy;
    }

    public Boolean getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(Boolean autoConverge) {
        this.autoConverge = autoConverge;
    }

    public Boolean getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(Boolean migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    public ClusterHostsAndVMs getClusterHostsAndVms() {
        return clusterHostsAndVms;
    }

    public void setClusterHostsAndVms(ClusterHostsAndVMs clusterHostsAndVms) {
        this.clusterHostsAndVms = clusterHostsAndVms;
    }

    public String getGlusterTunedProfile() {
        return glusterTunedProfile;
    }

    public void setGlusterTunedProfile(String glusterTunedProfile) {
        this.glusterTunedProfile = glusterTunedProfile;
    }

    public Set<SupportedAdditionalClusterFeature> getAddtionalFeaturesSupported() {
        return addtionalFeaturesSupported;
    }

    public void setAddtionalFeaturesSupported(Set<SupportedAdditionalClusterFeature> addtionalFeaturesSupported) {
        this.addtionalFeaturesSupported = addtionalFeaturesSupported;
    }

    public boolean isKsmMergeAcrossNumaNodes() {
        return ksmMergeAcrossNumaNodes;
    }

    public void setKsmMergeAcrossNumaNodes(boolean ksmMergeAcrossNumaNodes) {
        this.ksmMergeAcrossNumaNodes = ksmMergeAcrossNumaNodes;
    }

    public Integer getCustomMigrationNetworkBandwidth() {
        return customMigrationNetworkBandwidth;
    }

    public void setCustomMigrationNetworkBandwidth(Integer customMigrationNetworkBandwidth) {
        this.customMigrationNetworkBandwidth = customMigrationNetworkBandwidth;
    }

    public MigrationBandwidthLimitType getMigrationBandwidthLimitType() {
        return migrationBandwidthLimitType;
    }

    public void setMigrationBandwidthLimitType(MigrationBandwidthLimitType migrationBandwidthLimitType) {
        this.migrationBandwidthLimitType = migrationBandwidthLimitType;
    }

    public Guid getMigrationPolicyId() {
        return migrationPolicyId;
    }

    public void setMigrationPolicyId(Guid migrationPolicyId) {
        this.migrationPolicyId = migrationPolicyId;
    }

    public SwitchType getRequiredSwitchTypeForCluster() {
        return requiredSwitchTypeForCluster;
    }

    public void setRequiredSwitchTypeForCluster(SwitchType requiredSwitchTypeForCluster) {
        this.requiredSwitchTypeForCluster = requiredSwitchTypeForCluster;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                compatVersion,
                compatibilityVersion,
                cpuName,
                description,
                maxVdsMemoryOverCommit,
                countThreadsAsCores,
                migrateOnError,
                name,
                storagePoolId,
                storagePoolName,
                requiredSwitchTypeForCluster,
                transparentHugepages,
                virtService,
                glusterService,
                glusterCliBasedSchedulingOn,
                tunnelMigration,
                emulatedMachine,
                trustedService,
                haReservation,
                clusterPolicyName,
                clusterPolicyProperties,
                requiredRngSources,
                enableKsm,
                enableBallooning,
                optimizationType,
                serialNumberPolicy,
                customSerialNumber,
                clusterHostsAndVms,
                fencingPolicy,
                autoConverge,
                migrateCompressed,
                glusterTunedProfile,
                addtionalFeaturesSupported,
                maintenanceReasonRequired,
                ksmMergeAcrossNumaNodes,
                customMigrationNetworkBandwidth,
                migrationBandwidthLimitType,
                migrationPolicyId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cluster)) {
            return false;
        }
        Cluster other = (Cluster) obj;
        // *ATTENTION* when adding fields to this, please make sure that equals still works, if not this will
        // cause all kinds of havoc in the UI when clusters are refreshed.
        return Objects.equals(id, other.id)
                && Objects.equals(compatVersion, other.compatVersion)
                && Objects.equals(compatibilityVersion, other.compatibilityVersion)
                && Objects.equals(cpuName, other.cpuName)
                && Objects.equals(description, other.description)
                && maxVdsMemoryOverCommit == other.maxVdsMemoryOverCommit
                && countThreadsAsCores == other.countThreadsAsCores
                && migrateOnError == other.migrateOnError
                && Objects.equals(name, other.name)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(storagePoolName, other.storagePoolName)
                && Objects.equals(requiredSwitchTypeForCluster, other.requiredSwitchTypeForCluster)
                && transparentHugepages == other.transparentHugepages
                && virtService == other.virtService
                && glusterService == other.glusterService
                && glusterCliBasedSchedulingOn == other.glusterCliBasedSchedulingOn
                && tunnelMigration == other.tunnelMigration
                && Objects.equals(emulatedMachine, other.emulatedMachine)
                && trustedService == other.trustedService
                && haReservation == other.haReservation
                && Objects.equals(clusterPolicyId, other.clusterPolicyId)
                && Objects.equals(clusterPolicyName, other.clusterPolicyName)
                && Objects.equals(clusterPolicyProperties, other.clusterPolicyProperties)
                && enableKsm == other.enableKsm
                && enableBallooning == other.enableBallooning
                && detectEmulatedMachine == other.detectEmulatedMachine
                && optimizationType == other.optimizationType
                && serialNumberPolicy == other.serialNumberPolicy
                && Objects.equals(customSerialNumber, other.customSerialNumber)
                && Objects.equals(clusterHostsAndVms, other.clusterHostsAndVms)
                && Objects.equals(requiredRngSources, other.requiredRngSources)
                && Objects.equals(fencingPolicy, other.fencingPolicy)
                && Objects.equals(autoConverge, other.autoConverge)
                && Objects.equals(migrateCompressed, other.migrateCompressed)
                && Objects.equals(glusterTunedProfile, other.glusterTunedProfile)
                && Objects.equals(maintenanceReasonRequired, other.maintenanceReasonRequired)
                && Objects.equals(addtionalFeaturesSupported, other.addtionalFeaturesSupported)
                && ksmMergeAcrossNumaNodes == other.ksmMergeAcrossNumaNodes
                && Objects.equals(customMigrationNetworkBandwidth, other.customMigrationNetworkBandwidth)
                && Objects.equals(migrationBandwidthLimitType, other.migrationBandwidthLimitType)
                && Objects.equals(migrationPolicyId, other.migrationPolicyId);
    }

    @Override
    public String toString() {
        return "Cluster [" + name + "]";
    }
}

package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ClusterHostsAndVMs;
import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>ClusterDaoImpl</code> provides an implementation of {@link ClusterDao} that uses code previously
 * found in {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 */
@Named
@Singleton
public class ClusterDaoImpl extends BaseDao implements ClusterDao {
    private static final Logger log = LoggerFactory.getLogger(ClusterDaoImpl.class);

    @Override
    public Cluster get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public Cluster get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetClusterByClusterId", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public Cluster getWithRunningVms(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead("GetClusterWithRunningVms", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public Boolean getIsEmpty(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);
        return getCallsHandler().executeRead("GetIsClusterEmpty", BooleanRowMapper.instance, parameterSource);
    }

    @Override
    public Cluster getByName(String name) {
        return (Cluster) DbFacadeUtils.asSingleResult(getByName(name, true));
    }

    @Override
    public List<Cluster> getByName(String name, boolean isCaseSensitive) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_name", name)
                .addValue("is_case_sensitive", isCaseSensitive);

        return getCallsHandler().executeReadList("GetClusterByClusterName",
                        ClusterRowMapper.instance,
                        parameterSource);
    }

    @Override
    public Cluster getByName(String name, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_name", name).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return (Cluster) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetClusterForUserByClusterName",
                        ClusterRowMapper.instance,
                        parameterSource));
    }

    @Override
    public List<Cluster> getAllForStoragePool(Guid id) {
        return getAllForStoragePool(id, null, false);
    }

    @Override
    public List<Cluster> getAllForStoragePool(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetClustersByStoragePoolId",
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getAllWithQuery(String query) {
        List<Cluster> clusters = getJdbcTemplate().query(query, ClusterRowMapper.instance);
        return getHostsAndVmsForClusters(clusters);
    }

    @Override
    public List<Cluster> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<Cluster> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromCluster", ClusterRowMapper.instance, parameterSource);
    }

    @Override
    public void save(Cluster cluster) {
        Guid id = cluster.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            cluster.setId(id);
        }
        getCallsHandler().executeModification("InsertCluster", getClusterParamSource(cluster));
    }

    @Override
    public void update(Cluster cluster) {
        getCallsHandler().executeModification("UpdateCluster", getClusterParamSource(cluster));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id);

        getCallsHandler().executeModification("DeleteCluster", parameterSource);
    }

    @Override
    public List<Cluster> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

        return getCallsHandler().executeReadList("fn_perms_get_clusters_with_permitted_action",
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getClustersHavingHosts() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersHavingHosts",
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Cluster> getWithoutMigratingVms() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetClustersWithoutMigratingVms",
                ClusterRowMapper.instance,
                parameterSource);
    }

    @Override
    public void setEmulatedMachine(Guid clusterId, String emulatedMachine, boolean detectEmulatedMachine) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId)
                .addValue("emulated_machine", emulatedMachine)
                .addValue("detect_emulated_machine", detectEmulatedMachine);

        getCallsHandler().executeModification("UpdateClusterEmulatedMachine", parameterSource);
    }

    @Override
    public int getVmsCountByClusterId(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("cluster_id", clusterId);
        return getCallsHandler().executeRead("GetNumberOfVmsInCluster",
                getIntegerMapper(),
                parameterSource);
    }

    @Override
    public List<Cluster> getTrustedClusters() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("trusted_service", true);
        return getCallsHandler().executeReadList("GetTrustedClusters", ClusterRowMapper.instance, parameterSource);
    }

    private MapSqlParameterSource getClusterParamSource(Cluster cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", cluster.getDescription())
                .addValue("name", cluster.getName())
                .addValue("free_text_comment", cluster.getComment())
                .addValue("cluster_id", cluster.getId())
                .addValue("cpu_name", cluster.getCpuName())
                .addValue("storage_pool_id", cluster.getStoragePoolId())
                .addValue("max_vds_memory_over_commit",
                        cluster.getMaxVdsMemoryOverCommit())
                .addValue("count_threads_as_cores",
                        cluster.getCountThreadsAsCores())
                .addValue("transparent_hugepages",
                        cluster.getTransparentHugepages())
                .addValue("compatibility_version",
                        cluster.getCompatibilityVersion())
                .addValue("migrate_on_error", cluster.getMigrateOnError())
                .addValue("virt_service", cluster.supportsVirtService())
                .addValue("gluster_service", cluster.supportsGlusterService())
                .addValue("gluster_cli_based_snapshot_scheduled", cluster.isGlusterCliBasedSchedulingOn())
                .addValue("tunnel_migration", cluster.isTunnelMigration())
                .addValue("required_rng_sources", VmRngDevice.sourcesToCsv(cluster.getRequiredRngSources()))
                .addValue("emulated_machine", cluster.getEmulatedMachine())
                .addValue("detect_emulated_machine", cluster.isDetectEmulatedMachine())
                .addValue("trusted_service", cluster.supportsTrustedService())
                .addValue("ha_reservation", cluster.supportsHaReservation())
                .addValue("optional_reason", cluster.isOptionalReasonRequired())
                .addValue("maintenance_reason_required", cluster.isMaintenanceReasonRequired())
                .addValue("cluster_policy_id", cluster.getClusterPolicyId())
                .addValue("cluster_policy_custom_properties",
                                SerializationFactory.getSerializer().serialize(cluster.getClusterPolicyProperties()))
                .addValue("architecture", cluster.getArchitecture())
                .addValue("enable_balloon", cluster.isEnableBallooning())
                .addValue("optimization_type", cluster.getOptimizationType())
                .addValue("enable_ksm", cluster.isEnableKsm())
                .addValue("spice_proxy", cluster.getSpiceProxy())
                .addValue("serial_number_policy", cluster.getSerialNumberPolicy() == null ? null : cluster.getSerialNumberPolicy().getValue())
                .addValue("custom_serial_number", cluster.getCustomSerialNumber())
                .addValue("skip_fencing_if_sd_active", cluster.getFencingPolicy().isSkipFencingIfSDActive())
                .addValue("skip_fencing_if_connectivity_broken", cluster.getFencingPolicy().isSkipFencingIfConnectivityBroken())
                .addValue("hosts_with_broken_connectivity_threshold", cluster.getFencingPolicy().getHostsWithBrokenConnectivityThreshold())
                .addValue("fencing_enabled", cluster.getFencingPolicy().isFencingEnabled())
                .addValue("is_auto_converge", cluster.getAutoConverge())
                .addValue("is_migrate_compressed", cluster.getMigrateCompressed())
                .addValue("gluster_tuned_profile", cluster.getGlusterTunedProfile())
                .addValue("ksm_merge_across_nodes", cluster.isKsmMergeAcrossNumaNodes())
                .addValue("migration_bandwidth_limit_type", cluster.getMigrationBandwidthLimitType().name())
                .addValue("custom_migration_bandwidth_limit", cluster.getCustomMigrationNetworkBandwidth())
                .addValue("migration_policy_id", cluster.getMigrationPolicyId())
                .addValue("switch_type", cluster.getRequiredSwitchTypeForCluster().getOptionValue());

        return parameterSource;
    }

    private static final class ClusterHostsAndVMsRowMapper implements RowMapper<ClusterHostsAndVMs> {
        public static final RowMapper<ClusterHostsAndVMs> instance = new ClusterHostsAndVMsRowMapper();

        @Override
        public ClusterHostsAndVMs mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClusterHostsAndVMs entity = new ClusterHostsAndVMs();
            entity.setHosts(rs.getInt("hosts"));
            entity.setVms(rs.getInt("vms"));
            entity.setClusterId(getGuid(rs, "cluster_id"));
            return entity;
        }

    }
    private static final class ClusterRowMapper implements RowMapper<Cluster> {
        public static final RowMapper<Cluster> instance = new ClusterRowMapper();
        @Override
        public Cluster mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            Cluster entity = new Cluster();
            entity.setDescription(rs.getString("description"));
            entity.setName(rs.getString("name"));
            entity.setId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setCpuName(rs.getString("cpu_name"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setStoragePoolName(rs
                    .getString("storage_pool_name"));
            entity.setMaxVdsMemoryOverCommit(rs
                    .getInt("max_vds_memory_over_commit"));
            entity.setCountThreadsAsCores(rs
                    .getBoolean("count_threads_as_cores"));
            entity.setTransparentHugepages(rs
                    .getBoolean("transparent_hugepages"));
            entity.setCompatibilityVersion(new Version(rs
                    .getString("compatibility_version")));
            entity.setMigrateOnError(MigrateOnErrorOptions.forValue(rs.getInt("migrate_on_error")));
            entity.setVirtService(rs.getBoolean("virt_service"));
            entity.setGlusterService(rs.getBoolean("gluster_service"));
            entity.setGlusterCliBasedSchedulingOn(rs.getBoolean("gluster_cli_based_snapshot_scheduled"));
            entity.setTunnelMigration(rs.getBoolean("tunnel_migration"));
            entity.getRequiredRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("required_rng_sources")));
            entity.setEmulatedMachine(rs.getString("emulated_machine"));
            entity.setDetectEmulatedMachine(rs.getBoolean("detect_emulated_machine"));
            entity.setTrustedService(rs.getBoolean("trusted_service"));
            entity.setHaReservation(rs.getBoolean("ha_reservation"));
            entity.setOptionalReasonRequired(rs.getBoolean("optional_reason"));
            entity.setMaintenanceReasonRequired(rs.getBoolean("maintenance_reason_required"));
            entity.setClusterPolicyId(Guid.createGuidFromString(rs.getString("cluster_policy_id")));
            entity.setClusterPolicyName(rs.getString("cluster_policy_name"));
            entity.setClusterPolicyProperties(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("cluster_policy_custom_properties"), LinkedHashMap.class));
            entity.setEnableBallooning(rs.getBoolean("enable_balloon"));
            entity.setEnableKsm(rs.getBoolean("enable_ksm"));
            entity.setArchitecture(ArchitectureType.forValue(rs.getInt("architecture")));
            entity.setOptimizationType(OptimizationType.from(rs.getInt("optimization_type")));
            entity.setSpiceProxy(rs.getString("spice_proxy"));
            entity.setSerialNumberPolicy(SerialNumberPolicy.forValue((Integer) rs.getObject("serial_number_policy")));
            entity.setCustomSerialNumber(rs.getString("custom_serial_number"));
            entity.getFencingPolicy().setSkipFencingIfSDActive(rs.getBoolean("skip_fencing_if_sd_active"));
            entity.getFencingPolicy().setSkipFencingIfConnectivityBroken(rs.getBoolean("skip_fencing_if_connectivity_broken"));
            entity.getFencingPolicy().setHostsWithBrokenConnectivityThreshold(rs.getInt("hosts_with_broken_connectivity_threshold"));
            entity.getFencingPolicy().setFencingEnabled(rs.getBoolean("fencing_enabled"));
            entity.setAutoConverge((Boolean) rs.getObject("is_auto_converge"));
            entity.setMigrateCompressed((Boolean) rs.getObject("is_migrate_compressed"));
            entity.setGlusterTunedProfile(rs.getString("gluster_tuned_profile"));
            entity.setKsmMergeAcrossNumaNodes(rs.getBoolean("ksm_merge_across_nodes"));
            entity.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.valueOf(rs.getString("migration_bandwidth_limit_type")));
            entity.setCustomMigrationNetworkBandwidth(getInteger(rs, "custom_migration_bandwidth_limit"));
            entity.setMigrationPolicyId(getGuid(rs, "migration_policy_id"));
            entity.setRequiredSwitchTypeForCluster(SwitchType.parse(rs.getString("switch_type")));

            return entity;
        }
    }

    private static final class BooleanRowMapper implements RowMapper<Boolean> {
        public static final RowMapper<Boolean> instance = new BooleanRowMapper();

        @Override
        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Boolean.valueOf(rs.getBoolean(1));
        }
    }

    @Override
    public List<Cluster> getClustersByClusterPolicyId(Guid clusterPolicyId) {
        return getCallsHandler().executeReadList("GetClustersByClusterPolicyId",
                ClusterRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_policy_id", clusterPolicyId));
    }

    protected List<Cluster> getHostsAndVmsForClusters(List<Cluster> clusters) {
        Map<Guid, Cluster> clustersById = new HashMap<>();
        for (Cluster cluster : clusters) {
            clustersById.put(cluster.getId(), cluster);
        }
        List<ClusterHostsAndVMs> dataList = getCallsHandler().executeReadList("GetHostsAndVmsForClusters",
                ClusterHostsAndVMsRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_ids", createArrayOf("uuid", clustersById.keySet().toArray())));

        for (ClusterHostsAndVMs clusterDetail : dataList) {
            clustersById.get(clusterDetail.getClusterId()).setClusterHostsAndVms(clusterDetail);
        }
        //The VDS clusters have been updated, but we want to keep the order, so return the original list which is
        //in the right order.
        return clusters;

    }

    @Override
    public List<Cluster> getClustersByServiceAndCompatibilityVersion(boolean glusterService, boolean virtService, String compatibilityVersion) {
        return getCallsHandler().executeReadList("GetClustersByServiceAndCompatibilityVersion",
                ClusterRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("gluster_service", glusterService)
                        .addValue("virt_service", virtService)
                        .addValue("compatibility_version", compatibilityVersion));
    }
}

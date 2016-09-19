package org.ovirt.engine.core.dao;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;

public class ClusterDaoTest extends BaseDaoTestCase {
    private static final int NUMBER_OF_GROUPS = 9;
    private static final int NUMBER_OF_TRUSTED_GROUPS = 4;
    private static final int NUMBER_OF_GROUPS_FOR_PRIVELEGED_USER = 2;

    private ClusterDao dao;
    private Cluster existingCluster;
    private Cluster newGroup;
    private Cluster groupWithNoRunningVms;
    private StoragePool storagePool;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        VdsDao vdsDao = dbFacade.getVdsDao();

        VDS existingVds = vdsDao.get(FixturesTool.VDS_RHEL6_NFS_SPM);

        StoragePoolDao storagePoolDao = dbFacade.getStoragePoolDao();

        storagePool = storagePoolDao.get(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        dao = dbFacade.getClusterDao();

        existingCluster = dao.get(existingVds.getClusterId());
        groupWithNoRunningVms = dbFacade.getClusterDao().get(FixturesTool.CLUSTER_NO_RUNNING_VMS);

        newGroup = new Cluster();
        newGroup.setName("New VDS Group");
        newGroup.setCompatibilityVersion(Version.getLast());
        newGroup.setVirtService(true);
        newGroup.setGlusterService(false);
        newGroup.setClusterPolicyId(existingCluster.getClusterPolicyId());
        ClusterPolicyDao clusterPolicyDao = dbFacade.getClusterPolicyDao();
        // set cluster policy name to allow equals method to succeed
        newGroup.setClusterPolicyName(clusterPolicyDao.get(existingCluster.getClusterPolicyId(),
                Collections.emptyMap()).getName());
        newGroup.setClusterPolicyProperties(new LinkedHashMap<>());
        newGroup.setDetectEmulatedMachine(true);
        newGroup.setEmulatedMachine("rhel6.4.0");
        newGroup.setArchitecture(ArchitectureType.x86_64);
        newGroup.setGlusterCliBasedSchedulingOn(true);
        newGroup.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.CUSTOM);
        newGroup.setCustomMigrationNetworkBandwidth(1000);
        newGroup.setMigrationPolicyId(Guid.newGuid());
    }

    /**
     * Ensures that the id must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        Cluster result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that null is returned.
     */
    @Test
    public void testGetWithRunningVmsWhereThereAreNone() {
        Cluster result = dao.getWithRunningVms(groupWithNoRunningVms.getId());

        assertNull(result);
    }

    /**
     * Ensures that the VDS group is returned.
     */
    @Test
    public void testGetWithRunningVms() {
        Cluster result = dao.getWithRunningVms(existingCluster.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that retrieving a group works as expected.
     */
    @Test
    public void testGet() {
        Cluster result = dao.get(existingCluster.getId());

        assertCorrectCluster(result);
    }

    /**
     * Ensures that retrieving a group works as expected with a privileged user and optional filtering.
     */
    @Test
    public void testGetFilteredWithPermissions() {
        Cluster result = dao.get(existingCluster.getId(), PRIVILEGED_USER_ID, true);

        assertCorrectCluster(result);
    }

    /**
     * Ensures that retrieving a group works as expected with an unprivileged user and optional filtering disabled.
     */
    @Test
    public void testGetFilteredWithNoPermissionsAndNoFilter() {
        Cluster result = dao.get(existingCluster.getId(), UNPRIVILEGED_USER_ID, false);

        assertCorrectCluster(result);
    }

    /**
     * Ensures that retrieving a group works as expected with an unprivileged user.
     */
    @Test
    public void testGetFilteredWithNoPermissions() {
        Cluster result = dao.get(existingCluster.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Asserts that the given {@link org.ovirt.engine.core.common.businessentities.Cluster} is indeed the existing VDS Group the test uses.
     *
     * @param group
     *            The group to check
     */
    private void assertCorrectCluster(Cluster group) {
        assertNotNull(group);
        assertEquals(existingCluster, group);
    }

    /**
     * Ensures that a bad name result in a null group.
     */
    @Test
    public void testGetByNameWithBadName() {
        Cluster result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetByName() {
        Cluster result = dao.getByName(existingCluster.getName());

        assertCorrectCluster(result);
    }

    @Test
    public void testGetByNameForPrivilegedUser() {
        Cluster result = dao.getByName(existingCluster.getName(), PRIVILEGED_USER_ID, false);
        assertCorrectCluster(result);
    }

    @Test
    public void testGetByNameForUnprivilegedUser() {
        Cluster result = dao.getByName(existingCluster.getName(), UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<Cluster> result = dao.getAllForStoragePool(Guid.newGuid());
        assertGetAllForStoragePoolInvalidResult(result);
    }

    /**
     * Ensures that the right group is returned.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<Cluster> result = dao.getAllForStoragePool(storagePool.getId());
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that no groups are returned if the issuing user does not have the right permissions.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithNoPermissions() {
        List<Cluster> result = dao.getAllForStoragePool(storagePool.getId(), UNPRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolInvalidResult(result);
    }

    /**
     * Ensures that the right group is returned if the filtering mechanism is disabled.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithNoPermissionsAndNoFilter() {
        List<Cluster> result = dao.getAllForStoragePool(storagePool.getId(), UNPRIVILEGED_USER_ID, false);
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that no groups are returned if the issuing user has the right permissions.
     */
    @Test
    public void testGetAllForStoragePoolFilteredWithPermissions() {
        List<Cluster> result = dao.getAllForStoragePool(storagePool.getId(), PRIVILEGED_USER_ID, true);
        assertGetAllForStoragePoolValidResult(result);
    }

    /**
     * Ensures that the storage pool name is returned properly.
     */
    @Test
    public void testClusterCorrectStoragePoolName() {
        List<Cluster> result = dao.getAllForStoragePool(storagePool.getId());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Cluster group : result) {
            assertEquals(storagePool.getName(), group.getStoragePoolName());
        }
    }

    /**
     * Asserts the result of a invalid call to {@link ClusterDao#getAllForStoragePool(Guid, Guid, boolean)}
     */
    private static void assertGetAllForStoragePoolInvalidResult(List<Cluster> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of a valid call to {@link ClusterDao#getAllForStoragePool(Guid, Guid, boolean)}
     */
    private void assertGetAllForStoragePoolValidResult(List<Cluster> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (Cluster group : result) {
            assertEquals(storagePool.getId(), group.getStoragePoolId());
        }
    }

    /**
     * Ensures that the right number of groups are returned.
     */
    @Test
    public void testGetAll() {
        List<Cluster> result = dao.getAll();

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that retrieving VDS groups works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<Cluster> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_GROUPS_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingCluster);
    }

    /**
     * Ensures that retrieving VDS groups works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<Cluster> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no VDS group is retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        List<Cluster> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures saving a group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newGroup);

        Cluster result = dao.getByName(newGroup.getName());

        assertNotNull(result);
        assertEquals(newGroup, result);
    }

    /**
     * Ensures that updating a group works as expected.
     */
    @Test
    public void testUpdate() {
        String oldName = existingCluster.getName();

        existingCluster.setName("This is the new name");
        existingCluster.setVirtService(false);
        existingCluster.setGlusterService(true);
        existingCluster.setMigrationBandwidthLimitType(MigrationBandwidthLimitType.CUSTOM);
        existingCluster.setCustomMigrationNetworkBandwidth(20);

        dao.update(existingCluster);

        Cluster result = dao.get(existingCluster.getId());

        assertCorrectCluster(result);

        result = dao.getByName(oldName);

        assertNull(result);
    }

    /**
     * Ensures that removing a group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(groupWithNoRunningVms.getId());

        Cluster result = dao.get(groupWithNoRunningVms.getId());

        assertNull(result);
    }

    /**
     * Test the use of the special procedure to update emulated_machine
     */
    @Test
    public void testSetEmulatedMachine() {
        String preUpdate = existingCluster.getEmulatedMachine();
        String updatedValue = "pc-version-1.2.3";

        assertNotSame(preUpdate, updatedValue);

        existingCluster.setEmulatedMachine(updatedValue);
        dao.setEmulatedMachine(existingCluster.getId(), updatedValue, false);

        assertEquals(updatedValue, dao.get(existingCluster.getId()).getEmulatedMachine());
    }

    /**
     * Test the use of the special procedure to update detect_emulated_machine
     */
    @Test
    public void testSetDetectEmulatedMachine() {
        boolean preUpdate = existingCluster.isDetectEmulatedMachine();
        boolean updateValue = false;

        assertNotSame(preUpdate, updateValue);

        dao.setEmulatedMachine(existingCluster.getId(), existingCluster.getEmulatedMachine(), updateValue);

        assertEquals(updateValue, dao.get(existingCluster.getId()).isDetectEmulatedMachine());
    }

    @Test
    public void testUpdateClustersProps() {
        assertNotSame("pc-1.2.3", existingCluster.getEmulatedMachine());
        existingCluster.setEmulatedMachine("pc-1.2.3");
        dao.update(existingCluster);
        assertEquals("pc-1.2.3", existingCluster.getEmulatedMachine());

        existingCluster.setDetectEmulatedMachine(true);
        dao.update(existingCluster);
        assertEquals(true, existingCluster.isDetectEmulatedMachine());
    }


    /**
     * Test that the correct clusters are fetched when looking for trusted_services
     */
    @Test
    public void testGetAllTrustedClusters() {
        List<Cluster> trustedClusters = dao.getTrustedClusters();

        assertNotNull(trustedClusters);
        assertFalse(trustedClusters.isEmpty());
        assertEquals(trustedClusters.size(), NUMBER_OF_TRUSTED_GROUPS);
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.CLUSTER_RHEL6_NFS)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.CLUSTER_RHEL6_NFS_2)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.CLUSTER_RHEL6_LOCALFS)));
        assertTrue(trustedClusters.contains(dao.get(FixturesTool.CLUSTER_RHEL6_NFS_NO_SPECIFIC_QUOTAS)));
    }

    /**
     * Test that the correct cluster is fetched when querying by cluster policy id
     */
    @Test
    public void testGetClusterByClusterPolicyId() {
        List<Cluster> result = dao.getClustersByClusterPolicyId(FixturesTool.CLUSTER_POLICY_EVEN_DISTRIBUTION);
        List<Guid> clusterIdList = new ArrayList<>();
        for (Cluster group : result) {
            clusterIdList.add(group.getId());
        }

        assertTrue(clusterIdList.contains(FixturesTool.CLUSTER_RHEL6_ISCSI));
        assertTrue(clusterIdList.contains(FixturesTool.CLUSTER_NO_RUNNING_VMS));
        assertTrue(clusterIdList.contains(FixturesTool.CLUSTER_RHEL6_NFS));
    }

    /**
     * Test that no cluster is fetched when querying by wrong cluster policy id
     */
    @Test
    public void testGetClusterByClusterPolicyIdNegative() {
        List<Cluster> result = dao.getClustersByClusterPolicyId(Guid.newGuid());

        assertTrue(result == null || result.size() == 0);
    }

    /**
    * Ensure that only clusters where currently no VMs are migrating are found
    */
    @Test
    public void testGetAllClustersWithoutMigratingVMs() {
        List<Cluster> migrationFreeClusters = dao.getWithoutMigratingVms();
        assertFalse(migrationFreeClusters.isEmpty());

        final int migrationCount = migrationFreeClusters.size();
        // get a cluster with migrating VMs
        List<VM> vms = dbFacade.getVmDao().getAllRunningByCluster(FixturesTool.CLUSTER_RHEL6_ISCSI);
        // set every VM to UP
        for(VM migratingVM : vms) {
            dbFacade.getVmDynamicDao().updateStatus(migratingVM.getId(), VMStatus.Up);
            migrationFreeClusters = dao.getWithoutMigratingVms();
        }
        assertEquals(migrationCount + 1, migrationFreeClusters.size());
    }

    /**
     * Asserts the result from {@link ClusterDao#getAll()} is correct without filtering
     */
    private void assertCorrectGetAllResult(List<Cluster> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_GROUPS, result.size());
    }

    @Test
    public void testGetVmsCountByClusterId() {
        // Cluster with no VMs
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(FixturesTool.CLUSTER_RHEL6_NFS),
                FixturesTool.NUMBER_OF_VMS_IN_CLUSTER_RHEL6_NFS_CLUSTER);

        // Cluster with VMs
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(FixturesTool.CLUSTER_RHEL6_ISCSI),
                FixturesTool.NUMBER_OF_VMS_IN_CLUSTER_RHEL6_ISCSI);

        // Non existing cluster, should return 0
        assertEquals("Incorrect number of VMs in cluster", dao.getVmsCountByClusterId(Guid.newGuid()), 0);
    }

    @Test
    public void testGetVmHostCount() {
        Guid guid = FixturesTool.CLUSTER_RHEL6_ISCSI;
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(dao.get(guid));
        List<Cluster> data = ((ClusterDaoImpl) dao).getHostsAndVmsForClusters(clusters);
        assertEquals("Incorrect number of VMs in cluster", data.get(0).getClusterHostsAndVms().getVms(), 7);
        assertEquals("Incorrect number of Hosts in cluster", data.get(0).getClusterHostsAndVms().getHosts(), 1);
    }

    @Test
    public void testGetClustersByServiceAndCompatibilityVersion() {
        List<Cluster> clusters = dao.getClustersByServiceAndCompatibilityVersion(true, false, "2.3");
        assertNotNull(clusters);
        assertEquals(1, clusters.size());
        assertEquals(FixturesTool.GLUSTER_CLUSTER_ID, clusters.get(0).getId());
    }

    @Test
    public void testGetClustersHavingHosts() {
        List<Cluster> clusters = dao.getClustersHavingHosts();
        assertNotNull(clusters);
        assertThat(clusters, hasSize(4));
    }
}

package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuLevelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PinToHostPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityWeightPolicyUnit;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class InternalClusterPoliciesTest {
    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.ExternalSchedulerEnabled, false),
            MockConfigRule.mockConfig(ConfigValues.EnableVdsLoadBalancing, false),
            MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE),
            MockConfigRule.mockConfig(ConfigValues.SpmVmGraceForEvenGuestDistribute, 10),
            MockConfigRule.mockConfig(ConfigValues.MigrationThresholdForEvenGuestDistribute, 5),
            MockConfigRule.mockConfig(ConfigValues.HighVmCountForEvenGuestDistribute, 10)
    );

    @Test
    public void testConfiguredPolicyCreation() {
        assertNotNull(InternalClusterPolicies.getClusterPolicies());
        assertNotEquals(0, (long)InternalClusterPolicies.getClusterPolicies().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailureToAddUnitBadType() {
        InternalClusterPolicies.createBuilder(UUID.randomUUID().toString())
                .addFunction(1, InternalPolicyUnitsTest.DummyUnit.class)
                .getPolicy();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailureToAddUnitNotEnabled() {
        InternalClusterPolicies.createBuilder(UUID.randomUUID().toString())
                .addFilters(InternalPolicyUnitsTest.NotEnabledDummyUnit.class)
                .getPolicy();
    }

    @Test
    public void testPolicyCreation() throws Exception {
        Guid uuid = Guid.newGuid();
        ClusterPolicy policy = InternalClusterPolicies.createBuilder(uuid.toString())
                .name("test-policy")
                .isDefault()
                .description("test-description")
                .set(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES, "5")
                .set(PolicyUnitParameter.SPM_VM_GRACE, "1")
                .setBalancer(EvenDistributionBalancePolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(PinToHostPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(2, VmAffinityWeightPolicyUnit.class)
                .getPolicy();

        assertNotNull(policy);
        assertEquals(uuid, policy.getId());
        assertEquals("test-policy", policy.getName());
        assertEquals("test-description", policy.getDescription());
        assertEquals(true, policy.isDefaultPolicy());
        assertEquals(true, policy.isLocked());

        assertNotNull(policy.getFilterPositionMap());
        assertEquals(-1L, (long) policy.getFilterPositionMap().get(getUnitId(CpuLevelFilterPolicyUnit.class)));
        assertEquals(0, (long) policy.getFilterPositionMap().get(getUnitId(MemoryPolicyUnit.class)));
        assertEquals(1L, (long) policy.getFilterPositionMap().get(getUnitId(PinToHostPolicyUnit.class)));

        assertNotNull(policy.getParameterMap());
        assertEquals(2, policy.getParameterMap().size());
        assertEquals("5", policy.getParameterMap().get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()));
        assertEquals("1", policy.getParameterMap().get(PolicyUnitParameter.SPM_VM_GRACE.getDbName()));

        assertNotNull(policy.getFunctions());
        assertNotNull(policy.getFilters());

        assertEquals(3, policy.getFilters().size());
        assertEquals(2, policy.getFunctions().size());

        Map<Guid, Integer> funcMap = new HashMap<>();
        for (Pair<Guid, Integer> pair: policy.getFunctions()) {
            funcMap.put(pair.getFirst(), pair.getSecond());
        }

        assertEquals(1, (long)funcMap.get(getUnitId(HaReservationWeightPolicyUnit.class)));
        assertEquals(2, (long)funcMap.get(getUnitId(VmAffinityWeightPolicyUnit.class)));
    }

    private Guid getUnitId(Class<? extends PolicyUnitImpl> unit) {
        return Guid.createGuidFromString(unit.getAnnotation(SchedulingUnit.class).guid());
    }
}

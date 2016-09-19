package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.policyunits.CPUPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CompatibilityVersionFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuLevelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.CpuPinningPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EmulatedMachineFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenDistributionMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.EvenGuestDistributionWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HaReservationWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostDeviceFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.HostedEngineHAClusterWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.LabelFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MemoryPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.MigrationPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NetworkPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.NoneBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PinToHostPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingBalancePolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingCPUWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.PowerSavingMemoryWeightPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityFilterPolicyUnit;
import org.ovirt.engine.core.bll.scheduling.policyunits.VmAffinityWeightPolicyUnit;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class InternalClusterPolicies {
    private static final Map<Guid, ClusterPolicy> clusterPolicies = new HashMap<>();

    static {
        createBuilder("b4ed2332-a7ac-4d5f-9596-99a439cb2812")
                .name("none")
                .isDefault()
                .setBalancer(NoneBalancePolicyUnit.class)

                .addFilters(PinToHostPolicyUnit.class)
                .addFilters(LabelFilterPolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(EmulatedMachineFilterPolicyUnit.class)
                .addFilters(HostDeviceFilterPolicyUnit.class)
                .addFilters(HostedEngineHAClusterFilterPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(MigrationPolicyUnit.class)
                .addFilters(VmAffinityFilterPolicyUnit.class)
                .addFilters(NetworkPolicyUnit.class)
                .addFilters(CpuPinningPolicyUnit.class)
                .addFilters(CompatibilityVersionFilterPolicyUnit.class)

                .addFunction(1, EvenDistributionCPUWeightPolicyUnit.class)
                .addFunction(1, EvenDistributionMemoryWeightPolicyUnit.class)
                .addFunction(1, HostedEngineHAClusterWeightPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(1, VmAffinityWeightPolicyUnit.class)
                .register();

        createBuilder("20d25257-b4bd-4589-92a6-c4c5c5d3fd1a")
                .name("evenly_distributed")
                .setBalancer(EvenDistributionBalancePolicyUnit.class)

                .addFilters(PinToHostPolicyUnit.class)
                .addFilters(LabelFilterPolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(EmulatedMachineFilterPolicyUnit.class)
                .addFilters(HostDeviceFilterPolicyUnit.class)
                .addFilters(HostedEngineHAClusterFilterPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(MigrationPolicyUnit.class)
                .addFilters(VmAffinityFilterPolicyUnit.class)
                .addFilters(NetworkPolicyUnit.class)
                .addFilters(CpuPinningPolicyUnit.class)
                .addFilters(CompatibilityVersionFilterPolicyUnit.class)

                .addFunction(1, EvenDistributionCPUWeightPolicyUnit.class)
                .addFunction(1, EvenDistributionMemoryWeightPolicyUnit.class)
                .addFunction(1, HostedEngineHAClusterWeightPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(1, VmAffinityWeightPolicyUnit.class)

                .set(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES, "2")
                .set(PolicyUnitParameter.HIGH_UTILIZATION, "80")
                .register();

        createBuilder("5a2b0939-7d46-4b73-a469-e9c2c7fc6a53")
                .name("power_saving")
                .setBalancer(PowerSavingBalancePolicyUnit.class)

                .addFilters(PinToHostPolicyUnit.class)
                .addFilters(LabelFilterPolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(EmulatedMachineFilterPolicyUnit.class)
                .addFilters(HostDeviceFilterPolicyUnit.class)
                .addFilters(HostedEngineHAClusterFilterPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(MigrationPolicyUnit.class)
                .addFilters(VmAffinityFilterPolicyUnit.class)
                .addFilters(NetworkPolicyUnit.class)
                .addFilters(CpuPinningPolicyUnit.class)
                .addFilters(CompatibilityVersionFilterPolicyUnit.class)

                .addFunction(1, PowerSavingCPUWeightPolicyUnit.class)
                .addFunction(1, PowerSavingMemoryWeightPolicyUnit.class)
                .addFunction(1, HostedEngineHAClusterWeightPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(1, VmAffinityWeightPolicyUnit.class)

                .set(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES, "2")
                .set(PolicyUnitParameter.HIGH_UTILIZATION, "80")
                .set(PolicyUnitParameter.LOW_UTILIZATION, "20")
                .register();

        createBuilder("8d5d7bec-68de-4a67-b53e-0ac54686d579")
                .name("vm_evenly_distributed")
                .setBalancer(EvenGuestDistributionBalancePolicyUnit.class)

                .addFilters(PinToHostPolicyUnit.class)
                .addFilters(LabelFilterPolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(EmulatedMachineFilterPolicyUnit.class)
                .addFilters(HostDeviceFilterPolicyUnit.class)
                .addFilters(HostedEngineHAClusterFilterPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(MigrationPolicyUnit.class)
                .addFilters(VmAffinityFilterPolicyUnit.class)
                .addFilters(NetworkPolicyUnit.class)
                .addFilters(CpuPinningPolicyUnit.class)
                .addFilters(CompatibilityVersionFilterPolicyUnit.class)

                .addFunction(1, EvenGuestDistributionWeightPolicyUnit.class)
                .addFunction(1, HostedEngineHAClusterWeightPolicyUnit.class)
                .addFunction(1, HaReservationWeightPolicyUnit.class)
                .addFunction(1, VmAffinityWeightPolicyUnit.class)

                .set(PolicyUnitParameter.HIGH_VM_COUNT, "10")
                .set(PolicyUnitParameter.MIGRATION_THRESHOLD, "5")
                .set(PolicyUnitParameter.SPM_VM_GRACE, "5")
                .register();

        createBuilder("8d5d7bec-68de-4a67-b53e-0ac54686d586")
                .name("InClusterUpgrade")
                .setBalancer(NoneBalancePolicyUnit.class)
                .addFilters(EmulatedMachineFilterPolicyUnit.class)
                .addFilters(NetworkPolicyUnit.class)
                .addFilters(MigrationPolicyUnit.class)
                .addFilters(MemoryPolicyUnit.class)
                .addFilters(CPUPolicyUnit.class)
                .addFilters(CpuLevelFilterPolicyUnit.class)
                .addFilters(InClusterUpgradeFilterPolicyUnit.class)
                .addFunction(1, InClusterUpgradeWeightPolicyUnit.class)
                .register();
    }

    public static Map<Guid, ClusterPolicy> getClusterPolicies() {
        return Collections.unmodifiableMap(clusterPolicies);
    }

    protected static PolicyBuilder createBuilder(String guid) {
        final Guid realGuid = Guid.createGuidFromString(guid);
        final PolicyBuilder builder = new PolicyBuilder(realGuid);
        return builder;
    }

    protected static final class PolicyBuilder {
        final ClusterPolicy policy;

        private PolicyBuilder(Guid id) {
            policy = new ClusterPolicy();
            policy.setId(id);
            policy.setFilters(new ArrayList<>());
            policy.setFilterPositionMap(new HashMap<>());
            policy.setFunctions(new ArrayList<>());
            policy.setParameterMap(new HashMap<>());
            policy.setLocked(true);
        }

        public final ClusterPolicy getPolicy() {
            return policy;
        }

        public final PolicyBuilder name(String name) {
            policy.setName(name);
            return this;
        }

        public final PolicyBuilder description(String description) {
            policy.setDescription(description);
            return this;
        }

        public final PolicyBuilder isDefault() {
            policy.setDefaultPolicy(true);
            return this;
        }

        public final PolicyBuilder set(PolicyUnitParameter parameter, String value) {
            // This is only executed during application startup (class loading in fact)
            // and should never fail or we have a bug in the static initializer of this
            // class.
            assert parameter.validValue(value);

            policy.getParameterMap().put(parameter.getDbName(), value);

            return this;
        }

        @SafeVarargs
        public final PolicyBuilder addFilters(Class<? extends PolicyUnitImpl>... filters) {
            for (Class<? extends PolicyUnitImpl> filter: filters) {
                Guid guid = getGuidAndValidateType(filter, PolicyUnitType.FILTER);

                // Previously last item, but do not touch the first one
                if (policy.getFilters().size() >= 2) {
                    Guid last = policy.getFilters().get(policy.getFilters().size() - 1);
                    policy.getFilterPositionMap().put(last, 0);
                }

                // Mark first added item as first and any other as last
                if (policy.getFilters().isEmpty()) {
                    policy.getFilterPositionMap().put(guid, -1);
                } else {
                    policy.getFilterPositionMap().put(guid, 1);
                }

                policy.getFilters().add(guid);
            }
            return this;
        }

        @SafeVarargs
        public final PolicyBuilder addFunction(Integer factor, Class<? extends PolicyUnitImpl>... functions) {
            for (Class<? extends PolicyUnitImpl> function: functions) {
                Guid guid = getGuidAndValidateType(function, PolicyUnitType.WEIGHT);
                policy.getFunctions().add(new Pair<>(guid, factor));
            }
            return this;
        }

        public final PolicyBuilder setBalancer(Class<? extends PolicyUnitImpl> balancer) {
            policy.setBalance(getGuidAndValidateType(balancer, PolicyUnitType.LOAD_BALANCING));
            return this;
        }

        private Guid getGuidAndValidateType(Class<? extends PolicyUnitImpl> unit, PolicyUnitType expectedType) {
            SchedulingUnit guid = unit.getAnnotation(SchedulingUnit.class);

            // This is only executed during application startup (class loading in fact)
            // and should never fail or we have a bug in the static initializer of this
            // class.
            if (guid == null) {
                throw new IllegalArgumentException(unit.getName()
                        + " is missing the required SchedulingUnit annotation metadata.");
            }

            if (expectedType != guid.type()) {
                throw new IllegalArgumentException("Type " + expectedType.name() + " expected, but unit "
                        + unit.getName() + " is of type " + guid.type().name());
            }

            if (!InternalPolicyUnits.getList().contains(unit)) {
                throw new IllegalArgumentException("Policy unit " + unit.getName() + " is not present"
                        + " in the list of enabled internal policy units.");
            }

            return Guid.createGuidFromString(guid.guid());
        }

        private PolicyBuilder register() {
            clusterPolicies.put(policy.getId(), policy);
            return this;
        }
    }
}

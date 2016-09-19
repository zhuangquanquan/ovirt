package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.ManageNetworkClustersParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class DetachNetworkToClusterCommand<T extends AttachNetworkToClusterParameter> extends ClusterCommandBase<T> {

    private Network persistedNetwork;

    @Inject
    private DetachNetworkClusterPermissionFinder permissionFinder;

    public DetachNetworkToClusterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        final VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.DetachNetworkFromClusterInternal, getParameters());

        setSucceeded(returnValue.getSucceeded());

        if (returnValue.getSucceeded()) {
            if (NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getPersistedNetwork())) {
                detachLabeledNetworksFromClusterHosts();
            }
        } else {
            propagateFailure(returnValue);
        }
    }

    private void detachLabeledNetworksFromClusterHosts() {
        final AttachNetworkToClusterParameter attachNetworkToClusterParameter = getParameters();

        runInternalAction(
                VdcActionType.PropagateLabeledNetworksToClusterHosts,
                new ManageNetworkClustersParameters(
                        Collections.<NetworkCluster>emptyList(),
                        Collections.singleton(attachNetworkToClusterParameter.getNetworkCluster())));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_DETACH_NETWORK_TO_CLUSTER
                : AuditLogType.NETWORK_DETACH_NETWORK_TO_CLUSTER_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return permissionFinder.findPermissionCheckSubjects(getNetworkId(), getActionType());
    }

    private Guid getNetworkId() {
        return getNetwork() == null ? null : getNetwork().getId();
    }

    private Network getNetwork() {
        return getParameters().getNetwork();
    }

    private Network getPersistedNetwork() {
        if (persistedNetwork == null) {
            persistedNetwork = getNetworkDao().get(getNetwork().getId());
        }

        return persistedNetwork;
    }
}

package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHostData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NewHostModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NewEditStorageModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ITaskTarget;
import org.ovirt.engine.ui.uicompat.Task;
import org.ovirt.engine.ui.uicompat.TaskContext;

import com.google.gwt.user.client.Timer;

public class DataCenterGuideModel extends GuideModel implements ITaskTarget {

    public final String DataCenterConfigureClustersAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureClustersAction();
    public final String DataCenterAddAnotherClusterAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAddAnotherClusterAction();
    public final String DataCenterConfigureHostsAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureHostsAction();
    public final String DataCenterAddAnotherHostAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAddAnotherHostAction();
    public final String DataCenterSelectHostsAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterSelectHostsAction();
    public final String DataCenterConfigureStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureStorageAction();
    public final String DataCenterAddMoreStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAddMoreStorageAction();
    public final String DataCenterAttachStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachStorageAction();
    public final String DataCenterAttachMoreStorageAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachMoreStorageAction();
    public final String DataCenterConfigureISOLibraryAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterConfigureISOLibraryAction();
    public final String DataCenterAttachISOLibraryAction = ConstantsManager.getInstance()
            .getConstants()
            .dataCenterAttachISOLibraryAction();

    public final String NoUpHostReason = ConstantsManager.getInstance().getConstants().noUpHostReason();
    public final String NoDataDomainAttachedReason = ConstantsManager.getInstance()
            .getConstants()
            .noDataDomainAttachedReason();

    private StorageDomainStatic storageDomain;
    private TaskContext context;
    private IStorageModel storageModel;
    private Guid storageId;
    private StorageServerConnections connection;
    private Guid hostId = Guid.Empty;
    private String path;
    private boolean removeConnection;
    private ArrayList<Cluster> clusters;
    private ArrayList<StorageDomain> allStorageDomains;
    private ArrayList<StorageDomain> attachedStorageDomains;
    private ArrayList<StorageDomain> isoStorageDomains;
    private List<VDS> allHosts;
    private VDS localStorageHost;
    private boolean noLocalStorageHost;

    public DataCenterGuideModel() {
    }

    @Override
    public StoragePool getEntity() {
        return (StoragePool) super.getEntity();
    }

    public void setEntity(StoragePool value) {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        updateOptions();
    }

    private void updateOptionsNonLocalFSData() {
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        dataCenterGuideModel.clusters = (ArrayList<Cluster>) returnValue;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                                                                            new INewAsyncCallback() {
                                                                                @Override
                                                                                public void onSuccess(Object target, Object returnValue) {
                                                                                    DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                                                                    dataCenterGuideModel.allStorageDomains = (ArrayList<StorageDomain>) returnValue;
                                                                                    dataCenterGuideModel.updateOptionsNonLocalFS();
                                                                                }
                                                                            }));

        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        dataCenterGuideModel.attachedStorageDomains = (ArrayList<StorageDomain>) returnValue;
                        dataCenterGuideModel.updateOptionsNonLocalFS();
                    }
                }), getEntity().getId());

        AsyncDataProvider.getInstance().getISOStorageDomainList(new AsyncQuery(this,
                                                                               new INewAsyncCallback() {
                                                                                   @Override
                                                                                   public void onSuccess(Object target, Object returnValue) {
                                                                                       DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                                                                       dataCenterGuideModel.isoStorageDomains = (ArrayList<StorageDomain>) returnValue;
                                                                                       dataCenterGuideModel.updateOptionsNonLocalFS();
                                                                                   }
                                                                               }));

        AsyncDataProvider.getInstance().getHostList(new AsyncQuery(this,
                                                                   new INewAsyncCallback() {
                                                                       @Override
                                                                       public void onSuccess(Object target, Object returnValue) {
                                                                           DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                                                           dataCenterGuideModel.allHosts = (ArrayList<VDS>) returnValue;
                                                                           dataCenterGuideModel.updateOptionsNonLocalFS();
                                                                       }
                                                                   }));
    }

    private void updateOptionsLocalFSData() {
        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        dataCenterGuideModel.clusters = (ArrayList<Cluster>) returnValue;
                        dataCenterGuideModel.updateOptionsLocalFS();
                    }
                }), getEntity().getId());

        Frontend.getInstance().runQuery(VdcQueryType.Search, new SearchParameters("Hosts: datacenter!= " + getEntity().getName() //$NON-NLS-1$
                + " status=maintenance or status=pendingapproval ", SearchType.VDS), new AsyncQuery(this, //$NON-NLS-1$
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        List<VDS> hosts =
                                ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (hosts == null) {
                            hosts = new ArrayList<>();
                        }
                        dataCenterGuideModel.allHosts = hosts;
                        AsyncDataProvider.getInstance().getLocalStorageHost(new AsyncQuery(dataCenterGuideModel,
                                new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object target, Object returnValue) {
                                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                        if (returnValue != null) {
                                            dataCenterGuideModel.localStorageHost = (VDS) returnValue;
                                        } else {
                                            noLocalStorageHost = true;
                                        }
                                        dataCenterGuideModel.updateOptionsLocalFS();
                                    }
                                }), dataCenterGuideModel.getEntity().getName());
                    }
                }));
    }

    private void updateOptionsNonLocalFS() {
        if (clusters == null || allStorageDomains == null || attachedStorageDomains == null
                || isoStorageDomains == null || allHosts == null) {
            return;
        }

        updateAddClusterAvailability();

        ArrayList<VDS> hosts = new ArrayList<>();
        ArrayList<VDS> availableHosts = new ArrayList<>();
        ArrayList<VDS> upHosts = new ArrayList<>();
        for (VDS vds : allHosts) {
            if (Linq.isClusterItemExistInList(clusters, vds.getClusterId())) {
                hosts.add(vds);
            }

            if ((vds.getStatus() == VDSStatus.Maintenance || vds.getStatus() == VDSStatus.PendingApproval)
                && doesHostSupportAnyCluster(clusters, vds)) {
                availableHosts.add(vds);
            }

            if (vds.getStatus() == VDSStatus.Up && Linq.isClusterItemExistInList(clusters, vds.getClusterId())) {
                upHosts.add(vds);
            }
        }

        updateAddAndSelectHostAvailability(hosts, availableHosts);

        List<StorageDomain> unattachedStorages = getUnattachedStorages();

        List<StorageDomain> attachedDataStorages = new ArrayList<>();
        List<StorageDomain> attachedIsoStorages = new ArrayList<>();
        for (StorageDomain sd : attachedStorageDomains) {
            if (sd.getStorageDomainType().isDataDomain()) {
                attachedDataStorages.add(sd);
            }
            else if (sd.getStorageDomainType() == StorageDomainType.ISO) {
                attachedIsoStorages.add(sd);
            }
        }

        updateAddAndAttachDataDomainAvailability(upHosts, unattachedStorages, attachedDataStorages);

        updateAddAndAttachIsoDomainAvailability(upHosts, attachedDataStorages, attachedIsoStorages);

        stopProgress();
    }

    private void updateAddAndSelectHostAvailability(ArrayList<VDS> hosts, ArrayList<VDS> availableHosts) {
        UICommand addHostAction = new UICommand("AddHost", this); //$NON-NLS-1$
        addHostAction.setIsExecutionAllowed(clusters.size() > 0);

        if (hosts.isEmpty()) {
            addHostAction.setTitle(DataCenterConfigureHostsAction);
            getCompulsoryActions().add(addHostAction);
        } else {
            addHostAction.setTitle(DataCenterAddAnotherHostAction);
            getOptionalActions().add(addHostAction);
        }

        // Select host action.
        UICommand selectHostAction = new UICommand("SelectHost", this); //$NON-NLS-1$

        // If now compatible hosts are found - disable the select host button
        selectHostAction.setIsChangeable(availableHosts.size() > 0);
        selectHostAction.setIsExecutionAllowed(availableHosts.size() > 0);

        if (clusters.size() > 0) {
            if (hosts.isEmpty()) {
                selectHostAction.setTitle(DataCenterSelectHostsAction);
                getCompulsoryActions().add(selectHostAction);
            }
            else {
                selectHostAction.setTitle(DataCenterSelectHostsAction);
                getOptionalActions().add(selectHostAction);
            }
        }
    }

    private List<StorageDomain> getUnattachedStorages() {
        List<StorageDomain> unattachedStorage = new ArrayList<>();
        for (StorageDomain item : allStorageDomains) {
            if (item.getStorageDomainType() == StorageDomainType.Data
                    && item.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
                if (getEntity().getStoragePoolFormatType() == null ||
                        getEntity().getStoragePoolFormatType() == item.getStorageStaticData().getStorageFormat()) {
                    unattachedStorage.add(item);
                }
            }
        }
        return unattachedStorage;
    }

    private void updateAddClusterAvailability() {
        // Add cluster action.
        UICommand addClusterAction = new UICommand("AddCluster", this); //$NON-NLS-1$
        if (clusters.isEmpty()) {
            addClusterAction.setTitle(DataCenterConfigureClustersAction);
            getCompulsoryActions().add(addClusterAction);
        }
        else {
            addClusterAction.setTitle(DataCenterAddAnotherClusterAction);
            getOptionalActions().add(addClusterAction);
        }
    }

    // Attach ISO storage action.
    // Allow to attach ISO domain only when there are Data storages attached
    // and there ISO storages to attach and there are no ISO storages actually
    // attached.
    private void updateAddAndAttachIsoDomainAvailability(List<VDS> upHosts, List<StorageDomain> attachedDataStorages, List<StorageDomain> attachedIsoStorages) {
        boolean attachOrAddIsoAvailable = attachedIsoStorages.isEmpty();
        boolean masterStorageExistsAndRunning = Linq.isAnyStorageDomainIsMasterAndActive(attachedDataStorages);
        boolean addIsoAllowed =
                attachedDataStorages.size() > 0 && masterStorageExistsAndRunning
                        && attachedIsoStorages.isEmpty() && upHosts.size() > 0;


        if (attachOrAddIsoAvailable) {
            UICommand addIsoStorageAction = new UICommand("AddIsoStorage", this); //$NON-NLS-1$
            addIsoStorageAction.setTitle(DataCenterConfigureISOLibraryAction);
            getOptionalActions().add(addIsoStorageAction);

            UICommand attachIsoStorageAction = new UICommand("AttachIsoStorage", this); //$NON-NLS-1$
            attachIsoStorageAction.setTitle(DataCenterAttachISOLibraryAction);
            getOptionalActions().add(attachIsoStorageAction);

            if (!masterStorageExistsAndRunning) {
                addIsoStorageAction.getExecuteProhibitionReasons().add(NoDataDomainAttachedReason);
                attachIsoStorageAction.getExecuteProhibitionReasons().add(NoDataDomainAttachedReason);
            }

            if (upHosts.isEmpty()) {
                addIsoStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
                attachIsoStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
            }

            addIsoStorageAction.setIsExecutionAllowed(addIsoAllowed);
            attachIsoStorageAction.setIsExecutionAllowed(addIsoAllowed && isoStorageDomains.size() > 0);
        }
    }

    private void updateAddAndAttachDataDomainAvailability(List<VDS> upHosts, List<StorageDomain> unattachedStorage, List<StorageDomain> attachedDataStorages) {
        UICommand addDataStorageAction = new UICommand("AddDataStorage", this); //$NON-NLS-1$
        addDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
        addDataStorageAction.setIsExecutionAllowed(upHosts.size() > 0);

        if (unattachedStorage.isEmpty() && attachedDataStorages.isEmpty()) {
            addDataStorageAction.setTitle(DataCenterConfigureStorageAction);
            getCompulsoryActions().add(addDataStorageAction);
        }
        else {
            addDataStorageAction.setTitle(DataCenterAddMoreStorageAction);
            getOptionalActions().add(addDataStorageAction);
        }

        // Attach data storage action.
        UICommand attachDataStorageAction = new UICommand("AttachDataStorage", this); //$NON-NLS-1$
        if (upHosts.isEmpty()) {
            attachDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
        }
        attachDataStorageAction.setIsExecutionAllowed(unattachedStorage.size() > 0 && upHosts.size() > 0);

        if (attachedDataStorages.isEmpty()) {
            attachDataStorageAction.setTitle(DataCenterAttachStorageAction);
            getCompulsoryActions().add(attachDataStorageAction);
        }
        else {
            attachDataStorageAction.setTitle(DataCenterAttachMoreStorageAction);
            getOptionalActions().add(attachDataStorageAction);
        }
    }

    private boolean doesHostSupportAnyCluster(List<Cluster> clusterList, VDS host){
        for (Cluster cluster : clusterList){
            if (host.getSupportedClusterVersionsSet().contains(cluster.getCompatibilityVersion())){
                return true;
            }
        }
        return false;
    }

    private void updateOptionsLocalFS() {
        if (clusters == null || allHosts == null || (localStorageHost == null && !noLocalStorageHost)) {
            return;
        }

        UICommand addClusterAction = new UICommand("AddCluster", this); //$NON-NLS-1$
        if (clusters.isEmpty()) {
            addClusterAction.setTitle(DataCenterConfigureClustersAction);
            getCompulsoryActions().add(addClusterAction);
        }
        else {
            UICommand addHostAction = new UICommand("AddHost", this); //$NON-NLS-1$
            addHostAction.setTitle(DataCenterConfigureHostsAction);
            UICommand selectHost = new UICommand("SelectHost", this); //$NON-NLS-1$
            selectHost.setTitle(DataCenterSelectHostsAction);

            if (localStorageHost != null) {
                String hasHostReason =
                        ConstantsManager.getInstance().getConstants().localDataCenterAlreadyContainsAHostDcGuide();
                addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
                addHostAction.setIsExecutionAllowed(false);
                selectHost.getExecuteProhibitionReasons().add(hasHostReason);
                selectHost.setIsExecutionAllowed(false);
                if (localStorageHost.getStatus() == VDSStatus.Up) {
                    UICommand addLocalStorageAction = new UICommand("AddLocalStorage", this); //$NON-NLS-1$
                    addLocalStorageAction.setTitle(ConstantsManager.getInstance().getConstants().addLocalStorageTitle());
                    getOptionalActions().add(addLocalStorageAction);
                }

                getNote().setIsAvailable(true);
                getNote().setEntity(ConstantsManager.getInstance().getConstants().attachLocalStorageDomainToFullyConfigure());
            }
            else if (getEntity().getStatus() != StoragePoolStatus.Uninitialized) {
                String dataCenterInitializeReason =
                        ConstantsManager.getInstance().getConstants().dataCenterWasAlreadyInitializedDcGuide();
                addHostAction.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
                addHostAction.setIsExecutionAllowed(false);
                selectHost.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
                selectHost.setIsExecutionAllowed(false);
            }

            getOptionalActions().add(selectHost);
            getCompulsoryActions().add(addHostAction);
        }

        stopProgress();
    }

    private void updateOptions() {
        getCompulsoryActions().clear();
        getOptionalActions().clear();

        if (getEntity() != null) {
            startProgress();

            if (!getEntity().isLocal()) {
                updateOptionsNonLocalFSData();
            }
            else {
                updateOptionsLocalFSData();
            }
        }
    }

    private void resetData() {
        storageDomain = null;
        storageModel = null;
        storageId = null;
        connection = null;
        removeConnection = false;
        path = null;
        hostId = Guid.Empty;
        clusters = null;
        allStorageDomains = null;
        attachedStorageDomains = null;
        isoStorageDomains = null;
        allHosts = null;
        localStorageHost = null;
        noLocalStorageHost = false;
    }

    private void addLocalStorage() {
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newLocalDomainTitle());
        model.setHelpTag(HelpTag.new_local_domain);
        model.setHashName("new_local_domain"); //$NON-NLS-1$
        LocalStorageModel localStorageModel = new LocalStorageModel();
        localStorageModel.setRole(StorageDomainType.Data);

        ArrayList<IStorageModel> list = new ArrayList<>();
        list.add(localStorageModel);
        model.setStorageModels(list);
        model.setCurrentStorageItem(list.get(0));

        AsyncDataProvider.getInstance().getLocalStorageHost(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel listModel = (DataCenterGuideModel) array[0];
                        StorageModel model = (StorageModel) array[1];
                        VDS localHost = (VDS) returnValue;

                        model.getHost()
                                .setItems(new ArrayList<>(Arrays.asList(new VDS[]{localHost})));
                        model.getHost().setSelectedItem(localHost);
                        model.getDataCenter().setItems(Collections.singletonList(getEntity()), getEntity());
                        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAddStorage", listModel); //$NON-NLS-1$
                        model.getCommands().add(tempVar);
                        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", listModel); //$NON-NLS-1$
                        model.getCommands().add(tempVar2);
                    }
                }),
                getEntity().getName());
    }

    public void addIsoStorage() {
        addStorageInternal(ConstantsManager.getInstance().getConstants().newISOLibraryTitle(), StorageDomainType.ISO);
    }

    public void addDataStorage() {
        addStorageInternal(ConstantsManager.getInstance().getConstants().newStorageTitle(), StorageDomainType.Data);
    }

    private void addStorageInternal(String title, StorageDomainType type) {
        StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
        setWindow(model);
        model.setTitle(title);
        model.setHelpTag(HelpTag.new_domain);
        model.setHashName("new_domain"); //$NON-NLS-1$
        ArrayList<StoragePool> dataCenters = new ArrayList<>();
        dataCenters.add(getEntity());
        model.getDataCenter().setItems(dataCenters, getEntity());
        model.getDataCenter().setIsChangeable(false);

        List<IStorageModel> items = null;

        if (type == StorageDomainType.Data) {
            items = AsyncDataProvider.getInstance().getDataStorageModels();
        }
        else if (type == StorageDomainType.ISO) {
            items = AsyncDataProvider.getInstance().getIsoStorageModels();
        }

        model.setStorageModels(items);

        model.initialize();

        UICommand tempVar6 = UICommand.createDefaultOkUiCommand("OnAddStorage", this); //$NON-NLS-1$
        model.getCommands().add(tempVar6);
        UICommand tempVar7 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar7);
    }

    public void onAddStorage() {
        StorageModel model = (StorageModel) getWindow();
        String storageName = model.getName().getEntity();

        AsyncDataProvider.getInstance().isStorageDomainNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();
                        String name = storageModel.getName().getEntity();
                        String tempVar = storageModel.getOriginalName();
                        String originalName = (tempVar != null) ? tempVar : ""; //$NON-NLS-1$
                        boolean isNameUnique = (Boolean) returnValue;
                        if (!isNameUnique && name.compareToIgnoreCase(originalName) != 0) {
                            storageModel.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            storageModel.getName().setIsValid(false);
                        }
                        AsyncDataProvider.getInstance().getStorageDomainMaxNameLength(new AsyncQuery(dataCenterGuideModel,
                                                                                                     new INewAsyncCallback() {
                                                                                                         @Override
                                                                                                         public void onSuccess(Object target1, Object returnValue1) {

                                                                                                             DataCenterGuideModel dataCenterGuideModel1 = (DataCenterGuideModel) target1;
                                                                                                             StorageModel storageModel1 = (StorageModel) dataCenterGuideModel1.getWindow();
                                                                                                             int nameMaxLength = (Integer) returnValue1;
                                                                                                             RegexValidation tempVar2 = new RegexValidation();
                                                                                                             tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$"); //$NON-NLS-1$ //$NON-NLS-2$
                                                                                                             tempVar2.setMessage(ConstantsManager.getInstance().getMessages()
                                                                                                                                         .nameCanContainOnlyMsg(nameMaxLength));
                                                                                                             storageModel1.getName().validateEntity(new IValidation[] {
                                                                                                                     new NotEmptyValidation(), tempVar2});
                                                                                                             dataCenterGuideModel1.postOnAddStorage();

                                                                                                         }
                                                                                                     }));

                    }
                }),
                storageName);
    }

    public void postOnAddStorage() {
        StorageModel model = (StorageModel) getWindow();

        if (!model.validate()) {
            return;
        }

        // Save changes.
        if (model.getCurrentStorageItem() instanceof NfsStorageModel) {
            saveNfsStorage();
        }
        else if (model.getCurrentStorageItem() instanceof LocalStorageModel) {
            saveLocalStorage();
        }
        else {
            saveSanStorage();
        }
    }

    private void saveLocalStorage() {
        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveLocal"}))).run(); //$NON-NLS-1$
    }

    private void saveLocalStorage(TaskContext context) {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        VDS host = model.getHost().getSelectedItem();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        LocalStorageModel localModel = (LocalStorageModel) storageModel;
        path = localModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(isNew ? storageModel.getType() : storageDomain.getStorageType());
        storageDomain.setStorageDomainType(isNew ? storageModel.getRole() : storageDomain.getStorageDomainType());
        storageDomain.setStorageName(model.getName().getEntity());

        AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0) {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else {
                            dataCenterGuideModel.saveNewLocalStorage();
                        }

                    }
                }),
                host.getStoragePoolId(),
                path);
    }

    public void saveNewLocalStorage() {
        StorageModel model = (StorageModel) getWindow();
        LocalStorageModel localModel = (LocalStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setConnection(path);
        tempVar.setStorageType(localModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddLocalStorageDomain);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = true;

                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.removeConnection = false;

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();

                if (dataCenterGuideModel.removeConnection) {
                    dataCenterGuideModel.cleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                    dataCenterGuideModel.removeConnection = false;
                }

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1, callback2})),
                failureCallback,
                this);
    }

    private void cleanConnection(StorageServerConnections connection, Guid hostId) {
        Frontend.getInstance().runAction(VdcActionType.DisconnectStorageServerConnection,
                new StorageServerConnectionParametersBase(connection, hostId),
                null,
                this);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model) {
        onFinish(context, isSucceeded, model, null);
    }

    public void onFinish(TaskContext context, boolean isSucceeded, IStorageModel model, String message) {
        context.invokeUIThread(this,
                new ArrayList<>(Arrays.asList(new Object[]{"Finish", isSucceeded, model, message}))); //$NON-NLS-1$
    }

    private void saveNfsStorage() {
        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveNfs"}))).run(); //$NON-NLS-1$
    }

    private void saveNfsStorage(TaskContext context) {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        boolean isNew = model.getStorage() == null;
        storageModel = model.getCurrentStorageItem();
        NfsStorageModel nfsModel = (NfsStorageModel) storageModel;
        path = nfsModel.getPath().getEntity();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(isNew ? storageModel.getType() : storageDomain.getStorageType());
        storageDomain.setStorageDomainType(isNew ? storageModel.getRole() : storageDomain.getStorageDomainType());
        storageDomain.setStorageName(model.getName().getEntity());
        storageDomain.setStorageFormat(model.getFormat().getSelectedItem());

        AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0) {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else {
                            dataCenterGuideModel.saveNewNfsStorage();
                        }

                    }
                }),
                null,
                path);
    }

    public void saveNewNfsStorage() {
        StorageModel model = (StorageModel) getWindow();
        NfsStorageModel nfsModel = (NfsStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        hostId = host.getId();

        // Create storage connection.
        StorageServerConnections tempVar = new StorageServerConnections();
        tempVar.setConnection(path);
        tempVar.setStorageType(nfsModel.getType());
        connection = tempVar;

        ArrayList<VdcActionType> actionTypes = new ArrayList<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        actionTypes.add(VdcActionType.AddStorageServerConnection);
        actionTypes.add(VdcActionType.AddNFSStorageDomain);
        actionTypes.add(VdcActionType.DisconnectStorageServerConnection);

        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));
        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getId());
        parameters.add(tempVar2);
        parameters.add(new StorageServerConnectionParametersBase(connection, host.getId()));

        IFrontendActionAsyncCallback callback1 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageDomain.setStorage((String) vdcReturnValueBase.getActionReturnValue());

            }
        };
        IFrontendActionAsyncCallback callback2 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                VdcReturnValueBase vdcReturnValueBase = result.getReturnValue();
                dataCenterGuideModel.storageId = vdcReturnValueBase.getActionReturnValue();

            }
        };
        IFrontendActionAsyncCallback callback3 = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();

                // Attach storage to data center as neccessary.
                StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                    dataCenterGuideModel.attachStorageToDataCenter(dataCenterGuideModel.storageId,
                            dataCenter.getId());
                }

                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, true, dataCenterGuideModel.storageModel);

            }
        };
        IFrontendActionAsyncCallback failureCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {

                DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                dataCenterGuideModel.cleanConnection(dataCenterGuideModel.connection, dataCenterGuideModel.hostId);
                dataCenterGuideModel.onFinish(dataCenterGuideModel.context, false, dataCenterGuideModel.storageModel);

            }
        };
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                new ArrayList<>(Arrays.asList(new IFrontendActionAsyncCallback[]{callback1, callback2, callback3})),
                failureCallback,
                this);
    }

    private void saveSanStorage() {
        if (getWindow().getProgress() != null) {
            return;
        }

        getWindow().startProgress();

        Task.create(this, new ArrayList<>(Arrays.asList(new Object[]{"SaveSan"}))).run(); //$NON-NLS-1$
    }

    private void saveSanStorage(TaskContext context) {
        this.context = context;
        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getCurrentStorageItem();

        storageDomain = new StorageDomainStatic();
        storageDomain.setStorageType(sanModel.getType());
        storageDomain.setStorageDomainType(sanModel.getRole());
        storageDomain.setStorageFormat(sanModel.getContainer().getFormat().getSelectedItem());
        storageDomain.setStorageName(model.getName().getEntity());

        AsyncDataProvider.getInstance().getStorageDomainsByConnection(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> storages =
                                (ArrayList<StorageDomain>) returnValue;
                        if (storages != null && storages.size() > 0) {
                            String storageName = storages.get(0).getStorageName();
                            onFinish(dataCenterGuideModel.context,
                                    false,
                                    dataCenterGuideModel.storageModel,
                                    ConstantsManager.getInstance()
                                            .getMessages()
                                            .createOperationFailedDcGuideMsg(storageName));
                        }
                        else {
                            dataCenterGuideModel.saveNewSanStorage();
                        }

                        dataCenterGuideModel.getWindow().stopProgress();
                    }
                }),
                null,
                path);
    }

    public void saveNewSanStorage() {
        StorageModel storageModel = (StorageModel) getWindow();
        final SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getCurrentStorageItem();

        Guid hostId = sanStorageModel.getContainer().getHost().getSelectedItem().getId();
        Model target = getWidgetModel() != null ? getWidgetModel() : sanStorageModel.getContainer();
        List<String> unkownStatusLuns = new ArrayList<>();
        for (LunModel lunModel : sanStorageModel.getAddedLuns()) {
            unkownStatusLuns.add(lunModel.getLunId());
        }
        Frontend.getInstance()
                .runQuery(VdcQueryType.GetDeviceList,
                        new GetDeviceListQueryParameters(hostId,
                                sanStorageModel.getType(),
                                true,
                                unkownStatusLuns),
                        new AsyncQuery(target, new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                                if (response.getSucceeded()) {
                                    List<LUNs> checkedLuns = (ArrayList<LUNs>) response.getReturnValue();
                                    postGetLunsMessages(sanStorageModel.getUsedLunsMessages(checkedLuns));
                                } else {
                                    sanStorageModel.setGetLUNsFailure(
                                            ConstantsManager.getInstance()
                                                    .getConstants()
                                                    .couldNotRetrieveLUNsLunsFailure());
                                }
                            }
                        }, true));
    }

    private void postGetLunsMessages(ArrayList<String> usedLunsMessages) {

        if (usedLunsMessages.isEmpty()) {
            onSaveSanStorage();
        }
        else {
            forceCreationWarning(usedLunsMessages);
        }
    }

    private void onSaveSanStorage() {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (confirmationModel != null && !confirmationModel.validate()) {
            return;
        }

        cancelConfirm();
        getWindow().startProgress();

        StorageModel model = (StorageModel) getWindow();
        SanStorageModel sanModel = (SanStorageModel) model.getCurrentStorageItem();
        VDS host = model.getHost().getSelectedItem();
        boolean force = sanModel.isForce();

        ArrayList<String> lunIds = new ArrayList<>();
        for (LunModel lun : sanModel.getAddedLuns()) {
            lunIds.add(lun.getLunId());
        }

        AddSANStorageDomainParameters params = new AddSANStorageDomainParameters(storageDomain);
        params.setVdsId(host.getId());
        params.setLunIds(lunIds);
        params.setForce(force);
        Frontend.getInstance().runAction(VdcActionType.AddSANStorageDomain, params,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                        StorageModel storageModel = (StorageModel) dataCenterGuideModel.getWindow();
                        StoragePool dataCenter = storageModel.getDataCenter().getSelectedItem();
                        if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId)) {
                            VdcReturnValueBase returnValue = result.getReturnValue();
                            Guid storageId = returnValue.getActionReturnValue();
                            dataCenterGuideModel.attachStorageToDataCenter(storageId, dataCenter.getId());
                        }
                        dataCenterGuideModel.onFinish(dataCenterGuideModel.context,
                                true,
                                dataCenterGuideModel.storageModel);

                    }
                }, this);
    }

    private void forceCreationWarning(ArrayList<String> usedLunsMessages) {
        StorageModel storageModel = (StorageModel) getWindow();
        SanStorageModel sanStorageModel = (SanStorageModel) storageModel.getCurrentStorageItem();
        sanStorageModel.setForce(true);

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().forceStorageDomainCreation());
        model.setMessage(ConstantsManager.getInstance().getConstants().lunsAlreadyInUse());
        model.setHelpTag(HelpTag.force_storage_domain_creation);
        model.setHashName("force_storage_domain_creation"); //$NON-NLS-1$
        model.setItems(usedLunsMessages);

        UICommand onSaveSanStorageCommand = UICommand.createDefaultOkUiCommand("OnSaveSanStorage", this); //$NON-NLS-1$
        model.getCommands().add(onSaveSanStorageCommand);

        UICommand cancelConfirmCommand = UICommand.createCancelUiCommand("CancelConfirm", this); //$NON-NLS-1$
        model.getCommands().add(cancelConfirmCommand);
    }

    private void attachStorageInternal(List<StorageDomain> storages, String title) {
        ListModel model = new ListModel();
        model.setTitle(title);
        setWindow(model);

        ArrayList<EntityModel> items = new ArrayList<>();
        for (StorageDomain sd : storages) {
            EntityModel tempVar = new EntityModel();
            tempVar.setEntity(sd);
            items.add(tempVar);
        }

        model.setItems(items);

        UICommand tempVar2 = UICommand.createDefaultOkUiCommand("OnAttachStorage", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
        UICommand tempVar3 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar3);
    }

    private void attachStorageToDataCenter(Guid storageId, Guid dataCenterId) {
        Frontend.getInstance().runAction(VdcActionType.AttachStorageDomainToPool, new AttachStorageDomainToPoolParameters(storageId,
                dataCenterId),
                null,
                this);
    }

    public void onAttachStorage() {
        ListModel model = (ListModel) getWindow();

        ArrayList<StorageDomain> items = new ArrayList<>();
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems())) {
            if (a.getIsSelected()) {
                items.add((StorageDomain) a.getEntity());
            }
        }

        if (items.size() > 0) {
            for (StorageDomain sd : items) {
                attachStorageToDataCenter(sd.getId(), getEntity().getId());
            }
        }

        cancel();
        postAction();
    }

    public void attachIsoStorage() {
        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                        ArrayList<StorageDomain> attachedStorage =
                                new ArrayList<>();

                        AsyncDataProvider.getInstance().getISOStorageDomainList(new AsyncQuery(new Object[] {dataCenterGuideModel,
                                attachedStorage},
                                                                                               new INewAsyncCallback() {
                                                                                                   @Override
                                                                                                   public void onSuccess(Object target, Object returnValue) {
                                                                                                       Object[] array = (Object[]) target;
                                                                                                       DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                                                                                                       ArrayList<StorageDomain> attachedStorage =
                                                                                                               (ArrayList<StorageDomain>) array[1];
                                                                                                       ArrayList<StorageDomain> isoStorageDomains =
                                                                                                               (ArrayList<StorageDomain>) returnValue;
                                                                                                       ArrayList<StorageDomain> sdl =
                                                                                                               new ArrayList<>();

                                                                                                       for (StorageDomain a : isoStorageDomains) {
                                                                                                           boolean isContains = false;
                                                                                                           for (StorageDomain b : attachedStorage) {
                                                                                                               if (b.getId().equals(a.getId())) {
                                                                                                                   isContains = true;
                                                                                                                   break;
                                                                                                               }
                                                                                                           }
                                                                                                           if (!isContains) {
                                                                                                               sdl.add(a);
                                                                                                           }
                                                                                                       }
                                                                                                       dataCenterGuideModel.attachStorageInternal(sdl, ConstantsManager.getInstance()
                                                                                                               .getConstants()
                                                                                                               .attachISOLibraryTitle());
                                                                                                   }
                                                                                               }));
                    }
                }),
                getEntity().getId());
    }

    public void attachDataStorage() {
        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(this,
                                                                            new INewAsyncCallback() {
                                                                                @Override
                                                                                public void onSuccess(Object target, Object returnValue) {
                                                                                    DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) target;
                                                                                    ArrayList<StorageDomain> storageDomains =
                                                                                            (ArrayList<StorageDomain>) returnValue;

                                                                                    ArrayList<StorageDomain> unattachedStorage =
                                                                                            new ArrayList<>();
                                                                                    boolean addToList;
                                                                                    for (StorageDomain item : storageDomains) {
                                                                                        addToList = false;
                                                                                        if (item.getStorageDomainType() == StorageDomainType.Data
                                                                                                && (item.getStorageType() == StorageType.LOCALFS) == dataCenterGuideModel.getEntity()
                                                                                                .isLocal()
                                                                                                && item.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached) {
                                                                                            if (getEntity().getStoragePoolFormatType() == null) {
                                                                                                addToList = true;
                                                                                            } else if (getEntity().getStoragePoolFormatType() == item.getStorageStaticData()
                                                                                                    .getStorageFormat()) {
                                                                                                addToList = true;
                                                                                            }
                                                                                        }

                                                                                        if (addToList) {
                                                                                            unattachedStorage.add(item);
                                                                                        }
                                                                                    }
                                                                                    dataCenterGuideModel.attachStorageInternal(unattachedStorage, ConstantsManager.getInstance()
                                                                                            .getConstants()
                                                                                            .attachStorageTitle());
                                                                                }
                                                                            }));
    }

    public void addCluster() {
        ClusterModel model = new ClusterModel();
        model.init(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newClusterTitle());
        model.setHelpTag(HelpTag.new_cluster);
        model.setHashName("new_cluster"); //$NON-NLS-1$
        model.setIsNew(true);

        ArrayList<StoragePool> dataCenters = new ArrayList<>();
        dataCenters.add(getEntity());
        model.getDataCenter().setItems(dataCenters, getEntity());
        model.getDataCenter().setIsChangeable(false);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAddCluster", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onAddCluster() {
        ClusterModel model = (ClusterModel) getWindow();
        Cluster cluster = new Cluster();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate(model.getEnableOvirtService().getEntity())) { // CPU is mandatory only if the
                                                                                  // cluster is virt enabled
            return;
        }

        // Save changes.
        Version version = model.getVersion().getSelectedItem();

        cluster.setName(model.getName().getEntity());
        cluster.setDescription(model.getDescription().getEntity());
        cluster.setComment(model.getComment().getEntity());
        cluster.setStoragePoolId(model.getDataCenter().getSelectedItem().getId());
        if (model.getCPU().getSelectedItem() != null) {
            cluster.setCpuName(model.getCPU().getSelectedItem().getCpuName());
        }
        cluster.setMaxVdsMemoryOverCommit(model.getMemoryOverCommit());
        cluster.setTransparentHugepages(true);
        cluster.setCompatibilityVersion(version);
        cluster.setMigrateOnError(model.getMigrateOnErrorOption());
        cluster.setVirtService(model.getEnableOvirtService().getEntity());
        cluster.setGlusterService(model.getEnableGlusterService().getEntity());
        cluster.setOptionalReasonRequired(model.getEnableOptionalReason().getEntity());
        cluster.setMaintenanceReasonRequired(model.getEnableHostMaintenanceReason().getEntity());
        if (model.getClusterPolicy().getSelectedItem() != null) {
            ClusterPolicy selectedPolicy = model.getClusterPolicy().getSelectedItem();
            cluster.setClusterPolicyId(selectedPolicy.getId());
            cluster.setClusterPolicyProperties(KeyValueModel.convertProperties(model.getCustomPropertySheet()
                    .serialize()));
        }

        model.startProgress();

        Frontend.getInstance().runAction(VdcActionType.AddCluster, new ManagementNetworkOnClusterOperationParameters(cluster),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.postOnAddCluster(result.getReturnValue());

                    }
                }, this);
    }

    public void postOnAddCluster(VdcReturnValueBase returnValue) {
        ClusterModel model = (ClusterModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            postAction();
        }
    }

    public void selectHost() {
        MoveHost model = new MoveHost();
        model.setTitle(ConstantsManager.getInstance().getConstants().selectHostTitle());
        model.setHelpTag(HelpTag.select_host);
        model.setHashName("select_host"); //$NON-NLS-1$

        // In case of local storage, do not show the cluster selection in host select menu as there can be only one cluster in that case
        //also only one host is allowed in the cluster so we should disable multi selection
        boolean isMultiHostDC = getEntity().isLocal();
        if (isMultiHostDC) {
            model.getCluster().setIsAvailable(false);
            model.setMultiSelection(false);
        }

        setWindow(model);

        AsyncDataProvider.getInstance().getClusterList(new AsyncQuery(new Object[] { this, model },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Object[] array = (Object[]) target;
                        DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) array[0];
                        MoveHost moveHostModel = (MoveHost) array[1];
                        ArrayList<Cluster> clusters = (ArrayList<Cluster>) returnValue;

                        moveHostModel.getCluster().setItems(clusters);
                        moveHostModel.getCluster().setSelectedItem(Linq.firstOrNull(clusters));

                        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSelectHost", dataCenterGuideModel); //$NON-NLS-1$
                        moveHostModel.getCommands().add(tempVar);
                        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", dataCenterGuideModel); //$NON-NLS-1$
                        moveHostModel.getCommands().add(tempVar2);
                    }
                }), getEntity().getId());
    }

    public void onSelectHost() {
        MoveHost model = (MoveHost) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        model.setSelectedHosts(new ArrayList<MoveHostData>());
        for (EntityModel a : Linq.<EntityModel> cast(model.getItems())) {
            if (a.getIsSelected()) {
                model.getSelectedHosts().add((MoveHostData) a);
            }
        }

        Cluster cluster = model.getCluster().getSelectedItem();
        final List<VdcActionParametersBase> parameterList = new ArrayList<>();
        for (MoveHostData hostData : model.getSelectedHosts()) {
            VDS host = hostData.getEntity();
            // Try to change host's cluster as neccessary.
            if (host.getClusterId() != null && !host.getClusterId().equals(cluster.getId())) {
                parameterList.add(new ChangeVDSClusterParameters(cluster.getId(), host.getId()));

            }
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.ChangeVDSCluster, parameterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        final DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel) result.getState();
                        List<MoveHostData> hosts =
                                ((MoveHost) dataCenterGuideModel.getWindow()).getSelectedHosts();
                        List<VdcReturnValueBase> retVals = result.getReturnValue();
                        final List<VdcActionParametersBase> activateVdsParameterList = new ArrayList<>();
                        if (retVals != null && hosts.size() == retVals.size()) {
                            int i = 0;
                            for (MoveHostData selectedHostData : hosts) {
                                VDS selectedHost = selectedHostData.getEntity();
                                if (selectedHost.getStatus() == VDSStatus.PendingApproval && retVals.get(i) != null
                                        && retVals.get(i).getSucceeded()) {
                                    Frontend.getInstance().runAction(VdcActionType.ApproveVds,

                                            new ApproveVdsParameters(selectedHost.getId()),
                                            null,
                                            this);
                                } else if (selectedHostData.getActivateHost()) {
                                    activateVdsParameterList.add(new VdsActionParameters(selectedHostData.getEntity().getId()));
                                }
                                i++;
                            }
                        }

                        if (activateVdsParameterList.isEmpty()) {
                            dataCenterGuideModel.getWindow().stopProgress();
                            dataCenterGuideModel.cancel();
                            dataCenterGuideModel.postAction();
                        } else {
                            final String searchString = getVdsSearchString((MoveHost) dataCenterGuideModel.getWindow());
                            Timer timer = new Timer() {
                                public void run() {
                                    checkVdsClusterChangeSucceeded(dataCenterGuideModel, searchString, parameterList, activateVdsParameterList);
                                }
                            };
                            timer.schedule(2000);
                        }
                    }
                },
                this);
    }

    public void addHost() {
        final HostModel model = new NewHostModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        model.setHelpTag(HelpTag.new_host_guide_me);
        model.setHashName("new_host_guide_me"); //$NON-NLS-1$
        model.getPort().setEntity(54321);
        model.getOverrideIpTables().setEntity(true);
        model.setSpmPriorityValue(null);

        model.getDataCenter().setItems(Collections.singletonList(getEntity()), getEntity());
        model.getDataCenter().setIsChangeable(false);

        model.getCluster().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                ListModel<Cluster> clusterModel = model.getCluster();
                if (clusterModel.getSelectedItem() != null) {
                    Cluster cluster = clusterModel.getSelectedItem();
                    if (clusterModel.getSelectedItem() != null) {
                        if (Version.v3_6.compareTo(cluster.getCompatibilityVersion()) <= 0) {
                            model.getProtocol().setIsAvailable(false);
                        } else {
                            model.getProtocol().setIsAvailable(true);
                        }
                    }
                    model.getProtocol().setEntity(true);
                }
            }
        });

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnConfirmPMHost", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onConfirmPMHost() {
        HostModel model = (HostModel) getWindow();

        if (!model.validate()) {
            return;
        }

        if (!model.getIsPm().getEntity()) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
            confirmModel.setHelpTag(HelpTag.power_management_configuration);
            confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAddHost", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmWithFocus", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar2);
        }
        else {
            onAddHost();
        }
    }

    public void onAddHost() {
        cancelConfirm();

        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        // Save changes.
        VDS host = new VDS();
        host.setVdsName(model.getName().getEntity());
        host.setHostName(model.getHost().getEntity());
        host.setPort(model.getPort().getEntity());
        host.setProtocol(model.getProtocol().getEntity() ? VdsProtocol.STOMP : VdsProtocol.XML);
        host.setSshPort(model.getAuthSshPort().getEntity());
        host.setSshUsername(model.getUserName().getEntity());
        host.setSshKeyFingerprint(model.getFetchSshFingerprint().getEntity());
        host.setClusterId(model.getCluster().getSelectedItem().getId());
        host.setVdsSpmPriority(model.getSpmPriorityValue());

        // Save other PM parameters.
        host.setPmEnabled(model.getIsPm().getEntity());
        host.setDisablePowerManagementPolicy(model.getDisableAutomaticPowerManagement().getEntity());
        host.setPmKdumpDetection(model.getPmKdumpDetection().getEntity());


        AddVdsActionParameters addVdsParams = new AddVdsActionParameters();
        addVdsParams.setVdsId(host.getId());
        addVdsParams.setvds(host);
        if (model.getUserPassword().getEntity() != null) {
            addVdsParams.setPassword(model.getUserPassword().getEntity());
        }
        addVdsParams.setOverrideFirewall(model.getOverrideIpTables().getEntity());
        addVdsParams.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
        model.startProgress();

        Frontend.getInstance().runAction(VdcActionType.AddVds, addVdsParams,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        DataCenterGuideModel localModel = (DataCenterGuideModel) result.getState();
                        localModel.postOnAddHost(result.getReturnValue());

                    }
                }, this);
    }

    public void postOnAddHost(VdcReturnValueBase returnValue) {
        HostModel model = (HostModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            postAction();
        }
    }

    @Override
    protected void postAction() {
        resetData();
        updateOptions();
    }

    @Override
    protected void cancel() {
        resetData();
        setWindow(null);
    }

    public void cancelConfirm() {
        setConfirmWindow(null);
    }

    public void cancelConfirmWithFocus() {
        setConfirmWindow(null);

        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("AddCluster".equals(command.getName())) { //$NON-NLS-1$
            addCluster();
        }

        if ("AddHost".equals(command.getName())) { //$NON-NLS-1$
            addHost();
        }

        if ("SelectHost".equals(command.getName())) { //$NON-NLS-1$
            selectHost();
        }
        if ("AddDataStorage".equals(command.getName())) { //$NON-NLS-1$
            addDataStorage();
        }
        if ("AttachDataStorage".equals(command.getName())) { //$NON-NLS-1$
            attachDataStorage();
        }
        if ("AddIsoStorage".equals(command.getName())) { //$NON-NLS-1$
            addIsoStorage();
        }
        if ("AttachIsoStorage".equals(command.getName())) { //$NON-NLS-1$
            attachIsoStorage();
        }
        if ("OnAddCluster".equals(command.getName())) { //$NON-NLS-1$
            onAddCluster();
        }
        if ("OnSelectHost".equals(command.getName())) { //$NON-NLS-1$
            onSelectHost();
        }
        if ("OnAddHost".equals(command.getName())) { //$NON-NLS-1$
            onAddHost();
        }
        if ("OnAddStorage".equals(command.getName())) { //$NON-NLS-1$
            onAddStorage();
        }

        if ("OnSaveSanStorage".equals(command.getName())) { //$NON-NLS-1$
            onSaveSanStorage();
        }

        if ("OnAttachStorage".equals(command.getName())) { //$NON-NLS-1$
            onAttachStorage();
        }

        if ("AddLocalStorage".equals(command.getName())) { //$NON-NLS-1$
            addLocalStorage();
        }

        if ("OnConfirmPMHost".equals(command.getName())) { //$NON-NLS-1$
            onConfirmPMHost();
        }

        if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        }
        if ("CancelConfirmWithFocus".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmWithFocus();
        }

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    public void run(TaskContext context) {
        ArrayList<Object> data = (ArrayList<Object>) context.getState();
        String key = (String) data.get(0);

        if ("SaveNfs".equals(key)) { //$NON-NLS-1$
            saveNfsStorage(context);

        }
        else if ("SaveLocal".equals(key)) { //$NON-NLS-1$
            saveLocalStorage(context);

        }
        else if ("SaveSan".equals(key)) { //$NON-NLS-1$
            saveSanStorage(context);

        }
        else if ("Finish".equals(key)) { //$NON-NLS-1$
            getWindow().stopProgress();

            if ((Boolean) data.get(1)) {
                cancel();
                postAction();
            }
            else {
                ((Model) data.get(2)).setMessage((String) data.get(3));
            }
        }
    }
}

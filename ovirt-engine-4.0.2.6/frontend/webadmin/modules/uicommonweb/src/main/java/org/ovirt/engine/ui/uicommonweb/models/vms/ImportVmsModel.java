package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NameAndOptionalDomainValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.inject.Inject;

public class ImportVmsModel extends ListWithSimpleDetailsModel {

    private ListModel<StoragePool> dataCenters;
    private ListModel<ImportSource> importSources;
    private SortedListModel<EntityModel<VM>> externalVmModels;
    private SortedListModel<EntityModel<VM>> importedVmModels;
    private ListModel<Provider<VmwareVmProviderProperties>> vmwareProviders;

    private EntityModel<String> vCenter;
    private EntityModel<String> esx;
    private EntityModel<String> vmwareDatacenter;
    private EntityModel<String> vmwareCluster;
    private EntityModel<Boolean> verify;
    private EntityModel<String> username;
    private EntityModel<String> password;
    private ListModel<VDS> proxyHosts;

    private StorageDomain exportDomain;
    private String exportPath;
    private String exportName;
    private String exportDescription;

    private ListModel<VDS> hosts;
    private EntityModel<String> ovaPath;

    private EntityModel<String> xenUri;
    private ListModel<VDS> xenProxyHosts;

    private EntityModel<String> kvmUri;
    private EntityModel<String> kvmUsername;
    private EntityModel<String> kvmPassword;
    private ListModel<VDS> kvmProxyHosts;

    private UICommand addImportCommand = new UICommand(null, this);
    private UICommand cancelImportCommand = new UICommand(null, this);

    private com.google.inject.Provider<ImportVmFromExternalSourceModel> importFromExternalSourceModelProvider;
    private com.google.inject.Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider;
    private com.google.inject.Provider<ImportVmFromOvaModel> importFromOvaModelProvider;

    private ImportVmFromExternalSourceModel importFromExternalSourceModel;
    private ImportVmFromExportDomainModel importFromExportDomainModel;
    private ImportVmFromOvaModel importFromOvaModel;
    private ImportVmModel selectedImportVmModel;

    private EntityModel<String> problemDescription;
    private UIConstants constants;
    private UIMessages messages;

    /** Data Center Id -> Architectures that are supported by at least one virt cluster */
    private Map<Guid, Set<ArchitectureType>> clusterArchitecturesInDataCenters;

    @Inject
    public ImportVmsModel(
            com.google.inject.Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider,
            com.google.inject.Provider<ImportVmFromExternalSourceModel> importFromExternalSourceModelProvider,
            com.google.inject.Provider<ImportVmFromOvaModel> importFromOvaModelProvider) {
        this.importFromExportDomainModelProvider = importFromExportDomainModelProvider;
        this.importFromExternalSourceModelProvider = importFromExternalSourceModelProvider;
        this.importFromOvaModelProvider = importFromOvaModelProvider;

        constants = ConstantsManager.getInstance().getConstants();
        messages = ConstantsManager.getInstance().getMessages();

        // General
        setDataCenters(new ListModel<StoragePool>());
        setImportSources(new ListModel<ImportSource>());

        setExternalVmModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VM>, VM>()));
        setImportedVmModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VM>, VM>()));

        setVmwareProviders(new ListModel<Provider<VmwareVmProviderProperties>>());

        // VMWARE
        setProxyHosts(new ListModel<VDS>());
        setUsername(new EntityModel<String>());
        setPassword(new EntityModel<String>());
        setvCenter(new EntityModel<String>());
        setEsx(new EntityModel<String>());
        setVmwareDatacenter(new EntityModel<String>());
        setVerify(new EntityModel<>(false));
        setVmwareCluster(new EntityModel<String>());

        // OVA
        setHosts(new ListModel<VDS>());
        setOvaPath(new EntityModel<String>());

        // Xen
        setXenUri(new EntityModel<String>());
        getXenUri().setEntity(constants.xenUriExample());
        setXenProxyHosts(new ListModel<VDS>());

        // Kvm
        setKvmUri(new EntityModel<String>());
        setKvmUsername(new EntityModel<String>());
        setKvmPassword(new EntityModel<String>());
        setKvmProxyHosts(new ListModel<VDS>());

        setInfoMessage(new EntityModel<String>());

        getVmwareProviders().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                providerChanged();
            }
        });
        initImportSources();
    }

    public void initImportModels(UICommand ... commands) {
        importFromExportDomainModel = importFromExportDomainModelProvider.get();
        initImportModel(importFromExportDomainModel, commands);

        importFromExternalSourceModel = importFromExternalSourceModelProvider.get();
        initImportModel(importFromExternalSourceModel, commands);

        importFromOvaModel = importFromOvaModelProvider.get();
        initImportModel(importFromOvaModel, commands);
    }

    private void initImportModel(ImportVmModel importVmModel, UICommand ... commands) {
        importVmModel.setTitle(constants.importVirtualMachinesTitle());
        importVmModel.setHelpTag(HelpTag.import_virtual_machine);
        importVmModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
        for (UICommand command : commands) {
            importVmModel.getCommands().add(command);
        }
    }

    public ImportVmModel getSpecificImportModel() {
        selectedImportVmModel = null;
        switch(importSources.getSelectedItem()) {
        case EXPORT_DOMAIN:
            importFromExportDomainModel.setEntity(null);
            importFromExportDomainModel.init(getVmsToImport(), exportDomain.getId());
            importFromExportDomainModel.setEntity(exportDomain.getId());
            selectedImportVmModel = importFromExportDomainModel;
            break;
        case VMWARE:
            importFromExternalSourceModel.init(getVmsToImport(), getDataCenters().getSelectedItem().getId());
            importFromExternalSourceModel.setUrl(getUrl());
            importFromExternalSourceModel.setUsername(getUsername().getEntity());
            importFromExternalSourceModel.setPassword(getPassword().getEntity());
            importFromExternalSourceModel.setProxyHostId(getProxyHosts().getSelectedItem() != null ? getProxyHosts().getSelectedItem().getId() : null);
            selectedImportVmModel = importFromExternalSourceModel;
            break;
        case OVA:
            importFromOvaModel.init(getVmsToImport(), getDataCenters().getSelectedItem().getId());
            importFromOvaModel.setIsoName(getOvaPath().getEntity());
            importFromOvaModel.setHostId(getHosts().getSelectedItem().getId());
            selectedImportVmModel = importFromOvaModel;
            break;
        case XEN:
            importFromExternalSourceModel.init(getVmsToImport(), getDataCenters().getSelectedItem().getId());
            importFromExternalSourceModel.setUrl(getXenUri().getEntity());
            importFromExternalSourceModel.setUsername(""); //$NON-NLS-1$
            importFromExternalSourceModel.setPassword(""); //$NON-NLS-1$
            importFromExternalSourceModel.setProxyHostId(getXenProxyHosts().getSelectedItem() != null ? getXenProxyHosts().getSelectedItem().getId() : null);
            selectedImportVmModel = importFromExternalSourceModel;
            break;
        case KVM:
            importFromExternalSourceModel.init(getVmsToImport(), getDataCenters().getSelectedItem().getId());
            importFromExternalSourceModel.setUrl(getKvmUri().getEntity());
            importFromExternalSourceModel.setUsername(getKvmUsername().getEntity());
            importFromExternalSourceModel.setPassword(getKvmPassword().getEntity());
            importFromExternalSourceModel.setProxyHostId(getKvmProxyHosts().getSelectedItem() != null ? getKvmProxyHosts().getSelectedItem().getId() : null);
            selectedImportVmModel = importFromExternalSourceModel;
            break;
        default:
        }
        return selectedImportVmModel;
    }

    public void init() {
        startProgress();
        setTitle(constants.importVirtualMachinesTitle());
        setHelpTag(HelpTag.import_virtual_machine);
        setHashName("import_virtual_machine"); //$NON-NLS-1$

        initDataCenters();
        initDataCenterCpuArchitectureMap();
    }

    private void initDataCenterCpuArchitectureMap() {
        final AsyncQuery callback = new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object nothing, Object returnValue) {
                List<Cluster> allClusters = ((VdcQueryReturnValue) returnValue).getReturnValue();
                clusterArchitecturesInDataCenters = new HashMap<>();
                for (Cluster cluster : allClusters) {
                    if (cluster.supportsVirtService() && cluster.getArchitecture() != null) {
                        addArchitecture(cluster.getStoragePoolId(), cluster.getArchitecture());
                    }
                }
            }

            private void addArchitecture(Guid dataCenterId, ArchitectureType architecture) {
                Set<ArchitectureType> architectures = clusterArchitecturesInDataCenters.get(dataCenterId);
                if (architectures == null) {
                    architectures = new HashSet<>();
                    clusterArchitecturesInDataCenters.put(dataCenterId, architectures);
                }
                architectures.add(architecture);
            }
        });
        Frontend.getInstance().runQuery(VdcQueryType.GetAllClusters, new VdcQueryParametersBase(), callback);

    }

    private INewAsyncCallback createGetStorageDomainsByStoragePoolIdCallback(final StoragePool dataCenter) {
        return new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                List<StorageDomain> storageDomains = ((VdcQueryReturnValue) ReturnValue).getReturnValue();

               exportDomain = getExportDomain(storageDomains);
               if (exportDomain == null) {
                   stopProgress();
               } else {
                   setExportName(exportDomain.getName());
                   setExportDescription(exportDomain.getDescription());
                   // get export-path
                   AsyncQuery _asyncQuery = new AsyncQuery();
                   _asyncQuery.setModel(this);
                   _asyncQuery.asyncCallback = new INewAsyncCallback() {
                       @Override
                       public void onSuccess(Object model, Object ReturnValue) {
                           StorageServerConnections connection = (StorageServerConnections) ReturnValue;
                           setExportPath(connection == null ? null : connection.getConnection());
                           stopProgress();
                       }
                   };
                   AsyncDataProvider.getInstance().getStorageConnectionById(_asyncQuery, exportDomain.getStorage(), true);
               }
            }
        };
    }

    private static StorageDomain getExportDomain(List<StorageDomain> storageDomains) {
        for (StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageDomainType() == StorageDomainType.ImportExport
                    && storageDomain.getStatus() == StorageDomainStatus.Active) {
                return storageDomain;
            }
        }
        return null;
    }

    public StorageDomain getExportDomain() {
        return exportDomain;
    }

    private void providerChanged() {
        clearValidations();
        switch(importSources.getSelectedItem()) {
        case VMWARE:
            vmwareProviderChanged();
            break;
        default:
        }
    }

    private void vmwareProviderChanged() {
        Provider<VmwareVmProviderProperties> provider = getVmwareProviders().getSelectedItem();
        if (provider == null) {
            provider = new Provider<>();
            provider.setAdditionalProperties(new VmwareVmProviderProperties());
        }

        getUsername().setEntity(provider.getUsername());
        getPassword().setEntity(provider.getPassword());

        VmwareVmProviderProperties properties = provider.getAdditionalProperties();
        getvCenter().setEntity(properties.getvCenter());
        getEsx().setEntity(properties.getEsx());
        Pair<String, String> dcAndCluster = splitToDcAndCluster(properties.getDataCenter());
        getVmwareDatacenter().setEntity(dcAndCluster.getFirst());
        getVmwareCluster().setEntity(dcAndCluster.getSecond());
        getVerify().setEntity(properties.isVerifySSL());
        if (properties.getProxyHostId() == null) {
            getProxyHosts().setSelectedItem(null);
        } else {
            for (VDS host : getProxyHosts().getItems()) {
                if (host != null && host.getId().equals(properties.getProxyHostId())) {
                    getProxyHosts().setSelectedItem(host);
                    break;
                }
            }
        }
    }

    private void initDataCenters() {
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                loadProviders();

                StoragePool dataCenter = dataCenters.getSelectedItem();
                Frontend.getInstance().runQuery(
                        VdcQueryType.GetStorageDomainsByStoragePoolId,
                        new IdQueryParameters(dataCenter.getId()),
                        new AsyncQuery(this, createGetStorageDomainsByStoragePoolIdCallback(dataCenter)));
            }
        });

        dataCenters.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                AsyncQuery hostsQuery = new AsyncQuery();
                hostsQuery.setModel(ImportVmsModel.this);
                hostsQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<VDS> hosts = (List<VDS>) returnValue;
                        List<VDS> upHosts = filterUpHosts(hosts);
                        proxyHosts.setItems(addAnyHostInCluster(upHosts));
                        xenProxyHosts.setItems(addAnyHostInCluster(upHosts));
                        kvmProxyHosts.setItems(addAnyHostInCluster(upHosts));
                        ImportVmsModel.this.hosts.setItems(upHosts);
                        stopProgress();
                    }
                };

                AsyncDataProvider.getInstance().getHostListByDataCenter(hostsQuery, dataCenters.getSelectedItem().getId());
            }

            private List<VDS> filterUpHosts(List<VDS> hosts) {
                List<VDS> result = new ArrayList<>();
                for (VDS host : hosts) {
                    if (host.getStatus() == VDSStatus.Up) {
                        result.add(host);
                    }
                }
                return result;
            }

            private List<VDS> addAnyHostInCluster(List<VDS> hosts) {
                List<VDS> result = new ArrayList<>(hosts);
                result.add(0, null); // Any host in the cluster
                return result;
            }
        });

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                final List<StoragePool> dataCenters = new ArrayList<>();
                for (StoragePool a : (ArrayList<StoragePool>) returnValue) {
                    if (a.getStatus() == StoragePoolStatus.Up) {
                        dataCenters.add(a);
                    }
                }
                if (dataCenters.isEmpty()) {
                    getDataCenters().setIsChangeable(false);
                    getImportSources().setIsChangeable(false);
                    setError(constants.notAvailableWithNoUpDC());
                    stopProgress();
                    return;
                }

                Collections.sort(dataCenters, new NameableComparator());
                ImportVmsModel.this.dataCenters.setItems(dataCenters);
            }
        }));
    }

    private void initImportSources() {
        importSources.setItems(Arrays.asList(ImportSource.values()));
        importSources.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                validateSource();
                clearVms();
                clearValidations();
                loadProviders();
            }
        });
        importSources.setSelectedItem(ImportSource.EXPORT_DOMAIN);
        validateSource();
    }

    private void validateSource() {
        clearProblem();
        if (importSources.getSelectedItem() == ImportSource.EXPORT_DOMAIN && exportDomain == null) {
            setError(constants.notAvailableWithNoActiveExportDomain());
        }
    }

    private void clearVms() {
        importedVmModels.setItems(null);
        externalVmModels.setItems(null);
    }

    private void clearForLoad() {
        clearProblem();
        clearVms();
    }

    private void loadVmwareProviders() {
        AsyncDataProvider.getInstance().getAllProvidersByType(new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<Provider<VmwareVmProviderProperties>> providers = new ArrayList<>();
                for (Provider<VmwareVmProviderProperties> provider : (List<Provider<VmwareVmProviderProperties>>) returnValue) {
                    if (getDataCenters().getSelectedItem().getId().equals(provider.getAdditionalProperties().getStoragePoolId())
                            || provider.getAdditionalProperties().getStoragePoolId() == null) {
                        providers.add(provider);
                    }
                }
                providers.add(0, null);
                getVmwareProviders().setItems(providers);
            }
        }), ProviderType.VMWARE);
    }

    private void loadProviders() {
        switch(importSources.getSelectedItem()) {
        case VMWARE:
            loadVmwareProviders();
            break;
        default:
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        if (getAddImportCommand().equals(command)) {
            addImport();
        } else if (getCancelImportCommand().equals(command)) {
            cancelImport();
        } else {
            super.executeCommand(command);
        }
    }

    public void loadVmsFromExportDomain() {
        clearProblem();
        startProgress();
        Frontend.getInstance().runQuery(VdcQueryType.GetVmsFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getDataCenters().getSelectedItem().getId(), exportDomain.getId()),
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        updateVms(((VdcQueryReturnValue) returnValue).<List<VM>>getReturnValue());
                    }
                }));
    }

    public void loadVmFromOva() {
        clearForLoad();
        if (!validateOvaConfiguration()) {
            return;
        }

        startProgress();
        AsyncDataProvider.getInstance().getVmFromOva(new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VdcQueryReturnValue queryReturnValue = (VdcQueryReturnValue) returnValue;
                        if (queryReturnValue.getSucceeded()) {
                            VM vm = queryReturnValue.getReturnValue();
                            updateVms(Collections.singletonList(vm));
                        } else {
                            setError(messages.failedToLoadOva(getOvaPath().getEntity()));
                        }
                        stopProgress();
                    }
                }),
                getHosts().getSelectedItem().getId(),
                getOvaPath().getEntity());
    }

    private boolean validateOvaConfiguration() {
        getOvaPath().validateEntity(new IValidation[]{
                new NotEmptyValidation()});

        return getOvaPath().getIsValid();
    }

    public void loadVmsFromVmware() {
        clearForLoad();

        if (!validateVmwareConfiguration()) {
            return;
        }
        Guid proxyId = getProxyHosts().getSelectedItem() != null ? getProxyHosts().getSelectedItem().getId() : null;
        loadVMsFromExternalProvider(OriginType.VMWARE, getUrl(), getUsername().getEntity(), getPassword().getEntity(), proxyId);
    }

    public void loadVmsFromXen() {
        clearForLoad();
        if (!validateXenConfiguration()) {
            return;
        }
        Guid proxyId = getXenProxyHosts().getSelectedItem() != null ? getXenProxyHosts().getSelectedItem().getId() : null;
        loadVMsFromExternalProvider(OriginType.XEN, getXenUri().getEntity(), "", "", proxyId); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void loadVmsFromKvm() {
        clearForLoad();
        if (!validateKvmConfiguration()) {
            return;
        }
        Guid proxyId = getXenProxyHosts().getSelectedItem() != null ? getXenProxyHosts().getSelectedItem().getId() : null;
        loadVMsFromExternalProvider(OriginType.KVM, getKvmUri().getEntity(), getKvmUsername().getEntity(), getKvmPassword().getEntity(), proxyId);
    }

    private void loadVMsFromExternalProvider(final OriginType type, String uri, String username, String password, Guid proxyId) {
        startProgress();
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                if (returnValue instanceof VdcQueryReturnValue) {
                    setError(messages.providerFailure());
                    stopProgress();
                }
                else {
                    List<VM> remoteVms = (List<VM>) returnValue;
                    List<VM> remoteDownVms = new ArrayList<>();
                    for (VM vm : remoteVms) {
                        if (vm.isDown()) {
                            remoteDownVms.add(vm);
                        }
                    }
                    if (remoteVms.size() != remoteDownVms.size()) {
                        setWarning(constants.runningVmsWereFilteredOnImportVm());
                    }
                    updateVms(remoteDownVms);
                }
            }
        });
        query.setHandleFailure(true);
        AsyncDataProvider.getInstance().getVmsFromExternalServer(
                query,
                getDataCenters().getSelectedItem().getId(),
                proxyId,
                uri,
                username,
                password,
                type);
    }

    private boolean validateVmwareConfiguration() {
        getvCenter().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation() });
        getEsx().validateEntity(new IValidation[]{
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation()});
        getVmwareDatacenter().validateEntity(new IValidation[]{
                new NotEmptyValidation()});
        getUsername().validateEntity(new IValidation[]{
                new NotEmptyValidation(),
                new NameAndOptionalDomainValidation()});
        getPassword().validateEntity(new IValidation[]{
                new NotEmptyValidation()});

        return getvCenter().getIsValid()
                && getEsx().getIsValid()
                && getVmwareDatacenter().getIsValid()
                && getUsername().getIsValid()
                && getPassword().getIsValid();
    }

    private boolean validateXenConfiguration() {
        getXenUri().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255)});
        return getXenUri().getIsValid();
    }

    private boolean validateKvmConfiguration() {
        getKvmUri().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255)});
        getKvmUsername().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new NameAndOptionalDomainValidation() });
        getKvmPassword().validateEntity(new IValidation[] {
                new NotEmptyValidation() });
        return getKvmUri().getIsValid() &&
               getKvmUsername().getIsValid() &&
               getKvmPassword().getIsValid();
    }

    private void clearValidations() {
        getvCenter().setIsValid(true);
        getEsx().setIsValid(true);
        getVmwareDatacenter().setIsValid(true);
        getUsername().setIsValid(true);
        getPassword().setIsValid(true);
    }

    private String getUrl() {
        return getVmwareUrl(getUsername().getEntity(), getvCenter().getEntity(),
                getVmwareDatacenter().getEntity(), getVmwareCluster().getEntity(), getEsx().getEntity(), getVerify().getEntity());
    }

    public static String getVmwareUrl(String username, String vcenter,
            String dataCenter, String cluster, String esx, boolean verify) {
        if (username != null) {
            username = username.replace("@", "%40"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return "vpx://" + //$NON-NLS-1$
                (StringUtils.isEmpty(username) ? "" : username + "@") + //$NON-NLS-1$ //$NON-NLS-2$
                vcenter +
                "/" + //$NON-NLS-1$
                mergeDcAndCluster(dataCenter, cluster) +
                "/" + //$NON-NLS-1$
                esx +
                (verify ? "" : "?no_verify=1"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static Pair<String, String> splitToDcAndCluster(String dataCenter) {
        if (dataCenter == null) {
            return new Pair<>(null, null);
        }

        if (!dataCenter.contains("/")) { //$NON-NLS-1$
            return new Pair<>(dataCenter, null);
        }

        int lastSlash = dataCenter.lastIndexOf("/"); //$NON-NLS-1$
        String dataCenterPart = dataCenter.substring(0, lastSlash);
        String clusterPart = dataCenter.substring(lastSlash + 1);

        return new Pair<>(dataCenterPart, clusterPart);
    }

    public static String mergeDcAndCluster(String dataCenter, String cluster) {
        if (StringUtils.isEmpty(cluster)) {
            return dataCenter;
        }

        return dataCenter + "/" + cluster; //$NON-NLS-1$
    }

    private void updateVms(List<VM> vms) {
        clearVms();
        List<EntityModel<VM>> externalVms = new ArrayList<>();

        for (VM vm : vms) {
            externalVms.add(new EntityModel<>(vm));
        }

        externalVmModels.setItems(externalVms);
        stopProgress();
    }

    public List<VM> getVmsToImport() {
        List<VM> vmsToImport = new ArrayList<>();
        for (EntityModel<VM> externalVm : importedVmModels.getItems()) {
            vmsToImport.add(externalVm.getEntity());
        }
        return vmsToImport;
    }

    private void addImport() {
        getDefaultCommand().setIsExecutionAllowed(true);
    }

    private void cancelImport() {
        Collection<EntityModel<VM>> selectedVms = getImportedVmModels().getSelectedItems();
        Collection<EntityModel<VM>> totalVmsSetToImport = getImportedVmModels().getItems();
        getDefaultCommand().setIsExecutionAllowed(selectedVms.size() < totalVmsSetToImport.size());
    }

    /**
     * @return true if selection of VMs to import if valid with regard to CPU architecture, false otherwise
     */
    public boolean validateArchitectures() {
        final List<VM> vmsToImport = getVmsToImport();
        final StoragePool dataCenter = getDataCenters().getSelectedItem();

        if (vmsToImport.isEmpty() || dataCenter == null) {
            return false;
        }

        return validateSameArchitecture(vmsToImport)
                && validateClusterExistsForArchitecture(vmsToImport.get(0).getClusterArch(), dataCenter);
    }

    private boolean validateClusterExistsForArchitecture(ArchitectureType architecture, StoragePool dataCenter) {
        if (clusterArchitecturesInDataCenters == null // we want validation to fail if map initialization failed
                || clusterArchitecturesInDataCenters.get(dataCenter.getId()) == null
                || !clusterArchitecturesInDataCenters.get(dataCenter.getId()).contains(architecture)) {
            showErrorPopup(constants.noClusterSupportingArchitectureInDC());
            return false;
        }
        return true;
    }

    private boolean validateSameArchitecture(List<VM> vmsToImport) {
        final ArchitectureType architectureOfFirst = vmsToImport.get(0).getClusterArch();
        for (VM vm : vmsToImport) {
            if (!Objects.equals(architectureOfFirst, vm.getClusterArch())) {
                showErrorPopup(constants.sameArchitectureRequired());
                return false;
            }
        }
        return true;
    }

    private void showErrorPopup(String message) {
        final ErrorPopupManager popupManager =
                (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
        popupManager.show(message);
    }

    public UICommand getAddImportCommand() {
        return addImportCommand;
    }

    public UICommand getCancelImportCommand() {
        return cancelImportCommand;
    }

    public ListModel<StoragePool> getDataCenters() {
        return dataCenters;
    }

    private void setDataCenters(ListModel<StoragePool> storage) {
        this.dataCenters = storage;
    }

    @Override
    protected String getListName() {
        return "ImportVmsModel"; //$NON-NLS-1$
    }

    public ListModel<ImportSource> getImportSources() {
        return importSources;
    }

    private void setImportSources(ListModel<ImportSource> importSources) {
        this.importSources = importSources;
    }

    public SortedListModel<EntityModel<VM>> getExternalVmModels() {
        return externalVmModels;
    }

    private void setExternalVmModels(SortedListModel<EntityModel<VM>> externalVmModels) {
        this.externalVmModels = externalVmModels;
    }

    public SortedListModel<EntityModel<VM>> getImportedVmModels() {
        return importedVmModels;
    }

    private void setImportedVmModels(SortedListModel<EntityModel<VM>> importedVmModels) {
        this.importedVmModels = importedVmModels;
    }

    public void clearVmModelsExceptItems() {
        Collection<EntityModel<VM>> savedVms;

        savedVms = getImportedVmModels().getItems();
        setImportedVmModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VM>, VM>()));
        getImportedVmModels().setItems(savedVms);

        savedVms = getExternalVmModels().getItems();
        setExternalVmModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VM>, VM>()));
        getExternalVmModels().setItems(savedVms);
    }

    public EntityModel<String> getUsername() {
        return username;
    }

    public void setUsername(EntityModel<String> username) {
        this.username = username;
    }

    public EntityModel<String> getPassword() {
        return password;
    }

    public void setPassword(EntityModel<String> password) {
        this.password = password;
    }

    public ListModel<VDS> getProxyHosts() {
        return proxyHosts;
    }

    public void setProxyHosts(ListModel<VDS> proxyHosts) {
        this.proxyHosts = proxyHosts;
    }

    public EntityModel<String> getProblemDescription() {
        return problemDescription;
    }

    public void setInfoMessage(EntityModel<String> problemDescription) {
        this.problemDescription = problemDescription;
    }

    public void setError(String problem) {
        getProblemDescription().setIsValid(false);
        getProblemDescription().setEntity(problem);
    }

    public void setWarning(String problem) {
        if (!getProblemDescription().getIsValid()) {
            return;
        }
        getProblemDescription().setIsValid(true);
        getProblemDescription().setEntity(problem);
    }

    public void clearProblem() {
        getProblemDescription().setIsValid(true);
        getProblemDescription().setEntity(null);
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        if (!Objects.equals(this.exportPath, exportPath)) {
            this.exportPath = exportPath;
            onPropertyChanged(new PropertyChangedEventArgs("ExportPath")); //$NON-NLS-1$
        }
    }

    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        if (!Objects.equals(this.exportName, exportName)) {
            this.exportName = exportName;
            onPropertyChanged(new PropertyChangedEventArgs("ExportName")); //$NON-NLS-1$
        }
    }

    public String getExportDescription() {
        return exportDescription != null ? exportDescription : ""; //$NON-NLS-1$
    }

    public void setExportDescription(String exportDescription) {
        if (!Objects.equals(this.exportDescription, exportDescription)) {
            this.exportDescription = exportDescription;
            onPropertyChanged(new PropertyChangedEventArgs("ExportDescription")); //$NON-NLS-1$
        }
    }

    public void onRestoreVms(IFrontendMultipleActionAsyncCallback callback) {
        if (selectedImportVmModel.getProgress() != null) {
            return;
        }

        if (!selectedImportVmModel.validate()) {
            return;
        }

        selectedImportVmModel.importVms(callback);
    }

    public EntityModel<String> getEsx() {
        return esx;
    }

    public void setEsx(EntityModel<String> esx) {
        this.esx = esx;
    }

    public EntityModel<String> getVmwareDatacenter() {
        return vmwareDatacenter;
    }

    public EntityModel<String> getVmwareCluster() {
        return vmwareCluster;
    }

    public void setVmwareCluster(EntityModel<String> vmwareCluster) {
        this.vmwareCluster = vmwareCluster;
    }

    public void setVmwareDatacenter(EntityModel<String> vmwareDatacenter) {
        this.vmwareDatacenter = vmwareDatacenter;
    }

    public EntityModel<Boolean> getVerify() {
        return verify;
    }

    public void setVerify(EntityModel<Boolean> verify) {
        this.verify = verify;
    }

    public EntityModel<String> getvCenter() {
        return vCenter;
    }

    public void setvCenter(EntityModel<String> vCenter) {
        this.vCenter = vCenter;
    }

    public ListModel<Provider<VmwareVmProviderProperties>> getVmwareProviders() {
        return vmwareProviders;
    }

    public void setVmwareProviders(ListModel<Provider<VmwareVmProviderProperties>> vmwareProviders) {
        this.vmwareProviders = vmwareProviders;
    }

    public ListModel<VDS> getHosts() {
        return hosts;
    }

    public void setHosts(ListModel<VDS> hosts) {
        this.hosts = hosts;
    }

    public EntityModel<String> getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(EntityModel<String> ovaPath) {
        this.ovaPath = ovaPath;
    }

    public EntityModel<String> getXenUri() {
        return xenUri;
    }

    public void setXenUri(EntityModel<String> uri) {
        this.xenUri = uri;
    }

    public ListModel<VDS> getXenProxyHosts() {
        return xenProxyHosts;
    }

    public void setXenProxyHosts(ListModel<VDS> proxyHosts) {
        this.xenProxyHosts = proxyHosts;
    }

    public EntityModel<String> getKvmUri() {
        return kvmUri;
    }

    public void setKvmUri(EntityModel<String> uri) {
        this.kvmUri = uri;
    }

    public EntityModel<String> getKvmUsername() {
        return kvmUsername;
    }

    public void setKvmUsername(EntityModel<String> username) {
        this.kvmUsername = username;
    }

    public EntityModel<String> getKvmPassword() {
        return kvmPassword;
    }

    public void setKvmPassword(EntityModel<String> password) {
        this.kvmPassword = password;
    }

    public ListModel<VDS> getKvmProxyHosts() {
        return kvmProxyHosts;
    }

    public void setKvmProxyHosts(ListModel<VDS> proxyHosts) {
        this.kvmProxyHosts = proxyHosts;
    }
}

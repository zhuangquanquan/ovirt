package org.ovirt.engine.core.bll;

import java.util.Collection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class GetAllNetworkFiltersQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    @Inject
    private NetworkFilterDao networkFilterDao;

    public GetAllNetworkFiltersQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    public GetAllNetworkFiltersQuery(VdcQueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Collection<NetworkFilter> networkFilters = networkFilterDao.getAllNetworkFilters();
        getQueryReturnValue().setReturnValue(networkFilters);
    }

}

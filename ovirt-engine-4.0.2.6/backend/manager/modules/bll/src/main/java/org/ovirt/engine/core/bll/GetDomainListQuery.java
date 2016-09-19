package org.ovirt.engine.core.bll;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDomainListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = getDomainList();
        List<String> names = new ArrayList<>();
        if (response.containsKey("result")) {
            names.addAll((List<String>) response.get("result"));
        }
        sort(names);

        // Return the sorted list:
        getQueryReturnValue().setReturnValue(names);
    }

    public Map<String, Object> getDomainList() {
        return SsoOAuthServiceUtils.getDomainList(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()));
    }
}

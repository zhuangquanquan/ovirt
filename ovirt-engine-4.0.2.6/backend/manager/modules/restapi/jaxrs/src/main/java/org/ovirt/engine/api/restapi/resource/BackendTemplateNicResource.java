/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateNicResource;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicResource
        extends AbstractBackendActionableResource<Nic, VmNetworkInterface>
        implements TemplateNicResource {

    private Guid templateId;

    protected BackendTemplateNicResource(String nicId, Guid templateId) {
        super(nicId, Nic.class, VmNetworkInterface.class);
        this.templateId = templateId;
    }

    public Nic get() {
        VmNetworkInterface nic = lookupNic(guid);
        if (nic != null) {
            return addLinks(populate(map(nic), nic));
        }
        return notFound();
    }

    private VmNetworkInterface lookupNic(Guid nicId) {
        List<VmNetworkInterface> nics = getBackendCollection(
            VmNetworkInterface.class,
            VdcQueryType.GetTemplateInterfacesByTemplateId,
            new IdQueryParameters(templateId)
        );
        for (VmNetworkInterface nic : nics) {
            if (Objects.equals(nic.getId(), nicId)) {
                return nic;
            }
        }
        return null;
    }

    @Override
    public Nic update(Nic nic) {
        return performUpdate(
            nic,
            new NicResolver(),
            VdcActionType.UpdateVmTemplateInterface, new UpdateParametersProvider()
        );
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            VdcActionType.RemoveVmTemplateInterface,
            new RemoveVmTemplateInterfaceParameters(templateId, guid)
        );
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    @Override
    protected Nic addParents(Nic nic) {
        Template template = new Template();
        template.setId(templateId.toString());
        nic.setTemplate(template);
        return nic;
    }

    private class NicResolver extends EntityIdResolver<Guid> {
        @Override
        public VmNetworkInterface lookupEntity(Guid nicId) throws BackendFailureException {
            return lookupNic(nicId);
        }
    }

    private class UpdateParametersProvider implements ParametersProvider<Nic, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(Nic incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmTemplateInterfaceParameters(templateId, nic);
        }
    }
}

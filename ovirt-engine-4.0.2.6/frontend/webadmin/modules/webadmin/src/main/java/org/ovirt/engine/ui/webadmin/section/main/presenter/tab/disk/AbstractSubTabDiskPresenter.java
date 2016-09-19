package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class AbstractSubTabDiskPresenter<D extends HasEntity<?>,
    V extends AbstractSubTabPresenter.ViewDef<Disk>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter <Disk, DiskListModel, D, V, P> {

    public AbstractSubTabDiskPresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager,
            DetailModelProvider<DiskListModel, D> modelProvider, DiskMainTabSelectedItems selectedItems,
            Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, slot);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.diskMainTabPlace);
    }
}

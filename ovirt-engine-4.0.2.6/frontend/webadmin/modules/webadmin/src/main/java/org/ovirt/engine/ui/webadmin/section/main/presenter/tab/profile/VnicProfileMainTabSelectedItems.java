package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VnicProfileSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VnicProfileMainTabSelectedItems extends AbstractMainTabSelectedItems<VnicProfileView>
    implements VnicProfileSelectionChangeEvent.VnicProfileSelectionChangeHandler {

    @Inject
    VnicProfileMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(VnicProfileSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onVnicProfileSelectionChange(VnicProfileSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }

}

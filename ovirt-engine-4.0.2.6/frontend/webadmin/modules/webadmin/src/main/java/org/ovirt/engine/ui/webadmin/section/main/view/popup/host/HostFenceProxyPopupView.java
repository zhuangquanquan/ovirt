package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceProxyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceProxyPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostFenceProxyPopupView extends AbstractModelBoundPopupView<FenceProxyModel> implements HostFenceProxyPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<FenceProxyModel, HostFenceProxyPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostFenceProxyPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Path(value = "availableProxies.selectedItem")
    ListModelListBoxEditor<FenceProxyModel> pmProxyType;

    @Ignore
    @UiField
    Label noItemsAvailableLabel;

    private boolean doFlush = true;

    @Inject
    public HostFenceProxyPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    private void initEditors() {
        pmProxyType = new ListModelListBoxEditor<>(new StringRenderer<FenceProxyModel>() {
            @Override
            public String render(FenceProxyModel model) {
                return model != null ? model.getEntity().getValue() : ""; //$NON-NLS-1$
            }
        });
    }

    private void localize() {
        pmProxyType.setLabel(constants.hostProxyPreferenceTypeLabel());
        noItemsAvailableLabel.setText(constants.noHostProxyPrefenceTypeAvailableLabel());
    }

    @Override
    public void edit(FenceProxyModel object) {
        if (!object.getAvailableProxies().getItems().isEmpty()) {
            pmProxyType.setVisible(true);
            noItemsAvailableLabel.setVisible(false);
            driver.edit(object);
            doFlush = true;
        } else {
            //No available items to select, show message, hide dropdown.
            pmProxyType.setVisible(false);
            noItemsAvailableLabel.setVisible(true);
            doFlush = false;
        }
    }

    @Override
    public FenceProxyModel flush() {
        return doFlush ? driver.flush() : null;
    }

}

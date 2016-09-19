package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestContainerListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.event.shared.EventBus;

public class VmGuestContainerListModelTable extends AbstractModelBoundTableWidget<GuestContainer, VmGuestContainerListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmGuestContainerListModelTable(
            SearchableTableModelProvider<GuestContainer, VmGuestContainerListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                if (row != null) {
                    return row.getId();
                }
                return constants.empty();
            }
        }, constants.idContainer());
        getTable().addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return StringUtils.join(row.getNames(), ", "); //$NON-NLS-1$
            }
        }, constants.namesContainer());
        getTable().addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getImage();
            }
        }, constants.imageContainer());
        getTable().addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getCommand();
            }
        }, constants.commandContainer());
        getTable().addColumn(new AbstractTextColumn<GuestContainer>() {
            @Override
            public String getValue(GuestContainer row) {
                return row.getStatus();
            }
        }, constants.statusContainer());
    }

}

package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import java.util.Comparator;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;

public class SubTabUserEventNotifierView extends AbstractSubTabTableView<DbUser, EventSubscriber, UserListModel, UserEventNotifierListModel>
        implements SubTabUserEventNotifierPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserEventNotifierView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabUserEventNotifierView(SearchableDetailModelProvider<EventSubscriber, UserListModel, UserEventNotifierListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        AbstractTextColumn<EventSubscriber> eventNameColumn = new AbstractEnumColumn<EventSubscriber, AuditLogType>() {
            @Override
            protected AuditLogType getRawValue(EventSubscriber object) {
                return Enum.valueOf(AuditLogType.class, object.getEventUpName());
            }
        };
        eventNameColumn.makeSortable(new Comparator<EventSubscriber>() {
            private final LexoNumericComparator lexoNumericComparator = new LexoNumericComparator();

            @Override
            public int compare(EventSubscriber o1, EventSubscriber o2) {
                return lexoNumericComparator.compare(o1.getEventUpName(), o2.getEventUpName());
            }
        });
        getTable().addColumn(eventNameColumn, constants.eventNameEventNotifier());

        getTable().addActionButton(new WebAdminButtonDefinition<EventSubscriber>(constants.manageEventsEventNotifier()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageEventsCommand();
            }
        });
    }

}

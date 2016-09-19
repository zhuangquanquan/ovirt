package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.core.searchbackend.SessionConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabSessionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;

public class MainTabSessionView extends AbstractMainTabWithDetailsTableView<UserSession, SessionListModel>
        implements MainTabSessionPresenter.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabSessionView(MainModelProvider<UserSession, SessionListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<UserSession> sessionDbIdColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return Long.toString(session.getId());
                    }
                };
        sessionDbIdColumn.makeSortable(SessionConditionFieldAutoCompleter.SESSION_DB_ID);
        getTable().addColumn(sessionDbIdColumn, constants.sessionDbId(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<UserSession> userNameColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return session.getUserName();
                    }
                };
        userNameColumn.makeSortable(SessionConditionFieldAutoCompleter.USER_NAME);
        getTable().addColumn(userNameColumn, constants.userNameUser(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<UserSession> authzNameColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return session.getAuthzName();
                    }
                };
        authzNameColumn.makeSortable(SessionConditionFieldAutoCompleter.AUTHZ_NAME);
        getTable().addColumn(authzNameColumn, constants.authorizationProvider(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<UserSession> userIdColumn = new AbstractTextColumn<UserSession>() {
            @Override
            public String getValue(UserSession session) {
                return session.getUserId().toString();
            }
        };
        userIdColumn.makeSortable(SessionConditionFieldAutoCompleter.USER_ID);
        getTable().addColumn(userIdColumn, constants.userId(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<UserSession> sourceIpColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return session.getSourceIp();
                    }
                };
        sourceIpColumn.makeSortable(SessionConditionFieldAutoCompleter.SOURCE_IP);
        getTable().addColumn(sourceIpColumn, constants.sourceIp(), "200px"); //$NON-NLS-1$

        final DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

        AbstractTextColumn<UserSession> sessionStartColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return session.getSessionStartTime() == null ?
                                "" : //$NON-NLS-1$
                                dateFormat.format(session.getSessionStartTime());
                    }
                };
        sessionStartColumn.makeSortable(
            new Comparator<UserSession>() {
                @Override
                public int compare(UserSession s1, UserSession s2) {
                    return s1.getSessionStartTime().compareTo(s2.getSessionStartTime());
                }
            }
        );
        getTable().addColumn(sessionStartColumn, constants.sessionStartTime(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<UserSession> sessionLastActiveColumn =
                new AbstractTextColumn<UserSession>() {
                    @Override
                    public String getValue(UserSession session) {
                        return session.getSessionLastActiveTime() == null ?
                                "" : //$NON-NLS-1$
                                dateFormat.format(session.getSessionLastActiveTime());
                    }
                };
        sessionLastActiveColumn.makeSortable(
            new Comparator<UserSession>() {
                @Override
                public int compare(UserSession s1, UserSession s2) {
                    return s1.getSessionLastActiveTime().compareTo(s2.getSessionLastActiveTime());
                }
            }
        );
        getTable().addColumn(sessionLastActiveColumn, constants.sessionLastActiveTime(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<UserSession>(constants.terminateSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getTerminateCommand();
            }
        });
    }

    interface ViewIdHandler extends ElementIdHandler<MainTabSessionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

}

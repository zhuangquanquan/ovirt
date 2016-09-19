package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.DisplayUncaughtUIExceptions;
import org.ovirt.engine.ui.common.auth.AutoLoginData;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.CurrentUser.LogoutHandler;
import org.ovirt.engine.ui.common.logging.ApplicationLogManager;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Bootstrapper;

/**
 * Contains initialization logic that gets executed at application startup.
 *
 * @param <T>
 *            Login model type.
 */
public abstract class BaseApplicationInit<T extends LoginModel> implements Bootstrapper, LogoutHandler {

    private final ITypeResolver typeResolver;
    private final FrontendEventsHandlerImpl frontendEventsHandler;
    protected final FrontendFailureEventListener frontendFailureEventListener;

    protected final CurrentUser user;
    protected final EventBus eventBus;
    protected final Frontend frontend;
    private final CurrentUserRole currentUserRole;

    // Using Provider because any UiCommon model will fail before TypeResolver is initialized
    private final Provider<T> loginModelProvider;

    private final LockInteractionManager lockInteractionManager;

    private final ApplicationLogManager applicationLogManager;
    private final AlertManager alertManager;

    public BaseApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user, EventBus eventBus,
            Provider<T> loginModelProvider,
            LockInteractionManager lockInteractionManager,
            Frontend frontend, CurrentUserRole currentUserRole,
            ApplicationLogManager applicationLogManager,
            AlertManager alertManager) {
        this.typeResolver = typeResolver;
        this.frontendEventsHandler = frontendEventsHandler;
        this.frontendFailureEventListener = frontendFailureEventListener;
        this.user = user;
        this.eventBus = eventBus;
        this.loginModelProvider = loginModelProvider;
        this.lockInteractionManager = lockInteractionManager;
        this.frontend = frontend;
        this.currentUserRole = currentUserRole;
        this.applicationLogManager = applicationLogManager;
        this.alertManager = alertManager;
    }

    @Override
    public final void onBootstrap() {
        initUncaughtExceptionHandler();

        // Perform actual bootstrap via deferred command to ensure that
        // UncaughtExceptionHandler is effective during the bootstrap
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                performBootstrap();
            }
        });
    }

    void initUncaughtExceptionHandler() {
        // Prevent uncaught exceptions from escaping application code
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable t) {
                applicationLogManager.logUncaughtException(t);

                if (DisplayUncaughtUIExceptions.getValue()) {
                    alertManager.showUncaughtExceptionAlert(t);
                }
            }
        });
    }

    /**
     * Actual initialization logic that bootstraps the application.
     * <p>
     * Subclasses must override this method to initiate GWTP place transition via {@code PlaceManager},
     * for example:
     * <pre>
     * super.performBootstrap(); // Common initialization (mandatory)
     * performCustomBootstrap(); // Custom initialization (optional)
     * placeManager.revealCurrentPlace(); // Initiate place transition
     * </pre>
     */
    protected void performBootstrap() {
        // Handle UI logout requests
        user.setLogoutHandler(this);

        // Initialize UiCommon infrastructure
        initUiCommon();
        initFrontend();
        initLoginModel();

        // Check if the user should be logged in automatically
        AutoLoginData userInfo = AutoLoginData.instance();
        if (userInfo != null) {
            handleUserInfo(userInfo);
            user.setAutoLogin(true);
        }
    }

    protected T getLoginModel() {
        return loginModelProvider.get();
    }

    protected void initLoginModel() {
        final T loginModel = getLoginModel();

        // Add model login event handler
        loginModel.getLoggedInEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                onLogin(loginModel);
            }
        });

        loginModel.getCreateInstanceOnly().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                currentUserRole.setCreateInstanceOnly(loginModel.getCreateInstanceOnly().getEntity() );
            }
        });
    }

    /**
     * Called right after the model fires its login event.
     */
    protected abstract void onLogin(T loginModel);

    @Override
    public void onLogout() {
        AsyncQuery query = new AsyncQuery();
        query.setHandleFailure(true);
        query.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                // Redirect to SSO Logout after the user has logged out successfully on backend.
                Window.Location.assign(GWT.getModuleBaseURL() + "sso/logout"); //$NON-NLS-1$
            }
        };

        frontend.logoffAsync(query);
    }

    @Override
    public void onSessionExpired() {
        Window.Location.reload();
    }

    protected void performLogin(T loginModel) {
        DbUser loggedUser = loginModel.getLoggedUser();
        String loginPassword = loginModel.getPassword().getEntity();

        beforeLogin(loginModel);

        // UiCommon login preparation
        frontend.initLoggedInUser(loggedUser, loginPassword);

        // UI login actions
        user.onUserLogin(loggedUser);

        // Post-login actions
        loginModel.getPassword().setEntity(null);
    }

    /**
     * Invoked right after the user has logged in successfully on backend, before proceeding with UI login sequence.
     */
    protected abstract void beforeLogin(T loginModel);

    protected void initUiCommon() {
        // Set up UiCommon type resolver
        TypeResolver.initialize(typeResolver);
    }

    protected void initFrontend() {
        // Set up Frontend event handlers
        frontend.setEventsHandler(frontendEventsHandler);
        frontend.getFrontendFailureEvent().addListener(frontendFailureEventListener);

        frontend.getFrontendNotLoggedInEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                user.sessionExpired();
            }
        });

        frontend.setFilterQueries(filterFrontendQueries());
    }

    /**
     * Indicates if all queries triggered through {@link Frontend} should be filtered or not.
     * <p>
     * A query that is filtered has its results constrained by user permissions. On the other hand, a query that is not
     * filtered returns all matching results without additional constraints.
     */
    protected abstract boolean filterFrontendQueries();

    /**
     * When a user is already logged in on the server, the server provides user data within the host page.
     */
    protected void handleUserInfo(AutoLoginData userInfo) {
        final DbUser loggedUser = userInfo.getDbUser();

        // Use deferred command because CommonModel change needs to happen
        // after all model providers have been properly initialized
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                lockInteractionManager.showLoadingIndicator();
                getLoginModel().autoLogin(loggedUser);
            }
        });

        user.setUserInfo(userInfo);
    }

}

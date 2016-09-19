package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.util.ErrorMessageHelper;
import org.ovirt.engine.api.restapi.util.ExpectationHelper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.RunAsyncActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendResource extends BaseBackendResource {
    protected static final int NO_LIMIT = -1;
    private static final String CORRELATION_ID = "Correlation-Id";
    private static final String ASYNC_CONSTRAINT = "async";
    public static final String FORCE_CONSTRAINT = "force";
    protected static final String MAX = "max";
    private static final String NON_BLOCKING_EXPECTATION = "202-accepted";
    private static final Logger log = LoggerFactory.getLogger(BackendResource.class);
    public static final String POPULATE = "All-Content";
    public static final String JOB_ID_CONSTRAINT = "JobId";
    public static final String STEP_ID_CONSTRAINT = "StepId";

    private <T> T castQueryResultToEntity(Class<T> clz, VdcQueryReturnValue result,
                                          String constraint) throws BackendFailureException {
        T entity;

        if (List.class.isAssignableFrom(clz) && result.getReturnValue() instanceof List) {
            entity = clz.cast(result.getReturnValue());
        } else {
            List<T> list = asCollection(clz, result.getReturnValue());
            if (list == null || list.isEmpty()) {
                throw new EntityNotFoundException(constraint);
            }
            entity = clz.cast(list.get(0));
        }

        return entity;
    }

    @Deprecated
    protected <T> T getEntity(Class<T> clz, SearchType searchType, String constraint) {
        try {
            VdcQueryReturnValue result = runQuery(VdcQueryType.Search,
                    createSearchParameters(searchType, constraint));
            if (!result.getSucceeded()) {
                backendFailure(result.getExceptionString());
            }
            return castQueryResultToEntity(clz, result, constraint);
        } catch (Exception e) {
            return handleError(clz, e, false);
        }
    }

    public VdcQueryReturnValue runQuery(VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        BackendLocal backend = getBackend();
        queryParams.setFiltered(isFiltered());
        return backend.runQuery(queryType, sessionize(queryParams));
    }

    protected SearchParameters createSearchParameters(SearchType searchType, String constraint) {
        return new SearchParameters(constraint, searchType);
    }

    protected <T> T getEntity(Class<T> clz, VdcQueryType query, VdcQueryParametersBase queryParams, String identifier) {
        return getEntity(clz, query, queryParams, identifier, false);
    }

    public <T> T getEntity(Class<T> clz,
                              VdcQueryType query,
                              VdcQueryParametersBase queryParams,
                              String identifier,
                              boolean notFoundAs404) {
        return getEntity(clz, query, queryParams, identifier, notFoundAs404, true);
    }

    public <T> T getOptionalEntity(Class<T> clz,
                              VdcQueryType query,
                              VdcQueryParametersBase queryParams,
                              String identifier,
                              boolean notFoundAs404) {
        return getEntity(clz, query, queryParams, identifier, notFoundAs404, false);
    }

    private <T> T getEntity(Class<T> clz,
                              VdcQueryType query,
                              VdcQueryParametersBase queryParams,
                              String identifier,
                              boolean notFoundAs404,
                              boolean isMandatory) {
        try {
            return doGetEntity(clz, query, queryParams, identifier, isMandatory);
        } catch (Exception e) {
            return handleError(clz, e, notFoundAs404);
        }
    }

    protected <T> T doGetEntity(Class<T> clz,
                                VdcQueryType query,
                                VdcQueryParametersBase queryParams,
                                String identifier) throws BackendFailureException {
        return doGetEntity(clz, query, queryParams, identifier, true);
    }

    protected <T> T doGetEntity(Class<T> clz,
                                VdcQueryType query,
                                VdcQueryParametersBase queryParams,
                                String identifier,
                                boolean isMandatory) throws BackendFailureException {
        VdcQueryReturnValue result = runQuery(query, queryParams);
        if (!result.getSucceeded() || (isMandatory && result.getReturnValue() == null)) {
            if (result.getExceptionString() != null) {
                backendFailure(result.getExceptionString());
            } else {
                throw new EntityNotFoundException(identifier);
            }
        } else {
            if (result.getReturnValue() == null) {
                return null;
            }
        }
        return castQueryResultToEntity(clz, result, identifier);
    }

    protected <T> List<T> getBackendCollection(Class<T> clz, VdcQueryType query, VdcQueryParametersBase queryParams) {
        try {
            List<T> results = asCollection(clz, new ArrayList<T>());
            VdcQueryReturnValue result = runQuery(query, queryParams);
            if (result!=null ) {
                if (!result.getSucceeded()) {
                    backendFailure(result.getExceptionString());
                }
                results = asCollection(clz, result.getReturnValue());
                int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, MAX, NO_LIMIT, NO_LIMIT);
                if (max != NO_LIMIT && max < results.size()) {
                    results = results.subList(0, max);
                }
            }
            return results;
        } catch (Exception e) {
            return handleError(e, false);
        }
    }

    protected Response performAction(VdcActionType task, VdcActionParametersBase params, Action action) {
        return performAction(task, params, action, false);
    }

    protected Response performAction(VdcActionType task, VdcActionParametersBase params) {
        return performAction(task, params, null, false);
    }

    protected Response performAction(VdcActionType task, VdcActionParametersBase params, Action action, boolean getEntityWhenDone) {
        try {
            if (isAsync() || expectNonBlocking()) {
                return performNonBlockingAction(task, params, action);
            } else {
                VdcReturnValueBase actionResult = doAction(task, params);
                if (action == null) {
                    action = new Action();
                }
                if (actionResult.getJobId() != null) {
                    setJobLink(action, actionResult);
                }
                action.setStatus(CreationStatus.COMPLETE.value());
                if (getEntityWhenDone) {
                    setActionItem(action, getEntity());
                }
                return Response.ok().entity(action).build();
            }
        } catch (Exception e) {
            return handleError(Response.class, e, false);
        }
    }

    protected void setJobLink(final Action action, VdcReturnValueBase actionResult) {
        Job job = new Job();
        job.setId(actionResult.getJobId().toString());
        LinkHelper.addLinks(job, null, false);
        action.setJob(job);
    }

    protected boolean isAsync() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, ASYNC_CONSTRAINT, true, false);
    }

    protected boolean isForce() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORCE_CONSTRAINT, true, false);
    }

    protected void badRequest(String message) {
        throw new WebFaultException(null, message, Response.Status.BAD_REQUEST);
    }

    protected boolean expectNonBlocking() {
        Set<String> expectations = ExpectationHelper.getExpectations(httpHeaders);
        return expectations.contains(NON_BLOCKING_EXPECTATION);
    }
    protected Response performNonBlockingAction(VdcActionType task, VdcActionParametersBase params, Action action) {
        try {
            doNonBlockingAction(task, params);
            if (action!=null) {
                action.setStatus(CreationStatus.IN_PROGRESS.value());
                return Response.status(Response.Status.ACCEPTED).entity(action).build();
            } else {
                return Response.status(Response.Status.ACCEPTED).build();
            }
        } catch (Exception e) {
            return handleError(Response.class, e, false);
        }
    }

    protected <T> T performAction(VdcActionType task, VdcActionParametersBase params, Class<T> resultType) {
        try {
            return resultType.cast(doAction(task, params).getActionReturnValue());
        } catch (Exception e) {
            return handleError(resultType, e, false);
        }
    }

    protected VdcReturnValueBase doAction(VdcActionType task,
                                          VdcActionParametersBase params) throws BackendFailureException {
        BackendLocal backend = getBackend();
        setJobOrStepId(params);
        setCorrelationId(params);
        VdcReturnValueBase result = backend.runAction(task, sessionize(params));
        if (result != null && !result.isValid()) {
            backendFailure(result.getValidationMessages());
        } else if (result != null && !result.getSucceeded()) {
            backendFailure(result.getExecuteFailedMessages());
        }
        assert result != null;
        return result;
    }

    protected void doNonBlockingAction(final VdcActionType task, final VdcActionParametersBase params) {
        BackendLocal backend = getBackend();
        setCorrelationId(params);
        setJobOrStepId(params);
        backend.runAction(VdcActionType.RunAsyncAction, sessionize(new RunAsyncActionParameters(task, sessionize(params))));
    }

    private void setJobOrStepId(VdcActionParametersBase params) {
        String jobId = ParametersHelper.getParameter(httpHeaders, uriInfo, JOB_ID_CONSTRAINT);
        if (jobId != null) {
            params.setJobId(asGuid(jobId));
        }

        String stepId = ParametersHelper.getParameter(httpHeaders, uriInfo, STEP_ID_CONSTRAINT);
        if (stepId != null) {
            params.setJobId(asGuid(stepId));
        }
    }
    private void setCorrelationId(VdcActionParametersBase params) {
        if (httpHeaders == null) {
            return;
        }

        List<String> correlationIds = httpHeaders.getRequestHeader(CORRELATION_ID);
        if (correlationIds != null && correlationIds.size() > 0) {
            params.setCorrelationId(correlationIds.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getConfigurationValue(ConfigurationValues config, final Version version) {
        VdcQueryReturnValue result = runQuery(
            VdcQueryType.GetConfigurationValue,
            new GetConfigurationValueParameters(config, asString(version))
        );
        if (result.getSucceeded()) {
            return (T) result.getReturnValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getConfigurationValueDefault(ConfigurationValues config) {
        VdcQueryReturnValue result = runQuery(
            VdcQueryType.GetConfigurationValue,
            new GetConfigurationValueParameters(config, ConfigCommon.defaultConfigurationVersion)
        );
        if (result.getSucceeded()) {
            return (T) result.getReturnValue();
        }
        return null;
    }

    private static final String VERSION_FORMAT = "{0}.{1}";

    static String asString(Version version) {
        return version == null ? null : MessageFormat.format(VERSION_FORMAT, version.getMajor(), version.getMinor());
    }

    /**
     * @return true if request header contains [All-Content='true']
     */
    protected boolean isPopulate() {
        List<String> populates = httpHeaders.getRequestHeader(POPULATE);
        if (populates != null && populates.size() > 0) {
            return Boolean.valueOf(populates.get(0)).booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Runs implementation of the @GET annotated method of this resource (get() for single entity resources, and list()
     * for collection resources).
     *
     * @return The result of the @GET annotated method, an entity or list of entities.
     */
    protected Object getEntity() {
        try {
            Method m = resolveGet();
            if (m == null) {
                return null;
            }
            Object entity = m.invoke(this);
            return getEntityWithIdAndHref(entity);
        } catch (Exception e) {
            log.error("Getting resource after action failed.", e);
            return null;
        }
    }

    private Method resolveGet() throws NoSuchMethodException, SecurityException {
        Method methodSignature = findGetSignature(this.getClass());
        if (methodSignature == null) {
            return null;
        }
        Method methodImplementation =
                this.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        return methodImplementation;
    }

    private static Method findGetSignature(Class<?> clazz) {
        Class<?> currentAncestor = clazz;
        while (currentAncestor != null) {
            Class<?>[] interfaces = currentAncestor.getInterfaces();
            for (Class<?> ifc : interfaces) {
                Method m = find(ifc);
                if (m != null) {
                    return m;
                }
            }
            currentAncestor = currentAncestor.getSuperclass();
        }
        return null;
    }

    private static Method find(Class<?> ifc) {
        Class<?> currentAncestor = ifc;
        while (currentAncestor != null) {
            for (Method m : currentAncestor.getMethods()) {
                if (m.isAnnotationPresent(GET.class)) {
                    return m;
                }
            }
            currentAncestor = currentAncestor.getSuperclass();
        }
        return null;
    }

    protected Object getEntityWithIdAndHref(Object entity) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Object newEntity = entity.getClass().newInstance();
        setEntityValue(newEntity, "setId", getEntityValue(entity, "getId"));
        setEntityValue(newEntity, "setHref", getEntityValue(entity, "getHref"));
        return entity;
    }

    private void setEntityValue(Object entity, String methodName, Object value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = entity.getClass().getMethod(methodName);
        method.invoke(entity, value);
    }

    private Object getEntityValue(Object entity, String methodName) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object nullObj = null;
        Method method = entity.getClass().getMethod(methodName);
        return method.invoke(entity, nullObj);
    }

    protected Response actionStatus(CreationStatus status, Action action, Object result) {
        setActionItem(action, result);
        action.setStatus(status.value());
        return Response.ok().entity(action).build();
    }

    protected void setActionItem(Action action, Object result) {
        if (result == null) {
            return;
        }
        String name = result.getClass().getSimpleName().toLowerCase();
        for (Method m : action.getClass().getMethods()) {
            if (m.getName().startsWith("set") && m.getName().replace("set", "").toLowerCase().equals(name)) {
                try {
                    m.invoke(action, result);
                    break;
                } catch (Exception e) {
                    // should not happen
                    log.error("Resource to action assignment failure.", e);
                    break;
                }
            }
        }
    }

    protected void backendFailure(String msg) throws BackendFailureException {
        throw new BackendFailureException(localize(msg), ErrorMessageHelper.getErrorStatus(msg));
    }

    protected void backendFailure(List<String> messages) throws BackendFailureException {
        throw new BackendFailureException(localize(messages), ErrorMessageHelper.getErrorStatus(messages));
    }
}

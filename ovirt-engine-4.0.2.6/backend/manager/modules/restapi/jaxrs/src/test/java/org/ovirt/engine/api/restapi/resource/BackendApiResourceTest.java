package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class BackendApiResourceTest extends EasyMockSupport {

    private static final String ROOT_TAG_HREF = "/ovirt-engine/api/tags/00000000-0000-0000-0000-000000000000";
    private static final String BLANK_TEMPLATE_HREF = "/ovirt-engine/api/templates/00000000-0000-0000-0000-000000000000";

    protected BackendApiResource resource;

    protected BackendLocal backend;
    protected Current current;
    protected DbUser currentUser;
    protected HttpHeaders httpHeaders;

    protected static final String USER = "Aladdin";
    protected static final String DOMAIN = "Maghreb";

    protected static final String URI_ROOT = "http://localhost:8099";
    protected static final String BASE_PATH = "/ovirt-engine/api";
    protected static final String BUNDLE_PATH = "org/ovirt/engine/api/restapi/logging/Messages";
    protected static final String SESSION_ID = Guid.newGuid().toString();
    private static final String USER_FILTER_HEADER = "filter";

    protected static final int MAJOR = 11;
    protected static final int MINOR = 0;
    protected static final int BUILD = 99;
    protected static final int REVISION = 13;
    protected static final String SYSTEM_VERSION =
        Integer.toString(MAJOR) + "." +
        Integer.toString(MINOR) + "." +
        Integer.toString(BUILD) + "." +
        Integer.toString(REVISION);

    protected static int TOTAL_VMS = 123456;
    protected static int ACTIVE_VMS = 23456;
    protected static int TOTAL_HOSTS = 23456;
    protected static int ACTIVE_HOSTS = 3456;
    protected static int TOTAL_USERS = 3456;
    protected static int ACTIVE_USERS = 456;
    protected static int TOTAL_STORAGE_DOMAINS = 56;
    protected static int ACTIVE_STORAGE_DOMAINS = 6;

    private static final String[] relationships = {
        "clusters",
        "clusters/search",
        "datacenters",
        "datacenters/search",
        "events",
        "events/search",
        "hosts",
        "hosts/search",
        "networks",
        "networks/search",
        "roles",
        "storagedomains",
        "storagedomains/search",
        "tags",
        "bookmarks",
        "icons",
        "templates",
        "templates/search",
        "instancetypes",
        "instancetypes/search",
        "users",
        "users/search",
        "groups",
        "groups/search",
        "domains",
        "vmpools",
        "vmpools/search",
        "vms",
        "vms/search",
        "disks",
        "disks/search",
        "jobs",
        "storageconnections",
        "vnicprofiles",
        "diskprofiles",
        "cpuprofiles",
        "schedulingpolicyunits",
        "schedulingpolicies",
        "permissions",
        "macpools",
        "networkfilters",
        "operatingsystems",
        "externalhostproviders",
        "openstackimageproviders",
        "openstackvolumeproviders",
        "openstacknetworkproviders",
        "katelloerrata",
        "affinitylabels",
        "clusterlevels",
    };

    private static final String[] relationshipsGlusterOnly = {
            "clusters",
            "clusters/search",
            "events",
            "events/search",
            "hosts",
            "hosts/search",
            "networks",
            "networks/search",
            "roles",
            "tags",
            "users",
            "users/search",
            "groups",
            "groups/search",
            "domains",
    };

    private static final String[] hrefs = {
        BASE_PATH + "/clusters",
        BASE_PATH + "/clusters?search={query}",
        BASE_PATH + "/datacenters",
        BASE_PATH + "/datacenters?search={query}",
        BASE_PATH + "/events",
        BASE_PATH + "/events;from={event_id}?search={query}",
        BASE_PATH + "/hosts",
        BASE_PATH + "/hosts?search={query}",
        BASE_PATH + "/networks",
        BASE_PATH + "/networks?search={query}",
        BASE_PATH + "/roles",
        BASE_PATH + "/storagedomains",
        BASE_PATH + "/storagedomains?search={query}",
        BASE_PATH + "/tags",
        BASE_PATH + "/bookmarks",
        BASE_PATH + "/icons",
        BASE_PATH + "/templates",
        BASE_PATH + "/templates?search={query}",
        BASE_PATH + "/instancetypes",
        BASE_PATH + "/instancetypes?search={query}",
        BASE_PATH + "/users",
        BASE_PATH + "/users?search={query}",
        BASE_PATH + "/groups",
        BASE_PATH + "/groups?search={query}",
        BASE_PATH + "/domains",
        BASE_PATH + "/vmpools",
        BASE_PATH + "/vmpools?search={query}",
        BASE_PATH + "/vms",
        BASE_PATH + "/vms?search={query}",
        BASE_PATH + "/disks",
        BASE_PATH + "/disks?search={query}",
        BASE_PATH + "/jobs",
        BASE_PATH + "/storageconnections",
        BASE_PATH + "/vnicprofiles",
        BASE_PATH + "/diskprofiles",
        BASE_PATH + "/cpuprofiles",
        BASE_PATH + "/schedulingpolicyunits",
        BASE_PATH + "/schedulingpolicies",
        BASE_PATH + "/permissions",
        BASE_PATH + "/macpools",
        BASE_PATH + "/networkfilters",
        BASE_PATH + "/operatingsystems",
        BASE_PATH + "/externalhostproviders",
        BASE_PATH + "/openstackimageproviders",
        BASE_PATH + "/openstackvolumeproviders",
        BASE_PATH + "/openstacknetworkproviders",
        BASE_PATH + "/katelloerrata",
        BASE_PATH + "/affinitylabels",
        BASE_PATH + "/clusterlevels",
    };

    private static final String[] hrefsGlusterOnly = {
            BASE_PATH + "/clusters",
            BASE_PATH + "/clusters?search={query}",
            BASE_PATH + "/events",
            BASE_PATH + "/events;from={event_id}?search={query}",
            BASE_PATH + "/hosts",
            BASE_PATH + "/hosts?search={query}",
            BASE_PATH + "/networks",
            BASE_PATH + "/networks?search={query}",
            BASE_PATH + "/roles",
            BASE_PATH + "/tags",
            BASE_PATH + "/users",
            BASE_PATH + "/users?search={query}",
            BASE_PATH + "/groups",
            BASE_PATH + "/groups?search={query}",
            BASE_PATH + "/domains",
    };

    public BackendApiResourceTest() {
        resource = new BackendApiResource();
    }

    @Before
    public void setUp() {
        backend = createMock(BackendLocal.class);

        currentUser = new DbUser();
        currentUser.setLoginName(USER);
        currentUser.setDomain(DOMAIN);
        current = new Current();
        current.setUser(currentUser);
        current.setSessionId(SESSION_ID);
        current.setRoot(URI_ROOT);
        current.setPrefix(BASE_PATH);
        current.setPath("");
        current.setVersion("4");
        current.setVersionSource(VersionSource.DEFAULT);
        current.setBackend(backend);
        CurrentManager.put(current);

        MessageBundle messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        resource.setMessageBundle(messageBundle);

        httpHeaders = createMock(HttpHeaders.class);
        List<Locale> locales = new ArrayList<>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        List<String> filterValue = new ArrayList<>();
        filterValue.add("false");
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue).anyTimes();
        resource.setHttpHeaders(httpHeaders);
    }

    @After
    public void tearDown() {
        verifyAll();
        CurrentManager.remove();
    }

    @Test
    public void testGet() {
        doTestGet(ApplicationMode.AllModes);
    }

    @Test
    public void testGetVirtOnly() {
        doTestGet(ApplicationMode.VirtOnly);
    }

    @Test
    public void testGetWithTrailingSlash() {
        doTestGet(ApplicationMode.AllModes);
    }

    @Test
    public void testGetWithTrailingSlashVirtOnly() {
        doTestGet(ApplicationMode.VirtOnly);
    }

    @Test
    public void testGetGlusterOnly() {
        doTestGlusterOnlyGet();
    }

    protected void doTestGet(ApplicationMode appMode) {
        setupExpectations(appMode);
        verifyResponse(resource.get());
    }

    private void setupExpectations(ApplicationMode appMode) {
        current.setApplicationMode(appMode);
        resource.setUriInfo(setUpUriInfo());
        setUpGetSystemVersionExpectations();
        setUpGetSystemStatisticsExpectations();
    }

    protected void doTestGlusterOnlyGet() {
        setupExpectations(ApplicationMode.GlusterOnly);
        verifyResponseGlusterOnly(resource.get());
    }

    protected HashMap<String, Integer> setUpStats() {
        HashMap<String, Integer> stats = new HashMap<>();

        stats.put("total_vms", TOTAL_VMS);
        stats.put("active_vms", ACTIVE_VMS);
        stats.put("total_vds", TOTAL_HOSTS);
        stats.put("active_vds", ACTIVE_HOSTS);
        stats.put("total_users", TOTAL_USERS);
        stats.put("active_users", ACTIVE_USERS);
        stats.put("total_storage_domains", TOTAL_STORAGE_DOMAINS);
        stats.put("active_storage_domains", ACTIVE_STORAGE_DOMAINS);

        return stats;
    }

    protected void verifyResponse(Response response) {
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof Api);
        verifyApi((Api) response.getEntity());
    }

    protected void verifyResponseGlusterOnly(Response response) {
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof Api);
        verifyApiGlusterOnly((Api) response.getEntity());
    }

    protected void verifyApi(Api api) {
        assertNotNull(api);
        assertNotNull(api.getTime());
        assertNotNull(api.getLinks());

        assertEquals(relationships.length, api.getLinks().size());
        for (int i = 0; i < relationships.length; i++) {
            Link l = api.getLinks().get(i);
            assertNotNull(l);
            assertEquals(relationships[i], l.getRel());
            assertEquals(hrefs[i], l.getHref());
        }

        assertNotNull(api.getSpecialObjects());
        assertContainsRootTag(api.getSpecialObjects());
        assertContainsBlankTemplate(api.getSpecialObjects());
        assertNotNull(api.getProductInfo());
        assertNotNull(api.getProductInfo().getVersion());
        assertNotNull(api.getProductInfo().getVersion().getFullVersion());
        assertEquals(MAJOR,    api.getProductInfo().getVersion().getMajor().intValue());
        assertEquals(MINOR,    api.getProductInfo().getVersion().getMinor().intValue());
        assertEquals(BUILD,    api.getProductInfo().getVersion().getBuild().intValue());
        assertEquals(REVISION, api.getProductInfo().getVersion().getRevision().intValue());

        assertNotNull(api.getSummary());
        assertEquals(TOTAL_VMS,              api.getSummary().getVms().getTotal().intValue());
        assertEquals(ACTIVE_VMS,             api.getSummary().getVms().getActive().intValue());
        assertEquals(TOTAL_HOSTS,            api.getSummary().getHosts().getTotal().intValue());
        assertEquals(ACTIVE_HOSTS,           api.getSummary().getHosts().getActive().intValue());
        assertEquals(TOTAL_USERS,            api.getSummary().getUsers().getTotal().intValue());
        assertEquals(ACTIVE_USERS,           api.getSummary().getUsers().getActive().intValue());
        assertEquals(TOTAL_STORAGE_DOMAINS,  api.getSummary().getStorageDomains().getTotal().intValue());
        assertEquals(ACTIVE_STORAGE_DOMAINS, api.getSummary().getStorageDomains().getActive().intValue());
    }

    protected void verifyApiGlusterOnly(Api api) {
        assertNotNull(api);
        assertNotNull(api.getTime());
        assertNotNull(api.getLinks());

        assertEquals(relationshipsGlusterOnly.length, api.getLinks().size());
        for (int i = 0; i < relationshipsGlusterOnly.length; i++) {
            Link l = api.getLinks().get(i);
            assertNotNull(l);
            assertEquals(relationshipsGlusterOnly[i], l.getRel());
            assertEquals(hrefsGlusterOnly[i], l.getHref());
        }

        assertNotNull(api.getSpecialObjects());
        assertContainsRootTag(api.getSpecialObjects());
        assertNotNull(api.getProductInfo());
        assertNotNull(api.getProductInfo().getVersion());
        assertEquals(MAJOR, api.getProductInfo().getVersion().getMajor().intValue());
        assertEquals(MINOR, api.getProductInfo().getVersion().getMinor().intValue());
        assertEquals(BUILD, api.getProductInfo().getVersion().getBuild().intValue());
        assertEquals(REVISION, api.getProductInfo().getVersion().getRevision().intValue());

        assertNotNull(api.getSummary());
        assertEquals(TOTAL_HOSTS, api.getSummary().getHosts().getTotal().intValue());
        assertEquals(ACTIVE_HOSTS, api.getSummary().getHosts().getActive().intValue());
        assertEquals(TOTAL_USERS, api.getSummary().getUsers().getTotal().intValue());
        assertEquals(ACTIVE_USERS, api.getSummary().getUsers().getActive().intValue());
    }



    private static void assertContainsBlankTemplate(SpecialObjects objs) {
        assertNotNull(objs.getBlankTemplate());
        assertEquals(BLANK_TEMPLATE_HREF, objs.getBlankTemplate().getHref());
    }

    private static void assertContainsRootTag(SpecialObjects objs) {
        assertNotNull(objs.getRootTag());
        assertEquals(ROOT_TAG_HREF, objs.getRootTag().getHref());
    }

    protected UriInfo setUpUriInfo() {
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();

        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getQueryParameters()).andReturn(null).anyTimes();
        expect(uriInfo.getPathSegments()).andReturn(null).anyTimes();

        return uriInfo;
    }

    protected void setUpGetSystemVersionExpectations() {
        VdcQueryReturnValue productRpmQueryResult = createMock(VdcQueryReturnValue.class);
        expect(productRpmQueryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(productRpmQueryResult.getReturnValue()).andReturn(SYSTEM_VERSION).anyTimes();
        expect(backend.runQuery(eq(VdcQueryType.GetConfigurationValue), getProductRPMVersionParams())).andReturn(productRpmQueryResult);

        VdcQueryReturnValue productVersionQueryResult = createMock(VdcQueryReturnValue.class);
        expect(productVersionQueryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(productVersionQueryResult.getReturnValue()).andReturn(new Version(MAJOR, MINOR, BUILD, REVISION))
                .anyTimes();
        expect(backend.runQuery(eq(VdcQueryType.GetProductVersion), getProductVersionParams())).andReturn(productVersionQueryResult);
    }

    private VdcQueryParametersBase getProductVersionParams() {
        return eqQueryParams(VdcQueryParametersBase.class, new String[0], new Object[0]);
    }

    protected void setUpGetSystemStatisticsExpectations() {
        VdcQueryReturnValue queryResult = createMock(VdcQueryReturnValue.class);

        expect(backend.runQuery(eq(VdcQueryType.GetSystemStatistics), queryParams())).andReturn(queryResult);

        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(setUpStats()).anyTimes();

        replayAll();
    }

    protected VdcQueryParametersBase getProductRPMVersionParams() {
        return eqQueryParams(GetConfigurationValueParameters.class,
                new String[] { "SessionId", "ConfigValue" },
                new Object[] { SESSION_ID, ConfigurationValues.ProductRPMVersion });
    }

    protected VdcQueryParametersBase queryParams() {
        return eqQueryParams(GetSystemStatisticsQueryParameters.class,
                             new String[] { "SessionId" },
                new Object[] { SESSION_ID });
    }
}


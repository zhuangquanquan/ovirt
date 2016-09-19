package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class NetworkFilterDaoTest extends BaseDaoTestCase {

    private static final String INVALID_NETWORK_FILTER_NAME = "invalid-network-filter-name";
    private static final String INVALID_VERSION = "1.0";

    @Inject
    private NetworkFilterDao dao;

    @Test
    public void testGetAllNetworkFilters() {
        final List<NetworkFilter> allNetworkFilter = dao.getAllNetworkFilters();
        assertNotNull(allNetworkFilter);
        assertEquals(3, allNetworkFilter.size());
        NetworkFilter networkFilter = expectedNetworkFilter();
        assertEquals(networkFilter, allNetworkFilter.get(0));
    }

    @Test
    public void testGetAllSupportedNetworkFiltersByVersion() {
        final Version version = new Version("4.0");
        final List<NetworkFilter> allNetworkFilter = dao.getAllSupportedNetworkFiltersByVersion(version);
        assertNotNull(allNetworkFilter);
        assertEquals(3, allNetworkFilter.size());
        NetworkFilter expectedNetworkFilter = initOvirtNoFilter();
        assertEquals(expectedNetworkFilter, allNetworkFilter.get(1));
        expectedNetworkFilter = expectedNetworkFilter();
        assertEquals(expectedNetworkFilter, allNetworkFilter.get(0));
    }

    @Test
    public void testGetAllSupportedNetworkFiltersByVersionMediumVersion() {
        final Version version = new Version("3.5");
        assertFirstVersionOnly(version);
    }

    @Test
    public void testGetAllSupportedNetworkFiltersByVersionFirstVersion() {
        final Version version = new Version("3.0");
        assertFirstVersionOnly(version);
    }

    @Test
    public void testGetAllSupportedNetworkFiltersByInvalidVersion() {
        final Version version = new Version(INVALID_VERSION);
        final List<NetworkFilter> allNetworkFilter = dao.getAllSupportedNetworkFiltersByVersion(version);
        assertTrue(allNetworkFilter.isEmpty());
    }

    private void assertFirstVersionOnly(Version version) {
        final List<NetworkFilter> allNetworkFilter = dao.getAllSupportedNetworkFiltersByVersion(version);
        assertNotNull(allNetworkFilter);
        assertEquals(1, allNetworkFilter.size());
        NetworkFilter expectedNetworkFilter = initOvirtNoFilter();
        assertEquals(expectedNetworkFilter, allNetworkFilter.get(0));
    }

    @Test
    public void testGetFilterById() {
        final NetworkFilter actualNetworkFilter = dao.getNetworkFilterById(FixturesTool.NETWORK_FILTER);
        final NetworkFilter expectedNetworkFilter = expectedNetworkFilter();
        assertEquals(expectedNetworkFilter, actualNetworkFilter);
    }

    @Test
    public void testGetFilterByInvalidId() {
        final NetworkFilter actualNetworkFilter = dao.getNetworkFilterById(Guid.newGuid());
        assertNull(actualNetworkFilter);
    }

    @Test
    public void testGetFilterByName() {
        final NetworkFilter actualNetworkFilter = dao.getNetworkFilterByName(FixturesTool.NETWORK_FILTER_NAME);
        final NetworkFilter expectedNetworkFilter = expectedNetworkFilter();
        assertEquals(expectedNetworkFilter, actualNetworkFilter);
    }

    @Test
    public void testGetFilterByInvalidName() {
        final NetworkFilter actualNetworkFilter = dao.getNetworkFilterByName(INVALID_NETWORK_FILTER_NAME);
        assertNull(actualNetworkFilter);
    }

    private NetworkFilter expectedNetworkFilter() {
        NetworkFilter networkFilter = new NetworkFilter();
        networkFilter.setName(FixturesTool.NETWORK_FILTER_NAME);
        networkFilter.setVersion(FixturesTool.NETWORK_FILTER_VERSION);
        networkFilter.setId(FixturesTool.NETWORK_FILTER);
        return networkFilter;
    }

    private NetworkFilter initOvirtNoFilter() {
        NetworkFilter networkFilter = new NetworkFilter();
        networkFilter.setName(FixturesTool.OVIRT_NO_FILTER_NETWORK_FILTER_NAME);
        networkFilter.setVersion(FixturesTool.OVIRT_NO_FILTER_MINIMAL_SUPPORTED_VERSION);
        networkFilter.setId(FixturesTool.OVIRT_NO_FILTER_NETWORK_FILTER);
        return networkFilter;
    }

}

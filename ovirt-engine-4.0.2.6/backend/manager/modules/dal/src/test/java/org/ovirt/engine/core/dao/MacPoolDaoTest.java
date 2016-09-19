package org.ovirt.engine.core.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

public class MacPoolDaoTest extends BaseGenericDaoTestCase<Guid, MacPool, MacPoolDao>{
    @Test
    public void testGetDefaultPool() throws Exception {
        final MacPool defaultPool = dao.getDefaultPool();
        assertThat(defaultPool, notNullValue());
        assertThat(defaultPool.getId(), is(FixturesTool.DEFAULT_MAC_POOL_ID));
        assertThat(defaultPool.isDefaultPool(), is(true));
    }

    @Test
    public void testGetAllMacsForMacPool() throws Exception {
        final List<String> allMacsForMacPool = dao.getAllMacsForMacPool(FixturesTool.NON_DEFAULT_MAC_POOL);

        assertThat(allMacsForMacPool.containsAll(Arrays.asList("00:1a:4a:16:87:da", "00:1a:4a:16:87:d9")), is(true));
    }

    @Override
    protected MacPool generateNewEntity() {
        final MacPool macPool = new MacPool();

        macPool.setId(Guid.newGuid());
        macPool.setName("someName");
        macPool.setAllowDuplicateMacAddresses(true);
        macPool.setDescription("someDesc");
        macPool.setDefaultPool(false);

        final MacRange macRange = new MacRange();
        macRange.setMacPoolId(macPool.getId());
        macRange.setMacFrom("01:c0:81:21:71:17");
        macRange.setMacTo("01:c1:81:21:71:17");

        macPool.getRanges().add(macRange);

        return macPool;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setName(RandomUtils.instance().nextString(15));
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.NOT_USED_MAC_POOL_ID;
    }

    @Override
    protected MacPoolDao prepareDao() {
        return dbFacade.getMacPoolDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 4;
    }


}

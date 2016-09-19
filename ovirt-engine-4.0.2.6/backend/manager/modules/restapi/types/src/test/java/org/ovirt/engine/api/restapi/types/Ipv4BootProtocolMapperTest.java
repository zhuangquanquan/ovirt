package org.ovirt.engine.api.restapi.types;

import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import javax.ws.rs.WebApplicationException;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

public class Ipv4BootProtocolMapperTest {

    @Test(expected = WebApplicationException.class)
    public void testMapAutoconf() {
        Ipv4BootProtocolMapper.map(BootProtocol.AUTOCONF);
    }

    @Test
    public void testUnmappedEntityValue() {
        for (Ipv4BootProtocol value : Ipv4BootProtocol.values()) {
            assertThat(String.format("%s.%s is not mapped", Ipv6BootProtocol.class.getName(), value),
                    Ipv4BootProtocolMapper.map(value),
                    Matchers.notNullValue());
        }
    }

    @Test
    public void testUnmappedModelValue() {
        for (BootProtocol value : EnumSet.complementOf(EnumSet.of(BootProtocol.AUTOCONF))) {
            assertThat(String.format("%s.%s is not mapped", BootProtocol.class.getName(), value),
                    Ipv4BootProtocolMapper.map(value),
                    Matchers.notNullValue());
        }
    }
}

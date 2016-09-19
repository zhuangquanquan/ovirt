package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.gluster.UpdateGlusterHostPubKeyToSlaveParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class UpdateGlusterHostPubKeyToSlaveInternalCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    private UpdateGlusterHostPubKeyToSlaveInternalCommand command;

    @Mock
    protected VDS vds;

    @Test
    public void commandSucceeds() {
        List<String> pubKeys = new ArrayList<>();
        pubKeys.add("");
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys), null));
        doReturn(vds).when(command).getUpServer();
        assertTrue(command.validate());
    }

    @Test
    public void commandFailsNoPubKeys() {
        List<String> pubKeys = new ArrayList<>();
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys), null));
        doReturn(vds).when(command).getUpServer();
        assertFalse(command.validate());
    }

    @Test
    public void commandFailsNoUpServer() {
        List<String> pubKeys = new ArrayList<>();
        pubKeys.add("");
        command =
                spy(new UpdateGlusterHostPubKeyToSlaveInternalCommand(new UpdateGlusterHostPubKeyToSlaveParameters(Guid.newGuid(),
                        pubKeys), null));
        doReturn(null).when(command).getUpServer();
        command.setClusterId(Guid.newGuid());
        doReturn(new Cluster()).when(command).getCluster();
        assertFalse(command.validate());
    }
}

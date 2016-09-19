package org.ovirt.engine.core.bll.storage.disk;

import org.junit.Test;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class HotUnPlugDiskFromVmCommandTest extends HotPlugDiskToVmCommandTest {

    @Override
    @Test
    public void validateFailedWrongPlugStatus() throws Exception {
        mockVmStatusUp();
        mockInterfaceList();
        createDiskWrongPlug(false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.HOT_UNPLUG_DISK_IS_NOT_PLUGGED);
    }

    @Override
    protected HotUnPlugDiskFromVmCommand<VmDiskOperationParameterBase> createCommand() {
        return new HotUnPlugDiskFromVmCommand<>(createParameters(), null);
    }

    @Override
    protected void createVirtIODisk() {
        super.createVirtIODisk();
        vmDevice.setIsPlugged(true);
    }
}

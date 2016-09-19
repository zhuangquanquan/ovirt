package org.ovirt.engine.ui.uicompat;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

public class UIMessagesTest {
    @Test
    public void doTest() throws URISyntaxException, IOException {
        List<String> errors = GwtMessagesValidator.validateClass(UIMessages.class);
        assertTrue(GwtMessagesValidator.format(errors), errors.isEmpty());
    }
}

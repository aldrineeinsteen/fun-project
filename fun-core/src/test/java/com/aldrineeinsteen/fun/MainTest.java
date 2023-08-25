package com.aldrineeinsteen.fun;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testMainWithInvalidArguments() throws Exception {
        String[] args = {"invalid", "arguments"};
        exit.expectSystemExitWithStatus(500);
        Main.main(args);
    }

    @Test
    public void testMainWithSignatureOption() throws Exception {
        // This test would ideally mock the SignatureSelector and TerminalParser and check if they are invoked.
        String[] args = {"-sign"};
        Main.main(args);
    }

    @Test
    public void testMainWithKeepAliveOption() throws Exception {
        // This test would ideally mock the KeepAliveTimer and check if it's invoked with default values.
        String[] args = {"-k"};
        Main.main(args);
    }

    @Test
    public void testMainWithKeepAliveAndEndTimeOption() throws Exception {
        // This test would ideally mock the KeepAliveTimer and check if it's invoked with the specified end time.
        String[] args = {"-k", "-e", "18:30"};
        Main.main(args);
    }

    @Test
    public void testMainWithValidEndTime() throws Exception {
        String[] args = {"-k", "-e", "18:30"};
        Main.main(args);
    }

    @Test
    public void testMainWithInvalidEndTime() {
        String[] args = {"-k", "-e", "1830"};
        Assertions.assertThrows(DateTimeParseException.class, () -> Main.main(args));
    }

    @Test
    public void testMainWithKeepAliveAndSecondsOption() throws Exception {
        // This test would ideally mock the KeepAliveTimer and check if it's invoked with the specified seconds.
        String[] args = {"-k", "-sec", "45"};
        Main.main(args);
    }
}
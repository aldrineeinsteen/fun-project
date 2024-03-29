package com.aldrineeinsteen.fun;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testMainWithInvalidArguments() {
        String[] args = {"--invalid", "--arguments"};
        Assertions.assertThrows(ParseException.class, () -> Main.main(args));
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
        String[] args = {"--keep-alive"};
        Main.main(args);
    }

    /*@Test
    public void testMainWithKeepAliveAndEndTimeOption() throws Exception {
        // This test would ideally mock the KeepAliveTimer and check if it's invoked with the specified end time.
        String[] args = {"--keep-alive"};
        Main.main(args);
    }*/

    @Test
    public void testMainWithValidEndTime() throws Exception {
        String[] args = {"--keep-alive"};
        Main.main(args);
    }

    @Test
    public void testMainWithInvalidEndTime() {
        String[] args = {"--keep-alive", "--end-time", "1830"};
        Assertions.assertThrows(UnrecognizedOptionException.class, () -> Main.main(args));
    }

    /*@Test
    public void testMainWithKeepAliveAndSecondsOption() throws Exception {
        // This test would ideally mock the KeepAliveTimer and check if it's invoked with the specified seconds.
        String[] args = {"--keep-alive", "--seconds", "45"};
        Main.main(args);
    }*/
}
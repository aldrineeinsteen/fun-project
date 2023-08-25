package com.aldrineeinsteen.fun;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Test
    public void testMainWithInvalidArguments() throws Exception {
        String[] args = {"invalid", "arguments"};
        exit.expectSystemExitWithStatus(500);
        Main.main(args);
    }
}
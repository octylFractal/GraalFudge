package net.octyl.graalfudge.language.wutlang;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.octyl.graalfudge.language.wutlang.WutlangTesting.eval;

public class WutlangIntegrationTest {
    @Test
    void exampleNetProgram() throws IOException {
        var code = """
            ############################
            # Wutlang Example Program. #
            #     For Version 1.0      #
            ############################

            # Write out the port to listen on.
            +++++++++++++++++++++++++++++++++++++++++++++++++++> #3
            ++++++++++++++++++++++++++++++++++++++++++++++++> #0
            ++++++++++++++++++++++++++++++++++++++++++++++++> #0
            ++++++++++++++++++++++++++++++++++++++++++++++++> #0
            > # We want a blank bit.
            +++> # Length of 3.
            +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ # The 'a' character.

            <<<<<< # Go back to start.

            $@! # Start a webserver, set streams.

            [->.<] # Write 'aaa' to output.

            :%~ # Dump heap, and close server.
            """;
        eval("exampleNetProgram.wut", code);
    }
}

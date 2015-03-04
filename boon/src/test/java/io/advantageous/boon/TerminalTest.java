package io.advantageous.boon;

import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Terminal.Escape.*;

public class TerminalTest {

    @Test
    public void printColors() {
        puts(BG_GREEN, FG_MAGENTA, "This", INVERSE_ON, "is", INVERSE_OFF, "how", RESET, UNDERLINE_ON, "you",
                UNDERLINE_OFF, ITALICS_ON, BOLD_ON, FG_CYAN, BG_YELLOW, "use", BG_WHITE, FG_DEFAULT,
                BOLD_OFF, ITALICS_OFF, FG_RED, "escapes.", RESET);
    }
}

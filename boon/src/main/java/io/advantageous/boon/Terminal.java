package io.advantageous.boon;

/**
 * Created by gcc on 1/15/15.
 */
public class Terminal {

    public enum Escape {

        RESET("\u001B[0m"),
        BOLD_ON("\u001B[1m"),
        ITALICS_ON("\u001B[3m"),
        UNDERLINE_ON("\u001B[4m"),
        INVERSE_ON("\u001B[7m"),
        STRIKETHROUGH_ON("\u001B[9m"),

        BOLD_OFF("\u001B[22m"),
        ITALICS_OFF("\u001B[23m"),
        UNDERLINE_OFF("\u001B[24m"),
        INVERSE_OFF("\u001B[27m"),
        STRIKETHROUGH_OFF("\u001B[29m"),

        FG_BLACK("\u001B[30m"),
        FG_RED("\u001B[31m"),
        FG_GREEN("\u001B[32m"),
        FG_YELLOW("\u001B[33m"),
        FG_BLUE("\u001B[34m"),
        FG_MAGENTA("\u001B[35m"),
        FG_CYAN("\u001B[36m"),
        FG_WHITE("\u001B[37m"),
        FG_DEFAULT("\u001B[39m"),

        BG_BLACK("\u001B[40m"),
        BG_RED("\u001B[41m"),
        BG_GREEN("\u001B[42m"),
        BG_YELLOW("\u001B[43m"),
        BG_BLUE("\u001B[44m"),
        BG_MAGENTA("\u001B[45m"),
        BG_CYAN("\u001B[46m"),
        BG_WHITE("\u001B[47m"),
        BG_DEFAULT("\u001B[49m");

        private final String value;

        Escape(String s) {
            this.value = s;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}

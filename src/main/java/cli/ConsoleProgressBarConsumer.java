package cli;

import me.tongfei.progressbar.ProgressBarConsumer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;

public class ConsoleProgressBarConsumer implements ProgressBarConsumer {

    private static final int consoleRightMargin = 2;
    private final PrintStream out;
    private final Terminal terminal = getTerminal();

    public ConsoleProgressBarConsumer(PrintStream out) {
        this.out = out;
    }

    @Override
    public int getMaxProgressLength() {
        return getTerminalWidth(terminal) - consoleRightMargin;
    }

    @Override
    public void accept(String str) {
        out.print('\r'); // before update
        out.print(str);
    }

    @Override
    public void close() {
        out.println();
        out.flush();
        try {
            terminal.close();
        }
        catch (IOException ignored) { /* noop */ }
    }

    private static Terminal getTerminal() {
        Terminal terminal = null;
        try {
            // Issue #42
            // Defaulting to a dumb terminal when a supported terminal can not be correctly created
            // see https://github.com/jline/jline3/issues/291
            terminal = TerminalBuilder.builder().dumb(true).build();
        }
        catch (IOException ignored) { }
        return terminal;
    }
    private static int getTerminalWidth(Terminal terminal) {
        if (terminal != null && terminal.getWidth() >= 10) // Workaround for issue #23 under IntelliJ
            return terminal.getWidth();
        else return 80; // Default width
    }
}
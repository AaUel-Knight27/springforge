package dev.springforge;

import dev.springforge.cli.ForgeCommand;
import picocli.CommandLine;

public class ForgeMain {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ForgeCommand())
                .setExecutionExceptionHandler((ex, cmd, parseResult) -> {
                    cmd.getErr().println(cmd.getColorScheme().errorText("Error: " + ex.getMessage()));
                    return 1;
                })
                .execute(args);
        System.exit(exitCode);
    }
}

package dev.springforge.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "forge",
    mixinStandardHelpOptions = true,
    version = "SpringForge 1.0.0",
    description = "⚡ CLI accelerator for Spring Boot + Next.js full-stack microservices",
    subcommands = {
        InitCommand.class,
        AddCommand.class,
        SetupDbCommand.class,
        SyncCommand.class,
        RunCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class ForgeCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}

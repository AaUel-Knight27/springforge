package dev.springforge.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "add",
    description = "Add components to your project",
    subcommands = {
        AddServiceCommand.class,
        AddEntityCommand.class,
        AddAuthCommand.class,
        AddRelationCommand.class,
        AddAdminCommand.class,
        AddPipelineCommand.class,
        AddCacheCommand.class,
        AddBrokerCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class AddCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}

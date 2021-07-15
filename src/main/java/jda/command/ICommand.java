package jda.command;

import java.io.IOException;
import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx) throws IOException;

    String getName();

    String getHelp();

    default List<String> getAliases() {
        return List.of();
    }

}

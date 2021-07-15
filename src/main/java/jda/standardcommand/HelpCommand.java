package jda.standardcommand;

import jda.CommandManager;
import jda.TempDesign;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {

        List<String> args = ctx.getArgs();
        String prefix = TempDesign.PREFIXES
                .get(ctx.getGuild().getIdLong());
        TextChannel channel = ctx.getChannel();
        if (args.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            manager.getCommands().stream().map(ICommand::getName).forEach((it) -> builder.append(prefix)
                    .append(it)
                    .append("    "));
            EmbedBuilder info = EmbedUtils.embedMessageWithTitle("List of Commands", "`" + builder + "`");
            channel.sendMessageEmbeds(info.build()).queue();
            return;
        }
        String search = args.get(0);
        ICommand command = manager.getCommand(search);
        if (command == null) {
            channel.sendMessage("No usable command found for your search: " + search).queue();
            return;
        }
        String newprefixcmd = command.getHelp().replace("?", TempDesign.PREFIXES.get(ctx.getGuild().getIdLong()));
        EmbedBuilder info = EmbedUtils.embedMessageWithTitle(command.getName().toUpperCase(), newprefixcmd);
        channel.sendMessageEmbeds(info.build()).queue();

    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Shows the list of commands in the bot\n" + "Usage: `?help [command]`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("commands", "cmds", "commandlist");
    }
}

package jda.standardcommand;

import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.List;

public class WolframCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        if (args.isEmpty()) {
            channel.sendMessage("Please ask a question").queue();
            return;
        }
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).replace("+", "plus"));
        }
        String searchResult = String.join("+", args);
        String finalURL = "http://api.wolframalpha.com/v2/simple?appid=YRJUA8-YKAJKT52GG&i=" + searchResult + "%3F";
        EmbedBuilder info = EmbedUtils.embedImageWithTitle(String.join(" ", args), finalURL, finalURL);
        info.setFooter("Inutile || Image Won't Load? Click on the link!");
        channel.sendMessageEmbeds(info.build()).queue();
    }

    @Override
    public String getName() {
        return "wolfram";
    }

    @Override
    public String getHelp() {
        return "Ask Wolphram Alpha A Question! \n Usage: `?wolfram <query>`";
    }
}
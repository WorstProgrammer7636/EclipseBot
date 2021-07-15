package jda.standardcommand;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;

public class LanguageList implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final TextChannel channel = ctx.getChannel();
        EmbedBuilder info = new EmbedBuilder();
        info.setTitle("Language abbreviations");
        info.setFooter("These are only common languages. If you want to look up more languages, go to (https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes#BR)");
        info.setDescription("en - ENGLISH\n es - SPANISH\n fr - FRENCH\n zh - CHINESE\n ko - KOREAN\n "
                + "el - GREEK\n ja - JAPANESE\n ru - RUSSIAN");

        channel.sendMessage(info.build()).queue();
    }

    @Override
    public String getName() {
        return "languagelist";
    }

    @Override
    public String getHelp() {
        return "Language abbreviations";
    }
}

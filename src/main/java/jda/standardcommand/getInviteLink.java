package jda.standardcommand;

import com.darkprograms.speech.translator.GoogleTranslate;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class getInviteLink implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x03a5fc);
        builder.setAuthor("CLICK THIS TO INVITE ME TO YOUR OWN SERVER!", "https://discord.com/api/oauth2/authorize?client_id=765713285965807657&permissions=8&scope=bot");
        ctx.getChannel().sendMessage(builder.build()).queue();
        builder.clear();
    }

    @Override
    public String getName() {
        return "invitelink";
    }

    @Override
    public String getHelp() {
        return "get an invite link for this bot";
    }
}

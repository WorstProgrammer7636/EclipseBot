package jda.standardcommand.admin;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class FlushServerEmotes implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException, GeneralSecurityException {
        if (ctx.getMember().hasPermission(Permission.ADMINISTRATOR)){
            List<Emote> guildEmotes = ctx.getGuild().getEmotes();
            for (int i = 0; i < guildEmotes.size(); i++){
                ctx.getGuild().getEmoteById(guildEmotes.get(i).getIdLong()).delete().queue();
            }
            ctx.getChannel().sendMessage("Completed task").queue();
        } else {
            ctx.getChannel().sendMessage("Only administrators have this command").queue();
        }
    }

    @Override
    public String getName() {
        return "serveremoteflush";
    }

    @Override
    public String getHelp() {
        return null;
    }
}

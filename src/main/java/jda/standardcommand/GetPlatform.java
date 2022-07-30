package jda.standardcommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.web.ContentType;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GetPlatform implements ICommand {



    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();

        List<Member> mentionedMembers = ctx.getMessage().getMentionedMembers();
        if (mentionedMembers.size() == 0){
            channel.sendMessage("You didn't mention anyone!").queue();
            return;
        } else if (mentionedMembers.size() > 1){
            channel.sendMessage("Only one member at a time!").queue();
            return;
        }

        String onlineStatus = mentionedMembers.get(0).getOnlineStatus().toString();
        String platforms = mentionedMembers.get(0).getActiveClients().toString();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Presence settings of " + mentionedMembers.get(0).getEffectiveName());
        String platformSymbol = " ";
        if (platforms.contains("DESKTOP")){
            platformSymbol += ":desktop:";
        }

        if (platforms.contains("MOBILE")){
            platformSymbol += ":mobile_phone:";
        }

        if (platforms.contains("WEB")){
            platformSymbol += ":earth_americas:";
        }

        /**
         * ACTIVE CLIENTS STILL A LITTLE BUGGY, FIX LATER !!
         */

        builder.setDescription("ONLINE STATUS: " + onlineStatus + "\n"
        + "ACTIVE CLIENTS: " + platforms + platformSymbol);
        builder.setColor(Color.YELLOW);
        channel.sendMessage(builder.build()).queue();
        builder.clear();
        return;
    }

    @Override
    public String getName() {
        return "getplatform";
    }

    @Override
    public String getHelp() {
        return "get platform of user";
    }
}

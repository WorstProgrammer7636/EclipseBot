package jda.standardcommand;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class KickCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        final Member member = ctx.getMember();
        final List<String> args = ctx.getArgs();

        if (args.size() < 2 || message.getMentionedMembers().isEmpty()) {
            channel.sendMessage("Missing arguments! Use ?kick <@User> [Reason]").queue();
            return;
        }

        final Member target = message.getMentionedMembers().get(0);

        if (!member.canInteract(target) || !member.hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage("You are missing permission to kick this member.").queue();
            return;
        }

        final Member selfMember = ctx.getSelfMember();

        if (!selfMember.canInteract(target) || !selfMember.hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage("I am missing permissions to kick that member.").queue();
            return;
        }

        String reason = String.join(" ", args.subList(1, args.size()));

        ctx.getGuild()
                .kick(target, reason  + " || Moderator: " + member.getUser().getAsTag())
                .reason(reason)
                .queue(
                        (__) -> channel
                                .sendMessage("Kick was successful!" + "\n Kicked by: " + member.getUser().getAsTag() +
                                        " \n Kicked: "
                                        + target.getUser().getAsTag() + "\n For Reason: " + reason).queue(),
                        (error) -> channel.sendMessageFormat("Could not kick %s", error.getMessage()).queue()
                );
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getHelp() {
        return "Kick a member off the server.\n" +
                "Usage: `?kick <@User> [reason]`";
    }
}
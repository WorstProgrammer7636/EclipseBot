package jda.standardcommand;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import jda.Config;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class WebhookCommand implements ICommand {

    private final WebhookClient client;

    public WebhookCommand() {
        WebhookClientBuilder builder = new WebhookClientBuilder(Config.get("webhookurl"));
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Webhook-Thread");
            thread.setDaemon(true);
            return thread;
        });
        this.client = builder.build();
    }

    @Override
    public void handle(CommandContext ctx) {
        long sendGuild = Long.parseLong("778491869226467361");

        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        if (args.isEmpty()) {
            channel.sendMessage("Missing Text! Try again with a message").queue();
            return;
        }

        final User user = ctx.getAuthor();
        if (args.get(0).equals("bads")) {
            JDA bot = ctx.getJDA();
            Member bads = Objects.requireNonNull(bot.getGuildById(sendGuild)).getMemberByTag("Bads", "6156");
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.size(); i++) {
                message.append(args.get(i));
                message.append(" ");
            }
            WebhookMessageBuilder builder = new WebhookMessageBuilder()
                    .setUsername(bads.getEffectiveName())
                    .setAvatarUrl(bads.getUser().getEffectiveAvatarUrl().replaceFirst("gif", "png") + "?size=512")
                    .setContent(String.valueOf(message));

            client.send(builder.build());
            ctx.getMessage().delete().queue();
            return;
        }
        WebhookMessageBuilder builder = new WebhookMessageBuilder()
                .setUsername(ctx.getMember().getEffectiveName())
                .setAvatarUrl(user.getEffectiveAvatarUrl().replaceFirst("gif", "png") + "?size=512")
                .setContent(String.join(" ", args));

        client.send(builder.build());
        ctx.getMessage().delete().queue();
    }

    @Override
    public String getName() {
        return "webhook";
    }

    @Override
    public String getHelp() {
        return "Send a webhook message as your name" + "\nUsage: `?webhook [message]`";
    }
}

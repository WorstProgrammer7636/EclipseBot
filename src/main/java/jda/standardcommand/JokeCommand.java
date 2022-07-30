package jda.standardcommand;

import com.fasterxml.jackson.databind.JsonNode;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;

public class JokeCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        WebUtils.ins.getJSONObject("https://apis.duncte123.me/joke").async((json) -> {
            if (!json.get("success").asBoolean()) {
                channel.sendMessage("Something went wrong, try again later! If you continue to get this message ping 0Nard so he can reset the bot.").queue();
                System.out.println(json);
                return;
            }

            final JsonNode data = json.get("data");
            final String title = data.get("title").asText();
            final String url = data.get("url").asText();
            final String body = data.get("body").asText();
            final EmbedBuilder embed = EmbedUtils.defaultEmbed().setTimestamp(Instant.now()).setTitle(title, url).setDescription(body);

            channel.sendMessage(embed.build()).queue();

        });
    }

    @Override
    public String getName() {
        return "joke";
    }

    @Override
    public String getHelp() {
        return "Displays a random joke from the dank memers. \n Usage: `?joke`";
    }
}

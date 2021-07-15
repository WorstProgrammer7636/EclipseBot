package jda.standardcommand;

import com.fasterxml.jackson.databind.JsonNode;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;

public class MemeCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        WebUtils.ins.getJSONObject("https://apis.duncte123.me/meme").async((json) -> {
            if (!json.get("success").asBoolean()) {
                channel.sendMessage("Something went wrong, try again later! If you continue to get this message ping 0Nard so he can reset the bot.").queue();
                System.out.println(json);
                return;
            }

            final JsonNode data = json.get("data");
            final String title = data.get("title").asText();
            final String url = data.get("url").asText();
            final String image = data.get("image").asText();
            final EmbedBuilder embed = EmbedUtils.embedImageWithTitle(title, url, image).setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();

        });
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public String getHelp() {
        return "Displays a random meme" + "\n Usage: `?meme`";
    }
}

package jda.standardcommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.web.ContentType;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.function.Consumer;

public class HasteCommand implements ICommand {

    private static final String HASTE_SERVER = "https://hastebin.com/";

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getArgs().isEmpty()) {
            channel.sendMessage("Missing Text to paste!").queue();
            return;
        }
        final String invoke = this.getName();
        final String contentRaw = ctx.getMessage().getContentRaw();
        final int index = contentRaw.indexOf(invoke) + invoke.length();
        final String body = contentRaw.substring(index).trim();
        this.createPaste(body, (text) -> channel.sendMessage("**New haste bin created by: " + ctx.getAuthor().getAsTag() + "**\n" + text).queue());
        ctx.getMessage().delete().queue();
    }

    private void createPaste(String text, Consumer<String> callback) {

        Request request = WebUtils.defaultRequest()
                .post(RequestBody.create(null, text.getBytes()))
                .addHeader("Content-Type", ContentType.TEXT_HTML.getType())
                .url(HASTE_SERVER + "documents")
                .build();
        WebUtils.ins.prepareRaw(request, (r) -> WebParserUtils.toJSONObject(r, new ObjectMapper())).async(
                (json) -> {
                    String key = json.get("key").asText();

                    callback.accept(HASTE_SERVER + key);

                },
                (e) -> callback.accept("Error: " + e.getMessage())

        );
    }

    @Override
    public String getName() {
        return "haste";
    }

    @Override
    public String getHelp() {
        return "Posts some text to hastebin\n" + "Usage: `?haste [text]`";
    }
}

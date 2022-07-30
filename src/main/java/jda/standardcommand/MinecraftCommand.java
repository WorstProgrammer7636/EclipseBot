package jda.standardcommand;

import com.github.natanbc.reliqua.util.StatusCodeValidator;
import jda.command.CommandContext;
import jda.command.ICommand;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MinecraftCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();

        if (args.size() < 2) {
            channel.sendMessage("How to use this command: " + getHelp()).queue();
            return;
        }
        final String item = args.get(0);
        final String id = args.get(1);
        if (item.equals("uuid")) {
            fetchName(id, (newname) -> {
                if (newname == null) {
                    channel.sendMessage("User with name " + id + " does not exist.").queue();
                    return;
                }
                fetchUUID(id, (uuid) -> {
                    if (uuid == null) {
                        channel.sendMessage("User with name " + id + " does not exist.").queue();
                        return;
                    }
                    channel.sendMessage(newname + "'s UUID is " + uuid).queue();
                });
            });

        } else if (item.equals("names")) {

            fetchNameHistory(id, (names) -> {
                if (names == null) {
                    fetchUUID(id, (uuid) -> {
                        if (uuid == null) {
                            channel.sendMessage("User with name " + id + " does not exist.").queue();
                            return;
                        }
                        fetchNameHistory(uuid, (names2) -> {
                            if (names2 == null) {
                                return;
                            }
                            final String namesJoined = String.join("\n", names2);
                            channel.sendMessageFormat("Name history for %s:\n%s", uuid, namesJoined).queue();
                        });
                    });
                    return;
                }
                final String namesJoined = String.join("\n", names);
                channel.sendMessageFormat("Name history for %s:\n%s", id, namesJoined).queue();
                channel.sendMessage("UUID with ID " + id + " does not exist.").queue();
            });


        } else {
            channel.sendMessageFormat("%s is not a valid target, please choose either [uuid] or [names] ", item).queue();
        }
    }

    @Override
    public String getName() {
        return "minecraft";
    }

    @Override
    public String getHelp() {
        return "Get the UUID or name history of a minecraft player\n Usage: `?minecraft [uuid/names] <username>`";
    }

    private void fetchUUID(String username, Consumer<String> callback) {
        WebUtils.ins.getJSONObject(
                "https://api.mojang.com/users/profiles/minecraft/" + username,
                (builder) -> builder.setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
        ).async(
                (json) -> {
                    callback.accept(json.get("id").asText());
                },
                (error) -> {
                    callback.accept(null);
                }
        );
    }

    private void fetchName(String username, Consumer<String> callback) {
        WebUtils.ins.getJSONObject(
                "https://api.mojang.com/users/profiles/minecraft/" + username,
                (builder) -> builder.setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
        ).async(
                (json) -> {
                    callback.accept(json.get("name").asText());
                },
                (error) -> {
                    callback.accept(null);
                }
        );
    }

    private void fetchNameHistory(String uuid, Consumer<List<String>> callback) {
        WebUtils.ins.getJSONArray(
                "https://api.mojang.com/user/profiles/" + uuid + "/names",
                (builder) -> builder.setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
        ).async(
                (json) -> {

                    List<String> names = new ArrayList<>();
                    json.forEach((item) -> names.add(item.get("name").asText()));
                    callback.accept(names);
                },
                (error) -> {
                    callback.accept(null);
                }
        );
    }
}

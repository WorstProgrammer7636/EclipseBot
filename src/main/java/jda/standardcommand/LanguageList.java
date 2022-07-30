package jda.standardcommand;

import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageList implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws IOException {
        List<String> languages = new ArrayList<>(Arrays.asList("en", "es", "fr", "zh", "ko", "el", "ja", "ru", "ar", "aa", "ab", "ae", "af", "ak", "am", "an",
                "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv",
                "cy", "da", "de", "dv", "dz", "ee", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fy", "ga", "gd", "gl", "gn", "gu", "gv",
                "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv",
                "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv",
                "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny",
                "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si",
                "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty",
                "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi", "yo", "za", "zu"));
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

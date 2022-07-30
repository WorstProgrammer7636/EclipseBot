package jda.standardcommand;

import com.darkprograms.speech.translator.GoogleTranslate;
import jda.command.CommandContext;
import jda.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Translate implements ICommand {
    String languageToTranslate;
    public void checkCommonLanguageInput(){
        if (languageToTranslate.equalsIgnoreCase("english")){
            languageToTranslate = "en";
        } else if (languageToTranslate.equalsIgnoreCase("spanish")){
            languageToTranslate = "es";
        } else if (languageToTranslate.equalsIgnoreCase("french")){
            languageToTranslate = "fr";
        } else if (languageToTranslate.equalsIgnoreCase("chinese")){
            languageToTranslate = "zh";
        } else if (languageToTranslate.equalsIgnoreCase("korean")){
            languageToTranslate = "ko";
        } else if (languageToTranslate.equalsIgnoreCase("japanese")){
            languageToTranslate = "ja";
        } else if (languageToTranslate.equalsIgnoreCase("russian")){
            languageToTranslate = "ru";
        } else if (languageToTranslate.equalsIgnoreCase("greek")){
            languageToTranslate = "el";
        } else if (languageToTranslate.equalsIgnoreCase("arabic")){
            languageToTranslate = "ar";
        } else if (languageToTranslate.equalsIgnoreCase("albanian")){
            languageToTranslate = "sq";
        }
    }

    @Override
    public void handle(CommandContext ctx) throws IOException {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        String sentence = "";

        if (args.isEmpty() || args.size() == 1) {
            channel.sendMessage("Please translate a word or statement. Example: ?translate spanish please walk the dog.").queue();
            return;
        } else {
            for (int i = 1; i < args.size(); i++) {
                sentence += args.get(i);
                sentence += " ";
            }
        }

        List<String> languages = new ArrayList<>(Arrays.asList("en", "es", "fr", "zh", "ko", "el", "ja", "ru", "ar", "aa", "ab", "ae", "af", "ak", "am", "an",
                "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv",
                "cy", "da", "de", "dv", "dz", "ee", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fy", "ga", "gd", "gl", "gn", "gu", "gv",
                "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv",
                "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv",
                "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny",
                "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si",
                "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty",
                "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi", "yo", "za", "zu"));

        languageToTranslate = args.get(0);
        checkCommonLanguageInput();

        if (!languages.contains(languageToTranslate)) {
            channel.sendMessage("That language is not in our database!").queue();
            return;
        }

        String inputLanguage = GoogleTranslate.detectLanguage(sentence);
        EmbedBuilder builder = new EmbedBuilder();

        try {
            builder.setTitle("Translation:");
            builder.addField("Original Statement:", sentence, false);

            //convert iso language to readable language
            String readable1 = addLanguageList(inputLanguage);
            String readable2 = addLanguageList(languageToTranslate);

            builder.addField("Information:", "Expecting to translate from: " +
                    readable1 + " to " + readable2, false);
            builder.setDescription(GoogleTranslate.translate(languageToTranslate, sentence));
            builder.setColor(0x03a5fc);
            builder.setAuthor(ctx.getMember().getEffectiveName());
        } catch (IOException e) {
            channel.sendMessage("That language is unfortunately no longer supported").queue();
            return;
        }


        channel.sendMessage(builder.build()).queue();


    }

    public String addLanguageList(String x){
        HashMap<String, String> ISOReadables = new HashMap<String, String>();
        ISOReadables.put("en", "English");
        ISOReadables.put("es", "Spanish");
        ISOReadables.put("zh", "Chinese");
        ISOReadables.put("ru", "Russian");
        ISOReadables.put("ja", "Japanese");
        ISOReadables.put("fr", "French");
        ISOReadables.put("ko", "Korean");
        ISOReadables.put("el", "Greek");
        ISOReadables.put("ar", "Arabic");
        ISOReadables.put("sq", "Albanian");

        try {
            return ISOReadables.get(x);
        } catch (Exception e){
            return x;
        }

    }


    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public String getHelp() {
        return "Translate a word or statement into any language you want! Usage: ?translate [language to translate to] [input]";
    }
}

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
    HashMap<String, String> languageCodeList = new HashMap<String, String>();
    public void checkCommonLanguageInput(){
        languageToTranslate = languageToTranslate.toLowerCase();
        languageCodeList.put("english", "en");
        languageCodeList.put("spanish", "es");
        languageCodeList.put("french", "fr");
        languageCodeList.put("chinese", "zh");
        languageCodeList.put("korean", "ko");
        languageCodeList.put("japanese", "ja");
        languageCodeList.put("russian", "ru");
        languageCodeList.put("greek", "el");
        languageCodeList.put("arabic", "ar");
        languageCodeList.put("albanian", "sq");
        languageCodeList.put("armenian", "hy");
        languageCodeList.put("german", "de");
        languageCodeList.put("welsh", "cy");
        languageCodeList.put("vietnamese", "vi");
        languageCodeList.put("uzbek", "uz");
        languageCodeList.put("ukrainian", "uk");
        languageCodeList.put("turkmen", "tk");
        languageCodeList.put("turkish", "tr");
        languageCodeList.put("swedish", "sv");
        languageCodeList.put("swahili", "sw");
        languageCodeList.put("sundanese", "su");
        languageCodeList.put("somali", "so");
        languageCodeList.put("slovenian", "sl");
        languageCodeList.put("slovak", "sk");
        languageCodeList.put("serbian", "sr");
        languageCodeList.put("samoan", "sm");
        languageCodeList.put("romanian", "ro");
        languageCodeList.put("moldavian", "ro");
        languageCodeList.put("moldovan", "ro");
        languageCodeList.put("portuguese", "pt");
        languageCodeList.put("polish", "pl");
        languageCodeList.put("persian", "fa");
        languageCodeList.put("norwegian", "no");
        languageCodeList.put("navajo", "nv");
        languageCodeList.put("malay", "ms");
        languageCodeList.put("macedonian", "mk");
        languageCodeList.put("luxembourgish", "lb");
        languageCodeList.put("latin", "la");
        languageCodeList.put("irish", "ga");
        languageCodeList.put("italian", "it");
        languageCodeList.put("javanese", "jv");
        languageCodeList.put("indonesian", "id");
        languageCodeList.put("hungarian", "hu");
        languageCodeList.put("icelandic", "is");
        languageCodeList.put("hebrew", "he");
        languageCodeList.put("dutch", "nl");
        languageCodeList.put("flemish", "nl");
        languageCodeList.put("estonian", "et");
        languageCodeList.put("finnish", "fi");
        languageCodeList.put("danish", "da");
        languageCodeList.put("fijian", "fj");
        languageCodeList.put("czech", "cs");
        languageCodeList.put("chechen", "ce");
        languageCodeList.put("croatian", "hr");
        languageCodeList.put("cornish", "kw");
        languageCodeList.put("bulgarian", "bg");
        languageCodeList.put("burmese", "my");
        languageCodeList.put("georgian", "ka");
        try {
            languageToTranslate = languageCodeList.get(languageToTranslate);
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public String addLanguageList(String x){
        HashMap<String, String> ISOReadables = new HashMap<String, String>();
        for (HashMap.Entry<String, String> entry : languageCodeList.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            ISOReadables.put(value, key);
        }

        try {
            return ISOReadables.get(x);
        } catch (Exception e){
            return x;
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
        System.out.println(languageToTranslate);
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
            if (readable1 == null){
                readable1 = "Unknown language";
            }

            if (readable2 == null){
                readable2 = "Unknown language";
            }

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


    @Override
    public String getName() {
        return "translate";
    }

    @Override
    public String getHelp() {
        return "Translate a word or statement into any language you want! Usage: ?translate [language to translate to] [input].\n" +
                " Example: ?translate spanish please walk my dog.";
    }
}

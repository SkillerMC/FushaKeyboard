package com.devempire.fushakeyboard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline, dictionary-based fallback used when no API key is configured
 * or the AI request fails. Simple word/phrase substitution — not real
 * grammar correction, just keeps the button useful without internet.
 */
final class LocalFusha {

    private static final LinkedHashMap<String, String> DICTIONARY = new LinkedHashMap<>();

    static {
        String[][] pairs = {
                {"شو عم تعمل", "ماذا تفعل"}, {"شو عم تسوي", "ماذا تفعل"}, {"شو تسوي", "ماذا تفعل"},
                {"شو", "ماذا"}, {"ليش", "لماذا"}, {"وين", "أين"}, {"ايمت", "متى"},
                {"كيفك", "كيف حالك"}, {"شلونك", "كيف حالك"}, {"شخبارك", "كيف حالك"},
                {"هلق", "الآن"}, {"هسا", "الآن"}, {"هسه", "الآن"},
                {"بكرا", "غدًا"}, {"امبارح", "أمس"},
                {"هاد", "هذا"}, {"هاي", "هذه"}, {"هذي", "هذه"}, {"هداك", "ذلك"}, {"هديك", "تلك"},
                {"كتير", "كثيرًا"}, {"وايد", "كثيرًا"},
                {"بدي", "أريد"}, {"بدك", "تريد"}, {"بديك", "تريد"},
                {"بقدر", "أستطيع"}, {"مقدر", "لا أستطيع"}, {"ما بقدر", "لا أستطيع"},
                {"ما في", "لا يوجد"}, {"مافي", "لا يوجد"},
                {"لسا", "ما زال"}, {"لسه", "ما زال"},
                {"رح", "سوف"}, {"راح", "سوف"}, {"مو", "ليس"},
                {"خلص", "انتهى"}, {"تمام", "حسنًا"}, {"اي", "نعم"}, {"إي", "نعم"},
                {"لازم", "يجب"}, {"عشان", "لكي"}, {"لأنو", "لأنه"}, {"لانو", "لأنه"},
                {"بس", "لكن"}, {"معي", "لديّ"}, {"عندي", "لديّ"},
                {"مين", "من"}, {"منو", "من هو"},
                {"اعطيني", "أعطني"}, {"عطيني", "أعطني"},
                {"شو اسمه", "ما اسمه"}, {"وينك", "أين أنت"}
        };
        for (String[] pair : pairs) {
            DICTIONARY.put(pair[0], pair[1]);
        }
    }

    static String convert(String input) {
        String result = input;
        for (Map.Entry<String, String> entry : DICTIONARY.entrySet()) {
            String pattern = "(?<!\\S)" + Pattern.quote(entry.getKey()) + "(?!\\S)";
            result = result.replaceAll(pattern, Matcher.quoteReplacement(entry.getValue()));
        }
        return result.replaceAll("\\s+", " ").trim();
    }

    private LocalFusha() {
    }
}

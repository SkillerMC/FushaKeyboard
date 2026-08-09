package com.devempire.fushakeyboard;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Calls the Anthropic Messages API (using the user's own API key, entered
 * in SettingsActivity) to rewrite a sentence into correct Modern Standard
 * Arabic. Runs entirely on a background thread; callback is invoked there
 * too, so the caller must hop back to the main thread itself.
 */
final class AiClient {

    interface Callback {
        void onSuccess(String result);

        void onError(String message);
    }

    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    static void correctToFusha(String apiKey, String text, Callback callback) {
        EXECUTOR.execute(() -> {
            try {
                callback.onSuccess(call(apiKey, text));
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "network error" : e.getMessage());
            }
        });
    }

    private static String call(String apiKey, String text) throws Exception {
        String prompt = "أعد صياغة الجملة التالية بالعربية الفصحى الصحيحة نحويًا وإملائيًا، "
                + "مع الحفاظ على المعنى تمامًا. أجب بالجملة المعدَّلة فقط، بدون أي شرح أو علامات اقتباس:\n\n"
                + text;

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("max_tokens", 300);
        body.put("messages", new JSONArray().put(message));

        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setRequestProperty("anthropic-version", ANTHROPIC_VERSION);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(stream);

        if (code < 200 || code >= 300) {
            throw new RuntimeException("HTTP " + code + ": " + response);
        }

        JSONArray content = new JSONObject(response).getJSONArray("content");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            JSONObject block = content.getJSONObject(i);
            if ("text".equals(block.optString("type"))) {
                result.append(block.optString("text"));
            }
        }
        return result.toString().trim();
    }

    private static String readStream(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toString("UTF-8");
    }

    private AiClient() {
    }
}

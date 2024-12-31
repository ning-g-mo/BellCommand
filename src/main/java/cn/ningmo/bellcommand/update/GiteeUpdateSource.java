package cn.ningmo.bellcommand.update;

import cn.ningmo.bellcommand.BellCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GiteeUpdateSource implements UpdateSource {
    private final BellCommand plugin;
    private final String owner;
    private final String repo;
    private static final String API_URL = "https://gitee.com/api/v5/repos/%s/%s/releases/latest";

    public GiteeUpdateSource(BellCommand plugin, ConfigurationSection config) {
        this.plugin = plugin;
        this.owner = config.getString("owner", "ningmengchongshui");
        this.repo = config.getString("repo", "BellCommand");
    }

    @Override
    public UpdateInfo checkUpdate() throws Exception {
        String apiUrl = String.format(API_URL, owner, repo);
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                if (plugin.isDebugEnabled()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("response", response.toString());
                    plugin.getLogger().info(plugin.getLanguageManager()
                        .getMessage("messages.debug.update.response", placeholders));
                }

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(response.toString());
                String tagName = ((String) json.get("tag_name")).replace("v", "").trim();
                String downloadUrl = (String) json.get("html_url");

                return new UpdateInfo(
                    plugin.getDescription().getVersion(),
                    tagName,
                    downloadUrl
                );
            }
        }
        return null;
    }
} 
package Config.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.json.JSONArray;
import tools.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BossConfig {

    private static final Logger log = LoggerFactory.getLogger(BossConfig.class);
    public static final Map<String, JSONObject> map = new HashMap<>();

    public static void load() {
        map.clear();
        try {
            JSONArray jsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("config", "bossconfig.json")), StandardCharsets.UTF_8));
            for (Object o : jsonArray) {
                assert o instanceof JSONObject;
                JSONObject jsonObject = (JSONObject) o;
                map.put(jsonObject.getString("entrance"), jsonObject);
            }
        } catch (Exception e) {
            System.out.println("未讀取bossconfig.json配置, boss訊息將使用預設值");
        }
    }

    public static JSONObject getEntrance(String name) {
        return map.get(name);
    }
}

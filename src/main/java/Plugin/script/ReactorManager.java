package Plugin.script;

import Database.DatabaseLoader;
import Database.mapper.ReactorDropEntryMapper;
import Database.tools.SqlTool;
import Net.server.maps.ReactorDropEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 反應堆爆率數據實例
 *
 */

@Slf4j
public class ReactorManager {
    @Getter
    private static final ReactorManager instance = new ReactorManager();

    private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap<>();

    public List<ReactorDropEntry> getDrops(int reactorId) {
        return drops.get(reactorId);
    }

    public void clearDrops() {
        drops.clear();
        loadDrops();
    }

    public void loadDrops() {
        DatabaseLoader.DatabaseConnection.domain(con -> {
            List<Integer> droppers = SqlTool.queryAndGetList(con, "SELECT DISTINCT `dropperid` FROM `zdata_reactordrops`", rs -> rs.getInt("dropperid"));
            for (int dropperid : droppers) {
                List<ReactorDropEntry> dropEntries = SqlTool.queryAndGetList(con, "SELECT * FROM `zdata_reactordrops` WHERE `dropperid` = ?", new ReactorDropEntryMapper(), dropperid);
                drops.put(dropperid, dropEntries);
            }
            return null;
        }, "讀取反應堆爆率數據出錯");
    }

}

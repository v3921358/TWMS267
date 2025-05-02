package Server.login;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProvider;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginInformationProvider {

    private static LoginInformationProvider instance;
    protected final List<String> ForbiddenName = new ArrayList<>(); //禁止取名
    protected final List<String> Curse = new ArrayList<>(); //聊天禁止出現的字符
    //性別, val, 職業類型
    protected final Map<Triple<Integer, Integer, Integer>, List<Integer>> makeCharInfo = new HashMap<>();
    /*
     * 0 = 臉型
     * 1 = 髮型
     * 2 = 上衣
     * 3 = 褲/裙
     * 4 = 鞋子
     * 5 = 武器
     * 6 = 盾牌
     */

    protected LoginInformationProvider() {
        MapleDataProvider prov = MapleDataProviderFactory.getEtc();
        MapleData nameData = prov.getData("ForbiddenName.img"); //禁止玩家取的名字
        for (MapleData data : nameData.getChildren()) {
            ForbiddenName.add(MapleDataTool.getString(data));
        }
        nameData = prov.getData("Curse.img"); //聊天禁止出現的字符
        for (MapleData data : nameData.getChildren()) {
            Curse.add(MapleDataTool.getString(data).split(",")[0]);
            ForbiddenName.add(MapleDataTool.getString(data).split(",")[0]);
        }
        MapleData infoData = prov.getData("MakeCharInfo.img"); //新建角色WZ中默認的裝備
        //System.out.println("infoData - " + infoData.getName());
        for (MapleData dat : infoData) {
            if (!dat.getName().matches("^\\d+$") && !dat.getName().equals("000_1") && !dat.getName().equals("000_3")) {
                continue;
            }
            int type;
            if (dat.getName().equals("000_1")) {
                type = JobType.冒險家.type;
            } else if (dat.getName().equals("000_3")) {
                type = JobType.開拓者.type;
            } else {
                type = Integer.parseInt(dat.getName());
            }
            //System.out.println("dat - " + dat.getName());
            for (MapleData d : dat) {
                int gender;
                if (d.getName().startsWith("male")) {
                    gender = 0;
                } else if (d.getName().startsWith("female")) {
                    gender = 1;
                } else {
                    continue;
                }
                for (MapleData da : d) {
                    //System.out.println("da - " + da.getName());
                    Triple<Integer, Integer, Integer> key = new Triple<>(gender, Integer.parseInt(da.getName()), type);
                    List<Integer> our = makeCharInfo.computeIfAbsent(key, k -> new ArrayList<>());
                    for (MapleData dd : da) {
                        if (dd.getName().equalsIgnoreCase("color")) {
                            for (MapleData dda : dd.getChildren()) {
                                for (MapleData ddd : dda.getChildren()) {
                                    our.add(MapleDataTool.getInt(ddd, -1));
                                }
                            }
                        } else if (!dd.getName().equals("name")) {
                            our.add(MapleDataTool.getInt(dd, -1));
                            //System.out.println("dd - " + dd.getName() + " - " + MapleDataTool.getInt(dd, -1) + " our - " + our);
                        }
                    }
                }
            }
        }
    }

    public static LoginInformationProvider getInstance() {
        if (instance == null) {
            instance = new LoginInformationProvider();
        }
        return instance;
    }

    /*
     * 是否是禁止取的名字
     */
    public boolean isForbiddenName(String in) {
        for (String name : ForbiddenName) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /*
     * 是否是禁止聊天出現的字符
     */
    public boolean isCurseMsg(String in) {
        for (String name : Curse) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEligibleItem(int gender, int val, int job, int item) {
        if (item < 0) {
            return false;
        }
        Triple<Integer, Integer, Integer> key = new Triple<>(gender, val, job);
        List<Integer> our = makeCharInfo.get(key);
        return our != null && our.contains(item);
    }
}

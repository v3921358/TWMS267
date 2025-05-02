package Client.skills.handler;

import Client.skills.handler.其他.*;
import Client.skills.handler.冒險家.*;
import Client.skills.handler.冒險家.劍士類別.聖騎士;
import Client.skills.handler.冒險家.劍士類別.英雄;
import Client.skills.handler.冒險家.劍士類別.黑騎士;
import Client.skills.handler.冒險家.弓手類別.神射手;
import Client.skills.handler.冒險家.弓手類別.箭神;
import Client.skills.handler.冒險家.弓手類別.開拓者;
import Client.skills.handler.冒險家.法師類別.主教;
import Client.skills.handler.冒險家.法師類別.冰雷大魔導士;
import Client.skills.handler.冒險家.法師類別.火毒大魔導士;
import Client.skills.handler.冒險家.海盜類別.拳霸;
import Client.skills.handler.冒險家.海盜類別.槍神;
import Client.skills.handler.冒險家.海盜類別.重砲指揮官;
import Client.skills.handler.冒險家.盜賊類別.夜使者;
import Client.skills.handler.冒險家.盜賊類別.影武者;
import Client.skills.handler.冒險家.盜賊類別.暗影神偷;
import Client.skills.handler.怪物.皮卡啾;
import Client.skills.handler.怪物.雪吉拉;
import Client.skills.handler.曉之陣.劍豪;
import Client.skills.handler.曉之陣.曉之陣;
import Client.skills.handler.曉之陣.陰陽師;
import Client.skills.handler.末日反抗軍.*;
import Client.skills.handler.江湖.墨玄;
import Client.skills.handler.江湖.江湖;
import Client.skills.handler.江湖.琳恩;
import Client.skills.handler.異界.凱內西斯;
import Client.skills.handler.異界.異界;
import Client.skills.handler.皇家騎士團.*;
import Client.skills.handler.英雄團.*;
import Client.skills.handler.超新星.*;
import Client.skills.handler.阿尼瑪族.菈菈;
import Client.skills.handler.阿尼瑪族.虎影;
import Client.skills.handler.阿尼瑪族.阿尼瑪族;
import Client.skills.handler.雷普族.*;

import java.util.LinkedList;
import java.util.List;

public class SkillClassFetcher {

    private static final List<AbstractSkillHandler> skillHandlers = new LinkedList<>();

    static {
        loadHandlers();
    }

    public static void loadHandlers() {
        final Class<?>[] skillClasses = {
                初心者.class,
                劍士.class,
                英雄.class,
                聖騎士.class,
                黑騎士.class,
                法師.class,
                火毒大魔導士.class,
                冰雷大魔導士.class,
                主教.class,
                弓箭手.class,
                箭神.class,
                神射手.class,
                開拓者.class,
                盜賊.class,
                夜使者.class,
                暗影神偷.class,
                影武者.class,
                海盜.class,
                拳霸.class,
                槍神.class,
                重砲指揮官.class,
                MANAGER.class,
                管理員.class,
                貴族.class,
                聖魂劍士.class,
                烈焰巫師.class,
                破風使者.class,
                暗夜行者.class,
                閃雷悍將.class,
                狂狼勇士.class,
                龍魔導士.class,
                精靈遊俠.class,
                幻影俠盜.class,
                隱月.class,
                夜光.class,
                惡魔.class,
                惡魔殺手.class,
                惡魔復仇者.class,
                市民.class,
                煉獄巫師.class,
                狂豹獵人.class,
                機甲戰神.class,
                傑諾.class,
                爆拳槍神.class,
                劍豪.class,
                陰陽師.class,
                米哈逸.class,
                凱撒.class,
                凱殷.class,
                天使破壞者.class,
                卡蒂娜.class,
                神之子.class,
                皮卡啾.class,
                雪吉拉.class,
                凱內西斯.class,
                阿戴爾.class,
                伊利恩.class,
                卡莉.class,
                亞克.class,
                菈菈.class,
                虎影.class,
                墨玄.class,
                琳恩.class,

                冒險家.class,
                皇家騎士團.class,
                英雄團.class,
                末日反抗軍.class,
                曉之陣.class,
                超新星.class,
                雷普族.class,
                異界.class,
                阿尼瑪族.class,
                江湖.class,

                格蘭蒂斯.class,
                全部劍士.class,
                全部法師.class,
                全部弓箭手.class,
                全部盜賊.class,
                全部海盜.class,
                全職通用.class
        };

        skillHandlers.clear();
        for (Class<?> c : skillClasses) {
            try {
                if (!AbstractSkillHandler.class.isAssignableFrom(c)) {
                    continue;
                }
                skillHandlers.add((AbstractSkillHandler) c.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                System.err.println("Error: handle was not found in " + c.getSimpleName() + ".class");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static AbstractSkillHandler getHandlerBySkill(int skillid) {
        for (AbstractSkillHandler ash : skillHandlers) {
            if (!ash.containsSkill(skillid)) {
                continue;
            }
            return ash;
        }
        return null;
    }

    public static AbstractSkillHandler getHandlerByJob(int jobWithSub) {
        for (AbstractSkillHandler ash : skillHandlers) {
            if (!ash.containsJob(jobWithSub)) {
                continue;
            }
            return ash;
        }
        return null;
    }
}
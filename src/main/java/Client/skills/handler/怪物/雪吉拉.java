package Client.skills.handler.怪物;

import Client.MapleJob;
import Client.skills.handler.AbstractSkillHandler;

import java.lang.reflect.Field;

import static Config.constants.skills.雪吉拉.*;

public class 雪吉拉 extends AbstractSkillHandler {

    public 雪吉拉() {
        jobs = new MapleJob[]{
                MapleJob.雪吉拉,
                MapleJob.雪吉拉1轉
        };

        for (Field field : Config.constants.skills.雪吉拉.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 給我零食_1:
            case 給我零食_2:
            case 給我零食_3:
                return 給我零食;
            case 雪吉拉_很輕_1:
            case 雪吉拉_很輕_2:
            case 雪吉拉_很輕_3:
                return 雪吉拉_很輕;
            case 我製造的_推進器_1:
                return 我製造的_推進器;
            case 雪吉拉_聰明_讀書_1:
                return 雪吉拉_聰明_讀書;
            case 我製造的_超能推進器:
                return 憤怒強化_超能推進器;
            case 雪吉拉_聰明_解除炸彈_1:
                return 雪吉拉_聰明_解除炸彈;
            case 冰原雪域的勇士_1:
                return 冰原雪域的勇士;
            case 我的朋友_企鵝_1:
                return 我的朋友_企鵝;
            case 雪吉拉_聰明_科學實驗_1:
                return 雪吉拉_聰明_科學實驗;
            case 憤怒強化_烈焰爆破_1:
                return 憤怒強化_烈焰爆破;
            case 雪吉拉_聰明_小提琴_1:
                return 雪吉拉_聰明_小提琴;
            case 憤怒強化_雪吉拉重擊_1:
                return 憤怒強化_雪吉拉重擊;
            case 憤怒強化_憤怒咆哮_1:
                return 憤怒強化_憤怒咆哮;
            case 雪吉拉的領導力_1:
                return 雪吉拉的領導力;
            case 雪吉拉_重擊_1:
                return 雪吉拉_重擊;
            case 雪吉拉力量_1:
                return 雪吉拉力量;
        }
        return -1;
    }
}

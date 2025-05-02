package Client.skills.handler.怪物;

import Client.*;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;

import static Config.constants.skills.皮卡啾.*;

public class 皮卡啾 extends AbstractSkillHandler {

    public 皮卡啾() {
        jobs = new MapleJob[]{
                MapleJob.皮卡啾,
                MapleJob.皮卡啾1轉
        };

        for (Field field : Config.constants.skills.皮卡啾.class.getDeclaredFields()) {
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
            case 皮卡啾攻擊_1:
            case 皮卡啾攻擊_2:
            case 皮卡啾攻擊_3:
            case 皮卡啾攻擊_4:
            case 皮卡啾攻擊_5:
            case 皮卡啾攻擊_6:
            case 皮卡啾攻擊_7:
                return 皮卡啾攻擊;
            case 咕嚕咕嚕_1:
            case 咕嚕咕嚕_2:
                return 咕嚕咕嚕;
            case 皮卡啾的品格傷害:
            case 皮卡啾的品格_迷你啾攻擊:
                return 皮卡啾的品格;
            case 電吉他:
            case 哨子:
            case 紅喇叭:
                return 音波攻擊;
            case 天空豆豆空中:
            case 天空豆豆地上:
                return 天空豆豆;
            case 鬼_臉:
            case 大口吃肉:
            case Zzz:
            case 謎一般的雞尾酒:
            case 皮卡啾的頭戴式耳機:
                return 放鬆;
            case 皮卡啾之力傷害:
                return 皮卡啾之力;
            case 博拉多利:
            case 帕拉美:
            case 愛美麗:
                return 成長吧_喝啊;
            case 超烈焰溜溜球_1:
            case 超烈焰溜溜球_2:
                return 超烈焰溜溜球;
            case 迷你啾出動_1:
                return 迷你啾出動;
            case 粉紅天怒傷害:
                return 粉紅天怒;
            case 皮卡啾暗影_1:
            case 皮卡啾暗影_2:
                return 皮卡啾暗影;
            case 皮卡啾的勇士_1:
                return 皮卡啾的勇士;
            case 皮卡啾的芭蕾舞裙_1:
                return 皮卡啾的芭蕾舞裙;
            case 皮卡啾的速克達_1:
                return 皮卡啾的速克達;
            case 神祇的黃昏_1:
            case 神祇的黃昏_2:
            case 神祇的黃昏_3:
            case 神祇的黃昏_4:
            case 神祇的黃昏_5:
                return 神祇的黃昏;
            case 皮卡啾的俄羅斯娃娃_1:
            case 皮卡啾的俄羅斯娃娃_2:
            case 皮卡啾的俄羅斯娃娃_3:
            case 皮卡啾的俄羅斯娃娃_4:
                return 皮卡啾的俄羅斯娃娃;
            case 靈魂分離_1:
                return 靈魂分離;
            case 魔術秀時間_1:
            case 魔術秀時間_2:
                return 魔術秀時間;
        }
        return -1;
    }


    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 皮卡啾暗影) {
            chr.getSkillEffect(皮卡啾暗影_1).applyTo(chr);
            chr.getSkillEffect(皮卡啾暗影_2).applyTo(chr);
            return 1;
        }
        return -1;
    }

    @Override
    public int onApplyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        switch (applier.effect.getSourceId()) {
            case 超烈焰溜溜球:
            case 超烈焰溜溜球_1: {
                final SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.PinkbeanYoYoStack);
                final int value = Math.min(applyto.getBuffedIntValue(SecondaryStat.PinkbeanYoYoStack) + (applier.passive ? 1 : -1), 8);
                if (mbsvh != null && applier.passive && System.currentTimeMillis() < mbsvh.startTime + 1500L) {
                    return 0;
                }
                applier.localstatups.put(SecondaryStat.PinkbeanYoYoStack, value);
                return 1;
            }
            case 皮卡啾的勇士: {
                applier.localstatups.put(SecondaryStat.IndieStatR, applyfrom.getLevel() / applier.effect.getY());
                return 1;
            }
        }
        return -1;
    }
}

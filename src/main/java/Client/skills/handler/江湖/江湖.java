package Client.skills.handler.江湖;

import Client.skills.handler.AbstractSkillHandler;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.江湖通用;

import java.lang.reflect.Field;

import static Config.constants.skills.通用V核心.江湖通用.*;

public class 江湖 extends AbstractSkillHandler {

    public 江湖() {
        for (Field field : 江湖通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is江湖(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 輪迴之環:
            case 命運之力_戰鬥:
            case 命運之力_弱點:
            case 命運之力_祝福:
            case 命運之力_冒險:
                return 命運之力;
        }
        return -1;
    }
}

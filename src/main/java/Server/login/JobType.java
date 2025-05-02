package Server.login;

import Client.MapleJob;
import Config.constants.JobConstants;

public enum JobType {

    末日反抗軍(0, MapleJob.市民, 931000000, 1),
    冒險家(1, MapleJob.初心者, 4000000, 1),
    皇家騎士團(2, MapleJob.貴族, 130030000, 1, JobInfoFlag.披風.getVelue()),
    狂狼勇士(3, MapleJob.傳說, 914000000, 1, JobInfoFlag.褲裙.getVelue()),
    龍魔導士(4, MapleJob.龍魔導士, 900010000, 1, JobInfoFlag.褲裙.getVelue()),
    精靈遊俠(5, MapleJob.精靈遊俠, 910150000, 1),
    惡魔(6, MapleJob.惡魔, 927000000, 1, JobInfoFlag.臉飾.getVelue() | JobInfoFlag.副手.getVelue()),
    幻影俠盜(7, MapleJob.幻影俠盜, 915000000, 1, JobInfoFlag.披風.getVelue()),
    影武者(8, MapleJob.初心者_影武, 103050900, 1),
    米哈逸(9, MapleJob.米哈逸, 913070000, 1, JobInfoFlag.褲裙.getVelue()),
    夜光(10, MapleJob.夜光, 927020080, 1, JobInfoFlag.披風.getVelue()),
    凱撒(11, MapleJob.凱撒, 958000000, 1),
    天使破壞者(12, MapleJob.天使破壞者, 940011000, 1),
    重砲指揮官(13, MapleJob.初心者_重砲, 3000600, 1),
    傑諾(14, MapleJob.傑諾, 931060089, 1, JobInfoFlag.臉飾.getVelue()),
    神之子(15, MapleJob.神之子, 320000000, 100, JobInfoFlag.披風.getVelue() | JobInfoFlag.副手.getVelue()),
    隱月(16, MapleJob.隱月, 927030050, 1, JobInfoFlag.褲裙.getVelue() | JobInfoFlag.披風.getVelue()),
    皮卡啾(17, MapleJob.皮卡啾1轉, 927030090, 1),
    凱內西斯(18, MapleJob.凱內西斯, 331001000, 10),
    卡蒂娜(19, MapleJob.卡蒂娜, 10000, 10),
    伊利恩(20, MapleJob.伊利恩, 10000, 10),
    亞克(21, MapleJob.亞克, 402090000, 10, JobInfoFlag.臉飾.getVelue()),
    開拓者(22, MapleJob.初心者_開拓, 10000, 10, JobInfoFlag.披風.getVelue()),
    虎影(23, MapleJob.虎影, 10000, 10, JobInfoFlag.臉飾.getVelue() | JobInfoFlag.披風.getVelue()),
    阿戴爾(24, MapleJob.阿戴爾, 993162001, 1),
    凱殷(25, MapleJob.凱殷, 0, 1),
    雪吉拉(26, MapleJob.雪吉拉1轉, 0, 1),
    菈菈(27, MapleJob.菈菈, 0, 1),
    卡莉(28, MapleJob.卡莉, 0, 1, JobInfoFlag.帽子.getVelue()),
    墨玄(1000, MapleJob.墨玄, 0, 1),
    劍豪(1001, MapleJob.劍豪, 807100010, 1, JobInfoFlag.帽子.getVelue() | JobInfoFlag.手套.getVelue()),
    陰陽師(1002, MapleJob.陰陽師, 807100110, 1, JobInfoFlag.帽子.getVelue() | JobInfoFlag.手套.getVelue()),
    琳恩(1003, MapleJob.琳恩, 866000000, 1, JobInfoFlag.帽子.getVelue()),
    ;

    public final MapleJob job;
    public final int type, mapId;
    public final short level;
    public int flag = JobInfoFlag.臉型.getVelue() | JobInfoFlag.髮型.getVelue() | JobInfoFlag.衣服.getVelue() | JobInfoFlag.鞋子.getVelue() | JobInfoFlag.武器.getVelue();

    JobType(int type, MapleJob job, int map, int level) {
        this.type = type;
        this.job = job;
        this.mapId = map;
        this.level = (short) level;
    }

    JobType(int type, MapleJob job, int map, int level, int flag) {
        this(type, job, map, level);
        this.flag |= flag;
    }

    public static JobType getByType(int g) {
        for (JobType e : JobType.values()) {
            if (e.type == g) {
                return e;
            }
        }
        return null;
    }

    public static JobType getById(int g) {
        for (JobType e : JobType.values()) {
            if (e.job.getId() == g) {
                return e;
            }
        }
        return 冒險家;
    }

    public static JobType getByJob(int job) {
        if (JobConstants.is末日反抗軍(job)) {
            return 末日反抗軍;
        } else if (JobConstants.is冒險家(job)) {
            return 冒險家;
        } else if (JobConstants.is皇家騎士團(job)) {
            return 皇家騎士團;
        } else if (JobConstants.is狂狼勇士(job)) {
            return 狂狼勇士;
        } else if (JobConstants.is龍魔導士(job)) {
            return 龍魔導士;
        } else if (JobConstants.is精靈遊俠(job)) {
            return 精靈遊俠;
        } else if (JobConstants.is惡魔(job)) {
            return 惡魔;
        } else if (JobConstants.is幻影俠盜(job)) {
            return 幻影俠盜;
        } else if (JobConstants.is影武者(job)) {
            return 影武者;
        } else if (JobConstants.is米哈逸(job)) {
            return 米哈逸;
        } else if (JobConstants.is夜光(job)) {
            return 夜光;
        } else if (JobConstants.is凱撒(job)) {
            return 凱撒;
        } else if (JobConstants.is天使破壞者(job)) {
            return 天使破壞者;
        } else if (JobConstants.is重砲指揮官(job)) {
            return 重砲指揮官;
        } else if (JobConstants.is傑諾(job)) {
            return 傑諾;
        } else if (JobConstants.is神之子(job)) {
            return 神之子;
        } else if (JobConstants.is隱月(job)) {
            return 隱月;
        } else if (JobConstants.is皮卡啾(job)) {
            return 皮卡啾;
        } else if (JobConstants.is凱內西斯(job)) {
            return 凱內西斯;
        } else if (JobConstants.is卡蒂娜(job)) {
            return 卡蒂娜;
        } else if (JobConstants.is伊利恩(job)) {
            return 伊利恩;
        } else if (JobConstants.is亞克(job)) {
            return 亞克;
        } else if (JobConstants.is開拓者(job)) {
            return 開拓者;
        } else if (JobConstants.is虎影(job)) {
            return 虎影;
        } else if (JobConstants.is阿戴爾(job)) {
            return 阿戴爾;
        } else if (JobConstants.is凱殷(job)) {
            return 凱殷;
        } else if (JobConstants.is雪吉拉(job)) {
            return 雪吉拉;
        } else if (JobConstants.is菈菈(job)) {
            return 菈菈;
        } else if (JobConstants.is卡莉(job)) {
            return 卡莉;
        } else if (JobConstants.is墨玄(job)) {
            return 墨玄;
        } else if (JobConstants.is劍豪(job)) {
            return 劍豪;
        } else if (JobConstants.is陰陽師(job)) {
            return 陰陽師;
        } else if (JobConstants.is琳恩(job)) {
            return 琳恩;
        }
        return null;
    }
}

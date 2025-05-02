/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Config.constants;

import Client.MapleJob;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import tools.Pair;

public class JobConstants {

    // UI.wz/Login.img/RaceSelect_new/order
    public static final int JOB_ORDER = 255;

    public static int getEar(int job, int nEar) {
        int n3 = 0;
        if (is精靈遊俠(job)) {
            switch (nEar) {
                case 0: {
                    n3 = 1;
                    break;
                }
                case 1: {
                    n3 = 0;
                    break;
                }
                case 2: {
                    n3 = 2;
                    break;
                }
                case 3: {
                    n3 = 3;
                    break;
                }
            }
        } else if (is伊利恩(job)) {
            switch (nEar) {
                case 0: {
                    n3 = 2;
                    break;
                }
                case 1: {
                    n3 = 1;
                    break;
                }
                case 2: {
                    n3 = 0;
                    break;
                }
                case 3: {
                    n3 = 3;
                    break;
                }
            }
        } else if (is亞克(job) || is阿戴爾(job)) {
            switch (nEar) {
                case 0: {
                    n3 = 3;
                    break;
                }
                case 1: {
                    n3 = 1;
                    break;
                }
                case 2: {
                    n3 = 2;
                    break;
                }
                case 3: {
                    n3 = 0;
                    break;
                }
            }
        } else {
            n3 = nEar;
        }
        return n3;
    }

    public static int[][][] JobStats = new int[24][2][6];

    static {
        JobStats[0][0] = new int[]{12, 16, 0, 10, 12, 0};
        JobStats[0][1] = new int[]{8, 12, 0, 6, 8, 15};
        JobStats[1][0] = new int[]{64, 68, 0, 4, 6, 0};
        JobStats[1][1] = new int[]{50, 54, 0, 2, 4, 15};
        JobStats[2][0] = new int[]{10, 14, 0, 22, 24, 0};
        JobStats[2][1] = new int[]{6, 10, 0, 18, 20, 15};
        JobStats[3][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[3][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[4][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[4][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[5][0] = new int[]{22, 26, 0, 18, 22, 0};
        JobStats[5][1] = new int[]{18, 20, 0, 14, 16, 15};
        JobStats[6][0] = new int[]{37, 41, 0, 18, 22, 0};
        JobStats[6][1] = new int[]{28, 30, 0, 14, 16, 15};
        JobStats[7][0] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[7][1] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[8][0] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[8][1] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[9][0] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[9][1] = new int[]{0, 0, 0, 0, 0, 0};
        JobStats[10][0] = new int[]{20, 24, 0, 14, 16, 20};
        JobStats[10][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[11][0] = new int[]{44, 48, 0, 4, 8, 0};
        JobStats[11][1] = new int[]{30, 34, 0, 2, 4, 15};
        JobStats[12][0] = new int[]{16, 20, 0, 35, 39, 0};
        JobStats[12][1] = new int[]{12, 16, 0, 21, 25, 15};
        JobStats[13][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[13][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[14][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[14][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[15][0] = new int[]{16, 20, 0, 198, 200, 0};
        JobStats[15][1] = new int[]{12, 16, 0, 21, 25, 15};
        JobStats[16][0] = new int[]{34, 38, 0, 22, 24, 0};
        JobStats[16][1] = new int[]{20, 24, 0, 18, 20, 15};
        JobStats[17][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[17][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[18][0] = new int[]{22, 26, 0, 18, 22, 0};
        JobStats[18][1] = new int[]{18, 20, 0, 14, 16, 15};
        JobStats[19][0] = new int[]{52, 56, 0, 0, 0, 0};
        JobStats[19][1] = new int[]{38, 40, 0, 0, 0, 0};
        JobStats[20][0] = new int[]{28, 32, 0, 0, 0, 0};
        JobStats[20][1] = new int[]{24, 26, 0, 0, 0, 0};
        JobStats[21][0] = new int[]{15, 15, 0, 0, 0, 0};
        JobStats[21][1] = new int[]{15, 15, 0, 0, 0, 0};
        JobStats[22][0] = new int[]{20, 24, 0, 14, 16, 0};
        JobStats[22][1] = new int[]{16, 20, 0, 10, 12, 15};
        JobStats[23][0] = new int[]{64, 68, 0, 0, 0, 0};
        JobStats[23][1] = new int[]{50, 54, 0, 0, 0, 0};
    }

    public static boolean is劍士(int job) {
        return getJobBranch(job) == 1;
    }

    public static boolean is法師(int job) {
        return getJobBranch(job) == 2;
    }

    public static boolean is弓箭手(int job) {
        return getJobBranch(job) == 3;
    }

    public static boolean is盜賊(int job) {
        return getJobBranch(job) == 4 || is傑諾(job);
    }

    public static boolean is海盜(int job) {
        return getJobBranch(job) == 5 || is傑諾(job);
    }

    // 冒險家 start
    public static boolean is冒險家(final int job) {
        return job / 1000 == 0;
    }

    public static boolean is冒險家劍士(int job) {
        return job / 100 == 1;
    }

    public static boolean is英雄(int job) {
        return job / 10 == 11;
    }

    public static boolean is聖騎士(int job) {
        return job / 10 == 12;
    }

    public static boolean is黑騎士(int job) {
        return job / 10 == 13;
    }

    public static boolean is冒險家法師(int job) {
        return job / 100 == 2;
    }

    public static boolean is火毒(int job) {
        return job / 10 == 21;
    }

    public static boolean is冰雷(int job) {
        return job / 10 == 22;
    }

    public static boolean is主教(int job) {
        return job / 10 == 23;
    }

    public static boolean is冒險家弓箭手(int job) {
        return job / 100 == 3;
    }

    public static boolean is箭神(int job) {
        return job / 10 == 31;
    }

    public static boolean is神射手(int job) {
        return job / 10 == 32;
    }

    public static boolean is開拓者(int job) {
        return job == 301 || job / 10 == 33;
    }

    public static boolean is冒險家盜賊(int job) {
        return job / 100 == 4;
    }

    public static boolean is夜使者(int job) {
        return job / 10 == 41;
    }

    public static boolean is暗影神偷(int job) {
        return job / 10 == 42;
    }

    public static boolean is影武者(int job) {
        return job / 10 == 43 || job == 401;
    }

    public static boolean is冒險家海盜(int job) {
        return job / 100 == 5;
    }

    public static boolean is拳霸(int job) {
        return job / 10 == 51;
    }

    public static boolean is槍神(int job) {
        return job / 10 == 52;
    }

    public static boolean is拳霸新(int job) {
        return job == 509 || job / 10 == 58;
    }

    public static boolean is槍神新(int job) {
        return job == 509 || job / 10 == 59;
    }

    public static boolean is重砲指揮官(int job) {
        return job / 10 == 53 || job == 501;
    }

    public static boolean is管理員(int job) {
        return job == 800 || job == 900 || job == 910;
    }
    //冒險家end

    //皇家騎士團start
    public static boolean is皇家騎士團(int job) {
        return job / 1000 == 1;
    }

    public static boolean is聖魂劍士(int job) {
        return job / 100 == 11;
    }

    public static boolean is烈焰巫師(int job) {
        return job / 100 == 12;
    }

    public static boolean is破風使者(int job) {
        return job / 100 == 13;
    }

    public static boolean is暗夜行者(int job) {
        return job / 100 == 14;
    }

    public static boolean is閃雷悍將(int job) {
        return job / 100 == 15;
    }
    //皇家騎士團end

    //英雄團start
    public static boolean is英雄團(int job) {
        return job / 1000 == 2;
    }

    public static boolean is狂狼勇士(int job) {
        return job / 100 == 21 || job == 2000;
    }

    public static boolean is龍魔導士(int job) {
        return job / 100 == 22 || job == 2001;
    }

    public static boolean is精靈遊俠(int job) {
        return job / 100 == 23 || job == 2002;
    }

    public static boolean is幻影俠盜(int job) {
        return job / 100 == 24 || job == 2003;
    }

    public static boolean is夜光(int job) {
        return job / 100 == 27 || job == 2004;
    }

    public static boolean is隱月(int job) {
        return job / 100 == 25 || job == 2005;
    }
    //英雄團end

    //末日反抗軍start
    public static boolean is末日反抗軍(int job) {
        return job / 1000 == 3;
    }

    public static boolean is惡魔(int job) {
        return is惡魔殺手(job) || is惡魔復仇者(job) || job == 3001;
    }

    public static boolean is惡魔殺手(int job) {
        return job / 10 == 311 || job == 3100;
    }

    public static boolean is惡魔復仇者(int job) {
        return job / 10 == 312 || job == 3101;
    }

    public static boolean is煉獄巫師(int job) {
        return job / 100 == 32;
    }

    public static boolean is狂豹獵人(int job) {
        return job / 100 == 33;
    }

    public static boolean is機甲戰神(int job) {
        return job / 100 == 35;
    }

    public static boolean is傑諾(int job) {
        return job / 100 == 36 || job == 3002;
    }

    public static boolean is爆拳槍神(int job) {
        return job / 100 == 37;
    }
    //末日反抗軍end

    //曉の陣start
    public static boolean is曉の陣(int job) {
        return job / 1000 == 4;
    }

    public static boolean is劍豪(int job) {
        return job / 100 == 41 || job == 4001;
    }

    public static boolean is陰陽師(int job) {
        return job / 100 == 42 || job == 4002;
    }
    //曉陣end

    //騎士團團長start
    public static boolean is騎士團團長(final int job) {
        return job / 1000 == 5;
    }

    public static boolean is米哈逸(int job) {
        return job / 100 == 51 || job == 5000;
    }
    //騎士團團長end

    //超新星start
    public static boolean is超新星(final int job) {
        return job / 1000 == 6;
    }

    public static boolean is凱撒(int job) {
        return job / 100 == 61 || job == 6000;
    }

    public static boolean is凱殷(int job) {
        return job / 100 == 63 || job == 6003;
    }

    public static boolean is卡蒂娜(int job) {
        return job / 100 == 64 || job == 6002;
    }

    public static boolean is天使破壞者(int job) {
        return job / 100 == 65 || job == 6001;
    }
    //超新星end

    public static boolean is神之子(int job) {
        return job == 10000 || job / 100 == 101;
    }

    public static boolean is皮卡啾(int job) {
        return job / 100 == 131 || job == 13000;
    }

    public static boolean is雪吉拉(int job) {
        return job / 100 == 135 || job == 13001;
    }

    public static boolean is異界(final int job) {
        return job / 1000 == 14;
    }

    public static boolean is凱內西斯(int job) {
        return job / 100 == 142 || job == 14000;
    }

    //雷普族start
    public static boolean is雷普族(final int job) {
        return job / 1000 == 15;
    }

    public static boolean is阿戴爾(int job) {
        return job / 100 == 151 || job == 15002;
    }

    public static boolean is伊利恩(int job) {
        return job / 100 == 152 || job == 15000;
    }

    public static boolean is卡莉(int job) {
        return job / 100 == 154 || job == 15003;
    }

    public static boolean is亞克(int job) {
        return job / 100 == 155 || job == 15001;
    }
    //雷普族end

    public static boolean is阿尼瑪族(final int job) {
        return job / 1000 == 16;
    }

    public static boolean is菈菈(int job) {
        return job / 100 == 162 || job == 16001;
    }

    public static boolean is虎影(int job) {
        return job / 100 == 164 || job == 16000;
    }

    public static boolean is江湖(final int job) {
        return job / 1000 == 17;
    }

    public static boolean is墨玄(int job) {
        return job / 100 == 175 || job == 17000;
    }

    public static boolean is琳恩(int job) {
        return job / 100 == 172 || job == 17001;
    }

    public static boolean is格蘭蒂斯(final int job) {
        return is超新星(job) || is雷普族(job) || is阿尼瑪族(job);
    }

    public static boolean notNeedSPSkill(int job) {
        return SkillConstants.dZ(job) || is零轉職業(job);
    }

    public static boolean is零轉職業(int job) {
        return job % 1000 < 10;
    }

    public static short getBeginner(short jobid) {
        if (jobid % 1000 < 10) {
            return jobid;
        }
        int jobBranch = (jobid % 1000) / 100;
        switch (jobid / 1000) {
            case 2: // 英雄團
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.傳說.getId();
                    case 2:
                        return (short) MapleJob.龍魔導士.getId();
                    case 3:
                        return (short) MapleJob.精靈遊俠.getId();
                    case 4:
                        return (short) MapleJob.幻影俠盜.getId();
                    case 5:
                        return (short) MapleJob.隱月.getId();
                    case 7:
                        return (short) MapleJob.夜光.getId();
                }
                break;
            case 3: // 反抗軍
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.惡魔.getId();
                    case 2:
                    case 3:
                    case 5:
                    case 7:
                        return (short) MapleJob.市民.getId();
                    case 6:
                        return (short) MapleJob.傑諾.getId();
                }
                break;
            case 4: // 曉之陣
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.劍豪.getId();
                    case 2:
                        return (short) MapleJob.陰陽師.getId();
                }
                break;
            case 6: // 超新星
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.凱撒.getId();
                    case 3:
                        return (short) MapleJob.凱殷.getId();
                    case 4:
                        return (short) MapleJob.卡蒂娜.getId();
                    case 5:
                        return (short) MapleJob.天使破壞者.getId();
                }
                break;
            case 13: // 怪物
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.皮卡啾.getId();
                    case 5:
                        return (short) MapleJob.雪吉拉.getId();
                }
                break;
            case 15: // 雷普族
                switch (jobBranch) {
                    case 1:
                        return (short) MapleJob.阿戴爾.getId();
                    case 2:
                        return (short) MapleJob.伊利恩.getId();
                    case 4:
                        return (short) MapleJob.卡莉.getId();
                    case 5:
                        return (short) MapleJob.亞克.getId();
                }
                break;
            case 16: // 阿尼瑪族
                switch (jobBranch) {
                    case 2:
                        return (short) MapleJob.菈菈.getId();
                    case 4:
                        return (short) MapleJob.虎影.getId();
                }
                break;
            case 17: // 江湖
                switch (jobBranch) {
                    case 2:
                        return (short) MapleJob.琳恩.getId();
                    case 5:
                        return (short) MapleJob.墨玄.getId();
                }
                break;
        }
        return (short) (jobid / 1000 * 1000);
    }

    public static int get龍魔轉數(int jobid) {
        switch (jobid) {
            case 2200:
            case 2210:
                return 1;
            case 2211:
            case 2212:
            case 2213:
                return 2;
            case 2214:
            case 2215:
            case 2216:
                return 3;
            case 2217:
            case 2218:
                return 4;
            default:
                return 0;
        }
    }

    public static int getJobBranch(int job) {
        int result = job % 1000 / 100;
        switch (job / 100) {
            case 27:
                return 2;
            case 36:
                return 4;
            case 37:
                return 1;
        }
        return result;
    }

    public static String getJobBranchName(int job) {
        if (is傑諾(job)) {
            return "xenon";
        }
        String jobBranchName;
        switch (getJobBranch(job)) {
            case 1:
                jobBranchName = "warrior";
                break;
            case 2:
                jobBranchName = "magician";
                break;
            case 3:
                jobBranchName = "archer";
                break;
            case 4:
                jobBranchName = "rogue";
                break;
            case 5:
                jobBranchName = "pirate";
                break;
            case 0:
            default:
                jobBranchName = "beginner";
                break;
        }
        return jobBranchName;
    }

    public static int getJobFlag(int job) {
        int n = 1 << getJobBranch(job) - 1;
        if (job / 100 == 36) {
            n |= 0x10;
        }
        return n;
    }

    public static boolean isJobFamily(int baseJob, int currentJob) {
        return currentJob >= baseJob && currentJob / 100 == baseJob / 100;
    }

    public static boolean noBulletJob(int job) {
        return is狂狼勇士(job) || is機甲戰神(job) || is拳霸(job) || is重砲指揮官(job) || is精靈遊俠(job) || is米哈逸(job) || is凱撒(job) || is天使破壞者(job) || is傑諾(job) || is開拓者(job);
    }

    public static boolean isNotMpJob(int job) {
        return is陰陽師(job) || is惡魔殺手(job) || is惡魔復仇者(job) || is神之子(job) || is凱內西斯(job);
    }

    public static int getTeachSkillID(int Job) {
        int skillId = -1;
        if (JobConstants.is冒險家(Job) && JobConstants.getJobNumber(Job) > 1) {
            if (JobConstants.is英雄(Job)) {
                skillId = 英雄.無形的信任_英雄;
            } else if (JobConstants.is聖騎士(Job)) {
                skillId = 聖騎士.無形的信任_聖騎士;
            } else if (JobConstants.is黑騎士(Job)) {
                skillId = 黑騎士.無形的信任_黑騎士;
            } else if (JobConstants.is火毒(Job)) {
                skillId = 火毒.實戰的知識_火毒大魔導士;
            } else if (JobConstants.is冰雷(Job)) {
                skillId = 冰雷.實戰的知識_冰雷大魔導士;
            } else if (JobConstants.is主教(Job)) {
                skillId = 主教.實戰的知識_主教;
            } else if (JobConstants.is箭神(Job)) {
                skillId = 箭神.探險家的好奇心_箭神;
            } else if (JobConstants.is神射手(Job)) {
                skillId = 神射手.探險家的好奇心_神射手;
            } else if (JobConstants.is開拓者(Job)) {
                skillId = 開拓者.探險家的好奇心_開拓者;
            } else if (JobConstants.is夜使者(Job)) {
                skillId = 夜使者.小偷的狡詐_夜使者;
            } else if (JobConstants.is暗影神偷(Job)) {
                skillId = 暗影神偷.小偷的狡詐_暗影神偷;
            } else if (JobConstants.is影武者(Job)) {
                skillId = 影武者.小偷的狡詐_影武者;
            } else if (JobConstants.is拳霸(Job)) {
                skillId = 拳霸.海盜的祝福_拳霸;
            } else if (JobConstants.is槍神(Job)) {
                skillId = 槍神.海盜的祝福_槍神;
            } else if (JobConstants.is重砲指揮官(Job)) {
                skillId = 重砲指揮官.海盜的祝福_重砲指揮官;
            }
        } else if (JobConstants.is皇家騎士團(Job)) {
            int jobBranch = getJobBranch(Job);
            switch (jobBranch) {
                case 1:
                    skillId = 聖魂劍士.西格諾斯祝福_劍士;
                    break;
                case 2:
                    skillId = 烈焰巫師.西格諾斯祝福_法師;
                    break;
                case 3:
                    skillId = 破風使者.西格諾斯祝福_弓箭手;
                    break;
                case 4:
                    skillId = 暗夜行者.西格諾斯祝福_盜賊;
                    break;
                case 5:
                    skillId = 閃雷悍將.西格諾斯祝福_海盜;
                    break;
            }
        } else if (JobConstants.is狂狼勇士(Job)) {
            skillId = 狂狼勇士.連續擊殺優勢;
        } else if (JobConstants.is龍魔導士(Job)) {
            skillId = 龍魔導士.輪之堅持;
        } else if (JobConstants.is精靈遊俠(Job)) {
            skillId = 精靈遊俠.精靈的祝福;
        } else if (JobConstants.is幻影俠盜(Job)) {
            skillId = 幻影俠盜.致命本能;
        } else if (JobConstants.is夜光(Job)) {
            skillId = 夜光.滲透;
        } else if (JobConstants.is隱月(Job)) {
            skillId = 隱月.死裡逃生;
        } else if (JobConstants.is惡魔殺手(Job)) {
            skillId = 惡魔殺手.惡魔之怒;
        } else if (JobConstants.is惡魔復仇者(Job)) {
            skillId = 惡魔復仇者.狂暴鬥氣;
        } else if (JobConstants.is爆拳槍神(Job)) {
            skillId = 爆拳槍神.自由精神_爆拳槍神;
        } else if (JobConstants.is煉獄巫師(Job)) {
            skillId = 煉獄巫師.自由精神_煉獄巫師;
        } else if (JobConstants.is狂豹獵人(Job)) {
            skillId = 狂豹獵人.自由精神_狂豹獵人;
        } else if (JobConstants.is機甲戰神(Job)) {
            skillId = 機甲戰神.自由精神_機甲戰神;
        } else if (JobConstants.is傑諾(Job)) {
            skillId = 傑諾.合成邏輯;
        } else if (JobConstants.is劍豪(Job)) {
            skillId = 劍豪.疾風傳授;
        } else if (JobConstants.is陰陽師(Job)) {
            skillId = 陰陽師.紫扇傳授;
        } else if (JobConstants.is米哈逸(Job)) {
            skillId = 米哈逸.光之守護;
        } else if (JobConstants.is凱撒(Job)) {
            skillId = 凱撒.鋼鐵意志;
        } else if (JobConstants.is凱殷(Job)) {
            skillId = 凱殷.事前準備;
        } else if (JobConstants.is卡蒂娜(Job)) {
            skillId = 卡蒂娜.集中狂攻;
        } else if (JobConstants.is天使破壞者(Job)) {
            skillId = 天使破壞者.靈魂契約;
        } else if (JobConstants.is神之子(Job)) {
            skillId = 神之子.時之祝福;
        } else if (JobConstants.is凱內西斯(Job)) {
            skillId = 凱內西斯.判斷;
        } else if (JobConstants.is阿戴爾(Job)) {
            skillId = 阿戴爾.貴族;
        } else if (JobConstants.is伊利恩(Job)) {
            skillId = 伊利恩.戰鬥的流動;
        } else if (JobConstants.is卡莉(Job)) {
            skillId = 卡莉.天賦;
        } else if (JobConstants.is亞克(Job)) {
            skillId = 亞克.無我;
        } else if (JobConstants.is菈菈(Job)) {
            skillId = 菈菈.大自然夥伴;
        } else if (JobConstants.is虎影(Job)) {
            skillId = 虎影.自信心;
        } else if (JobConstants.is墨玄(Job)) {
            skillId = 墨玄.氣魄;
        } else if (JobConstants.is琳恩(Job)) {
            skillId = 琳恩.守護神的保佑;
        }
        return skillId;
    }

    /*
     * 獲取角色的默認髮型和臉型
     */
    public static Pair<Integer, Integer> getDefaultFaceAndHair(int job, int gender) {
        int face = gender == 0 ? 20100 : 21700;
        int hair = gender == 0 ? 30030 : 31002;
        if (JobConstants.is影武者(job)) {
            face = gender == 0 ? 20265 : 21261;
            hair = gender == 0 ? 33830 : 34820;
        } else if (JobConstants.is精靈遊俠(job)) {
            face = gender == 0 ? 20549 : 21547;
            hair = gender == 0 ? 33453 : 34423;
        } else if (JobConstants.is幻影俠盜(job)) {
            face = gender == 0 ? 20659 : 21656;
            hair = gender == 0 ? 33703 : 34703;
        } else if (JobConstants.is夜光(job)) {
            face = gender == 0 ? 20174 : 21169;
            hair = gender == 0 ? 36190 : 37070;
        } else if (JobConstants.is惡魔殺手(job)) {
            face = gender == 0 ? 20248 : 21246;
            hair = gender == 0 ? 33531 : 34411;
        } else if (JobConstants.is惡魔復仇者(job)) {
            face = gender == 0 ? 20248 : 21280;
            hair = gender == 0 ? 36460 : 37450;
        } else if (JobConstants.is傑諾(job)) {
            face = gender == 0 ? 20185 : 21182;
            hair = gender == 0 ? 36470 : 37490;
        } else if (JobConstants.is米哈逸(job)) {
            face = gender == 0 ? 20169 : 21700;
            hair = gender == 0 ? 36033 : 31002;
        } else if (JobConstants.is凱撒(job)) {
            face = gender == 0 ? 20576 : 21571;
            hair = gender == 0 ? 36245 : 37125;
        } else if (JobConstants.is天使破壞者(job)) {
            face = gender == 0 ? 20576 : 21374;
            hair = gender == 0 ? 36245 : 37242;
        }
        return new Pair<>(face, hair);
    }

    public static int getSkillBookBySkill(int skillId) {
        return getSkillBookByJob(skillId / 10000, skillId);
    }

    public static int getJobNumber(int jobz) {
        int job = jobz % 1000;
        if (jobz > 40000) {
            return 5;
        }
        if (is龍魔導士(jobz)) {
            return get龍魔轉數(jobz);
        } else if (jobz / 10 == 43) {
            if ((jobz - 430) / 2 <= 2) {
                return (jobz - 430) / 2 + 2;
            }
            return 0;
        } else if (job < 100) {
            return 0; //新手 beginner
        } else if ((job / 10) % 10 == 0) {
            return 1;
        } else {
            return 2 + (job % 10);
        }
    }

    public static int getJobGrade(int jobz) {
        if (is龍魔導士(jobz)) {
            return get龍魔轉數(jobz);
        }
        int job = (jobz % 1000);
        if (job / 10 == 0) {
            return 0; // beginner
        } else if (job / 10 % 10 == 0) {
            return 1;
        } else {
            return job % 10 + 2;
        }
    }

    public static boolean isSameJob(int job, int job2) {
        int jobNum = getJobGrade(job);
        int job2Num = getJobGrade(job2);
        // 對初心者判斷
        if (jobNum == 0 || job2Num == 0) {
            return getBeginner((short) job) == getBeginner((short) job2);
        }

        // 初心者過濾掉后, 對職業群進行判斷
        if (getJobGroup(job) != getJobGroup(job2)) {
            return false;
        }

        // 代碼特殊的單獨判斷
        if (is管理員(job) || is管理員(job2)) {
            return is管理員(job) && is管理員(job2);
        } else if (is重砲指揮官(job) || is重砲指揮官(job2)) {
            return is重砲指揮官(job) && is重砲指揮官(job2);
        } else if (is惡魔復仇者(job) || is惡魔復仇者(job2)) {
            return is惡魔復仇者(job) && is惡魔復仇者(job2);
        }

        // 對一轉分支判斷(如 劍士 跟 黑騎)
        if (jobNum == 1 || job2Num == 1) {
            return job / 100 == job2 / 100;
        }

        return job / 10 == job2 / 10;
    }

    public static int getJobGroup(int job) {
        return job / 1000;
    }

    public static int getSkillBookByLevel(int job, int level) {
        if (is龍魔導士(job)) {
            switch (job) {
                case 2211:
                case 2212:
                case 2213: {
                    return 1;
                }
                case 2214:
                case 2215:
                case 2216: {
                    return 2;
                }
                case 2217:
                case 2218: {
                    return 3;
                }
            }
        } else if (is影武者(job)) {
            if (level > 100) {
                return 5;
            } else if (level > 60) {
                return 4;
            } else if (level > 45) {
                return 3;
            } else if (level > 30) {
                return 2;
            } else if (level > 20) {
                return 1;
            }
            return 0;
        } else if (isSeparatedSpJob(job)) {
            if (level > 100) {
                return 3;
            } else if (level > 60) {
                return 2;
            } else if (level > 30) {
                return 1;
            }
        }
        return 0;
    }

    public static int getSkillBookByJob(int job) {
        return getSkillBookByJob(job, 0);
    }

    public static int getSkillBookByJob(int job, int skillId) {
        if (JobConstants.is龍魔導士(job)) {
            switch (job) {
                case 2211:
                case 2212:
                case 2213: {
                    return 1;
                }
                case 2214:
                case 2215:
                case 2216: {
                    return 2;
                }
                case 2217:
                case 2218: {
                    return 3;
                }
                default: {
                    return 0;
                }
            }
        } else if (JobConstants.is影武者(job)) {
            return job - 429;
        } else if (JobConstants.is神之子(job)) {
            if (skillId > 0) {
                return (skillId % 1000) / 100 == 1 ? 1 : 0;
            } else {
                return 0;
            }
        } else if (notNeedSPSkill(job) || job % 100 == 0) {
            return 0;
        } else {
            switch (job) {
                case 301:
                case 501:
                case 508:
                case 3101:
                    return 0;
                default:
                    return job % 10 + 1;
            }
        }
    }

    public static int getBOF_ForJob(int job) {
        return SkillConstants.getSkillByJob(12, job);
    }

    public static int getEmpress_ForJob(int job) {
        return SkillConstants.getSkillByJob(73, job);
    }

    public static String getJobBasicNameById(int job) {
        if (JobConstants.is劍士(job)) {
            return "劍士";
        } else if (JobConstants.is法師(job)) {
            return "法師";
        } else if (JobConstants.is弓箭手(job)) {
            return "弓箭手";
        } else if (JobConstants.is盜賊(job)) {
            return "盜賊";
        } else if (JobConstants.is海盜(job)) {
            return "海盜";
        } else {
            return "初心者";
        }
    }

    public static boolean isSeparatedSpJob(int job) {
        return !(is管理員(job) || is皮卡啾(job) || is雪吉拉(job));
    }

    public static int getLevelUpMp(final int job, final boolean b) {
        int mp = getJobStat(job, b)[3];
//        if ((job / 1000 == 0 || job / 1000 == 1) && getTrueJobGrade(job) == 2) {
//            mp += 104;
//        }
        return mp;
    }

    public static int getLevelUpHp(final int job, final int level, final boolean b) {
        int hp = getJobStat(job, b)[0];
//        if ((job / 10 == 51 || job / 10 == 151) && level > 30) {
//            hp += 20;
//        }
        return hp;
    }

    private static int[] getJobStat(final int job, final boolean b) {
        final int n2 = job / 1000;
        final int jobGrade = getJobBranch(job);
        int[] array = null;
        final int n3 = b ? 0 : 1;
        if (jobGrade <= 9) {
            array = JobStats[jobGrade][n3];
            if (job / 10 == 53 || job == 501) {
                array = JobStats[6][n3];
            } else if (is夜光(job)) {
                array = JobStats[15][n3];
            } else if (n2 == 2) {
                switch (jobGrade) {
                    case 1: {
                        array = JobStats[11][n3];
                        break;
                    }
                    case 2: {
                        array = JobStats[12][n3];
                        break;
                    }
                    case 3: {
                        array = JobStats[13][n3];
                        break;
                    }
                    case 4: {
                        array = JobStats[14][n3];
                        break;
                    }
                }
            } else if (is卡蒂娜(job)) {
                array = JobStats[11][n3];
            } else if (job / 100 == 32) {
                array = JobStats[16][n3];
            } else if (is狂豹獵人(job)) {
                array = JobStats[17][n3];
            } else if (is機甲戰神(job)) {
                array = JobStats[18][n3];
            } else if (is惡魔殺手(job)) {
                array = JobStats[19][n3];
            } else if (is天使破壞者(job)) {
                array = JobStats[20][n3];
            } else if (is惡魔復仇者(job)) {
                array = JobStats[21][n3];
            } else if (is傑諾(job)) {
                array = JobStats[22][n3];
            } else if (is神之子(job)) {
                array = JobStats[23][n3];
            } else if (is凱內西斯(job) || is伊利恩(job)) {
                array = JobStats[16][n3];
            } else if (is爆拳槍神(job)) {
                array = JobStats[1][n3];
            }
        }
        return array;
    }

    public static boolean hasDecorate(int job) {
        return JobConstants.is惡魔(job) || JobConstants.is傑諾(job) || JobConstants.is亞克(job) || JobConstants.is虎影(job);
    }

    public static boolean isDexPirate(int job) {
        return JobConstants.is槍神(job) || JobConstants.is機甲戰神(job) || JobConstants.is天使破壞者(job) || JobConstants.is墨玄(job);
    }
}

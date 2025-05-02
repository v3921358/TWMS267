package Client.skills.handler;

import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.status.MonsterStatus;
import Net.server.buffs.MapleStatEffect;
import Server.channel.handler.AttackInfo;
import tools.Pair;

import java.awt.*;
import java.util.Map;

public class SkillClassApplier {

    public MapleStatEffect effect;
    public boolean primary, att, passive, b3, b4, b5, b7, overwrite, cancelEffect, applySummon;
    public Point pos;
    public int duration, maskedDuration, cooldown, buffz, mobOid, hpHeal, mpHeal, prop, unk, plus;
    public Map<SecondaryStat, Integer> localstatups, maskedstatups;
    public Map<SecondaryStat, Pair<Integer, Integer>> sendstatups;
    public Map<MonsterStatus, Integer> localmobstatups;
    public Map<Integer, SkillEntry> skillMap;
    public long startChargeTime, startTime, totalDamage;
    public Skill theSkill;
    public AttackInfo ai = null;
}

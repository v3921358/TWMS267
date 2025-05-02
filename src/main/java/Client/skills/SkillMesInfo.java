/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.skills;

/**
 * @author setosan
 */
public enum SkillMesInfo {
    Null,
    blind,//暗黑效果
    seal,//封印 
    dot,//持續傷害
    cold,
    stun,//暈[無法移動]
    restrict,//限制
    elementalWeaken,//元素的削弱
    attackLimit,//攻擊極限
    freeze, //冰凍
    incTargetEXP,//提高經驗值[x] 
    lifting,
    homing,//導航?
    darkness,//恐慌的黑暗效果[x]
    incapacitate,//癱瘓
    incTargetReward,//提高怪物掉物[x]
    polymorph,//把全部玩家變成冰蝸牛
    amplifyDamage,//傷害增強[x]
    mindControl,//精神控制
    haste,
    reduceTargetDam,//減少怪物的攻擊力[X]
    buffLimit,
    incTargetPDP,
    incTargetMeso,//楓幣增加
    reduceTargetPDP,//下降怪物的PDP
    slow,//減速
    reduceTargetMDP,//下降怪物的防禦力
    reduceTargetACC,//下降怪物的攻擊力
    incTargetMDP;

    public static SkillMesInfo getInfo(String Name) {
        if (Name.equals("reduceTargetACC ")) {
            Name = "reduceTargetACC";
        }
        SkillMesInfo info = Null;
        for (SkillMesInfo value : values()) {
            if (value.name().equals(Name)) {
                info = value;
            }
        }
        return info;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.maps.pvp;

import java.awt.*;

/**
 * @author PlayDK
 */
public class PvpAttackInfo {

    public int skillId;
    public int critRate; //爆擊概率
    public int ignoreDef; //無視防禦
    public int skillDamage; //技能攻擊
    public int mobCount; //攻擊角色的數量
    public int attackCount; //攻擊角色的次數
    public int pvpRange; //攻擊的距離
    public boolean facingLeft;
    public double maxDamage;
    public Rectangle box;
}

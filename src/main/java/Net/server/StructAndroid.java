/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PlayDK
 */
public class StructAndroid {

    public final List<Integer> skin = new ArrayList<>(); //皮膚
    public final List<Integer> face = new ArrayList<>(); //臉型
    public final List<Integer> hair = new ArrayList<>(); //髮型
    public int type; //智能機器人的ID類型
    public int gender; //性別
    public int chatBalloon;
    public int nameTag;
    public boolean shopUsable; // 可使用商店
}

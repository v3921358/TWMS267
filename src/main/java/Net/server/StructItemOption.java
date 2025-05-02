/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author PlayDK
 */
public class StructItemOption {

    public static final List<String> types = new LinkedList<>();
    public final Map<String, Integer> data = new HashMap<>();
    public int optionType, reqLevel, opID; // opID = nebulite Id or potential ID
    public String face; // angry, cheers, love, blaze, glitter
    public String opString; //potential string

    public int get(String type) {
        return data.get(type) != null ? data.get(type) : 0;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean info) {
        String ret = opString;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            ret = ret.replace("#" + entry.getKey(), entry.getValue().toString());
        }
        if (info) {
            ret += "(" + opID + ") optionType: " + optionType + " reqLevel:" + reqLevel + " face:" + face;
        }
        return ret;
    }
}

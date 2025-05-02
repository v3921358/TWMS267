/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 設定所有端內的定時任務
 *
 * @author PlayDK
 */
public class PlayMSEvent {

    private static final Logger log = LoggerFactory.getLogger(PlayMSEvent.class);

    public static void start() {
        log.info("所有定時活動已經啟動完畢...");
    }
}

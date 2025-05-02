/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Class to hold information about a HipChat notice to be sent.
 *
 * @author Tyler
 */
public class HipchatSendNoticeData {

    public String content;

    public HipchatSendNoticeData(String message) {
        this.content = message;
    }
}

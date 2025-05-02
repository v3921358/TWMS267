/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * @author William
 */
public interface ApiReturnCode {

    int getValue();

    String getMessage();

    String getType();
}

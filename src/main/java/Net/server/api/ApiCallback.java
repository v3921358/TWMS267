/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Used to make anonymous classes as call backs when the API classes return asynchronously.
 *
 * @author Tyler
 */
public interface ApiCallback {

    void onSuccess();

    void onFail();
}

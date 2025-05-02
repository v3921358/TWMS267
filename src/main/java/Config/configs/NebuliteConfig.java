/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Config.configs;

import tools.config.Property;

public class NebuliteConfig {

    @Property(key = "server.benulite.C", defaultValue = "10")
    public static int benuliteC;

    @Property(key = "server.benulite.B", defaultValue = "10")
    public static int benuliteB;

    @Property(key = "server.benulite.A", defaultValue = "10")
    public static int benuliteA;

    @Property(key = "server.benulite.S", defaultValue = "10")
    public static int benuliteS;
}

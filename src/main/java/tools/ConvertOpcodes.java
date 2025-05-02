package tools;

import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConvertOpcodes {

    public static void main(String[] args) {
        boolean decimal;
        String recvopsName = "InHeader.properties";
        String sendopsName = "OutHeader.properties";
        try {
            decimal = Boolean.parseBoolean(args[0]);
        } catch (Exception e) {
            decimal = true;
        }
        StringBuilder sb = new StringBuilder();
        FileOutputStream out;
        try {
            out = new FileOutputStream(recvopsName, false);
            for (InHeader recv : InHeader.values()) {
                if (recv == InHeader.UNKNOWN) {
                    break;
                }
                sb.append(recv.name()).append(" = ")
                        .append(decimal ? recv.getValue() : HexTool.getOpcodeToString(recv.getValue()))
                        .append("\r\n");
            }
            out.write(sb.toString().getBytes());
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConvertOpcodes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertOpcodes.class.getName()).log(Level.SEVERE, null, ex);
        }
        sb = new StringBuilder();
        try {
            out = new FileOutputStream(sendopsName, false);
            for (OutHeader send : OutHeader.values()) {
                if (send == OutHeader.UNKNOWN) {
                    break;
                }
                sb.append(send.name()).append(" = ")
                        .append(decimal ? send.getValue() : HexTool.getOpcodeToString(send.getValue()))
                        .append("\r\n");
            }
            out.write(sb.toString().getBytes());
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConvertOpcodes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConvertOpcodes.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("匯出完成");
    }
}

/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileoutputUtil {

    // Logging output file
    public static final String Acc_Stuck = "Logs/Log_AccountStuck.rtf",
            Login_Error = "Logs/Log_Login_Error.rtf",
    //Timer_Log = "Log_Timer_Except.rtf",
    //MapTimer_Log = "Log_MapTimer_Except.rtf",
    IP_Log = "Logs/Log_AccountIP.rtf",
    //GMCommand_Log = "Log_GMCommand.rtf",
    Zakum_Log = "Logs/Log_Zakum.rtf",
            Horntail_Log = "Logs/Log_Horntail.rtf",
            Pinkbean_Log = "Logs/Log_Pinkbean.rtf",
            ScriptEx_Log = "Logs/Log_Script_Except.rtf",
    // I cba looking for every error, adding this back in.
    PacketEx_Log = "Logs/Log_Packet_Except.rtf";
    // End
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");

    public static void log(final String file, final String msg) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
            out.write(("\n------------------------ " + CurrentReadable_Time() + " ------------------------\n").getBytes());
            out.write(msg.getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static void outputFileError(final String file, final Throwable t) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
            out.write(("\n------------------------ " + CurrentReadable_Time() + " ------------------------\n").getBytes());
            out.write(getString(t).getBytes());
        } catch (IOException ess) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static String CurrentReadable_Date() {
        return sdf_.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_Time() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String getString(final Throwable e) {
        String retValue = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
            }
        }
        return retValue;
    }

    public static boolean deleteFile(String var0) {
        boolean var1 = false;
        File var2;
        if ((var2 = new File(var0)).isFile() && var2.exists()) {
            var2.delete();
            var1 = true;
        }

        return var1;
    }

    public static void logToFile(String var0, String var1) {
        logToFile(var0, var1, false, true);
    }

    public static void logToFile(String var0, String var1, boolean var2, boolean var3) {
        logToFile(var0, var1, var2, true, var3);
    }

    public static void logToFile(String var0, String var1, boolean var2, boolean var3, boolean var4) {
        String var5 = var1;
        if (!var1.contains("\r\n")) {
            var5 = "\r\n" + var1;
        }

        FileOutputStream var17 = null;

        try {
            File var6 = new File(var0);
            if (var4 && var6.exists() && var6.isFile() && var6.length() >= 1024000L && var3) {
                String var10000 = var0.substring(0, var0.indexOf(47, var0.indexOf("/") + 1) + 1);
                String var19 = var10000 + "old/" + var0.substring(var0.indexOf(47, var0.indexOf("/") + 1) + 1, var0.length() - 4);
                String var18 = new SimpleDateFormat("yyyy年MM月dd日HH時mm分ss秒").format(Calendar.getInstance().getTime());
                String var7 = var0.substring(var0.length() - 4);
                var18 = var19 + "_" + var18 + var7;
                if ((new File(var18)).getParentFile() != null) {
                    (new File(var18)).getParentFile().mkdirs();
                }

                var6.renameTo(new File(var18));
                var6 = new File(var0);
            }

            if (var6.getParentFile() != null) {
                var6.getParentFile().mkdirs();
            }

            if (!(var17 = new FileOutputStream(var0, true)).toString().contains(var5) || !var2) {
                OutputStreamWriter var20 = new OutputStreamWriter(var17, StandardCharsets.UTF_8);
                var20.write(var5);
                var20.flush();
            }

        } catch (IOException var15) {
        } finally {
            try {
                if (var17 != null) {
                    var17.close();
                }
            } catch (IOException var14) {
            }

        }

    }

    public static String NowTime() {
        Date var0 = new Date();
        return (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(var0);
    }

    public static String NowTime2() {
        Date var0 = new Date();
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(var0);
    }
}

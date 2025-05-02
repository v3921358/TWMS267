package tools;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.UUID;
import java.util.prefs.Preferences;

public class IPAddressTool {

    /**
     * 获取CPU序列号
     *
     * @return
     */
    private static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("CPUSerial", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n"
                    + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n"
                    + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            System.out.println(e);
            result = null;
        }
        if (result == null || result.trim().length() < 1) {
            result = null;
        }
        return result.trim();
    }

    /**
     * 获取硬盘序列号
     *
     * @param drive 盘符
     * @return
     */
    private static String getHardDiskSN(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("disksn", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\""
                    + drive
                    + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            result = "";
        }
        return result.trim();
    }

    private static String getUUID() {
        String value = Preferences.userRoot().node("javaplayer").get("EthanMS", null);
        if (value == null || value.isEmpty()) {
            Preferences node = Preferences.userRoot().node("javaplayer");
            String uuid = UUID.randomUUID().toString().toUpperCase();
            node.put("INFO_VALUE", uuid);
            value = uuid;
        }
        return value;
    }

    private static String getProcessorId() {
        String id = "";
        try {
            Process exec = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
            exec.getOutputStream().close();
            final Scanner scanner = new Scanner(exec.getInputStream());
            scanner.next();
            id = scanner.next();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (id.trim().length() <= 0) {
            id = "Empty_ProcessorId";
        }
        return id.trim();
    }

    public static String getkey() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            throw new IllegalAccessError("This program can only run on Windows system.");
        } else {
            String cpu = getCPUSerial();
            String diskSN = getHardDiskSN("c");
            String pid = getProcessorId();
//            String mac = getMacAddress(false);
            //int num = cpu.length();
            //result += cpu.substring(num - 8, num);
            String result = new DesCryptor(getUUID()).encrypt((cpu + diskSN + pid).toUpperCase()).toUpperCase();
            //            if (mac != null) {
//                result += mac;
//            }
            String md5 = encryptToMD5(result);
            if (md5 == null) throw new RuntimeException("MD5 Error");
            return md5;
        }
    }

    /**
     * 进行MD5加密
     *
     * @param info 要加密的信息
     * @return String 加密后的字符串
     */
    private static String encryptToMD5(String info) {
        try {
            byte[] md5Data = MessageDigest.getInstance("MD5").digest(info.getBytes());
            return HexTool.toString(md5Data).toUpperCase().replaceAll(" ", "");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getkey());
    }

    /*
     * 取机器的mac地址和本机IP地址
     * True = 取IP地址
     */
    public static String getMacAddress(boolean ipAddress) {
        String macs = null;
        String localip = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress inetAddress;
            boolean finded = false; // 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    inetAddress = address.nextElement();
                    String ip = inetAddress.getHostAddress();
                    if (ip.contains(":") || ip.startsWith("221.231.") || ip.equalsIgnoreCase("127.0.0.1")) {
                        continue;
                    }
                    //System.out.println(ni.getName() + " - " + inetAddress.getHostAddress() + " - " + inetAddress.isSiteLocalAddress() + " - " + !inetAddress.isLoopbackAddress());
                    if (!inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) { //外网
                        localip = inetAddress.getHostAddress();
                        byte[] mac = ni.getHardwareAddress();
                        if (mac == null) {
                            continue;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            if (i != 0) {
                                sb.append("-");
                            }
                            String str = Integer.toHexString(mac[i] & 0xFF);
                            sb.append(str.length() == 1 ? 0 + str : str);
                        }
                        macs = sb.toString().toUpperCase();
                        //System.out.println("外网 - localip: " + localip);
                        //System.out.println("外网 - macs: " + macs);
                        finded = true;
                        break;
                    } else if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) { //内网
                        localip = inetAddress.getHostAddress();
                        byte[] mac = ni.getHardwareAddress();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            if (i != 0) {
                                sb.append("-");
                            }
                            String str = Integer.toHexString(mac[i] & 0xFF);
                            sb.append(str.length() == 1 ? 0 + str : str);
                        }
                        macs = sb.toString().toUpperCase();
                        //System.out.println("内网 - localip: " + localip);
                        //System.out.println("内网 - macs: " + macs);
                        finded = true;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress ? localip : macs;
    }
}

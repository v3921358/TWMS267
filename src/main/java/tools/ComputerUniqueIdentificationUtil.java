package tools;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * ComputerUniqueIdentificationUtil<br>
 * 计算机唯一标识工具
 * </p>
 *
 * @author Heping_Ge2333
 * @version 2.0
 * @since 2022/12/23
 */

@Slf4j
public class ComputerUniqueIdentificationUtil {

//    private static final Logger log = LoggerFactory.getLogger(ComputerUniqueIdentificationUtil.class);
    /**
     * Windows OS Identification
     */
    public static final String WINDOWS = "WINDOWS";
    /**
     * Linux OS Identification
     */
    public static final String LINUX = "LINUX";
    /**
     * Unix OS Identification
     */
    public static final String UNIX = "UNIX";
    /**
     * 正则表达式
     */
    public static final String REGEX = "\\b\\w+:\\w+:\\w+:\\w+:\\w+:\\w+\\b";
    private static final String WINDOWS_MOTHERBOARD_INFO_ERROR_MSG = "获取 Windows 主板信息错误";
    private static final String WINDOWS_MAC_ADDRESS_ERROR_MSG = "获取 Windows MAC 信息错误";
    private static final String WINDOWS_CPU_INFO_ERROR_MSG = "获取 Windows CPU 信息错误";
    private static final String LINUX_MOTHERBOARD_INFO_ERROR_MSG = "获取 Linux 主板信息错误";
    private static final String LINUX_MAC_ADDRESS_ERROR_MSG = "获取 Linux MAC 信息错误";
    private static final String LINUX_CPU_INFO_ERROR_MSG = "获取 Linux CPU 信息错误";
    private static final String DELETE_FILE_ERROR_MSG = "删除文件时发生了错误";

    private ComputerUniqueIdentificationUtil() {
    }

    @Getter
    private static String HashCache = "";

    /**
     * 在 Windows 环境下执行一个 vbs 脚本， 并返回运行结果
     *
     * @param result 用于存储返回结果的 StringBuilder
     * @param file   存储 vbs 代码的文件
     * @param fw     存储 vbs 代码文件的输入流
     * @param vbs    要执行的代码
     * @throws IOException 写入 vbs 脚本至文件 或 读取结果时发生 IO异常
     * @author Heping_Ge2333
     */
    private static void loadVBS(StringBuilder result, File file, FileWriter fw, String vbs) throws IOException {
        fw.write(vbs);
        fw.close();
        Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            result.append(line);
        }
        input.close();
    }

    /**
     * 获取 Windows 主板序列号
     *
     * @return String - 计算机主板序列号
     * @author Heping_Ge2333
     */
    private static String getWindowsMotherboardSerialNumber() {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = """
                    Set objWMIService = GetObject("winmgmts:\\\\.\\root\\cimv2")
                    Set colItems = objWMIService.ExecQuery _\s
                       ("Select * from Win32_BaseBoard")\s
                    For Each objItem in colItems\s
                        Wscript.Echo objItem.SerialNumber\s
                        exit for  ' do the first cpu only!\s
                    Next\s
                    """;

            loadVBS(result, file, fw, vbs);
        } catch (Exception e) {
            log.error(WINDOWS_MOTHERBOARD_INFO_ERROR_MSG, e);
        }
        return result.toString().trim();
    }

    /**
     * 获取 Linux 主板序列号
     *
     * @return String - 计算机主板序列号
     * @author Heping_Ge2333
     */
    private static String getLinuxMotherboardSerialNumber() {
        String result = CommonConstants.EMPTY_STR;
        String motherboardCmd = "dmidecode | grep 'Serial Number' | awk '{print $3}' | tail -1";
        Process p;
        try {
            // 管道
            p = Runtime.getRuntime().exec(new String[]{"sh", "-c", motherboardCmd});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            result = br.readLine();
            br.close();
        } catch (IOException e) {
            log.error(LINUX_MOTHERBOARD_INFO_ERROR_MSG, e);
        }
        return result;
    }

    /**
     * 从字节获取 MAC 地址
     *
     * @param bytes - 字节
     * @return String - MAC
     * @author Heping_Ge2333
     */
    private static String getMacAddressFromBytes(byte[] bytes) {
        if(bytes == null){
            return "";
        }
        StringBuilder mac = new StringBuilder();
        byte currentByte;
        boolean first = false;
        for (byte b : bytes) {
            if (first) {
                mac.append("-");
            }
            currentByte = (byte) ((b & 240) >> 4);
            mac.append(String.format("%02X", currentByte));
            currentByte = (byte) (b & 15);
            mac.append(String.format("%02X", currentByte));
            first = true;
        }
        return mac.toString().toUpperCase();
    }

    /**
     * 获取 Windows 网卡的 MAC 地址
     *
     * @return String - MAC 地址
     * @author Heping_Ge2333
     */
    private static String getWindowsMACAddress() {
        InetAddress ip;
        NetworkInterface ni;
        List<String> macList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                ni = netInterfaces.nextElement();
                //  遍历所有 IP 特定情况，可以考虑用 ni.getName() 判断
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = ips.nextElement();
                    // 非127.0.0.1
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                        macList.add(getMacAddressFromBytes(ni.getHardwareAddress()));
                    }
                }
            }
        } catch (Exception e) {
            log.error(WINDOWS_MAC_ADDRESS_ERROR_MSG, e);
        }
        if (!macList.isEmpty()) {
            return macList.get(0);
        } else {
            return "";
        }
    }

    /**
     * 获取 Linux 网卡的 MAC 地址 （如果 Linux 下有 eth0 这个网卡）
     *
     * @return String - MAC 地址
     * @author Heping_Ge2333
     */
    private static String getLinuxMACAddressForEth0() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process;
        try {
            // Linux下的命令，一般取eth0作为本地主网卡
            process = Runtime.getRuntime().exec("ipconfig eth0");
            // 显示信息中包含有 MAC 地址信息
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int index;
            while ((line = bufferedReader.readLine()) != null) {
                // 寻找标示字符串[hwaddr]
                index = line.toLowerCase().indexOf("hwaddr");
                if (index >= 0) {
                    // // 找到并取出 MAC 地址并去除2边空格
                    mac = line.substring(index + "hwaddr".length() + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            log.error(LINUX_MAC_ADDRESS_ERROR_MSG, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e1) {
                log.error(LINUX_MAC_ADDRESS_ERROR_MSG, e1);
            }
        }
        return mac;
    }

    /**
     * 获取 Linux 网卡的 MAC 地址
     *
     * @return String - MAC 地址
     * @author Heping_Ge2333
     */
    private static String getLinuxMACAddress() {
        String mac = null;
        BufferedReader bufferedReader = null;
        Process process;
        try {
            // Linux下的命令 显示或设置网络设备
            process = Runtime.getRuntime().exec("ipconfig");
            // 显示信息中包含有 MAC 地址信息
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Pattern pat = Pattern.compile(REGEX);
                Matcher mat = pat.matcher(line);
                if (mat.find()) {
                    mac = mat.group(0);
                }
            }
        } catch (IOException e) {
            log.error(LINUX_MAC_ADDRESS_ERROR_MSG, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e1) {
                log.error(LINUX_MAC_ADDRESS_ERROR_MSG, e1);
            }
        }
        return mac;
    }

    /**
     * 获取 Windows 的 CPU 序列号
     *
     * @return String - CPU 序列号
     * @author Heping_Ge2333
     */
    private static String getWindowsProcessorSerialNumber() {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = """
                    Set objWMIService = GetObject("winmgmts:\\\\.\\root\\cimv2")
                    Set colItems = objWMIService.ExecQuery _\s
                       ("Select * from Win32_Processor")\s
                    For Each objItem in colItems\s
                        Wscript.Echo objItem.ProcessorId\s
                        exit for  ' do the first cpu only!\s
                    Next\s
                    """;

            loadVBS(result, file, fw, vbs);
            if (!file.delete()) {
                log.error(DELETE_FILE_ERROR_MSG);
            }
        } catch (Exception e) {
            log.error(WINDOWS_CPU_INFO_ERROR_MSG, e);
        }
        return result.toString().trim();
    }

    /**
     * 获取 Linux 的 CPU 序列号
     *
     * @return String - CPU 序列号
     * @author Heping_Ge2333
     */
    private static String getLinuxProcessorSerialNumber() {
        String result = "";
        String CPU_ID_CMD = "dmidecode";
        BufferedReader bufferedReader;
        Process p;
        try {
            // 管道
            p = Runtime.getRuntime().exec(new String[]{"sh", "-c", CPU_ID_CMD});
            bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int index;
            while ((line = bufferedReader.readLine()) != null) {
                index = line.toLowerCase().indexOf("uuid");
                if (index >= 0) {
                    result = line.substring(index + "uuid".length() + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            log.error(LINUX_CPU_INFO_ERROR_MSG, e);
        }
        return result.trim();
    }

    /**
     * 获取当前计算机操作系统名称 例如:Windows,Linux,Unix等.
     *
     * @return String - 计算机操作系统名称
     * @author Heping_Ge2333
     */
    public static String getOSName() {
        return System.getProperty("os.name").toUpperCase();
    }

    /**
     * 获取当前计算机操作系统名称前缀 例如:Windows, Linux, Unix等.
     *
     * @return String - 计算机操作系统名称
     * @author Heping_Ge2333
     */
    public static String getOSNamePrefix() {
        String name = getOSName();
        if (name.startsWith(WINDOWS)) {
            return WINDOWS;
        } else if (name.startsWith(LINUX)) {
            return LINUX;
        } else if (name.startsWith(UNIX)) {
            return UNIX;
        } else {
            return CommonConstants.EMPTY_STR;
        }
    }

    /**
     * 获取当前计算机主板序列号
     *
     * @return String - 计算机主板序列号
     * @author Heping_Ge2333
     */
    public static String getMotherBoardSerialNumber() {
        return switch (getOSNamePrefix()) {
            case WINDOWS -> getWindowsMotherboardSerialNumber();
            case LINUX -> getLinuxMotherboardSerialNumber();
            default -> CommonConstants.EMPTY_STR;
        };
    }

    /**
     * 获取当前计算机网卡的 MAC 地址
     *
     * @return String - 网卡的 MAC 地址
     * @author Heping_Ge2333
     */
    public static String getMACAddress() {
        switch (getOSNamePrefix()) {
            case WINDOWS -> {
                return getWindowsMACAddress();
            }
            case LINUX -> {
                String macAddressForEth0 = getLinuxMACAddressForEth0();
                if (StringUtil.isEmpty(macAddressForEth0)) {
                    macAddressForEth0 = getLinuxMACAddress();
                }
                return macAddressForEth0;
            }
            default -> {
                return CommonConstants.EMPTY_STR;
            }
        }
    }

    /**
     * 获取当前计算机的 CPU 序列号
     *
     * @return String - CPU 序列号
     * @author Heping_Ge2333
     */
    public static String getCPUSerialNumber() {
        return switch (getOSNamePrefix()) {
            case WINDOWS -> getWindowsProcessorSerialNumber();
            case LINUX -> getLinuxProcessorSerialNumber();
            default -> CommonConstants.EMPTY_STR;
        };
    }

    /**
     * 获取计算机唯一标识
     *
     * @return ComputerUniqueIdentification - 计算机唯一标识
     * @author Heping_Ge2333
     */
    public static ComputerUniqueIdentification getComputerUniqueIdentification() {
        return new ComputerUniqueIdentification(getOSNamePrefix(), getMotherBoardSerialNumber(), getMACAddress(), getCPUSerialNumber());
    }

    /**
     * 获取计算机唯一标识的 json 格式文本
     *
     * @return String - 计算机唯一标识
     * @author Heping_Ge2333
     */
    public static String getComputerUniqueIdentificationString() {
        return getComputerUniqueIdentification().toString();
    }

    /**
     * 获取计算机唯一标识的SHA-512哈希值
     *
     * @return 计算机唯一标识的SHA-512哈希值
     */
    public static String getComputerUniqueIdentificationHash() {
        HashCache = HashUtil.sha512(getComputerUniqueIdentification());
        return HashCache;
    }

    /**
     * <p>
     * ComputerUniqueIdentification<br>
     * 计算机唯一标识
     * </p>
     *
     * @author Heping_Ge2333
     * @version 2.0
     * @since 2022/12/13
     */
    @Data
    private static class ComputerUniqueIdentification {
        private final String osNamePrefix;
        private final String motherboardSerialNumber;
        private final String macAddress;
        private final String cpuSerialNumber;

        public ComputerUniqueIdentification(String osNamePrefix, String motherboardSerialNumber, String macAddress, String cpuSerialNumber) {
            this.osNamePrefix = osNamePrefix;
            this.motherboardSerialNumber = motherboardSerialNumber;
            this.macAddress = macAddress;
            this.cpuSerialNumber = cpuSerialNumber;
        }

        /**
         * 将计算机唯一标识转化为 json 并返回
         *
         * @return 转化后的结果
         */
        @Override
        public String toString() {
            return '{' +
                    "\n\t\"osNamePrefix\": \"" + osNamePrefix + "\"," +
                    "\n\t\"motherboardSerialNumber\": \"" + motherboardSerialNumber + "\"," +
                    "\n\t\"macAddress\": \"" + macAddress + "\"," +
                    "\n\t\"cpuSerialNumber\": \"" + cpuSerialNumber + "\"" +
                    "\n}";
        }
    }

}

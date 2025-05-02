package Net;

import tools.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PacketUnpackerGUI {
    private static boolean isSwordieFormat = false;
    private static String functionName = "";
    private static String opcodeName = "";

    public static void main(String[] args) {
        // 使用 SwingUtilities.invokeLater 確保 GUI 在事件處理緒上初始化
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("[ TMS - PhantomTMS ] 解包工具 - By Hertz");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null); // 畫面置中

            // ===== 上方功能選項面板 =====
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel functionLabel = new JLabel("FunctionName:");
            JTextField functionField = new JTextField(functionName, 15);
            JLabel opcodeLabel = new JLabel("OutHeaderName:");
            JTextField opcodeField = new JTextField(opcodeName, 15);
            JButton formatButton = new JButton("切換格式 (Mode:OD)");
            formatButton.addActionListener(e -> {
                isSwordieFormat = !isSwordieFormat;
                formatButton.setText(isSwordieFormat ? "切換格式 (Mode:Swordie)" : "切換格式 (Mode:OD)");
            });
            topPanel.add(functionLabel);
            topPanel.add(functionField);
            topPanel.add(opcodeLabel);
            topPanel.add(opcodeField);
            topPanel.add(formatButton);

            // ===== 左側輸入面板 =====
            JPanel inputPanel = new JPanel(new BorderLayout());
            JTextArea inputTextArea = new JTextArea();
            inputTextArea.setLineWrap(true);
            inputTextArea.setWrapStyleWord(true);
            JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
            inputScrollPane.setBorder(BorderFactory.createTitledBorder("請輸入封包數據 (例如: 00 00 00 00)"));
            inputPanel.add(inputScrollPane, BorderLayout.CENTER);

            // ===== 右側分頁式輸出面板 =====
            JTabbedPane outputTabbedPane = new JTabbedPane();

            // [1] 生成的代碼
            JTextArea codeTextArea = new JTextArea();
            codeTextArea.setEditable(false);
            codeTextArea.setLineWrap(true);
            codeTextArea.setWrapStyleWord(true);
            JScrollPane codeScrollPane = new JScrollPane(codeTextArea);
            codeScrollPane.setBorder(BorderFactory.createTitledBorder("生成的代碼"));
            outputTabbedPane.addTab("代碼輸出", codeScrollPane);

            // [2] 封包分析結果
            JTextArea analysisTextArea = new JTextArea();
            analysisTextArea.setEditable(false);
            analysisTextArea.setLineWrap(true);
            analysisTextArea.setWrapStyleWord(true);
            JScrollPane analysisScrollPane = new JScrollPane(analysisTextArea);
            analysisScrollPane.setBorder(BorderFactory.createTitledBorder("封包分析"));
            outputTabbedPane.addTab("封包分析", analysisScrollPane);

            // [3] 搜尋（用來搜尋生成的代碼）
            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
            JPanel searchFieldPanel = new JPanel(new BorderLayout());
            JLabel searchLabel = new JLabel("搜尋:");
            JTextField searchField = new JTextField();
            searchFieldPanel.add(searchLabel, BorderLayout.WEST);
            searchFieldPanel.add(searchField, BorderLayout.CENTER);
            searchPanel.add(searchFieldPanel);
            JTextArea searchResultsArea = new JTextArea();
            searchResultsArea.setEditable(false);
            searchResultsArea.setLineWrap(true);
            searchResultsArea.setWrapStyleWord(true);
            JScrollPane searchScrollPane = new JScrollPane(searchResultsArea);
            searchScrollPane.setBorder(BorderFactory.createTitledBorder("搜尋結果"));
            searchPanel.add(searchScrollPane);
            outputTabbedPane.addTab("搜尋", searchPanel);

            // 加入搜尋邏輯：搜尋代碼區中包含關鍵字的行並顯示行號
            searchField.addActionListener((ActionEvent e) -> {
                String query = searchField.getText().trim();
                if (query.isEmpty()) {
                    searchResultsArea.setText("請輸入搜尋關鍵字。");
                    return;
                }
                String[] lines = codeTextArea.getText().split("\n");
                StringBuilder results = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains(query)) {
                        results.append("行 ").append(i + 1).append(": ").append(lines[i]).append("\n");
                    }
                }
                if (results.length() == 0) {
                    searchResultsArea.setText("沒有找到匹配的內容。");
                } else {
                    searchResultsArea.setText(results.toString());
                }
            });

            // ===== 使用分割面板放入左側輸入與右側輸出 =====
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputTabbedPane);
            splitPane.setResizeWeight(0.4);

            // ===== 下方按鈕面板 =====
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton generateButton = new JButton("生成代碼");
            JButton analyzeButton = new JButton("分析封包");
            bottomPanel.add(generateButton);
            bottomPanel.add(analyzeButton);

            // ===== 生成代碼按鈕動作 =====
            generateButton.addActionListener(e -> {
                String inputText = inputTextArea.getText().trim();
                if (inputText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "請輸入封包數據，例如: 00 00 00 00", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    // 將使用空白（含換行）區隔的十六進制字串轉為 byte 陣列
                    String[] byteStrings = inputText.split("\\s+");
                    byte[] packet = new byte[byteStrings.length];
                    for (int i = 0; i < byteStrings.length; i++) {
                        packet[i] = (byte) Integer.parseInt(byteStrings[i], 16);
                    }
                    functionName = functionField.getText().trim();
                    opcodeName = opcodeField.getText().trim();
                    String generatedCode = generateCode(packet);
                    int packetSize = packet.length;
                    String header;
                    if (isSwordieFormat) {
                        header = String.format("public static OutPacket _(MapleClient c) {\n    OutPacket outPacket = new OutPacket(OutHeader.LTX_%s);\n\n",
                                DateUtil.getCurrentDate(), functionName, opcodeName);
                        generatedCode += "\n    c.write(outPacket);";
                    } else {
                        header = String.format("public static byte[] _(MapleClient c) {\n    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();\n    mplew.writeShort(OutHeader.CTX_%s.getValue());\n\n",
                                DateUtil.getCurrentDate(), functionName, opcodeName);
                        generatedCode += "\n    return mplew.getPacket();";
                    }
                    String fullCode = header + generatedCode + String.format("\n", packetSize);
                    codeTextArea.setText(fullCode);
                    outputTabbedPane.setSelectedComponent(codeScrollPane);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "無效的輸入格式，請確保使用十六進制數字（例如：00 00 00 00）", "格式錯誤", JOptionPane.ERROR_MESSAGE);
                }
            });

            // ===== 分析封包按鈕動作 =====
            analyzeButton.addActionListener(e -> {
                String inputText = inputTextArea.getText().trim();
                if (inputText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "請輸入封包數據，例如: 00 00 00 00", "輸入錯誤", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    // 以一個或多個空白字元分隔輸入字串，得到每個十六進制數字的字串陣列
                    String[] byteStrings = inputText.split("\\s+");
                    // 建立與字串陣列長度相同的 byte 陣列
                    byte[] packet = new byte[byteStrings.length];
                    // 逐一將每個十六進制字串轉成整數，再轉為 byte
                    for (int i = 0; i < byteStrings.length; i++) {
                        // Integer.parseInt(字串, 16) 將十六進制字串轉換成整數
                        // 再以 (byte) 強制轉型存入 packet 陣列
                        packet[i] = (byte) Integer.parseInt(byteStrings[i], 16);
                    }
                    // 此時 packet 陣列中即包含了解析後的封包數據
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "無效的輸入格式，請確保使用正確的十六進制數字（例如：00 00 00 00）", "格式錯誤", JOptionPane.ERROR_MESSAGE);
                }
            });

            // ===== 主畫面配置 =====
            frame.setLayout(new BorderLayout());
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(splitPane, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        });
    }

    /**
     * 依照封包內容產生對應的寫入程式碼。
     * 為了避免解包錯誤，當剩餘數據不足 4 個 byte 時，
     * 將根據剩餘數量分別採用 short/byte 處理。
     */
    private static String generateCode(byte[] packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
        StringBuilder code = new StringBuilder();
        int index = 0;

        while (buffer.hasRemaining()) {
            int remaining = buffer.remaining();
            if (remaining >= 4) {
                int value = buffer.getInt();
                code.append(generateCodeLine(isSwordieFormat, "Int", value, index));
                index += 4;
            } else if (remaining == 3) {
                // 當剩 3 個 byte 時：先讀取 2 個 byte 為 short，再讀取 1 個 byte
                short value = buffer.getShort();
                code.append(generateCodeLine(isSwordieFormat, "Short", value, index));
                index += 2;
                byte valueByte = buffer.get();
                code.append(generateCodeLine(isSwordieFormat, "byte", valueByte, index));
                index += 1;
            } else if (remaining == 2) {
                short value = buffer.getShort();
                code.append(generateCodeLine(isSwordieFormat, "Short", value, index));
                index += 2;
            } else if (remaining == 1) {
                byte value = buffer.get();
                code.append(generateCodeLine(isSwordieFormat, "byte", value, index));
                index++;
            }
        }
        return code.toString();
    }

    /**
     * 根據解包型態與數值，產生對應的程式碼行（並附上位移註解）
     */
    private static String generateCodeLine(boolean isSwordieFormat, String type, int value, int index) {
        String line;
        if (isSwordieFormat) {
            switch (type) {
                case "Int":
                    line = String.format("    outPacket.encodeInt(%d); ", value, index);
                    break;
                case "Short":
                    line = String.format("    outPacket.encodeShort(%d); ", value, index);
                    break;
                case "byte":
                    line = String.format("    outPacket.encodeByte(%d); ", value, index);
                    break;
                default:
                    line = String.format("    outPacket.encode(%d); ", value, index);
                    break;
            }
        } else {
            switch (type) {
                case "Int":
                    line = String.format("    mplew.writeInt(%d); ", value, index);
                    break;
                case "Short":
                    line = String.format("    mplew.writeShort(%d); ", value, index);
                    break;
                case "byte":
                    line = String.format("    mplew.write(%d); ", value, index);
                    break;
                default:
                    line = String.format("    mplew.write(%d); ", value, index);
                    break;
            }
        }
        return line + "\n";
    }

    /**
     * 分析封包內各 byte 資料，並嘗試判斷連續 0 字節可能代表的資料長度（例如：2 個 0 可能是 Short、4 個則可能為 Int）
     */
    private static String analyzePacket(byte[] packet) {
        if (packet == null || packet.length == 0) {
            return "無效的封包資料";
        }

        // 若封包過大則僅分析前 1000 個 byte
        int maxBytesToAnalyze = 1000;
        byte[] packetToAnalyze = packet.length > maxBytesToAnalyze ? Arrays.copyOf(packet, maxBytesToAnalyze) : packet;

        StringBuilder analysis = new StringBuilder();
        analysis.append("封包分析結果:\n");
        for (int i = 0; i < packetToAnalyze.length; i++) {
            analysis.append(String.format("偏移量 %02X - 字節值: %02X", i, packetToAnalyze[i]));
            // 若遇 0 字節，檢查連續 0 的數量
            if (packetToAnalyze[i] == 0) {
                int zeroCount = 1;
                int j = i + 1;
                while (j < packetToAnalyze.length && packetToAnalyze[j] == 0) {
                    zeroCount++;
                    j++;
                }
                if (zeroCount == 2) {
                    analysis.append("  -> 可能為 Short (2 字節)");
                } else if (zeroCount == 4) {
                    analysis.append("  -> 可能為 Int (4 字節)");
                } else if (zeroCount == 6 || zeroCount == 8) {
                    analysis.append(String.format("  -> 可能為 Long (%d 字節)", zeroCount));
                } else if (zeroCount > 1) {
                    analysis.append(String.format("  -> 連續 0 字節數: %d", zeroCount));
                }
                i = j - 1; // 跳過已分析的 0 字節
            }
            analysis.append("\n");
        }
        return analysis.toString();
    }
}

package Net.tools;

import Config.constants.ServerConstants;
import tools.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketGUI {
    private static boolean isSwordieFormat = false;
    private static String functionName = "";
    private static String opcodeName = "";

    public static void main(String[] args) {
        JFrame frame = new JFrame("[ TMS - twms ] Packet Tool - By Hertz");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 400);
        frame.setLayout(new BorderLayout());

        // Input Area
        JTextArea inputTextArea = new JTextArea();
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("Input Area:"));

        // Output Area
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Output Area:"));

        // Function Name Area
        JPanel functionPanel = new JPanel(new FlowLayout());
        JTextField functionField = new JTextField(functionName, 15);
        functionPanel.add(new JLabel("Function Name:"));
        functionPanel.add(functionField);

        // Opcode Name Area
        JPanel opcodePanel = new JPanel(new FlowLayout());
        JTextField opcodeField = new JTextField(opcodeName, 15);
        opcodePanel.add(new JLabel("Opcode Name:"));
        opcodePanel.add(opcodeField);

        // Button Area
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("Run Code (OD Mode)");
        JButton analyzeButton = new JButton("Analyze");
        JButton formatButton = new JButton("Switch Format");

        generateButton.addActionListener(e -> {
            String inputText = inputTextArea.getText().trim();
            if (!inputText.isEmpty()) {
                try {
                    byte[] outPacket = parseInput(inputText);
                    functionName = functionField.getText();
                    opcodeName = opcodeField.getText();
                    String generatedCode = generateCode(outPacket);
                    int packetSize = outPacket.length;

                    if (isSwordieFormat) {
                        outputTextArea.setText(String.format(
                                "/* @return: packetSize: %d */\n" +
                                        "/* @ver_%s Packet tools by Hertz - Date:%s */\n" +
                                        "public static OutPacket %s(MapleClient c) {\n" +
                                        "    OutPacket outPacket = new OutPacket(%s);\n" +
                                        "    %s\n" +
                                        "    return outPacket;\n}",
                                packetSize, ServerConstants.MapleMajor, DateUtil.getCurrentDate(), functionName, opcodeName, generatedCode));
                    } else {
                        outputTextArea.setText(String.format(
                                "/* @return: packetSize: %d */\n" +
                                        "/* @ver_%s Packet tools by Hertz - Date:%s */\n" +
                                        "public static byte[] %s(MapleClient c) {\n" +
                                        "    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();\n" +
                                        "    mplew.writeShort(%s);\n" +
                                        "    %s\n" +
                                        "    return mplew.getPacket();\n}",
                                packetSize, ServerConstants.MapleMajor, DateUtil.getCurrentDate(), functionName, opcodeName, generatedCode));
                    }
                    outputTextArea.append(String.format("\n/* Size: %d */", packetSize));
                } catch (NumberFormatException ex) {
                    outputTextArea.setText("Invalid input format.");
                }
            } else {
                outputTextArea.setText("Please enter packet data, e.g., 00 00 00 00.");
            }
        });

        analyzeButton.addActionListener(e -> {
            String inputText = inputTextArea.getText().trim();
            if (!inputText.isEmpty()) {
                try {
                    byte[] outPacket = parseInput(inputText);
                    outputTextArea.setText(analyzePacket(outPacket));
                } catch (NumberFormatException ex) {
                    outputTextArea.setText("Invalid input format.");
                }
            } else {
                outputTextArea.setText("Please enter packet data, e.g., 00 00 00 00.");
            }
        });

        formatButton.addActionListener(e -> {
            isSwordieFormat = !isSwordieFormat;
            generateButton.setText(isSwordieFormat ? "Run Code (Swordie Mode)" : "Run Code (OD Mode)");
        });

        // Layout
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JSplitPane(JSplitPane.VERTICAL_SPLIT, opcodePanel, functionPanel), new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScrollPane, outputScrollPane));
        mainPane.setResizeWeight(0.2);

        frame.add(mainPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(generateButton);
        buttonPanel.add(analyzeButton);
        buttonPanel.add(formatButton);

        frame.setVisible(true);
    }

    private static byte[] parseInput(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }
        if (inputText.startsWith("\"")) {
            return inputText.substring(1, inputText.length() - 1).getBytes(StandardCharsets.UTF_8);
        } else {
            try {
                String[] byteStrings = inputText.split("\\s+");
                byte[] outPacket = new byte[byteStrings.length];
                for (int i = 0; i < byteStrings.length; i++) {
                    outPacket[i] = (byte) Integer.parseInt(byteStrings[i], 16);
                }
                return outPacket;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid hexadecimal input. Ensure it's space-separated, e.g., '00 01 FF'.");
            }
        }
    }

    private static String generateCode(byte[] outPacket) {
        ByteBuffer buffer = ByteBuffer.wrap(outPacket).order(ByteOrder.LITTLE_ENDIAN);
        StringBuilder code = new StringBuilder();
        int index = 0;

        while (buffer.hasRemaining()) {
            int remaining = buffer.remaining();
            if (index == 0 && remaining >= 4) {
                int header = buffer.getInt();
                code.append(generateCodeLine("Int", header));
                index += 4;
            } else {
                if (remaining >= 4) {
                    int value = buffer.getInt();
                    code.append(generateCodeLine("Int", value));
                    index += 4;
                } else if (remaining >= 2) {
                    short value = buffer.getShort();
                    code.append(generateCodeLine("Short", value));
                    index += 2;
                } else if (remaining >= 1) {
                    byte value = buffer.get();
                    code.append(generateCodeLine("Byte", value));
                    index++;
                }
            }
        }
        return code.toString();
    }

    private static String generateCodeLine(String type, int value) {
        return isSwordieFormat ? String.format("outPacket.encode%s(%d);\n", type, value) : String.format("mplew.write%s(%d);\n", type.equals("Int") ? type : "", value);
    }

    private static String analyzePacket(byte[] outPacket) {
        if (outPacket == null || outPacket.length == 0) {
            return "Invalid packet data";
        }

        StringBuilder analysis = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.wrap(outPacket).order(ByteOrder.LITTLE_ENDIAN);
        int offset = 0;

        while (buffer.hasRemaining()) {
            byte currentByte = buffer.get();
            analysis.append(String.format("Offset: %02X - Byte Value: %02X\n", offset, currentByte));
            offset++;

            if (currentByte == 0) {
                int zeroCount = 1;
                while (buffer.hasRemaining() && buffer.get() == 0) {
                    zeroCount++;
                    offset++;
                }
                buffer.position(buffer.position() - 1);
                offset--;

                switch (zeroCount) {
                    case 2 -> analysis.append(" - Possible Short (2 bytes) data\n");
                    case 4 -> analysis.append(" - Possible Int (4 bytes) data\n");
                    case 6 -> analysis.append(" - Possible Long (6 bytes) data\n");
                    case 8 -> analysis.append(" - Possible Long (8 bytes) data\n");
                    default -> analysis.append(String.format(" - Possible %d bytes of data\n", zeroCount));
                }
            }
        }
        return analysis.toString();
    }
}

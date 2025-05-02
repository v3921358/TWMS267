/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import Config.constants.ServerConstants;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProvider;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Pungin
 */
public class CheckCodeImageCreator {

    public static Color getRandColor(int start, int end) {
        Random random = new Random();
        if (start > 255) {
            start = 255;
        }
        if (end > 255) {
            end = 255;
        }
        int randNum;
        if (start > end) {
            randNum = start - end;
        } else {
            randNum = end - start;
        }
        int r = start + random.nextInt(randNum);
        int g = start + random.nextInt(randNum);
        int b = start + random.nextInt(randNum);
        return new Color(r, g, b);
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D;
    }

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    public static String getRandWord(int length, int width, int height, Graphics2D g, ServerConstants.MapleType type) {
        Random random = new Random();
        String finalWord = "", firstWord = "";

        MapleData data;
        MapleDataProvider dataProvider = MapleDataProviderFactory.getString();
        data = dataProvider.getData("Mob.img");
        List<String> mobNameList = new LinkedList<>();
        for (MapleData mobIdData : data.getChildren()) {
            mobNameList.add(MapleDataTool.getString(mobIdData.getChildByPath("name"), ""));
        }
        for (int i = 0; i < length; i++) {
            firstWord = "";
            switch (type) {
                case 한국:
                case 日本:
                case 中国:
                case 台灣:
                    while (firstWord.isEmpty()) {
                        String name = mobNameList.get(random.nextInt(mobNameList.size()));
                        name = name.replaceAll("[a-zA-Z]", "");
                        name = name.replaceAll("[^\\pL]", "");
                        if (type != ServerConstants.MapleType.한국 && name.matches("[\uAC00-\uD7A3]+")) {
                            continue;
                        }
                        if ((type == ServerConstants.MapleType.中国 || type == ServerConstants.MapleType.台灣) && !isChinese(name)) {
                            continue;
                        }
                        if (!name.isEmpty()) {
                            int nameX = random.nextInt(name.length());
                            firstWord = name.substring(nameX, nameX + 1);
                            break;
                        }
                    }
                    break;
                default:
                    int temp = random.nextInt(2) == 0 ? 0x41 : 0x61;
                    firstWord = String.valueOf((char) (random.nextInt(0x1A) + temp));
                    break;
            }
            finalWord += firstWord;
            coloredAndRotation(firstWord, i, g, length, width, height);
        }

        return finalWord;
    }

    public static void coloredAndRotation(String word, int i, Graphics2D g2d, int length, int width, int height) {
        Random random = new Random();
        g2d.setColor(getRandColor(5, 100));

        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D rc = fm.getStringBounds(word, g2d);

        int x = width / length / 2 + width * i / length;
        int y = height / 2 + 10 - random.nextInt(11);

        int rRand = random.nextInt(30);
        AffineTransform trans = new AffineTransform();
        trans.rotate((-15.0 + rRand) * Math.PI / 180.0, x, y);

        float scaleSize = random.nextFloat() + 0.8F;
        if (scaleSize > 1F) {
            scaleSize = 1F;
        }
        trans.scale(scaleSize, scaleSize);

        g2d.setTransform(trans);

        for (int j = 1; j >= 0; j--) {
            if (j == 1) {
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.5F));
            } else {
                g2d.setComposite(AlphaComposite.SrcOver.derive(1.0F));
            }
            if (random.nextInt(2) == 0) {
                AttributedString as = new AttributedString(word);
                as.addAttribute(TextAttribute.FONT, g2d.getFont());
                as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, 1);
                g2d.drawString(as.getIterator(), x - (int) (rc.getCenterX()), y + (fm.getHeight() - fm.getDescent()) / 2 * j + random.nextInt(fm.getDescent() / 2));
            } else {
                g2d.drawString(word, x - (int) (rc.getCenterX()), y + (fm.getHeight() - fm.getDescent()) / 2 * j + random.nextInt(fm.getDescent() / 2));
            }
        }
    }

    public static String getRandCode() {
        return getRandCode(4);
    }

    public static String getRandCode(boolean words) {
        return getRandCode(4, words);
    }

    public static String getRandCode(int length) {
        return getRandCode(length, false);
    }

    public static String getRandCode(int length, boolean words) {
        ServerConstants.MapleType type = words ? ServerConstants.MapleType.GLOBAL : ServerConstants.MapleType.getByType(ServerConstants.MapleRegion);
        Random random = new Random();
        String finalWord = "", firstWord;

        for (int i = 0; i < length; i++) {
            firstWord = "";
            switch (type) {
                case 한국:
                case 日本:
                case 中国:
                case 台灣:
                    MapleData data;
                    MapleDataProvider dataProvider = MapleDataProviderFactory.getString();
                    data = dataProvider.getData("Mob.img");
                    List<String> mobNameList = new LinkedList<>();
                    for (MapleData mobIdData : data.getChildren()) {
                        mobNameList.add(MapleDataTool.getString(mobIdData.getChildByPath("name"), ""));
                    }
                    while (firstWord.isEmpty()) {
                        String name = mobNameList.get(random.nextInt(mobNameList.size()));
                        name = name.replaceAll("[a-zA-Z]", "");
                        name = name.replaceAll("[^\\pL]", "");
                        if (type != ServerConstants.MapleType.한국 && name.matches("[\uAC00-\uD7A3]+")) {
                            continue;
                        }
                        if ((type == ServerConstants.MapleType.中国 || type == ServerConstants.MapleType.台灣) && !isChinese(name)) {
                            continue;
                        }
                        if (!name.isEmpty()) {
                            int nameX = random.nextInt(name.length());
                            firstWord = name.substring(nameX, nameX + 1);
                            break;
                        }
                    }
                    break;
                default:
                    int temp = random.nextInt(2) == 0 ? 65 : 97;
                    firstWord = String.valueOf((char) (random.nextInt(26) + temp));
                    break;
            }
            finalWord += firstWord;
        }

        return finalWord;
    }

    public static byte[] createImage(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        int width = 196, height = 44;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        Random random = new Random();

        g.setColor(getRandColor(254, 255));
        g.fillRect(0, 0, width, height);

        int point = 2000 + random.nextInt(500);
        for (int i = 0; i < point; i++) {
            g.setColor(getRandColor(80, 150));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int z = random.nextInt(2);
            g.fillRect(x, y, z, z);
        }

        int line = 2 + random.nextInt(3);
        for (int i = 0; i < line; i++) {
            g.setColor(getRandColor(80, 150));
            int x = random.nextInt(width - 50);
            int y = random.nextInt(height + 20);
            int xl = 50 + random.nextInt(50);
            int yl = -20 + random.nextInt(50);
            g.drawLine(x, y, x + xl, y + yl);
        }

        String[] fontList;
        ServerConstants.MapleType type = code.matches("[a-zA-Z]+") ? ServerConstants.MapleType.GLOBAL : ServerConstants.MapleType.getByType(ServerConstants.MapleRegion);
        switch (type) {
            case 한국:
                fontList = new String[]{"Batang"};
                break;
            case 日本:
                fontList = new String[]{"MS Gothic"};
                break;
            case 中国:
                fontList = new String[]{"Simsun", "Microsoft YaHei", "KaiTi", "SimHei"};
                break;
            case 台灣:
                fontList = new String[]{"MingLiU", "Microsoft JhengHei", "DFKai-SB", "SimHei"};
                break;
            default:
                fontList = new String[]{"Arial"};
                break;
        }
        Font font = new Font(fontList[(int) (Math.random() * fontList.length)], Font.BOLD + Font.ITALIC, 25);
        g.setFont(font);

        for (int i = 0; i < code.length(); i++) {
            coloredAndRotation(code.substring(i, i + 1), i, g, code.length(), width, height);
        }

        g.dispose();
        //File file = null;
        byte[] jpgData = new byte[]{};
        try {
            //file = File.createTempFile("MapleStoryCheckCode", "");
            ByteArrayOutputStream jpg = new ByteArrayOutputStream();
            ImageIO.write(image, "JPEG", jpg);
            jpgData = jpg.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(CheckCodeImageCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jpgData;
    }

    public static void main(String[] argc) {
        /*long startTime = System.currentTimeMillis();
        String code = getRandCode(true);
        System.out.printf("獲取驗證碼[%s] 耗時 %d ms%n", code, System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        File image = createImage(code);
        System.out.printf("創建圖片 耗時 %d ms%n", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();
        image.renameTo(new File(code + ".jpg"));
        System.out.printf("移動圖片 耗時 %d ms%n", System.currentTimeMillis() - startTime);*/
    }
}

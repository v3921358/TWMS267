package tools;

//TODO ADD : V267 BY HERTZ
public class CRC32 {
    private static final int[] CRC_TABLE = generateCRCTable();
    private static int[] generateCRCTable() {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            int crc = i << 24;
            for (int j = 0; j < 8; j++) {
                crc = (crc & 0x80000000) != 0 ? (crc << 1) ^ 0x04C11DB7 : crc << 1;
            }
            table[i] = crc;
        }
        return table;
    }
    public static int getTable(int index) {
        return CRC_TABLE[index & 0xFF];
    }
}

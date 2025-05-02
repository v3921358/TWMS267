package tools;

import java.util.Random;

public class Randomizer {
    /* 7 */ private static final Random rand = new Random();

    public static int nextInt() {
        /* 10 */
        return rand.nextInt();
    }

    public static int nextInt(int arg0) {
        /* 14 */
        return rand.nextInt(arg0);
    }

    public static void nextBytes(byte[] bytes) {
        /* 18 */
        rand.nextBytes(bytes);
    }

    public static boolean nextBoolean() {
        /* 22 */
        return rand.nextBoolean();
    }

    public static double nextDouble() {
        /* 26 */
        return rand.nextDouble();
    }

    public static double nextDouble(double min, double max) {
        return min + (rand.nextDouble() * (max - min));
    }

    public static float nextFloat() {
        /* 30 */
        return rand.nextFloat();
    }

    public static long nextLong() {
        /* 34 */
        return rand.nextLong();
    }

    public static int rand(int lbound, int ubound) {
        /* 38 */
        return nextInt(ubound - lbound + 1) + lbound;
    }

    public static boolean isSuccess(int rate) {
        /* 42 */
        return (rate > nextInt(100));
    }
}

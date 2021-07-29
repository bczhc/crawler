package pers.zhc.crawler;

/**
 * @author bczhc
 */
public class JNILoader {
    private static boolean isLoaded = false;

    public static void load(String path) {
        System.load(path);
    }

    public static synchronized void loadMyJNILib() {
        if (!isLoaded) {
            load("/home/bczhc/code/jni/build/libjni-lib.so");
            isLoaded = true;
        }
    }
}

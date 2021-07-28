package pers.zhc.crawler;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author bczhc
 */
public class Downloader {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter URL: ");
        final String urlString = sc.nextLine();
        final URL url = new URL(urlString);

        System.out.println("Enter split number: ");
        final int splitN = sc.nextInt();

        final File headersFile = new File("headers");

        String headersString = "";
        if (headersFile.exists()) {
            headersString = readFileToString(headersFile);
        }

        final Map<String, String> headersMap = Connection.parseHeadersString(headersString);

        final URLConnection connection = Connection.get(url, headersMap);
        final long length = connection.getContentLengthLong();

        final Range[] ranges = getRanges(length, splitN);

        LockCountDownLatch latch = new LockCountDownLatch(splitN);
        ExecutorService es = Executors.newFixedThreadPool(splitN);
        for (int i = 0; i < ranges.length; i++) {
            int finalI = i;
            es.submit(() -> {
                try {
                    Range range = ranges[finalI];
                    final URLConnection urlConnection = Connection.get(url, headersMap);
                    final String rangeHeader = String.format("bytes=%d-%d", range.from, range.to);
                    System.out.println("start " + finalI + ' ' + rangeHeader);

                    urlConnection.setRequestProperty("Range", rangeHeader);
                    final InputStream inputStream = urlConnection.getInputStream();
                    writeToFile(inputStream, new File(String.valueOf(finalI)));
                    System.out.println("OK: " + finalI);
                    inputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        es.shutdown();

        System.out.println("Start combining the split files");

        File outFile = new File("out");
        FileOutputStream out = new FileOutputStream(outFile);
        for (int i = 0; i < splitN; i++) {
            File file = new File(String.valueOf(i));
            FileInputStream is = new FileInputStream(file);
            copyStream(is, out);
            is.close();
            if (!file.delete()) {
                System.out.println("Failed to delete \"" + i + "\"");
            }
        }

        out.close();
        System.out.println("Done");
    }

    private static void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        copyStream(inputStream, out);
        out.close();
    }

    private static Range[] getRanges(long length, int splitN) {
        Range[] ranges = new Range[splitN];
        long range = length / splitN;
        long sum = 0;
        for (int i = 0; i < splitN - 1; i++) {
            ranges[i] = new Range(sum, sum + range - 1);
            sum += range;
        }
        ranges[splitN - 1] = new Range(sum, length - 1);
        return ranges;
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int readLen;
        while ((readLen = in.read(buf)) != -1) {
            out.write(buf, 0, readLen);
            out.flush();
        }
    }

    private static String readFileToString(File file) throws IOException {
        byte[] buf = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        final int readLen = in.read(buf);
        in.close();

        return new String(buf, 0, readLen, StandardCharsets.UTF_8);
    }

    /**
     * <p>indicated the request range of the content in HTTP request header
     * <p>header format: Range: bytes=from-to
     */
    private static class Range {
        /**
         * inclusive
         */

        private final long from;
        /**
         * inclusive
         */
        private final long to;

        private Range(long from, long to) {
            this.from = from;
            this.to = to;
        }
    }
}

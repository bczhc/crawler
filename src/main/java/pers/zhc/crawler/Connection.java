package pers.zhc.crawler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bczhc
 */
public class Connection {
    public static Map<String, String> parseHeadersString(String headers) {
        final HashMap<String, String> map = new HashMap<>();

        for (String header : headers.split("\\n")) {
            final String[] split = header.split(":");
            switch (split.length) {
                case 1:
                    map.put(split[0], "");
                    break;
                case 2:
                    map.put(split[0], split[1]);
                    break;
                default:
            }
        }

        return map;
    }

    public static URLConnection get(@NotNull URL url, @Nullable Map<String, String> headers) throws IOException {
        final URLConnection connection = url.openConnection();
        if (headers != null) {
            headers.forEach(connection::setRequestProperty);
        }

        return connection;
    }

    public static URLConnection get(@NotNull URL url) throws IOException {
        return get(url, null);
    }
}

package org.tsanie.valkyriehelper.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import android.os.Build;
import android.util.Log;

public class HttpHelper {
    private static final int BUFFER = 1024;
    private static final int CONNECT_TIMEOUT = 8000;
    private static final int READ_TIMEOUT = 12000;

    private String url;
    private boolean isRedirect;
    private HashMap<String, String> headers;

    private byte[] content;
    private String contentString;
    private Map<String, List<String>> responseHeaders;

    public HttpHelper() {
        headers = new HashMap<String, String>();
        isRedirect = false;
        setHeader("Accept-Encoding", "gzip,deflate").setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4,zh-CN;q=0.2");
    }

    public HttpHelper setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpHelper setRedirect(boolean isRedirect) {
        this.isRedirect = isRedirect;
        return this;
    }

    public HttpHelper setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpHelper setMobile() {
        String userAgent = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.DEVICE + " Build/" + Build.ID
                + ") AppleWebKit/537.36 (KHTML, like Gecko) Wallpaper/1.1 Mobile Safari/537.36";
        return setHeader("User-Agent", userAgent);
    }

    private byte[] readFromStream(InputStream reader, String encoding, int total, IReading reading) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (encoding != null) {
            if ("gzip".equals(encoding)) {
                reader = new GZIPInputStream(reader);
            } else if ("deflate".equals(encoding)) {
                reader = new DeflaterInputStream(reader);
            }
        }
        byte[] buffer = new byte[BUFFER];
        int count;
        while ((count = reader.read(buffer, 0, BUFFER)) > 0) {
            bos.write(buffer, 0, count);
            if (reading != null) {
                reading.onReading(bos.size(), total);
            }
        }
        return decompress(bos.toByteArray());
    }

    private static byte[] decompress(byte[] data) {
        if (data == null || data.length < 6) {
            return data;
        }
        if (data[0] == 'C' && data[1] == 'O' && data[2] == 'M' && data[3] == 'P') {
            int i;
            for (i = 4; i < data.length; i++) {
                if (data[i] == ';') {
                    i++;
                    break;
                }
            }
            if (i >= data.length) {
                return data;
            }

            byte[] output;
            Inflater decompresser = new Inflater();
            decompresser.reset();
            decompresser.setInput(data, i, data.length - i);

            ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
            try {
                byte[] buf = new byte[BUFFER];
                while (!decompresser.finished()) {
                    int c = decompresser.inflate(buf);
                    o.write(buf, 0, c);
                }
                output = o.toByteArray();
            } catch (Exception e) {
                output = data;
                Log.e("HttpHelper.decompress", e.getMessage(), e);
            } finally {
                try {
                    o.close();
                } catch (IOException e) {
                    Log.e("HttpHelper.decompress.close", e.getMessage(), e);
                }
            }

            decompresser.end();
            return output;
        }
        return data;
    }

    public HttpHelper connect(IReading reading) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(this.url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(isRedirect);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            for (String kv : headers.keySet()) {
                conn.addRequestProperty(kv, headers.get(kv));
            }

            responseHeaders = conn.getHeaderFields();
            if (reading != null) {
                reading.onConnected(conn);
            }
        } catch (Exception e) {
            Log.e("HttpHelper.connect", e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return this;
    }

    public byte[] getBytes(final IReading reading) {
        content = null;
        connect(new ReadingAdapter() {
            @Override
            public void onConnected(HttpURLConnection conn) {
                try {
                    content = readFromStream(conn.getInputStream(), conn.getContentEncoding(), conn.getContentLength(), reading);
                } catch (IOException e) {
                    content = getExceptions(e);
                    Log.e("HttpHelper.getBytes", e.getMessage(), e);
                }
            }
        });
        return content;
    }

    public String getHeaderField(String key) {
        if (responseHeaders == null) {
            return null;
        }

        List<String> result = responseHeaders.get(key);
        int size = result.size();

        if (size < 1) {
            return null;
        } else if (size == 1) {
            return result.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (String s : result) {
            sb.append(s);
            sb.append("; ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    public String getLines(final IReading reading) {
        contentString = null;

        connect(new ReadingAdapter() {
            @Override
            public void onConnected(HttpURLConnection conn) throws Exception {
                InputStream ins = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if (encoding != null) {
                    if ("gzip".equals(encoding)) {
                        ins = new GZIPInputStream(ins);
                    } else if ("deflate".equals(encoding)) {
                        ins = new DeflaterInputStream(ins);
                    }
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                    result.append(System.getProperty("line.separator"));
                    if (reading != null) {
                        if (!reading.onReadLine(line)) {
                            break;
                        }
                    }
                }
                contentString = result.toString();
            }
        });

        return contentString;
    }

    public String postString(String data, IReading reading) {
        byte[] result = postBytes(data, reading);
        return new String(result, Charset.defaultCharset());
    }

    public byte[] postBytes(String data) {
        return postBytes(data, null);
    }

    public byte[] postBytes(String data, IReading reading) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(this.url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(true);
            for (String kv : headers.keySet()) {
                conn.addRequestProperty(kv, headers.get(kv));
            }

            byte[] d = data.getBytes();
            OutputStream os = conn.getOutputStream();
            os.write(d, 0, d.length);
            os.flush();
            os.close();

            InputStream ins = conn.getInputStream();
            responseHeaders = conn.getHeaderFields();

            // TODO cookie
            if (reading != null) {
                reading.onConnected(conn);
            }
            return readFromStream(ins, conn.getContentEncoding(), 0, reading);
        } catch (Exception e) {
            return getExceptions(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static byte[] getExceptions(Throwable e) {
        String result = getExceptionString(e);
        return result.getBytes(Charset.defaultCharset());
    }

    private static String getExceptionString(Throwable e) {
        String result = "{\"code\":-1,\"type\":\"" + e.getClass() + "\",\"msg\":\"" + e.getMessage() + "\"";
        Throwable cause = e.getCause();
        if (cause != null) {
            result += ",\"cause\":" + getExceptionString(cause);
        }
        result += "}";
        return result;
    }
}

package org.tsanie.valkyriehelper.utils;

import java.net.HttpURLConnection;

public interface IReading {

    void onConnected(HttpURLConnection conn) throws Exception;

    void onReading(int position, int total);

    boolean onReadLine(String line);

}

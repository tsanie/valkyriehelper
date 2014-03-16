package org.tsanie.valkyriehelper.utils;

import java.net.HttpURLConnection;

public class ReadingAdapter implements IReading {
    @Override
    public void onConnected(HttpURLConnection conn) throws Exception {
    }

    @Override
    public void onReading(int position, int total) {
    }

    @Override
    public boolean onReadLine(String line) {
        return true;
    }
}

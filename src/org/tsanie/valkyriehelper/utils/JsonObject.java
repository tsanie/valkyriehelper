package org.tsanie.valkyriehelper.utils;

import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.util.Log;

public class JsonObject {
    private JSONObject obj;

    public JsonObject() {
        obj = new JSONObject();
    }

    public JsonObject(String json) {
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            Log.e("JsonObject.ctor", e.getMessage(), e);
            obj = new JSONObject();
        }
    }

    public JsonObject(JSONObject o) {
        obj = o;
    }

    public JsonObject put(String name, Object value) {
        try {
            obj.put(name, value);
        } catch (Exception e) {
            Log.e("JsonObject.put", e.getMessage(), e);
        }
        return this;
    }

    public Object get(String name) {
        try {
            return obj.get(name);
        } catch (Exception e) {
            Log.e("JsonObject.get", e.getMessage(), e);
        }
        return null;
    }

    public JsonObject getJson(String name) {
        return JsonObject.parse(get(name));
    }

    public String getString(String name) {
        return (String) get(name);
    }

    public int getInt(String name) {
        return (Integer) get(name);
    }

    public double getDouble(String name) {
        return (Double) get(name);
    }

    public boolean getBoolean(String name) {
        return (Boolean) get(name);
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    public static JsonObject parse(Object o) {
        if (o instanceof JSONObject) {
            return new JsonObject((JSONObject) o);
        }
        return new JsonObject();
    }

    public static String convertToString(List<NameValuePair> list) {
        StringBuilder sb = new StringBuilder("{");
        int count = list.size();

        for (int i = 0; i < count; i++) {
            NameValuePair nv = list.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"" + nv.getName() + "\"");
            sb.append(':');
            String str = nv.getValue();
            sb.append('\"');
            if (!"".equals(str)) {
                sb.append(str.replace("\"", "\\\""));
            }
            sb.append('\"');
        }
        sb.append("}");
        return sb.toString();
    }
}

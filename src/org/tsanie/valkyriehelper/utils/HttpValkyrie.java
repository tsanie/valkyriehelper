package org.tsanie.valkyriehelper.utils;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.util.Log;

public class HttpValkyrie {

    public static final String CHARSET = "UTF-8";

    private static final String APP_ID = "valkyriecrusade";
    private static final String[] VERSIONS = {
            "2.3", "2.3.5", "4.0", "4.0.3", "4.0.4", "4.1", "4.1.2", "4.2", "4.3", "4.4.1", "4.4.2"
    };
    private static final String NAMES = "ㅜㅞㅟㅝㅔ무메미며마므우에이여아으구게기겨가그ㅣㅕㅏㅡㄴㅁㅇㄱㅈㅎㅅㅊ팬돍라";
    private static final String USER_AGENT_NUBEE = "NubeePlatform/2.1.2 (android/4.0.3; GT-i9300) valkyriecrusade/2.2.0";
    private static final String USER_AGENT = "sg2-agent/2.2.0 (android/4.0.3/i9300/xx/xx)";

    private static Random random;

    static {
        random = new Random();
    }

    private static String[] getVersions() {
        return VERSIONS;
    }

    private static String generateUniqueID(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextInt(10) > 4) {
                sb.append((char) (random.nextInt(10) + '0'));
            } else {
                sb.append((char) (random.nextInt(6) + 'a'));
            }
        }
        return sb.toString();
    }

    private static String generateMac() {
        return generateUniqueID(2) + ":" + generateUniqueID(2) + ":" + generateUniqueID(2) + ":" + generateUniqueID(2) + ":"
                + generateUniqueID(2) + ":" + generateUniqueID(2);
    }

    private static String bin2hex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        int i = bytes.length;
        for (int j = 0; j < i; j++) {
            int k = bytes[j];
            sb.append(Character.forDigit(0xF & k >> 4, 16));
            sb.append(Character.forDigit(k & 0xF, 16));
        }
        return sb.toString();
    }

    @SuppressLint("TrulyRandom")
    private static String getHardwareHash(String hardware) {
        try {
            byte[] iv = new byte[] {
                    4, 8, 12, 13, 9, 0, 2, 13, 6, 13, 13, 9, 14, 11, 1, 15
            };
            byte[] key = new byte[] {
                    5, 5, 9, 11, 0, 10, 8, 5, 0, 7, 12, 4, 6, 0, 12, 14, 15, 8, 15, 14, 8, 9, 15, 11, 8, 1, 4, 7, 6, 15, 12, 13
            };

            IvParameterSpec spec = new IvParameterSpec(iv);
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, secret, spec);
            return bin2hex(cipher.doFinal(hardware.getBytes(CHARSET)));
        } catch (Exception ex) {
            Log.e("HttpValkyrie.getHardwareHash", ex.getMessage(), ex);
        }
        return null;
    }

    private static byte[] getAuthKey() {
        byte[] arrayOfByte1 = {
                14, 34, 115, 120, 28, 120, 25, 26, 127, 120, 59, 10, 126, 61, 123, 44, 122, 127, 2, 115, 120, 120, 13, 1, 50, 28, 5, 33,
                125, 115
        };
        StringBuilder localStringBuilder = new StringBuilder();
        byte[] arrayOfByte2 = new byte[31];
        for (int i = 0; i < 30; i++) {
            arrayOfByte2[i] = ((byte) (0x4B ^ arrayOfByte1[i]));
            localStringBuilder.append((char) arrayOfByte2[i]);
        }
        return arrayOfByte2;
    }

    private static String getSignatureKey(String json) {
        try {
            byte[] key = getAuthKey();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            String sha = new BigInteger(1, mac.doFinal(json.getBytes())).toString(16);
            if (sha.length() % 2 != 0) {
                sha = "0" + sha;
            }
            return "Nubee method=HMAC-SHA256,version=1.0,signature=" + sha;
        } catch (Exception ex) {
            Log.e("HttpValkyrie.getSignatureKey", ex.getMessage(), ex);
        }
        return null;
    }

    private static String getRandomName() {
        int size = random.nextInt(4) + 3;
        int length = NAMES.length();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < size; i++) {
            sb.append(NAMES.charAt(random.nextInt(length)));
        }
        return sb.toString();
    }

    public HttpValkyrie(int master_version) {
        this.master_version = master_version;
    }

    private String cookie;
    private int master_version;
    private String user_string;
    private String user_string0;

    @SuppressLint("DefaultLocale")
    public JsonObject device(List<String> devices) {
        String version = getVersions()[random.nextInt(getVersions().length)];
        String device = devices.get(random.nextInt(devices.size()));

        String finger = device + "/" + device + ":" + generateUniqueID(6).toUpperCase() + "/" + version + ":user/release-keys";
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("fingerprint", finger));
        list.add(new BasicNameValuePair("version.incremental", version));
        list.add(new BasicNameValuePair("device", device));
        list.add(new BasicNameValuePair("mac_address", generateMac()));

        String hardware = JsonObject.convertToString(list);
        Log.d("hardware", hardware);
        String hw_hash = getHardwareHash(hardware);

        list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("app_id", APP_ID));
        list.add(new BasicNameValuePair("os", "Android"));
        list.add(new BasicNameValuePair("udid", generateUniqueID(16)));
        list.add(new BasicNameValuePair("hw_hash", hw_hash));
        list.add(new BasicNameValuePair("time", String.valueOf(System.currentTimeMillis())));
        String json = JsonObject.convertToString(list);

        Log.d("os", json);
        String auth = getSignatureKey(json);

        HttpHelper helper = new HttpHelper();
        helper.setUrl("https://connect.nubee.com/devices");
        helper.setHeader("Content-Type", "application/json");
        helper.setHeader("Authorization", auth);
        helper.setHeader("User-Agent", USER_AGENT_NUBEE);

        String result = helper.postString(json, null);
        String s = helper.getHeaderField("Set-Cookie");
        int i = s.indexOf(';');
        if (i > 0) {
            s = s.substring(0, i);
        }
        cookie = s;
        Log.d("cookie", cookie);

        return new JsonObject(result);
    }

    public JsonObject me_self(String device_info) {
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("device_info", device_info));
        list.add(new BasicNameValuePair("app_id", APP_ID));
        list.add(new BasicNameValuePair("os", "Android"));
        list.add(new BasicNameValuePair("language", "jp"));
        list.add(new BasicNameValuePair("time", String.valueOf(System.currentTimeMillis())));
        String json = JsonObject.convertToString(list);
        String auth = getSignatureKey(json);

        HttpHelper helper = new HttpHelper();
        helper.setUrl("https://connect.nubee.com/people/@me/@self");
        helper.setHeader("Content-Type", "application/json");
        helper.setHeader("Authorization", auth);
        helper.setHeader("User-Agent", USER_AGENT_NUBEE);
        helper.setHeader("Cookie", cookie);
        helper.setHeader("Cookie2", "$Version=1");

        return new JsonObject(helper.postString(json, null));
    }

    public static JsonObject post(String url, String data) {
        HttpHelper helper = new HttpHelper();
        helper.setUrl(url);
        helper.setHeader("User-Agent", USER_AGENT);
        return new JsonObject(helper.postString(data, null));
    }

    public JsonObject get_user_id(JsonObject me, String device_info) {
        JsonObject person = me.getJson("person");
        Object user_id = person.get("id");
        String json = "person={\"country\":\"" + person.get("country") + "\",\"id\":" + user_id + ",\"registered\":"
                + person.get("registered") + ",\"time\":" + person.get("time") + "}";
        json += "&signature=" + me.get("signature");
        json += "&language=jp";
        json += "&nb_device_info=" + device_info;
        json += "&root=0&log=";

        person = post("https://valkyriecrusade.nubee.com/user/get_user_id", json);
        String s = "user_id=" + user_id + "&device_info=" + person.getJson("user_info").get("device_info");
        user_string = s + "&master_version=" + master_version + "&compress=1&resume=0";
        user_string0 = s + "&master_version=0&compress=1&resume=0";
        return person;
    }

    public JsonObject skip_tutorial() {
        return post("https://valkyriecrusade.nubee.com/user/skip_tutorial", user_string0);
    }

    public JsonObject save_user() {
        String data = user_string + "&user_name=";
        try {
            data += URLEncoder.encode(getRandomName(), CHARSET);
        } catch (Exception ex) {
            Log.e("HttpValkyrie.save_user", ex.getMessage(), ex);
        }
        data += "&random_name_id=0&tutorial_id=17";
        return post("https://valkyriecrusade.nubee.com/user/save_user", data);
    }

    public JsonObject quest_list_18() {
        String data = user_string + "&tutorial_id=18";
        return post("https://valkyriecrusade.nubee.com/quest/list", data);
    }

    public JsonObject gacha_pull_19() {
        String data = user_string + "&gacha_id=19&friend_point=0&cash=0&gacha_ticket_id=0&gacha_ticket_num=0";
        return post("https://valkyriecrusade.nubee.com/gacha/pull", data);
    }

    public JsonObject map_event(int area_id) {
        String data = user_string + "&area_id=" + area_id;
        return post("https://valkyriecrusade.nubee.com/battle/npc/event", data);
    }
}

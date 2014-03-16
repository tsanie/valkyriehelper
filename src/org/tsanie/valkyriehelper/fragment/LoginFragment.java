package org.tsanie.valkyriehelper.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.tsanie.valkyriehelper.R;
import org.tsanie.valkyriehelper.utils.HttpValkyrie;
import org.tsanie.valkyriehelper.utils.JsonObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoginFragment extends BaseFragment {

    private TextView textView;
    private List<String> devices;

    private List<String> getDevices() {
        if (this.devices == null) {
            ArrayList<String> devices = new ArrayList<String>();
            BufferedReader reader = null;
            try {
                AssetManager asset = this.getActivity().getAssets();
                reader = new BufferedReader(new InputStreamReader(asset.open("mobiles")));
                String line;
                while ((line = reader.readLine()) != null) {
                    devices.add(line);
                }
            } catch (IOException e) {
                Log.e("HttpValkyrie.getDevices", e.getMessage(), e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Log.e("HttpValkyrie.getDevices", e.getMessage(), e);
                }
            }
            this.devices = devices;
        }
        return this.devices;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("login...");

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //new LoginTask().execute();
    }

    class LoginTask extends AsyncTask<Void, Void, JsonObject> {
        @Override
        protected JsonObject doInBackground(Void... params) {
            HttpValkyrie vc = new HttpValkyrie(149);
            JsonObject obj = vc.device(getDevices());
            String device_info = obj.getString("device");
            // String device_info =
            // "jkAdw9sHJpodztlO7L_N9CISaTNYgN-qui787BBP9RfHeckAPJE_n2efwYCzproVxfazmvYSDqhYlKlAi4FEb_X1N6ieDeM1C5T_eUjixa97bWZ3OuP0nD888xvbtcqa";
            JsonObject o = vc.me_self(device_info);
            o = vc.get_user_id(o, device_info);
            vc.skip_tutorial();
            vc.save_user();
            vc.quest_list_18();
            o = vc.gacha_pull_19();
            return o;
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            String text = result.toString();
            textView.setText(text);
        }
    }
}

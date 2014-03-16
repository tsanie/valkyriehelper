package org.tsanie.valkyriehelper.fragment;

import org.tsanie.valkyriehelper.R;
import org.tsanie.valkyriehelper.ui.KingsLog;
import org.tsanie.valkyriehelper.ui.KingsLogAdapter;
import org.tsanie.valkyriehelper.utils.JsonObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class KingsFragment extends BaseFragment {

    private ListView list;
    private KingsLogAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kings, container, false);

        list = (ListView) rootView.findViewById(R.id.listKingsLog);
        adapter = new KingsLogAdapter(getActivity());
        list.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        new KingsTask().execute();
    }

    class KingsTask extends AsyncTask<Void, Void, JsonObject> {
        @Override
        protected JsonObject doInBackground(Void... params) {
            while (list == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < 20; i++) {
                final KingsLog log = new KingsLog();
                log.setId(String.valueOf(i));
                log.setTitle("title: " + i);
                log.setDetail("detail - " + i);
                list.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addFirst(log);
                    }
                });

                new Thread(new AccountRunner(log)).start();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JsonObject result) {

        }
    }

    class AccountRunner implements Runnable {
        private KingsLog log;

        public AccountRunner(KingsLog log) {
            this.log = log;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            log.setTitle("new title");

            list.post(new Runnable() {
                @Override
                public void run() {
                    adapter.put(log.getId(), log);
                }
            });
        }
    }
}

package org.tsanie.valkyriehelper.ui;

import java.util.LinkedList;

import org.tsanie.valkyriehelper.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class KingsLogAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private LinkedList<KingsLog> logs;

    public KingsLogAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        logs = new LinkedList<KingsLog>();
    }

    public void addFirst(KingsLog log) {
        logs.addFirst(log);
        notifyDataSetChanged();
    }

    public void put(String id, KingsLog log) {
        int size = logs.size();
        for (int i = 0; i < size; i++) {
            KingsLog l = logs.get(i);
            if (id.equals(l.getId())) {
                logs.set(i, log);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public KingsLog getItem(String id) {
        if (id != null) {
            for (KingsLog log : logs) {
                if (id.equals(log.getId())) {
                    return log;
                }
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return logs.size();
    }

    @Override
    public Object getItem(int position) {
        return logs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_kingslog, null);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.textKingTitle);
            holder.detail = (TextView) convertView.findViewById(R.id.textKingDetail);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        KingsLog log = logs.get(position);
        holder.title.setText(log.getTitle());
        holder.detail.setText(log.getDetail());

        return convertView;
    }

    private class Holder {
        public TextView title;
        public TextView detail;
    }

}

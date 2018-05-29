package com.usharik.seznamslovnik.dialog.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.usharik.seznamslovnik.dialog.ProxyInfo;

import java.util.List;

public class ProxySpinnerAdapter extends ArrayAdapter<ProxyInfo> {

    private Context context;
    private List<ProxyInfo> proxyInfoList;

    public ProxySpinnerAdapter(Context context, int textViewResourceId,
                       List<ProxyInfo> proxyInfoList) {
        super(context, textViewResourceId, proxyInfoList.toArray(new ProxyInfo[proxyInfoList.size()]));
        this.context = context;
        this.proxyInfoList = proxyInfoList;
    }

    @Override
    public int getCount(){
        return proxyInfoList.size();
    }

    @Override
    public ProxyInfo getItem(int position){
        return proxyInfoList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(proxyInfoList.get(position).getDescription());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(proxyInfoList.get(position).getDescription());

        return label;
    }
}
package com.ibangalore.bustrac.UXAssets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ibangalore.bustrac.R;

import java.util.List;

/**
 * Created by ahegde on 6/24/15.
 */
public class ArrivalsLViewAdapter extends ArrayAdapter<ArrivalsRowItem> {

    public ArrivalsLViewAdapter(Context context, List<ArrivalsRowItem> items){
        super(context, 0, items);
    }

    private class ViewHolder{
        TextView tvRouteNum;
        TextView tvDestination;
        TextView tvEtaMins;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;
        ArrivalsRowItem rowItem = getItem(position);
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.arrivals_list_item, null);
            holder = new ViewHolder();
            holder.tvRouteNum = (TextView) convertView.findViewById(R.id.arvls_routenum_item_textview);
            holder.tvDestination = (TextView) convertView.findViewById(R.id.arvls_dest_item_textview);
            holder.tvEtaMins = (TextView) convertView.findViewById(R.id.arvls_eta_item_textview);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvRouteNum.setText(rowItem.getRouteNum());
        holder.tvDestination.setText(rowItem.getDestination());
        holder.tvEtaMins.setText(rowItem.getEtaMin());

        return convertView;
    }
}

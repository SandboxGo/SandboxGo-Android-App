package com.xu.jialu.sandboxgo;

/**
 * Created by maxu on 3/27/17.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CustomAdapter extends SimpleAdapter {
    String[] names;
    private Context mContext;
    public LayoutInflater inflater = null;

    public CustomAdapter(Context context,
                           List<? extends Map<String, ?>> data, int resource, String[] from,
                           int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public class Holder
    {
        TextView tv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.list_item, null);


        HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

        // display tutor images
        new DownloadTask((ImageView) vi.findViewById(R.id.tutorImage))
                .execute((String) data.get("imagesource"));

        // display tutor names in the textView
        holder.tv=(TextView) vi.findViewById(R.id.name);
        holder.tv.setText((String) data.get("name"));

        return vi;
    }

}
package carstendressler.com.fancontrol;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Carsten on 7/25/2014.
 */
public class FanAdapter extends ArrayAdapter<FanEntity>  {

    private List<FanEntity> items;
    private Context myContext;


    public FanAdapter(Context context, int textViewResourceId, List<FanEntity> objects) {
        super(context, textViewResourceId, objects);

        this.myContext = context;
        this.items = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {

            LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listitem, null);
        }
        FanEntity o = items.get(position);

        if (o != null) {
            TextView tvName = (TextView) v.findViewById(R.id.tvName);
            tvName.setText(o.Name);

            TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);

            if (o.Status == FanEntity.Status1.UNKNOWN)
                tvStatus.setText("Unknown");

            if (o.Status == FanEntity.Status1.OFF)
                tvStatus.setText("Off");

            if (o.Status == FanEntity.Status1.LOW)
                tvStatus.setText("Low");

            if (o.Status == FanEntity.Status1.MEDIUM)
                tvStatus.setText("Medium");

            if (o.Status == FanEntity.Status1.HIGH)
                tvStatus.setText("High");

        }
        return v;

    }
}

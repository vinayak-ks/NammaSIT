package campuschat.wifi.adapter;

/**
 * Created by vinayak ks on 4/1/2016.
 */
import campuschat.wifi.bean.TeamMembers;

import java.util.ArrayList;
import java.util.List;

import campuschat.wifi.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AboutAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<TeamMembers> mDatas;
    private Context mContext;

    public AboutAdapter(int[] images, String []str,Context context) {
        super();
        mDatas = new ArrayList<TeamMembers>();
        mInflater = LayoutInflater.from(context);
        mContext = context;
        int mSize = images.length;
        for (int i = 0; i < mSize; i++) {
            TeamMembers TeamMembers = new TeamMembers(images[i],str[i]);
            mDatas.add(TeamMembers);
        }
    }

    @Override
    public int getCount() {
        if (null != mDatas) {
            return mDatas.size();
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_team, null);
            holder = new ViewHolder();
            holder.ivTeamMembers = (ImageView) convertView.findViewById(R.id.image);
            holder.names = (TextView)convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext).load(((TeamMembers) mDatas.get(position)).getImageId())
                .into(holder.ivTeamMembers);
        holder.names.setText(mDatas.get(position).getStringid());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView ivTeamMembers;
        public TextView names;
    }
}

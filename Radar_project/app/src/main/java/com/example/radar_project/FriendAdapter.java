package com.example.radar_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;
/*自定义适配器*/

/**
 * 这里一定要指明ArrayAdapter的类型！！！！
 */
public class FriendAdapter extends ArrayAdapter<Friend> {
    private int resourceId;
    public FriendAdapter(Context context, int textViewResourceId, List<Friend> objects){
        super(context,textViewResourceId,objects);
        //将ListView下面的布局文件存起来
        resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend =getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            //使用LyaoutInflate为子项传入布局
            view=LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder=new ViewHolder();
            //将控件进行缓存
            viewHolder.FriendName=(TextView)view.findViewById(R.id.name);
            viewHolder.TeleNum=(TextView)view.findViewById(R.id.telenum);
            view.setTag(viewHolder);
        }else {//如果有布局文件的话
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        //设置数据
        viewHolder.FriendName.setText(friend.getName());
        viewHolder.TeleNum.setText(friend.getNum());
        return view;
    }
    //缓存类
    class ViewHolder{
        TextView FriendName;
        TextView TeleNum;
    }
}

package com.example.radar_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑界面
 */
public class friends_list extends AppCompatActivity {
    private Button btn_friends_list_radar;
    private List<Friend> friendList=new ArrayList<>();
    private ImageView btn_save;
    private ImageView btn_friends_list_edit;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.friends_list);

        //跳转回主界面
        btn_friends_list_radar=findViewById(R.id.btn_friends_list_radar);
        btn_friends_list_radar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(friends_list.this,MainActivity.class);
                startActivity(intent);
            }
        });
        //刷新列表
        refreshList();
        //调用弹出框
        btn_save=findViewById(R.id.btn_add);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        //刷新列表按钮
        btn_friends_list_edit=findViewById(R.id.btn_friends_list_edit);
        btn_friends_list_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
            }
        });

    }

    private void refreshList() {
        queryDatabases();
        FriendAdapter adapter=new FriendAdapter(friends_list.this,R.layout.friendlist_item,friendList);
        ListView listView=findViewById(R.id.friends_list);
        listView.setAdapter(adapter);
        //点击删除功能
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend=friendList.get(position);
                DataSupport.deleteAll(Friend_database.class,"name=? and num=?",friend.getName(),friend.getNum());
                refreshList();
            }
        });
    }

    /**
     * 调用弹出框
     */
    public void showEditDialog(){
        CreateFriendDialog createFriendDialog = new CreateFriendDialog(this);
        createFriendDialog.show();
    }

    /**
     * 初始化数据
     */

    //从数据库里面查找数据
    private void queryDatabases(){
        //先清空List
        friendList.clear();
        List<Friend_database> friends=DataSupport.findAll(Friend_database.class);
        for(Friend_database friend:friends){
            Friend friend1=new Friend(friend.getName(),friend.getNum());
            friendList.add(friend1);
        }
    }
}

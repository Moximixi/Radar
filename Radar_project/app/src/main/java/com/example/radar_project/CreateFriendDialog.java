package com.example.radar_project;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 定义要弹出的Dialog以及点击事件
 */
public class CreateFriendDialog extends Dialog {
    Activity context;
    private Button btn_save;
    public EditText text_name;
    public EditText text_mobile;

    public CreateFriendDialog(Activity context){
        super(context);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_item);

        text_name=findViewById(R.id.add_name);
        text_mobile=findViewById(R.id.add_num);
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 0.76); // 宽度设置为屏幕的0.8
        dialogWindow.setAttributes(p);
        // 根据id在布局中找到控件对象  
        btn_save = (Button) findViewById(R.id.btn_save);

        // 为按钮绑定点击事件监听器  
        btn_save.setOnClickListener(mClickListener);

        this.setCancelable(true);//点击空白处消失
    }

    /**
     * 点击事件
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Friend_database friendss=new Friend_database();
            boolean exit=false;
            List<Friend_database> friends=DataSupport.findAll(Friend_database.class);
            switch (v.getId()) {
                case R.id.btn_save:
                    //将数据存入数据库
                    //Toast.makeText(context,"我被点了",Toast.LENGTH_SHORT).show();
                    //输入框里面的数据
                    String name_content=text_name.getText().toString();
                    String tele_content=text_mobile.getText().toString();
                    if(name_content.length()!=0&&tele_content.length()!=0){//输入的数据非空
                        for(Friend_database friend:friends){
                            //找到姓名相同或者电话相同的数据了
                            if(name_content.equals(friend.getName())){
                                friendss.setNum(tele_content);
                                friendss.updateAll("name=?",name_content);
                                exit=true;
                                break;
                            }else if(tele_content.equals(friend.getNum())){
                                friendss.setName(name_content);
                                friendss.updateAll("num=?",tele_content);
                                exit=true;
                                break;
                            }
                        }
                        //所有循环都结束，还是没有找到相同的数据再添加
                        if(exit==false){
                            friendss.setNum(tele_content);
                            friendss.setName(name_content);
                            friendss.save();
                            break;
                        }
                        Toast.makeText(context,"插入成功,请点击右上角刷新",Toast.LENGTH_SHORT).show();
                    }else{//输入的数据有为空的情况
                        Toast.makeText(context,"插入失败,请检查你的数据是否为空",Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    break;
            }
        }
    };
}

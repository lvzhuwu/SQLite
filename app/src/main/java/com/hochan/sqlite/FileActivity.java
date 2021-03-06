package com.hochan.sqlite;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.hochan.sqlite.multi_images_selector.MultiImageSelectorActivity;
import com.hochan.sqlite.multi_images_selector.utils.ScreenUtils;

import java.util.ArrayList;


public class FileActivity extends AppCompatActivity {

    private Button btnUpload;
    private ListPopupWindow mListPopupWindow;
    private ArrayList<String> mUploadFileType;
    private ArrayAdapter<String> mFileTypeAdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backarrow));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpload = (Button) findViewById(R.id.btn_upload_files);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListPopupWindow == null) {
                    System.out.println("ListPopupWindow is null!");
                    createPopupWindow();
                }

                if(mListPopupWindow.isShowing()) {
                    System.out.println("隐藏窗口");
                    mListPopupWindow.dismiss();
                }
                else {
                    System.out.println("显示窗口");
                    mListPopupWindow.show();
                }

            }
        });
    }

    private void createPopupWindow(){
        System.out.println("创建弹出窗口");
        Point point = ScreenUtils.getScreenSize(this);
        int width = point.x;

        mListPopupWindow = new ListPopupWindow(this);
        mListPopupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBackground)));
        mListPopupWindow.setContentWidth(width);
        mListPopupWindow.setWidth(width);
        mListPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mListPopupWindow.setAnchorView(btnUpload);
        mListPopupWindow.setModal(true);
        mUploadFileType = new ArrayList<>();
        mUploadFileType.add("图片");
        mUploadFileType.add("音频");
        mUploadFileType.add("视频");
        mUploadFileType.add("文本");
        mFileTypeAdater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mUploadFileType);
        mListPopupWindow.setAdapter(mFileTypeAdater);
        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                mListPopupWindow.dismiss();
                switch (position){
                    case 0:
                        intent = new Intent(FileActivity.this, MultiImageSelectorActivity.class);
                        // 是否显示拍摄图片
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
                        // 最大可选择图片数量
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 20);
                        // 选择模式
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
                                MultiImageSelectorActivity.MODE_MULTI);
                        // 默认选择
                        break;
                }
                startActivity(intent);
            }
        });
    }

}

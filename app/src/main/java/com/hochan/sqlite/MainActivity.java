package com.hochan.sqlite;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hochan.sqlite.data.Worker;
import com.hochan.sqlite.fragment.EditDialogFragment;
import com.hochan.sqlite.fragment.LoginFragment;
import com.hochan.sqlite.fragment.SearchDialogFragment;
import com.hochan.sqlite.fragment.WorkersListFragment;
import com.hochan.sqlite.sql.DataHelper;
import com.hochan.sqlite.tools.ClientThread;
import com.hochan.sqlite.tools.DataHandler;
import com.hochan.sqlite.tools.SQLHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        EditDialogFragment.OnDialogListener, android.support.v7.widget.Toolbar.OnMenuItemClickListener{

    private Button btnSearch, btnAdd;
    private WorkersListFragment mWorkersListFragment;
    private LinearLayout llButton;
    private TextView tvText;
    private EditDialogFragment mAddDialog;
    private ClientThread mClientThread;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private LoginFragment mLoginFragment, mSyncFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this);

        System.out.println("MainActivity start");

        btnSearch = (Button) findViewById(R.id.btn_search);
        btnAdd = (Button) findViewById(R.id.btn_add);

        btnSearch.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        llButton = (LinearLayout) findViewById(R.id.ll_button);
        tvText = (TextView) findViewById(R.id.tv_text);

        mWorkersListFragment = (WorkersListFragment) getSupportFragmentManager().findFragmentByTag(WorkersListFragment.TAG);
        if(mWorkersListFragment == null) {
            mWorkersListFragment = WorkersListFragment.newInstance();
            mWorkersListFragment.setmShowFragmentListener(new WorkersListFragment.ShowFragmentListener() {
                @Override
                public void chooseMore(boolean isChooseMore) {
                    if(isChooseMore){
                        tvText.setVisibility(View.GONE);
                        llButton.setVisibility(View.GONE);
                    }
                    else{
                        tvText.setVisibility(View.VISIBLE);
                        llButton.setVisibility(View.VISIBLE);
                    }
                }
            });
            getSupportFragmentManager().beginTransaction().add(R.id.rl_workers_list, mWorkersListFragment, WorkersListFragment.TAG).commit();
        }

        String str = "{\"id\":\"13349015\", \"name\":\"陈振东\", \"phone_num\":\"15622271342\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Worker worker = objectMapper.readValue(str, Worker.class);
            System.out.println("worker.getmID():"+worker.getmID());
            System.out.println("worker.getmName():"+worker.getmName());
            System.out.println("worker.getmPhoneNumber():"+worker.getmPhoneNumber());
            System.out.println("worker.getmTowerNumber():"+worker.getmTowerNumber());
            System.out.println("worker.getmWorkState():"+worker.getmWorkState());

            String s = objectMapper.writeValueAsString(worker);
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.btn_search:
                //intent.setClass(this, SearchActivity.class);
                SearchDialogFragment searchDialog = SearchDialogFragment.newInstance();
                searchDialog.setSearchListener(new SearchDialogFragment.OnSearchDialogListener() {
                    @Override
                    public void searchDialog(String key, int type) {
                        mWorkersListFragment.search(key, type);
                    }

                    @Override
                    public void showAll() {
                        mWorkersListFragment.showAll();
                    }
                });
                searchDialog.show(getSupportFragmentManager(), "search");
                return;
            case R.id.btn_add:
                mAddDialog = EditDialogFragment.newInstance(EditDialogFragment.ADD_DIALOG);
                mAddDialog.setOnDialogListener(this);
                mAddDialog.show(getSupportFragmentManager(), "adddialog");
                return;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAddDialog = (EditDialogFragment) getSupportFragmentManager().findFragmentByTag("adddialog");
        if(mAddDialog != null){
            mAddDialog.setOnDialogListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void clicked(int index, Worker worker, int position) {
        DataHelper tmpDataHelper = new DataHelper(getApplicationContext());
        Long result = tmpDataHelper.addData(worker);
        worker.setmID(String.valueOf(result));
        Toast.makeText(getApplicationContext(), "添加成功：" + result, Toast.LENGTH_SHORT).show();
        mWorkersListFragment.add(worker);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_clear:
                DataHelper dataHelper = new DataHelper(getApplicationContext());
                dataHelper.clearTable(getApplicationContext());
                mWorkersListFragment.clear();
                Toast.makeText(getApplicationContext(), "已清空表的记录", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_connect:
                mLoginFragment = (LoginFragment) getSupportFragmentManager()
                        .findFragmentByTag(LoginFragment.TAG_LOGIN);
                if(mLoginFragment == null)
                    mLoginFragment = LoginFragment.newInstance(LoginFragment.TAG_LOGIN);
                mLoginFragment.show(getSupportFragmentManager(), LoginFragment.TAG_LOGIN);
                break;
            case R.id.action_file:
                Intent intent = new Intent(this, FileActivity.class);
                startActivity(intent);
                break;
            //获取服务器所有数据
            case R.id.action_get_all:
                SQLHttpClient.get(SQLHttpClient.GET_ALL, null, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        System.out.println(response);
                        ArrayList<Worker> workers = new ArrayList<Worker>();
                        String[][] result = DataHandler.jsonArray2StringArray(response);
                        for(int i = 0; i < result.length; i++){
                            System.out.println("解析："+result[i][0]);
                            System.out.println("解析："+result[i][1]);
                            System.out.println("解析："+result[i][2]);
                            System.out.println("解析："+result[i][3]);
                            Worker worker = new Worker();
                            worker.setmID(result[i][0]);
                            worker.setmName(result[i][1]);
                            worker.setmPassword(result[i][2]);
                            worker.setmDateTime(result[i][3]);
                            workers.add(worker);
                        }
                        DataHelper tmpDataHelper = new DataHelper(MainActivity.this);
                        tmpDataHelper.addDatas(workers);
                        tmpDataHelper.close();
                        mWorkersListFragment.showAll();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        System.out.println(responseString);
                    }
                });
                break;
            //上传本地所有数据
            case R.id.action_upload_all:
                DataHelper adataHelper = new DataHelper(this);
                String[][] allWorkers = adataHelper.getAllWorkers();
                adataHelper.close();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        JSONArray jsonarray = new JSONArray(allWorkers);
                        System.out.println(jsonarray.toString());
                        StringEntity stringEntity = new StringEntity(jsonarray.toString());
                        SQLHttpClient.post(this, SQLHttpClient.UPLOAD_ALL, stringEntity, RequestParams.APPLICATION_JSON,
                                new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                        super.onSuccess(statusCode, headers, response);
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            //增量同步
            case R.id.action_sync:
                DataHelper bdataHelper = new DataHelper(this);
                String[][] results = bdataHelper.getTimeStamps();
                bdataHelper.close();
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        JSONArray jsonArray = new JSONArray(results);
                        StringEntity stringEntity = new StringEntity(jsonArray.toString());
                        System.out.println(jsonArray.toString());
                        SQLHttpClient.post(this, SQLHttpClient.SYNC_URL, stringEntity, RequestParams.APPLICATION_JSON,
                                new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                        super.onSuccess(statusCode, headers, response);
                                        System.out.println(response.toString());
                                    }
                                });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //mSyncFragment = (LoginFragment) getSupportFragmentManager()
                //        .findFragmentByTag(LoginFragment.TAG_SYNC);
                //if(mSyncFragment == null)
                //    mSyncFragment = LoginFragment.newInstance(LoginFragment.TAG_SYNC);
                //mSyncFragment.show(getSupportFragmentManager(), LoginFragment.TAG_SYNC);
                break;
        }
        return true;
    }
}

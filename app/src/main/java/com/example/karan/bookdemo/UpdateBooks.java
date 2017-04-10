package com.example.karan.bookdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class UpdateBooks extends AppCompatActivity implements CustDialog.MyClickEvent,MyServer {

    private RequestQueue mqueue;
    private ProgressBar pDailog;
    private ListView mylist;
    private TextView tv;
    private List<listinfo> data;
    private static final String url = MyServerUrl+"getUserBooks";
    private SharedPreferences sharedPreferences;
    private String sellername;
    private RInventoryAdapter rInventoryAdapter;
    private RecyclerView recyclerView;
    private AlertDialog dialog;
    private int ItemPosition;
    private SharedPreferences sh;
    private SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_books);
        MyVolley.init(this.getApplicationContext());
        mqueue = MyVolley.getRequestQueue();
        sh = getSharedPreferences("update",Context.MODE_PRIVATE);
        tv = (TextView) findViewById(R.id.no_of_books);
        data = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.inventorylist);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), R.drawable.divider));
        ScaleInAnimator animator = new ScaleInAnimator();
        animator.setAddDuration(1000);
        animator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(animator);
        rInventoryAdapter = new  RInventoryAdapter(UpdateBooks.this,data);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(rInventoryAdapter);
        recyclerView.setAdapter(new ScaleInAnimationAdapter(alphaAdapter));
        // recyclerView.setAdapter(RVadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.e("clicked","true1");
                Log.e("pos:",String.valueOf(position));
                ItemPosition = position;
                Log.e("vid:",String.valueOf(view.getId()));
                FragmentManager fm = getSupportFragmentManager();
                CustDialog cd = new CustDialog();
                cd.show(fm,"Action");
            }
        }));


        VolleyConnect();


    }

    public void StopProgress(){
        if (dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void startprogress(){
        dialog = new SpotsDialog(this, R.style.Custom2);
        dialog.show();
    }


    public void VolleyConnect(){
        sharedPreferences = getSharedPreferences("UserDetail", Context.MODE_PRIVATE);
        sellername = sharedPreferences.getString("sellerid","").toLowerCase();
        data.clear();
        startprogress();
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        try {
            jo.put("username",sellername);
            ja.put(jo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest mreq = new JsonArrayRequest(Request.Method.POST, url, ja, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                StopProgress();
                if (response!=null) {
                    tv.setText(String.valueOf(response.length()));
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jo;
                        try {
                            jo = response.getJSONObject(i);
                            listinfo current = new listinfo();
                            current.title = jo.getString("title");
                            current.yourprice = jo.getInt("yourprice");
                            current.originalprice = jo.getInt("originalprice");
                            current.url = jo.getString("imgUrl");
                            StringBuilder sb = new StringBuilder(current.url);
                            sb.replace(nthsearch(current.url,'/',2),nthsearch(current.url,':',2)-1,Localhost);
                            current.url = sb.toString();
                            data.add(current);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Err", e.getMessage());
                            Toast.makeText(getApplicationContext(), "Error in parsing", Toast.LENGTH_SHORT).show();
                        }

                        // Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();

                    }

                    rInventoryAdapter = new RInventoryAdapter(UpdateBooks.this, data);
                    AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(rInventoryAdapter);
                    recyclerView.setAdapter(new ScaleInAnimationAdapter(alphaAdapter));
                }
                else {
                    Toast.makeText(getApplicationContext(),"No books Found",Toast.LENGTH_SHORT).show();
                    tv.setText(String.valueOf(0));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vErr",error.getMessage());
               StopProgress();
                Toast.makeText(getApplicationContext(),"Error in fetching",Toast.LENGTH_SHORT).show();
            }
        });

        mqueue.add(mreq);


    }

    public static int nthsearch(String str, char ch, int n){
        int pos=0;
        if(n!=0){
            for(int i=1; i<=n;i++){
                pos = str.indexOf(ch, pos)+1;
            }
            return pos;
        }
        else{
            return 0;
        }
    }

    public void VolleyUpdate(int yp){

        String Url = MyServerUrl+"updateBook";
        ed = sh.edit();
        JSONObject jo = new JSONObject();
        listinfo lf = data.get(ItemPosition);
        try {
            jo.put("username",sellername);
            jo.put("title",lf.title);
            jo.put("yourprice",yp);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest mreq1 = new JsonObjectRequest(Request.Method.PUT, Url, jo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String msg = response.getString("message");
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    ed.putBoolean("update",true);
                    ed.apply();
                    mqueue.getCache().invalidate(url,true);
                    VolleyConnect();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ed.clear();
                ed.apply();
                Toast.makeText(getApplicationContext(),"Error in Connecting",Toast.LENGTH_SHORT).show();
                Log.d("error:-",error.toString());
            }
        });

        mqueue.add(mreq1);

    }

    public void VolleyDel(){


        ed = sh.edit();
        listinfo lf = data.get(ItemPosition);
        String Url = MyServerUrl+"deletebook";
        Log.d("url",Url);
        JSONObject jo = new JSONObject();
        try {
            jo.put("username",sellername);
            jo.put("title",lf.title);
            Log.d("jo:",jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest mreq2 = new JsonObjectRequest(Request.Method.PUT, Url,jo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String msg = response.getString("msg");
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    ed.putBoolean("update",true);
                    ed.apply();
                    mqueue.getCache().invalidate(url,true);
                    VolleyConnect();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("jsonErr:",e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ed.clear();
                ed.apply();
                Toast.makeText(getApplicationContext(),"Error in Connecting",Toast.LENGTH_SHORT).show();
                Log.d("err:",error.toString());
            }
        });
        mqueue.add(mreq2);


    }



    @Override
    public void onsubmit1(boolean update,String price) {
        if (update){
            if (price!=null && price!="") {
                int p = Integer.parseInt(price);
                VolleyUpdate(p);
            }
            else {
                Toast.makeText(getApplicationContext(),"Empty Fields:"+price,Toast.LENGTH_SHORT).show();
            }
        }
        else {
            VolleyDel();
        }
    }
}

package a12developer.projectalpha20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import Api.Api;
import data.CommentItem;
import data.DetailItem;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {
    TextView tvDetailTitle, tvDetailContent;
    ImageView ivNo1, ivNo2, ivNo3, ivNo4;
    LinearLayout llNo1, llNo2;
    CardView cvDetail;
    EditText etComment;
    ListView lvComment;
    ArrayList<String> imgUrls;
    Button btnSetComment, btnBack;
    ImageButton ibZoom;
    int ori_id;
    String oriToken;
    ArrayList<CommentItem> arrayComments;
    CommentListAdapter adapter;
    int id;
    DetailItem detailItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        lvComment = (ListView) findViewById(R.id.lv_comments);
        View header = getLayoutInflater().inflate(R.layout.detail_header, null, false);
        lvComment.addHeaderView(header, null, false);

        tvDetailContent = (TextView) header.findViewById(R.id.tv_detailcontent);
        tvDetailTitle = (TextView) header.findViewById(R.id.tv_detailtitle);
        cvDetail = (CardView) header.findViewById(R.id.cv_detail);
        etComment = (EditText) header.findViewById(R.id.et_comment);

        ivNo1 = (ImageView) header.findViewById(R.id.iv_detailimg1);
        ivNo2 = (ImageView) header.findViewById(R.id.iv_detailimg2);
        ivNo3 = (ImageView) header.findViewById(R.id.iv_detailimg3);
        ivNo4 = (ImageView) header.findViewById(R.id.iv_detailimg4);
        llNo1 = (LinearLayout) header.findViewById(R.id.ll_no1);
        llNo2 = (LinearLayout) header.findViewById(R.id.ll_no2);
        ibZoom = (ImageButton) header.findViewById(R.id.btn_zoom);
        btnSetComment = (Button) header.findViewById(R.id.btn_setcomment);
        btnBack = (Button) findViewById(R.id.btn_backdetail);
        adapter = new CommentListAdapter();
        arrayComments = new ArrayList<>();



        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);

        oriToken = intent.getStringExtra("token");
        ori_id = intent.getIntExtra("id",0);
        GetDetailTask getDetailTask = new GetDetailTask();
        getDetailTask.execute(Api.GET_DETAIL_ITEM, String.valueOf(id));


        btnSetComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentPost = etComment.getText().toString();
                if(commentPost.getBytes().length <= 0){
                    Toast.makeText(DetailActivity.this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    setComment(commentPost,Api.USER_ID,ori_id);
                }

            }
        });
        ibZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent zoomIntent = new Intent(getApplicationContext(),ZoomPhotoActivity.class);
                zoomIntent.putStringArrayListExtra("photoImgs", imgUrls);
                startActivity(zoomIntent);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        lvComment.setAdapter(adapter);
        getCommnetData();
    }
    private void getCommnetData(){
        GetCommentTask getCommentTask = new GetCommentTask();
        getCommentTask.execute(String.valueOf(ori_id));
    }
    private void setDetail(DetailItem detailItems){
        this.detailItem = detailItems;
        imgUrls = new ArrayList<>();
        imgUrls.add(detailItem.getImgUrl());
        Glide.with(this)
                .load(detailItem.getImgUrl())
                .centerCrop()
                .into(ivNo1);
        if(!detailItems.getImgUrl_2().isEmpty()){
            imgUrls.add(detailItem.getImgUrl_2());
            Glide.with(this)
                    .load(detailItem.getImgUrl_2())
                    .centerCrop()
                    .into(ivNo2);
        }
        if (!detailItems.getImgUrl_3().isEmpty()){
            imgUrls.add(detailItem.getImgUrl_3());
            Glide.with(this)
                    .load(detailItem.getImgUrl_3())
                    .centerCrop()
                    .into(ivNo3);
        }
        if (!detailItems.getImgUrl_4().isEmpty()){
            imgUrls.add(detailItem.getImgUrl_4());
            Glide.with(this)
                    .load(detailItem.getImgUrl_4())
                    .centerCrop()
                    .into(ivNo4);
        }

        setImageToCardview(imgUrls.size());
        tvDetailTitle.setText(detailItems.getTitle());
        tvDetailContent.setText(detailItems.getContent());
    }

    private void setImageToCardview(int arraySize){
        switch (arraySize){
            case 1 :
                llNo2.setVisibility(View.GONE);
                ivNo3.setVisibility(View.GONE);
                break;
            case 2 :
                ivNo3.setVisibility(View.GONE);
                ivNo4.setVisibility(View.GONE);
                break;
            case 3 :
                ivNo4.setVisibility(View.GONE);
                break;
            case 4 :
                break;
        }
    }
    private void setComment(String content,String user_id,int ori_id){
        PrepareCommnetAsync prepareCommnetAsync = new PrepareCommnetAsync(content, user_id, ori_id);
        PostCommentTask postCommentTask = new PostCommentTask();
        postCommentTask.execute(prepareCommnetAsync);
    }
    private class PrepareCommnetAsync{
        String content;
        String user_id;
        int ori_id;

        public PrepareCommnetAsync(String content, String user_id, int ori_id) {
            this.content = content;
            this.user_id = user_id;
            this.ori_id = ori_id;
        }

        public String getContent() {
            return content;
        }

        public String getUser_id() {
            return user_id;
        }

        public int getOri_id() {
            return ori_id;
        }
    }

    private class PostCommentTask extends AsyncTask<PrepareCommnetAsync, Void, Boolean> {
        ProgressDialog progressDialog;
        String content;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(DetailActivity.this, "덧글 등록 중..", "잠시만 기다려 주세요", true, false);
        }

        @Override
        protected Boolean doInBackground(PrepareCommnetAsync... prepareCommnetAsyncs) {
            content = prepareCommnetAsyncs[0].getContent();
            String user_id = prepareCommnetAsyncs[0].getUser_id();
            int ori_id = prepareCommnetAsyncs[0].getOri_id();

            try {


                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_id", user_id)
                            .addFormDataPart("content", content)
                            .addFormDataPart("ori_id", String.valueOf(ori_id))
                            .build();



                Request request = new Request.Builder()
                        .url(Api.SET_COMMENT)
                        .post(requestBody)
                        .build();
                Response response = okHttpClient.newCall(request).execute();

                if (response.code() == 200) return true;
                else return false;

            } catch (IOException e) {
                Log.d("PostTask", "post failed", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);


            progressDialog.dismiss();
            if (aBoolean) {
                Toast.makeText(DetailActivity.this, "덧글이 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                Log.d("PostTask", "Success");
                getCommnetData();
                adapter.notifyDataSetChanged();
                SendNotificationTask sendTask = new SendNotificationTask();
                sendTask.execute(content);

            } else {
                Log.d("PostTask", "Failed");
                Toast.makeText(DetailActivity.this, "덧글 등록 실패!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class SendNotificationTask extends AsyncTask<String, Void, Boolean>{
        ProgressDialog progressDialog;
        public final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(DetailActivity.this, "알림 전송 중..", "잠시만 기다려 주세요", true, false);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String content = strings[0];
            try {

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .build();

                URL url = new URL("https://fcm.googleapis.com/fcm/send"); //Enter URL here
                /** 2개 이상의 내용 보내서 알림에 설정하기
                 * 알림 클릭시 detailpage로 이동시키기*/
                String requestForm = "{\"to\""+":"+"\""+oriToken+"\""+ ","
                        + "\"data\""+":" + "{"+"\"message\""+":"+"\""+content+"\""+","
                        +"\"id\""+":"+"\""+detailItem.getId()+"\""+","+"}"+"}";
                Log.d("requestFormTest", requestForm);
                RequestBody body = RequestBody.create(JSON, requestForm);

                Request request = new Request.Builder()
                            .addHeader("Content-Type","application/json")
                        .addHeader("Authorization","key=AAAAortHVmE:APA91bGtC1KjTf2JVtn3MYFMGi1w6IyVRkZ6fpSq-13aMb7LEw0_CAhdlrYPhBLJueFxJnViE9snEVZ9r5Mn4MHp1rq60OJRgQF00UefN3gY_J-wQ8Lx5QAqeBYkSip3k1CuVqQbddmI")
                        .url(url)
                        .post(body)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if(response.code()==200){
                    return true;
                }else{
                    return false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if(aBoolean){
                Toast.makeText(DetailActivity.this, "알림이 전송되었습니다.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(DetailActivity.this, "알림 전송 실패!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class CommentListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrayComments.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayComments.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tvComment, tvCommentDate, tvCommentId;
            View customView = view;
            if(customView == null){
                LayoutInflater inflater = getLayoutInflater();
                customView = inflater.inflate(R.layout.comment_detail,viewGroup,false);
            }
            tvComment = (TextView) customView.findViewById(R.id.tv_commentDetail);
            tvCommentDate = (TextView) customView.findViewById(R.id.tv_commnetdate);
            tvCommentId = (TextView) customView.findViewById(R.id.tv_commentid);
            tvComment.setText(arrayComments.get(i).getContent());
            tvCommentDate.setText(arrayComments.get(i).getCreated_at());
            tvCommentId.setText(arrayComments.get(i).getUser_id());

            return customView;
        }
    }
    private class GetCommentTask extends AsyncTask<String, Void, CommentItem[]> {
        //ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog = ProgressDialog.show(MainActivity.this,"데이터 로딩중","잠시만 기다려 주세요",true,false);
        }

        @Override
        protected CommentItem[] doInBackground(String... strings) {
            String ori_id_num = strings[0];
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("ori_id", ori_id_num)
                        .build();
            Request request = new Request.Builder()
                    .url(Api.GET_COMMENTS)
                    .post(requestBody)
                    .build();

            try {

                Response response = client.newCall(request).execute();
                Gson gson = new Gson();
                CommentItem[] comments = gson.fromJson(response.body().charStream(), CommentItem[].class);
                return comments;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(CommentItem[] commentItems) {
            super.onPostExecute(commentItems);
            arrayComments = null;
            arrayComments = new ArrayList<>();
            for(CommentItem comments : commentItems){
                arrayComments.add(comments);
            }
            adapter.notifyDataSetChanged();
            //progressDialog.dismiss();
        }
    }
    private class GetDetailTask extends AsyncTask<String, Void, DetailItem[]>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected DetailItem[] doInBackground(String... strings) {
            String url = strings[0];
            String id = strings[1];
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("id", id)
                            .build();
            Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();



            try {

                Response response = client.newCall(request).execute();
                Gson gson = new Gson();
                return gson.fromJson(response.body().charStream(), DetailItem[].class);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DetailItem[] detailItem) {
            super.onPostExecute(detailItem);
            if(detailItem != null) {
                setDetail(detailItem[0]);
            }
            else{
                Toast.makeText(DetailActivity.this, "다시 시도해 주세요", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(this,MainActivity.class);
        startActivity(mainIntent);
    }
}

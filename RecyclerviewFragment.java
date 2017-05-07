package a12developer.projectalpha20;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import data.PostItem;
import layout.Header;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecyclerviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecyclerviewFragment extends Fragment implements
        SwipyRefreshLayout.OnRefreshListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String API_CATEGORY = "api_category";
    private static final String API_TYPE = "api_type";
    private static final String SEARCH_WORD = "searchword";

    public static final int BY_DISTANCE = 100;
    public static final int BY_TIME = 101;
    // TODO: Rename and change types of parameters
    private String api;
    private int type;
    String searchWord;

    RecyclerView recyclerView;
    MyRecyclerAdapter adapter;
    ArrayList<PostItem> arrayList, arrayListUse;
    int numOfItems = 0;
    double user_lat;
    double user_lng;
    LocationManager locationManager;
    LocationListener listener;
    Location userLocation;
    TextView tvPullToLoad;
    SwipyRefreshLayout srlMainView;


    public RecyclerviewFragment() {
        // Required empty public constructor
    }


    public static RecyclerviewFragment newInstance(String apiCategory, int apiType, String searchWord) {
        RecyclerviewFragment fragment = new RecyclerviewFragment();
        Bundle args = new Bundle();
        args.putString(API_CATEGORY, apiCategory);
        args.putInt(API_TYPE, apiType);
        args.putString(SEARCH_WORD, searchWord);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            api = getArguments().getString(API_CATEGORY);
            type = getArguments().getInt(API_TYPE);
            searchWord = getArguments().getString(SEARCH_WORD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View baseView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        recyclerView = (RecyclerView) baseView.findViewById(R.id.f_rv_main);
        srlMainView = (SwipyRefreshLayout) baseView.findViewById(R.id.srl_mainview);
        tvPullToLoad = (TextView) baseView.findViewById(R.id.tv_pulltoload);
        startDataLoading(api,type);

        srlMainView.setOnRefreshListener(this);
        srlMainView.setColorSchemeColors(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW);
        srlMainView.setDistanceToTriggerSync(100);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                if(lastVisibleItemPosition == arrayList.size()){
                    tvPullToLoad.setText("더 이상 등록된 아이템이 없습니다.");
                    tvPullToLoad.setVisibility(View.VISIBLE);
                }
                else if (lastVisibleItemPosition == itemTotalCount) {
                    tvPullToLoad.setText("당겨서 더보기");
                    tvPullToLoad.setVisibility(View.VISIBLE);
                }else{
                    tvPullToLoad.setVisibility(View.GONE);
                }
            }
        });


        return baseView;
    }
    public static int convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
    public void fetchAsyncPost(String api, int type) {
        arrayList = new ArrayList<>();
        arrayListUse = new ArrayList<>();
        FetchPostTask fetchPostTask = new FetchPostTask();
        fetchPostTask.execute(new ServerDataRequest(api,type));
    }
    private void startDataLoading(String api, int type) {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new MyRecyclerAdapter(new Header());
        recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
            @Override
            public int getSpanSize(int position) {
                if(position == 0){
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        getMapPoint();
        fetchAsyncPost(api, type);
    }
    public class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            user_lng = location.getLongitude();
            user_lat = location.getLatitude();

            Log.d("locationcheck", user_lng+" :: " + user_lat);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
    private Location getMapPoint() {
        long minTime = 10000;
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime , 0 , listener);

        Location curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(curLocation == null){
            curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        user_lat = curLocation.getLatitude();
        user_lng = curLocation.getLongitude();
        if(curLocation == null){
            user_lat = 37.5757166;
            user_lng = 126.976883;
        }

        return curLocation;
    }


    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        if(direction == SwipyRefreshLayoutDirection.TOP){
            adapter.notifyDataSetChanged();
            srlMainView.setRefreshing(false);
        }
        if(direction == SwipyRefreshLayoutDirection.BOTTOM){
            add10Items();
            adapter.notifyDataSetChanged();
            srlMainView.setRefreshing(false);
        }
    }

    class VHItem extends RecyclerView.ViewHolder{
        TextView tvTitle, tvTimeBefore, tvDistance, tvCategory;
        ImageView ivImg;
        CardView cvMain;
        LinearLayout llMainItem;
        public VHItem(View itemView) {
            super(itemView);

            tvDistance = (TextView) itemView.findViewById(R.id.tv_distance);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            ivImg = (ImageView) itemView.findViewById(R.id.iv_mainitem);
            cvMain = (CardView) itemView.findViewById(R.id.cv_main);
            tvTimeBefore = (TextView) itemView.findViewById(R.id.tv_timeBefore);
            llMainItem = (LinearLayout) itemView.findViewById(R.id.ll_mainitem);
            tvCategory = (TextView) itemView.findViewById(R.id.tv_category);
        }
    }
    class VHHeader extends RecyclerView.ViewHolder{
        public VHHeader(View itemView) {
            super(itemView);
        }
    }
    public class MyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        Header header;


        public MyRecyclerAdapter(Header header) {
            this.header = header;

        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_HEADER){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header,parent,false);
                return new VHHeader(v);
            }
            else{
                View baseView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_mainitem, null);
                return new VHItem(baseView);}
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof VHHeader)
            {
                VHHeader VHheader = (VHHeader)holder;
            }
            else
            {
                final PostItem item = getItem(position-1);
                VHItem VHitem = (VHItem)holder;
                final String imgUrl = item.getImgUrl();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                CharSequence timeAgo=null;
                try {
                    Date createdDate = dateFormat.parse(item.getCreated_at());
                    Date presentDate = new Date();
                    long timer = presentDate.getTime()- createdDate.getTime();
                    String s = String.valueOf(timer);
                    Log.d("timer", s);
                    timeAgo = DateUtils.getRelativeTimeSpanString (
                            createdDate.getTime(),
                            presentDate.getTime(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_NUMERIC_DATE);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Location getLocation = new Location("pointGet");
                getLocation.setLatitude(Double.valueOf(item.getLat()));
                getLocation.setLongitude(Double.valueOf(item.getLng()));
                userLocation = getMapPoint();
                double distance = Math.round(getLocation.distanceTo(userLocation));
                String categoryText = getCategoryText(Integer.valueOf(item.getCategory()));
                Glide.with(getContext())
                        .load(imgUrl)
                        .crossFade()
                        .into(VHitem.ivImg);
                VHitem.tvTitle.setText(item.getTitle());
                VHitem.tvTimeBefore.setText(timeAgo.toString());
                VHitem.tvDistance.setText(getDistanceText(distance));
                VHitem.tvCategory.setText(categoryText);
                VHitem.llMainItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), DetailActivity.class);
                        intent.putExtra("id", item.getId());
                        startActivity(intent);
                    }
                });
            }
        }

        private PostItem getItem(int i) {
            return arrayListUse.get(i);
        }
        /* public int getItemViewType(int position) {
             if(isPositionHeader(position)) {
                 return TYPE_HEADER;
             }else if (isPositionFooter(position)){
                 return TYPE_FOOTER;
             }
             else {
                 return TYPE_ITEM;
             }
         }*/
        public int getItemViewType(int position) {
            if(isPositionHeader(position)) {
                return TYPE_HEADER;
            }
            else {
                return TYPE_ITEM;
            }
        }
        private boolean isPositionHeader(int position)
        {
            return position == 0;
        }

        private String getCategoryText(int i){
            switch(i){
                case 1 :
                    return "유아동";
                case 2 :
                    return "가구";
                case 3 :
                    return "전기제품";
                case 4 :
                    return "의류/잡화";
                case 5 :
                    return "도서";
                case 6 :
                    return "컴퓨터";
                case 7 :
                    return "기타";
                default:
                    return "카테고리없음";
            }
        }



        @Override
        public int getItemCount() {
            return arrayListUse.size()+1;
        }


    }

    private String getDistanceText(double distance){
        if(distance <= 999){
            int distanceInt = (int)Math.ceil(distance);
            return distanceInt+"m";
        }

        if(distance > 1000 && distance <= 2000){
            String pattern = "####.#";
            DecimalFormat decimalFormat = new DecimalFormat(pattern);
            return decimalFormat.format(distance)+"m";
        }
        if(distance > 2000){

            int distanceInt = (int) Math.ceil(distance/1000);
            return distanceInt+"km 이내";
        }
        return "거리정보없음";
    }
    class ServerDataRequest{
        String serverName;
        int type;

        public ServerDataRequest(String serverName, int type) {
            this.serverName = serverName;
            this.type = type;
        }

        public String getServerName() {
            return serverName;
        }

        public int getType() {
            return type;
        }
    }
    class FetchPostTask extends AsyncTask<ServerDataRequest, Void, PostItem[]> {
        //ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog = ProgressDialog.show(MainActivity.this,"데이터 로딩중","잠시만 기다려 주세요",true,false);
        }

        @Override
        protected PostItem[] doInBackground(ServerDataRequest... serverDataRequest) {
            String url = serverDataRequest[0].getServerName();
            int dataType = serverDataRequest[0].getType();
            OkHttpClient client = new OkHttpClient();;
            Request request = null;
            RequestBody requestBody = null;
            if(searchWord.length() > 0){
                if(dataType == BY_TIME){
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("searchword", searchWord)
                            .build();
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();}
                if(dataType == BY_DISTANCE) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("searchword", searchWord)
                            .addFormDataPart("user_lat", String.valueOf(user_lat))
                            .addFormDataPart("user_lng", String.valueOf(user_lng))
                            .build();
                    //.addFormDataPart("selected_category", String.valueOf(category))
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                }
            }
            else{
                if(dataType == BY_TIME){
                    /*RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .build();*/
                    //.addFormDataPart("selected_category", String.valueOf(category))
                    request = new Request.Builder()
                            .url(url)
                            //.post(requestBody)
                            .build();}
                if(dataType == BY_DISTANCE) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_lat", String.valueOf(user_lat))
                            .addFormDataPart("user_lng", String.valueOf(user_lng))
                            .build();
                    //.addFormDataPart("selected_category", String.valueOf(category))
                    request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                }
            }

            try {

                Response response = client.newCall(request).execute();
                Gson gson = new Gson();
                PostItem[] posts = gson.fromJson(response.body().charStream(), PostItem[].class);
                return posts;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(PostItem[] postItems) {
            super.onPostExecute(postItems);
            arrayList = null;
            arrayList = new ArrayList<>();
            arrayListUse = null;
            arrayListUse = new ArrayList<>();
            numOfItems = 0;
            for(PostItem post : postItems){
                arrayList.add(post);
            }
            add10Items();
            adapter.notifyDataSetChanged();
            //progressDialog.dismiss();
        }
    }
    private void add10Items(){
        int repeatNum;
        if(arrayList.size()-arrayListUse.size()>10){
            repeatNum = 10;
        }
        else{
            repeatNum = arrayList.size()-arrayListUse.size();
        }
        for(int i = 0; i < repeatNum; i++){
            arrayListUse.add(arrayList.get(numOfItems));
            numOfItems++;
        }
        tvPullToLoad.setVisibility(View.GONE);

    }

}


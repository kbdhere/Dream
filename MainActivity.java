package a12developer.projectalpha20;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import Api.Api;
import layout.BackPressCloseHandler;

public class MainActivity extends AppCompatActivity{

    public static String user_post_id;
    public static boolean firstConnect = true;

    RadioButton rbDistance, rbTime;
    RadioGroup rgSortType;
    ImageButton ibUpload, ibSearch;
    TabLayout tlTabs,tlTabsTime;
    Fragment[] arrFragment, arrFragmentTime;
    MainPagerAdapter adapter, adapterTime;
    ViewPager vpMain, vpTime;

    private BackPressCloseHandler backPressCloseHandler;
    public static final int BY_DISTANCE = 100;
    public static final int BY_TIME = 101;
    public static final int START_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(this);
        setContentView(R.layout.activity_main);
        if(firstConnect){
            permissionCheck();
            Intent startIntent = new Intent(this,StartActivity.class);
            startActivityForResult(startIntent, START_REQUEST);
        }


        tlTabs = (TabLayout) findViewById(R.id.tl_tabs);
        tlTabs.setSelectedTabIndicatorColor(Color.rgb(0,119,199));
        tlTabsTime = (TabLayout) findViewById(R.id.tl_tabstime);
        tlTabsTime.setSelectedTabIndicatorColor(Color.rgb(0,119,199));
        vpMain = (ViewPager) findViewById(R.id.vp_main);
        vpTime = (ViewPager) findViewById(R.id.vp_time);



        permissionCheck();

        rbDistance = (RadioButton) findViewById(R.id.rb_distance);
        rbTime = (RadioButton) findViewById(R.id.rb_time);
        rgSortType = (RadioGroup) findViewById(R.id.rg_sorttype);
        ibUpload = (ImageButton) findViewById(R.id.ib_upload);
        ibSearch = (ImageButton) findViewById(R.id.ib_search);


        backPressCloseHandler = new BackPressCloseHandler(this);
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("tokenFcm",token);

        FirebaseInstanceId.getInstance().getToken();










        ibUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent uploadIntent = new Intent(MainActivity.this,UploadOption.class);
                startActivity(uploadIntent);
            }
        });
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearch();
            }
        });

        rbDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    vpMain.setVisibility(View.VISIBLE);
                    tlTabs.setVisibility(View.VISIBLE);
                    vpTime.setVisibility(View.GONE);
                    tlTabsTime.setVisibility(View.GONE);
                }
            }
        });
        rbTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    vpTime.setVisibility(View.VISIBLE);
                    tlTabsTime.setVisibility(View.VISIBLE);
                    vpMain.setVisibility(View.GONE);
                    tlTabs.setVisibility(View.GONE);
                }
            }
        });

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        if(id >=0){
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("id", id);
            startActivity(detailIntent);
        }
        //srlMainView.setProgressViewOffset(false, dpToPixel(125), dpToPixel(150));
    }
    public static void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.rgb(91,155,211));
        }
    }
    private void startSearch(){
        Intent searchIntent = new Intent(this,SearchActivity.class);
        startActivity(searchIntent);
    }
    class MainPagerAdapter extends FragmentStatePagerAdapter {

        Fragment[] arrFragments;

        public MainPagerAdapter(FragmentManager fm, Fragment[] arrFragments) {
            super(fm);
            this.arrFragments = arrFragments;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "전체보기";
                case 1:
                    return "유아동";
                case 2:
                    return "가구";
                case 3:
                    return "가전";
                case 4:
                    return "의류";
                case 5:
                    return "도서류";
                case 6:
                    return "컴퓨터";
                case 7:
                    return "기타";
                default:
                    return "";
            }
        }

        @Override
        public Fragment getItem(int position) {
            return arrFragments[position];
        }

        @Override
        public int getCount() {
            return arrFragments.length;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == START_REQUEST && resultCode == RESULT_OK){
            String user_id = data.getStringExtra("user_id");
            String user_displayName = data.getStringExtra("user_displayName");
            String user_token = data.getStringExtra("user_token");
            String user_email = data.getStringExtra("user_email");
            user_post_id = user_email;
            //tvUserId.setText(user_id);
            firstConnect = false;
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    private void permissionCheck() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                startFragment();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        new TedPermission(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한을 설정하지 않으시면 서비스 이용이 어렵습니다. [설정] > [권한]에서 권한을 허용하여 주십시요.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE)
                .check();
    }

    private void startFragment() {
        setFragmentDistance();
        setFragmentTime();
        adapter = new MainPagerAdapter(getSupportFragmentManager(),arrFragment);
        adapterTime = new MainPagerAdapter(getSupportFragmentManager(),arrFragmentTime);
        vpTime.setAdapter(adapterTime);
        vpMain.setAdapter(adapter);
        tlTabs.setupWithViewPager(vpMain);
        tlTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tlTabsTime.setupWithViewPager(vpTime);
        tlTabsTime.setTabMode(TabLayout.MODE_SCROLLABLE);
    }
    private void setFragmentDistance(){
        arrFragment = new Fragment[8];
        arrFragment[0] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE,BY_DISTANCE,"");
        arrFragment[1] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_CHILD,BY_DISTANCE,"");
        arrFragment[2] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_FURNITURE,BY_DISTANCE,"");
        arrFragment[3] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_ELECTRIC,BY_DISTANCE,"");
        arrFragment[4] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_CLOTH,BY_DISTANCE,"");
        arrFragment[5] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_BOOKS,BY_DISTANCE,"");
        arrFragment[6] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_COMPUTER,BY_DISTANCE,"");
        arrFragment[7] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_ETC,BY_DISTANCE,"");





    }
    private void setFragmentTime(){
        arrFragmentTime = new Fragment[8];
        arrFragmentTime[0] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY,BY_TIME,"");
        arrFragmentTime[1] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_CHILD,BY_TIME,"");
        arrFragmentTime[2] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_FURNITURE,BY_TIME,"");
        arrFragmentTime[3] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_ELECTRIC,BY_TIME,"");
        arrFragmentTime[4] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_CLOTH,BY_TIME,"");
        arrFragmentTime[5] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_BOOKS,BY_TIME,"");
        arrFragmentTime[6] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_COMPUTER,BY_TIME,"");
        arrFragmentTime[7] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_CATEGORY_ETC,BY_TIME,"");
    }

}


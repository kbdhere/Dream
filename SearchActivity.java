package a12developer.projectalpha20;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import Api.Api;


public class SearchActivity extends AppCompatActivity{
    ViewPager vpSearch;
    TabLayout tlSearch;
    Fragment[] arrFragmentSearch;
    String searchWord;
    Button btnBack;
    ImageButton ibSearch;
    EditText etSearchWord;
    RelativeLayout rlSearch;
    SearchPagerAdapter adapter;
    public static final int BY_DISTANCE = 100;
    public static final int BY_TIME = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setStatusBarColor(this);
        setContentView(R.layout.activity_search);

        vpSearch = (ViewPager) findViewById(R.id.vp_search);
        tlSearch = (TabLayout) findViewById(R.id.tl_search);
        btnBack = (Button) findViewById(R.id.btn_backfromsearch);
        ibSearch = (ImageButton) findViewById(R.id.ib_searchenter);
        etSearchWord = (EditText) findViewById(R.id.et_searchword);
        rlSearch = (RelativeLayout) findViewById(R.id.rl_search);

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    searchButtonClickEvent();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void searchButtonClickEvent(){
        searchWord = etSearchWord.getText().toString();

        arrFragmentSearch = new Fragment[2];
        arrFragmentSearch[0] = RecyclerviewFragment.newInstance(Api.GET_POST_DISTANCE_SEARCH,BY_DISTANCE,searchWord);
        arrFragmentSearch[1] = RecyclerviewFragment.newInstance(Api.GET_POST_WITH_SEARCH,BY_TIME,searchWord);

        adapter = new SearchPagerAdapter(getSupportFragmentManager());
        adapter.notifyDataSetChanged();
        vpSearch = (ViewPager) findViewById(R.id.vp_search);
        vpSearch.setAdapter(adapter);
        tlSearch.setupWithViewPager(vpSearch);
    }


    private class SearchPagerAdapter extends FragmentStatePagerAdapter {

        private Map<Integer, String> mFragmentTags;
        FragmentManager fm;

        public SearchPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
            this.mFragmentTags = new HashMap<Integer, String>();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "거리순";
                case 1:
                    return "시간순";
                default:
                    return "";
            }
        }

        @Override
        public Fragment getItem(int position) {
            return arrFragmentSearch[position];
        }

        @Override
        public int getCount() {
            return arrFragmentSearch.length;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object object = super.instantiateItem(container, position);
            if (object instanceof Fragment) {
                Fragment fragment = (Fragment) object;
                String tag = fragment.getTag();
                mFragmentTags.put(position, tag);
            }
            return object;
        }
        public Fragment getFragment(int position) {
            Fragment fragment = null;
            String tag = mFragmentTags.get(position);
            if (tag != null) {
                fragment = fm.findFragmentByTag(tag);
            }
            return fragment;
        }
    }
}

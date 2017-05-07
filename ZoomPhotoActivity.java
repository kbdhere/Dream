package a12developer.projectalpha20;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class ZoomPhotoActivity extends AppCompatActivity {
    CustomViewPager vpZoom;
    ArrayList<String> photoImgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_photo);

        Intent getIntent = getIntent();
        photoImgs = getIntent.getStringArrayListExtra("photoImgs");

        vpZoom = (CustomViewPager) findViewById(R.id.vp_zoom);
        ZoomViewPagerAdapter adapter = new ZoomViewPagerAdapter(photoImgs, this);
        vpZoom.setAdapter(adapter);
    }

    private class ZoomViewPagerAdapter extends PagerAdapter{
        ArrayList<String> imgs;
        Context context;

        public ZoomViewPagerAdapter(ArrayList<String> imgs, Context context) {
            this.imgs = imgs;
            this.context = context;
        }

        @Override
        public int getCount() {
            return imgs.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(getApplicationContext());
            Glide.with(context)
                    .load(imgs.get(position))
                    .into(photoView);

            container.addView(photoView);

            return photoView;
        }

    }
}

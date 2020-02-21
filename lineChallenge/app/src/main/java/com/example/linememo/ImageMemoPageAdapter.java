package com.example.linememo;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class ImageMemoPageAdapter extends PagerAdapter
{
    private Context context = null;
    private LayoutInflater inflater;
    private Uri[] imageUris ;

    public ImageMemoPageAdapter(Context context)
    {
        this.context = context;
    }

    public void setImageUris(Uri[] imageUris)
    {
        this.imageUris = imageUris;
    }

    public Uri[] getImageUris() {
        return imageUris;
    }

    @Override
    public int getCount() {
        if(imageUris !=null) return imageUris.length;
        else return 0;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;
        if (context != null) {
            // LayoutInflater를 통해 "/res/layout/layout_page.xml"을 뷰로 생성.
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_page, container, false);
            ImageView image = (ImageView) view.findViewById(R.id.memoImage);
            image.setImageURI(imageUris[position]); // 이미지 위치 찾아 제공
            image.setScaleType(ImageView.ScaleType.FIT_XY); //이미지 가득 채우기
        }
        // 뷰페이저에 추가.
        container.addView(view) ;
        return view ;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);//false면 이미지 이미지 구현 안됨
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();
    }

    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }
}

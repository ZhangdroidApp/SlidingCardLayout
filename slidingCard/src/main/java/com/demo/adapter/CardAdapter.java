package com.demo.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.demo.entity.CardModel;
import com.zhangdroid.R;
import com.zhangdroid.library.adapter.abslistview.BaseAdapterHelper;
import com.zhangdroid.library.adapter.abslistview.CommonAbsListViewAdapter;
import com.zhangdroid.library.image.ImageLoader;
import com.zhangdroid.library.image.ImageLoaderUtil;

import java.util.List;

/**
 * Created by zhangdroid on 2016/10/13.
 */
public class CardAdapter extends CommonAbsListViewAdapter<CardModel> {

    public CardAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    public CardAdapter(Context context, int layoutResId, List<CardModel> list) {
        super(context, layoutResId, list);
    }

    @Override
    protected void convert(int position, BaseAdapterHelper helper, CardModel bean) {
        if (bean != null) {
            ImageView imageView = (ImageView) helper.getView(R.id.item_card_avatar);
            ImageLoaderUtil.getInstance().loadImage(mContext, new ImageLoader.Builder().url(bean.getThumbnailUrl()).imageView(imageView).build());
            helper.setText(R.id.item_card_nickname, bean.getNickname());
            helper.setText(R.id.item_card_age, String.valueOf(bean.getAge()));
            helper.setText(R.id.item_card_location, String.valueOf(bean.getLocation()));
        }
    }

}

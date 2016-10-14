package com.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.demo.adapter.CardAdapter;
import com.demo.entity.CardModel;
import com.zhangdroid.R;
import com.zhangdroid.widgets.SlidingCardLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.slidingCardLayout)
    SlidingCardLayout mSlidingCardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        List<CardModel> list = new ArrayList<CardModel>();
        list.add(createModel("妹子1", 18, "北京", "http://c.hiphotos.baidu.com/image/h%3D360/sign=2b7f667d39c79f3d90e1e2368aa0cdbc/f636afc379310a55515bfd76b54543a982261030.jpg"));
        list.add(createModel("妹子2", 19, "北京", "http://f.hiphotos.baidu.com/image/h%3D360/sign=827c3174af345982da8ae3943cf5310b/9358d109b3de9c82c4f95c8f6e81800a19d84315.jpg"));
        list.add(createModel("妹子3", 20, "北京", "http://h.hiphotos.baidu.com/image/h%3D360/sign=ea11a7a30cf41bd5c553eef261db81a0/f9198618367adab4dde25bcb89d4b31c8701e40c.jpg"));
        list.add(createModel("妹子4", 18, "上海", "http://g.hiphotos.baidu.com/image/h%3D360/sign=f99935df74c6a7efa626ae20cdfbafe9/f9dcd100baa1cd116c03e220bb12c8fcc3ce2d7c.jpg"));
        list.add(createModel("妹子5", 19, "上海", "http://a.hiphotos.baidu.com/image/h%3D360/sign=d6a3d89c0d2442a7b10efba3e142ad95/4d086e061d950a7b4afaacdb08d162d9f3d3c9e6.jpg"));
        list.add(createModel("妹子6", 20, "上海", "http://f.hiphotos.baidu.com/image/h%3D360/sign=e75511800ed79123ffe092729d365917/48540923dd54564ebf8da5b1b1de9c82d0584f49.jpg"));
        list.add(createModel("妹子7", 18, "广州", "http://g.hiphotos.baidu.com/image/h%3D360/sign=93b0f8aa38f33a87816d061cf65d1018/8d5494eef01f3a296b7cae549b25bc315d607cf7.jpg"));
        list.add(createModel("妹子8", 19, "广州", "http://c.hiphotos.baidu.com/image/h%3D360/sign=8aee851dd62a28345ca6300d6bb4c92e/e61190ef76c6a7ef24ac3d26fffaaf51f3de662c.jpg"));
        list.add(createModel("妹子9", 20, "广州", "http://c.hiphotos.baidu.com/image/h%3D360/sign=3a204fab013b5bb5a1d726f806d3d523/a6efce1b9d16fdfabd4588eeb68f8c5494ee7b80.jpg"));
        list.add(createModel("妹子10", 18, "深圳", "http://h.hiphotos.baidu.com/image/h%3D360/sign=073bc0a0347adab422d01d45bbd5b36b/f31fbe096b63f62494424a8b8544ebf81a4ca31f.jpg"));
        list.add(createModel("妹子11", 19, "深圳", "http://c.hiphotos.baidu.com/image/h%3D360/sign=7cfcfcf67ed98d1069d40a37113fb807/838ba61ea8d3fd1f1315c830324e251f95ca5f9f.jpg"));
        list.add(createModel("妹子12", 20, "深圳", "http://c.hiphotos.baidu.com/image/h%3D360/sign=2bd759df74094b36c4921deb93cd7c00/810a19d8bc3eb135298010bba41ea8d3fd1f446e.jpg"));
        list.add(createModel("妹子13", 18, "安庆", "http://g.hiphotos.baidu.com/image/h%3D360/sign=45a1fdfbfa1986185e47e9827aec2e69/7acb0a46f21fbe099dd9bb8e69600c338644ada1.jpg"));
        list.add(createModel("妹子14", 19, "安庆", "http://b.hiphotos.baidu.com/image/h%3D360/sign=c4f33f61f1deb48fe469a7d8c01f3aef/b812c8fcc3cec3fdc50a0275d488d43f879427fd.jpg"));
//        list.add(createModel("妹子15", 20, "安庆", ""));
//        list.add(createModel("妹子16", 21, "安庆", ""));
//        list.add(createModel("妹子17", 18, "杭州", ""));
//        list.add(createModel("妹子18", 19, "杭州", ""));
//        list.add(createModel("妹子19", 20, "杭州", ""));
//        list.add(createModel("妹子20", 21, "杭州", ""));
        CardAdapter cardAdapter = new CardAdapter(this, R.layout.item_cardview, list);
        mSlidingCardLayout.setAdapter(cardAdapter);
    }

    private CardModel createModel(String nickname, int age, String location, String thumbnailUrl) {
        CardModel cardModel = new CardModel();
        cardModel.setNickname(nickname);
        cardModel.setAge(age);
        cardModel.setLocation(location);
        cardModel.setThumbnailUrl(thumbnailUrl);
        cardModel.setImgCnt(new Random().nextInt(10));
        return cardModel;
    }

}

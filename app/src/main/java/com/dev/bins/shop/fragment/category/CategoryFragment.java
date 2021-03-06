package com.dev.bins.shop.fragment.category;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.dev.bins.shop.R;
import com.dev.bins.shop.bean.Banner;
import com.dev.bins.shop.bean.Category;
import com.dev.bins.shop.bean.Goods;
import com.dev.bins.shop.bean.GoodsItem;
import com.dev.bins.shop.bean.ResponseMsg;
import com.dev.bins.shop.fragment.BaseFragment;
import com.dev.bins.shop.net.NetworkManager;
import com.dev.bins.shop.widget.MyToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Subscriber;
import rx.Subscription;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    List<Category> mCategories = new ArrayList<>();
    List<GoodsItem> mCategoryDatas = new ArrayList<>();
    @BindView(R.id.slider)
    SliderLayout mSliderLayout;
    @BindView(R.id.category_toolbar)
    MyToolbar mToolbar;
    @BindView(R.id.item)
    RecyclerView mItemRecyclerView;
    @BindView(R.id.content)
    RecyclerView mContentRecyclerView;
    @BindView(R.id.swipe_content)
    SwipeRefreshLayout mContentRefresh;
    CategoryContentAdapter mContentAdapter;
    CategoryItemAdapter mItemAdapter;
    GestureDetectorCompat mItemGestureDetectorCompat;
    private List<Banner> mBanner = new ArrayList<>();

    public CategoryFragment() {
        // Required empty public constructor
    }

    public static CategoryFragment newInstance() {

        Bundle args = new Bundle();

        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_category;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        addBanner();
        initItemRecycler(view);
        initCategoryContent(view);
    }

    private void initCategoryContent(View view) {
        mContentRefresh.setOnRefreshListener(this);
        mContentAdapter = new CategoryContentAdapter(mCategoryDatas);
        mContentRecyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        mContentRecyclerView.setAdapter(mContentAdapter);
        loadData();

    }

    private void loadData() {
        loadData(1);
    }

    private void loadData(int cid) {
        Subscriber<ResponseMsg<Goods>> subscriber =new Subscriber<ResponseMsg<Goods>>() {
            @Override
            public void onCompleted() {
                mContentRefresh.setRefreshing(false);
                mContentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mContentRefresh.setRefreshing(false);
            }

            @Override
            public void onNext(ResponseMsg<Goods> goodsResponseMsg) {
                mCategoryDatas.clear();
                mCategoryDatas.addAll(goodsResponseMsg.getData().getGoods());
            }
        };

        NetworkManager.getInstance().getCategoryData(subscriber, cid, 1, 10);

    }


    private void addBanner() {
        Subscriber<ResponseMsg<List<Banner>>> subscriber = new Subscriber<ResponseMsg<List<Banner>>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ResponseMsg<List<Banner>> listResponseMsg) {
                mBanner.clear();
                mBanner.addAll(listResponseMsg.getData());
                for (Banner banner : mBanner) {

                    TextSliderView textSliderView = new TextSliderView(getActivity());
                    textSliderView.image(banner.getImgUrl());
                    textSliderView.description(banner.getName());
                    textSliderView.setScaleType(BaseSliderView.ScaleType.Fit);
                    mSliderLayout.addSlider(textSliderView);
                }
            }
        };

        NetworkManager.getInstance().getBanner(subscriber);
    }

    private void initItemRecycler(View view) {
        mItemAdapter = new CategoryItemAdapter(mCategories);
        mItemRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mItemRecyclerView.setAdapter(mItemAdapter);
        mItemGestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                View child = mItemRecyclerView.findChildViewUnder(x, y);
                int position = mItemRecyclerView.getChildLayoutPosition(child);
                loadData(position + 1);
                return true;
            }
        });

        mItemRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                mItemGestureDetectorCompat.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                mItemGestureDetectorCompat.onTouchEvent(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        Subscriber<ResponseMsg<List<Category>>> subscriber = new Subscriber<ResponseMsg<List<Category>>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ResponseMsg<List<Category>> listResponseMsg) {
                mCategories.clear();
                mCategories.addAll(listResponseMsg.getData());
                mItemAdapter.notifyDataSetChanged();
            }
        };
        NetworkManager.getInstance().getCategory(subscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mSubscriptions) {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}

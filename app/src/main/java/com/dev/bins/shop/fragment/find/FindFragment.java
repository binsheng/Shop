package com.dev.bins.shop.fragment.find;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dev.bins.shop.R;
import com.dev.bins.shop.bean.Goods;
import com.dev.bins.shop.bean.GoodsItem;
import com.dev.bins.shop.fragment.BaseFragment;
import com.dev.bins.shop.net.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<GoodsItem> mGoods = new ArrayList<>();
    private int curPage = 1;
    @BindView(R.id.hot_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;
    FindAdapter mAdapter;

    public FindFragment() {
        // Required empty public constructor
    }

    public static FindFragment newInstance() {

        Bundle args = new Bundle();

        FindFragment fragment = new FindFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_hot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        mAdapter = new FindAdapter(mGoods);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        load(1);
    }

    public void load(int page) {
        Subscriber<Goods> subscriber = new Subscriber<Goods>() {
            @Override
            public void onCompleted() {
                mAdapter.notifyDataSetChanged();
                mSwipe.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mSwipe.setRefreshing(false);
                e.printStackTrace();
            }

            @Override
            public void onNext(Goods goodses) {
                mGoods.clear();
                mGoods.addAll(goodses.getGoods());
            }
        };
        NetworkManager.getInstance().getGoods(subscriber, page, 10);
//        mSubscriptions.add(subscription);

    }



    @Override
    public void onRefresh() {
        load(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mSubscriptions) {
            mSubscriptions.unsubscribe();
        }
    }
}
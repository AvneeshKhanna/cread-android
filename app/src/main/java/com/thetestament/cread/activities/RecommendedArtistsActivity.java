package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.thetestament.cread.R;

import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * AppCompat activity class to list of artists who are yet to be followed by user..
 */

public class RecommendedArtistsActivity extends BaseActivity {

    /*@BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_recommended_artists);
        setContentView(R.layout.item_recommended_artists);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }
}

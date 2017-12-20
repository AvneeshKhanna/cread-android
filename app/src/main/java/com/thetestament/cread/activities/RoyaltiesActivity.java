package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.RoyaltiesAdapter;
import com.thetestament.cread.fragments.RoyaltiesFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.RoyaltiesModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.utils.Constant.TAG_ROYALTIES_FRAGMENT;

public class RoyaltiesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_royalties);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Royalties");

        //initialize view
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*Method to initialize views for this screen*/


    private void initView() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.royalties_container, new RoyaltiesFragment(), TAG_ROYALTIES_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}

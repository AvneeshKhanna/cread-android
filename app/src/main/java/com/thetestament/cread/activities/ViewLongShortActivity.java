package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.ViewLongShortFragment;
import com.thetestament.cread.models.ShortModel;
import com.thetestament.cread.utils.Constant;

import icepick.State;

public class ViewLongShortActivity extends BaseActivity {

    ViewLongShortFragment mLongShortFragment;

    @State
    boolean mIsReadingMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_long_short);
        //init screen
        initView();

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
        //inflate this menu
        getMenuInflater().inflate(R.menu.menu_view_longshort, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_reading_mode:
                // toggle flag
                mIsReadingMode = !mIsReadingMode;
                // toggle reading mode
                mLongShortFragment.toggleReadingMode(mIsReadingMode);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Method to initialize this screen*/
    private void initView() {

        mLongShortFragment = new ViewLongShortFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("shortData", retrieveIntentData());
        mLongShortFragment.setArguments(bundle);
        //open fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.view_longshort_container, mLongShortFragment, Constant.TAG_VIEW_LONG_SHORT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private ShortModel retrieveIntentData() {
        return getIntent().getParcelableExtra(Constant.EXTRA_SHORT_DATA);
    }


}

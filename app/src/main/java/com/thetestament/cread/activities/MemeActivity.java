package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.MemeLayoutAdapter;
import com.thetestament.cread.fragments.MemeFifthFragment;
import com.thetestament.cread.fragments.MemeFirstFragment;
import com.thetestament.cread.fragments.MemeFourthFragment;
import com.thetestament.cread.fragments.MemeSecondFragment;
import com.thetestament.cread.fragments.MemeThirdFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.MemeLayoutModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * Appcompat activity for Meme creation.
 */

public class MemeActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.recycler_view_meme_layout)
    RecyclerView recyclerViewMemeLayout;

    //endregion

    //region :Fields and constants
    /**
     * To maintain reference of this screen.
     */
    MemeActivity mContext;

    /**
     * Flag to maintain last selected meme layout.
     */
    @State
    int mLastSelectedLayout = 0;

    SharedPreferenceHelper mSpHelper;

    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme);
        ButterKnife.bind(this);
        //Method called
        initViews();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //endregion

    //region :Private methods

    /**
     * Method to initialize view for this screen.
     */
    private void initViews() {
        //obtain reference of this screen
        mContext = this;
        //Obtain reference of shared preference helper
        mSpHelper = new SharedPreferenceHelper(mContext);
        //Update flag
        mLastSelectedLayout = mSpHelper.getLastSelectedMemePosition();

        //Method called
        initMemeLayout();
        replaceFragment(mLastSelectedLayout);
    }


    /**
     * Method to initialize meme layout view
     */
    private void initMemeLayout() {
        recyclerViewMemeLayout.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL
                , false);
        recyclerViewMemeLayout.setLayoutManager(layoutManager);
        //Set adapter
        List<MemeLayoutModel> data = getMemeLayoutData();
        MemeLayoutAdapter adapter = new MemeLayoutAdapter(data, mContext, mLastSelectedLayout);
        recyclerViewMemeLayout.setAdapter(adapter);
        //Smooth scroll to position
        recyclerViewMemeLayout.smoothScrollToPosition(mLastSelectedLayout);
        //Method called
        initMemeLayoutSelectionListener(adapter, layoutManager, data);
    }

    /**
     * Method to return list of meme layout data.
     *
     * @return List<MemeLayoutModel>.
     */
    private List<MemeLayoutModel> getMemeLayoutData() {
        List<MemeLayoutModel> data = new ArrayList<>();
        //fixme update data here
        data.add(new MemeLayoutModel(R.drawable.img_welcome, "1"));
        data.add(new MemeLayoutModel(R.drawable.image_placeholder, "2"));
        data.add(new MemeLayoutModel(R.drawable.img_welcome, "3"));
        data.add(new MemeLayoutModel(R.drawable.image_placeholder, "4"));
        data.add(new MemeLayoutModel(R.drawable.img_welcome, "5"));
        return data;
    }

    /**
     * Method to initialize meme layout selection listener.
     *
     * @param adapter       MemeLayoutAdapter reference.
     * @param layoutManager LinearLayoutManager reference.
     * @param listData      Meme data list.
     */
    private void initMemeLayoutSelectionListener(MemeLayoutAdapter adapter, final LinearLayoutManager layoutManager, final List<MemeLayoutModel> listData) {
        //Set listener
        adapter.setListener(new listener.OnMemeLayoutClickListener() {
            @Override
            public void onMemeLayoutClick(MemeLayoutModel data, int itemPosition) {
                //Method called
                ViewHelper.scrollToNextItemPosition(layoutManager, recyclerViewMemeLayout
                        , itemPosition
                        , listData.size());
                //Update flags
                mLastSelectedLayout = itemPosition;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                //Method called
                replaceFragment(itemPosition);
            }
        });
    }


    /**
     * Method to replace current screen with new fragment.
     *
     * @param memeLayoutPosition Position of selected meme layout.
     */
    public void replaceFragment(int memeLayoutPosition) {
        Fragment fragment;

        switch (memeLayoutPosition) {
            case 0:
                fragment = new MemeFirstFragment();
                break;
            case 1:
                fragment = new MemeSecondFragment();
                break;
            case 2:
                fragment = new MemeThirdFragment();
                break;
            case 3:
                fragment = new MemeFourthFragment();
                break;
            case 4:
                fragment = new MemeFifthFragment();
                break;
            default:
                fragment = new MemeFirstFragment();
                break;
        }


        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.meme_frame_layout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    //endregion
}

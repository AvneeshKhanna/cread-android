package com.thetestament.cread.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.HelpAdapter;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.HelpModel;
import com.thetestament.cread.networkmanager.HelpNetworkManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment class for help.
 */
public class HelpFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Unbinder mUnbinder;

    List<HelpModel> mHelpDataList = new ArrayList<>();
    HelpAdapter mAdapter;

    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        //View to inflate
        View view = inflater
                .inflate(R.layout.fragment_help
                        , container
                        , false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Method called
        initScreen();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //Hide menu option
        menu.setGroupVisible(R.id.menu_main, false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_help, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chat_with_cread:
                //Open chat details screen
                IntentHelper.openChatWithCreadKalakaar(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //endregion

    //region :Private methods

    /**
     * Method to initialize swipeRefreshLayout.
     */
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mHelpDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                //Load data here
                loadHelpData();
            }
        });
        //Load data here
        loadHelpData();
    }


    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadHelpData() {
        HelpNetworkManager.getHelpData(getActivity()
                , mCompositeDisposable
                , new HelpNetworkManager.OnHelpDataLoadListener() {
                    @Override
                    public void onSuccess(List<HelpModel> dataList) {
                        //Hide progress
                        swipeRefreshLayout.setRefreshing(false);
                        //set data here
                        mHelpDataList = dataList;
                        //Set layout manger for recyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        //Set adapter
                        mAdapter = new HelpAdapter(mHelpDataList, getActivity());
                        recyclerView.setAdapter(mAdapter);

                        //Apply 'Slide Up' animation
                        int resId = R.anim.layout_animation_from_bottom;
                        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity()
                                , resId);
                        recyclerView.setLayoutAnimation(animation);

                        //listeners
                        initFeedBackUploadListener();
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        //Hide progress
                        swipeRefreshLayout.setRefreshing(false);
                        //Show error snack abr
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Method to initialize feedBack upload listener.
     */
    private void initFeedBackUploadListener() {
        mAdapter.setFeedBackClickListener(new listener.OnFeedBackClickListener() {
            @Override
            public void onFeedBackUpdate(String qaID) {
                HelpNetworkManager.updateHelpFeedbackData(getActivity()
                        , mCompositeDisposable
                        , qaID
                        , new HelpNetworkManager.OnFeedBackUpdateListener() {
                            @Override
                            public void onSuccess() {
                                //Method called
                                showDoneDialog(getActivity());
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                //Show snack bar
                                ViewHelper.getSnackBar(rootView, errorMsg);
                            }
                        });
            }
        });
    }


    /**
     * Method to show done dialog.
     *
     * @param context Context to use.
     */
    public static void showDoneDialog(FragmentActivity context) {
        new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_feedback_done, false)
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

//endregion
}

package com.thetestament.cread.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.SearchAdapter;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.SearchModel;
import com.thetestament.cread.utils.RxSearchObservable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.State;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
import static com.thetestament.cread.helpers.NetworkHelper.getSearchObservableServer;
import static com.thetestament.cread.helpers.ViewHelper.convertToPx;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_NO_RESULTS;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PEOPLE;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PROGRESS;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPeopleFragment extends Fragment {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SearchView searchView;


    List<SearchModel> mDataList = new ArrayList<>();
    SearchAdapter mAdapter;

    @State
    String mLastIndexKey = "", mSearchKeyWord;
    @State
    boolean mRequestMoreData;

    private Unbinder unbinder;

    public SearchPeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_people
                , container
                , false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Bind views
        unbinder = ButterKnife.bind(this, view);
        //initialize screen
        initializeScreen();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Dispose compositeDisposable
        mCompositeDisposable.dispose();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        //Set up searchView
        configureSearch(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Method to set up search framework for search functionality.
     *
     * @param menu Activity menu reference.
     */
    private void configureSearch(Menu menu) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        //Obtain searchView reference
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Enable android system search framework
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        //Expand searchView and request focus
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        //Decrease left padding
        searchView.setPadding(-convertToPx(getActivity(), 16), 0, 0, 0);
        //Hide searchView default icon
        ImageView magImage = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);
        //Set imeOptions
        searchView.setImeOptions(IME_ACTION_SEARCH);
        //Set query hint text
        searchView.setQueryHint("Search");

        //initialize searchView
        initializeSearchView(searchView);
    }

    /**
     * Method to initialize views for this screen
     */
    private void initializeScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new SearchAdapter(mDataList, getActivity());
        recyclerView.setAdapter(mAdapter);
        //Initialize listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * Method to initialize searchView functionality.
     */
    private void initializeSearchView(final SearchView searchView) {
        RxSearchObservable.fromView(searchView)
                //Only emit an item from an Observable if a particular time span
                // has passed without it emitting another item
                .debounce(1, TimeUnit.SECONDS)
                //Emit only those items from an Observable that pass a predicate test
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String text) throws Exception {
                        if (text.trim().isEmpty()) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                //whether or not two adjacently-emitted items are distinct.
                .distinctUntilChanged()
                //transform the items emitted by an Observable into Observables,
                // then flatten the emissions from those into a single Observable
                .switchMap(new Function<String, ObservableSource<JSONObject>>() {
                    @Override
                    public ObservableSource<JSONObject> apply(String s) throws Exception {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Show progress indicator
                                //Clear list
                                mDataList.clear();
                                //Add progress view
                                SearchModel searchData = new SearchModel();
                                searchData.setSearchType(SEARCH_TYPE_PROGRESS);
                                mDataList.add(searchData);
                                //Notify changes
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                        mSearchKeyWord = s;
                        //Reset last index key
                        mLastIndexKey = "";

                        //Network call
                        return getSearchObservableServer(mSearchKeyWord, mLastIndexKey, SEARCH_TYPE_PEOPLE);
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //Add disposable here
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Clear data
                        mDataList.clear();
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            mRequestMoreData = mainData.getBoolean("requestmore");
                            mLastIndexKey = mainData.getString("lastindexkey");
                            //Data list
                            JSONArray dataArray = mainData.getJSONArray("items");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObj = dataArray.getJSONObject(i);
                                SearchModel searchData = new SearchModel();
                                searchData.setUserUUID(dataObj.getString("uuid"));
                                searchData.setUserName(dataObj.getString("name"));
                                searchData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                searchData.setSearchType(SEARCH_TYPE_PEOPLE);

                                mDataList.add(searchData);
                                //Notify changes
                                mAdapter.notifyItemInserted(mDataList.size() - 1);
                            }

                            //If no data
                            if (mDataList.size() == 0) {
                                SearchModel searchData = new SearchModel();
                                searchData.setSearchType(SEARCH_TYPE_NO_RESULTS);
                                mDataList.add(searchData);
                                //Notify changes
                                mAdapter.notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "SearchPeopleFragment");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        initializeSearchView(searchView);
                        //Dismiss progress indicator
                        mDataList.clear();
                        mAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "SearchPeopleFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    /**
     * Initialize load more listener.
     *
     * @param adapter SearchAdapter reference.
     */
    private void initLoadMoreListener(SearchAdapter adapter) {
        //Load more data listener
        adapter.setOnSearchLoadMoreListener(new listener.OnSearchLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mDataList.add(null);
                                               mAdapter.notifyItemInserted(mDataList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * Method to retrieve next set of data from server.
     */
    private void loadMoreData() {
        mCompositeDisposable.add(getSearchObservableServer(mSearchKeyWord, mLastIndexKey, SEARCH_TYPE_PEOPLE)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            mRequestMoreData = mainData.getBoolean("requestmore");
                            mLastIndexKey = mainData.getString("lastindexkey");
                            //Data list
                            JSONArray dataArray = mainData.getJSONArray("items");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObj = dataArray.getJSONObject(i);
                                SearchModel searchData = new SearchModel();
                                searchData.setUserUUID(dataObj.getString("uuid"));
                                searchData.setUserName(dataObj.getString("name"));
                                searchData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                searchData.setSearchType(SEARCH_TYPE_PEOPLE);


                                mDataList.add(searchData);
                                //Notify changes
                                mAdapter.notifyItemInserted(mDataList.size() - 1);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "SearchPeopleFragment");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "SearchPeopleFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Notify changes
                        mAdapter.setLoaded();
                    }
                })
        );
    }


}

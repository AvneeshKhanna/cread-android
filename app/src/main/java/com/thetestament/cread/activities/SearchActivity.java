package com.thetestament.cread.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.androidnetworking.common.Priority;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.SearchAdapter;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.SearchModel;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.RxSearchObservable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.Observable;
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
import static com.thetestament.cread.helpers.ViewHelper.convertToPx;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_HASHTAG;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_NO_RESULTS;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PEOPLE;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PROGRESS;

/**
 * AppCompatActivity class to provide search functionality.
 */

public class SearchActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SearchView searchView;


    List<SearchModel> mDataList = new ArrayList<>();
    SearchAdapter mAdapter;

    @State
    String mSearchType = SEARCH_TYPE_PEOPLE, mLastIndexKey = "", mSearchKeyWord;
    @State
    boolean mRequestMoreData;

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //Bind views
        ButterKnife.bind(this);
        //initialize screen
        initializeScreen();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            initializeSearchView(searchView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Dispose compositeDisposable
        mCompositeDisposable.dispose();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate this menu
        getMenuInflater()
                .inflate(R.menu.menu_search, menu);
        //Set up searchView
        configureSearch(menu);

        return true;
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

    /**
     * Method to set up search framework for search functionality.
     *
     * @param menu Activity menu reference.
     */
    private void configureSearch(Menu menu) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Obtain searchView reference
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Enable android system search framework
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //Expand searchView and request focus
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        //Decrease left padding
        searchView.setPadding(-convertToPx(SearchActivity.this, 16), 0, 0, 0);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        //Set adapter
        mAdapter = new SearchAdapter(mDataList, SearchActivity.this);
        recyclerView.setAdapter(mAdapter);
        //Initialize listener
        initLoadMoreListener(mAdapter);
        //initialize tabLayout
        initializeTabLayoutListener();
    }

    /**
     * Method to initialize tabLayout click listener.
     */
    private void initializeTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        //Set search type
                        mSearchType = SEARCH_TYPE_PEOPLE;
                        //Update list
                        List<SearchModel> temp = new ArrayList<>();
                        for (SearchModel f : mDataList) {
                            if (f.getSearchType().equals(SEARCH_TYPE_PEOPLE)) {
                                temp.add(f);
                            }
                        }
                        //If no data
                        if (temp.size() == 0) {
                            SearchModel searchData = new SearchModel();
                            searchData.setSearchType(SEARCH_TYPE_NO_RESULTS);
                            temp.add(searchData);
                        }
                        mAdapter.updateList(temp);

                        break;
                    case 1:
                        //Set search type
                        mSearchType = Constant.SEARCH_TYPE_HASHTAG;
                        //Update list
                        List<SearchModel> tempHash = new ArrayList<>();
                        for (SearchModel f : mDataList) {
                            if (f.getSearchType().equals(SEARCH_TYPE_HASHTAG)) {
                                tempHash.add(f);
                            }
                        }
                        //If no data
                        if (tempHash.size() == 0) {
                            SearchModel searchDataHash = new SearchModel();
                            searchDataHash.setSearchType(SEARCH_TYPE_NO_RESULTS);
                            tempHash.add(searchDataHash);
                        }
                        mAdapter.updateList(tempHash);

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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

                        runOnUiThread(new Runnable() {
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
                                mAdapter.updateList(mDataList);
                                //mAdapter.notifyDataSetChanged();
                            }
                        });
                        mSearchKeyWord = s;
                        //Reset last index key
                        mLastIndexKey = "";

                        //Network call
                        return getDataFromServer(mSearchKeyWord, mLastIndexKey, mSearchType);
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
                                if (mainData.getString("searchtype").equals(SEARCH_TYPE_PEOPLE)) {
                                    searchData.setUserUUID(dataObj.getString("uuid"));
                                    searchData.setUserName(dataObj.getString("name"));
                                    searchData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    searchData.setSearchType(SEARCH_TYPE_PEOPLE);
                                } else {
                                    searchData.setSearchType(SEARCH_TYPE_HASHTAG);
                                    searchData.setHashTagCount(dataObj.getLong("postcount"));
                                    searchData.setHashTagLabel(dataObj.getString("hashtag"));
                                }
                                mDataList.add(searchData);
                                //Notify changes
                                // mAdapter.notifyItemInserted(mDataList.size() - 1);
                            }
                            //Notify changes
                            //mAdapter.notifyDataSetChanged();
                            mAdapter.updateList(mDataList);
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
                            FirebaseCrash.report(e);
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
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    /**
     * Method to return requested data from the server.
     *
     * @param queryMessage Search text entered by the user.
     * @param lastIndexKey Url of next page.
     * @param searchType   SearchType i.e USER or HASH TAG
     */
    private Observable<JSONObject> getDataFromServer(String queryMessage, String lastIndexKey, String searchType) {

        String inputText;
        //Replace spaces if search type is HASHTAG
        if (mSearchType.equals(Constant.SEARCH_TYPE_HASHTAG)) {
            inputText = queryMessage.replaceAll("\\s+", "");
        } else {
            inputText = queryMessage;
        }
        return Rx2AndroidNetworking.get(BuildConfig.URL + "/search/load")
                .addQueryParameter("keyword", inputText)
                .addQueryParameter("lastindexkey", lastIndexKey)
                .addQueryParameter("searchtype", searchType)
                .setPriority(Priority.HIGH)
                .build()
                .getJSONObjectObservable();
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
        mCompositeDisposable.add(getDataFromServer(mSearchKeyWord
                , mLastIndexKey
                , mSearchType)
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
                                if (mainData.getString("searchtype").equals(SEARCH_TYPE_PEOPLE)) {
                                    searchData.setUserUUID(dataObj.getString("uuid"));
                                    searchData.setUserName(dataObj.getString("name"));
                                    searchData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    searchData.setSearchType(SEARCH_TYPE_PEOPLE);
                                } else {
                                    searchData.setSearchType(SEARCH_TYPE_HASHTAG);
                                    searchData.setHashTagCount(dataObj.getLong("postcount"));
                                    searchData.setHashTagLabel(dataObj.getString("hashtag"));
                                }

                                mDataList.add(searchData);
                                //Notify changes
                                mAdapter.notifyItemInserted(mDataList.size() - 1);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        FirebaseCrash.report(e);
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

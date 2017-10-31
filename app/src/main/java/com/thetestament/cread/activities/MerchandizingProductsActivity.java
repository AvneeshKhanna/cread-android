package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ProductsAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnBuyButtonClickedListener;
import com.thetestament.cread.models.ProductsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MerchandizingProductsActivity extends BaseActivity {


    @BindView(R.id.header_products)
    RelativeLayout headerProducts;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.descText)
    TextView descText;

    private ProductsAdapter mAdapter;
    private List<ProductsModel> mDataList = new ArrayList<>();

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    String mEntityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchandizing_products);
        ButterKnife.bind(this);


        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        initView();

    }

    /**
     * Method to initialize views
     */
    private void initView() {

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(MerchandizingProductsActivity.this));
        //Set adapter
        mAdapter = new ProductsAdapter(MerchandizingProductsActivity.this, mDataList);
        recyclerView.setAdapter(mAdapter);

        //initialize  recyclerView
        initScreen();

    }


    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                headerProducts.setVisibility(View.GONE);

                //Clear data
                mDataList.clear();
                mAdapter.notifyDataSetChanged();

                getProductsData();

            }
        });

        initBuyButtonListener();

        getProductsData();

    }

    /**
     * Method to retrieve products related data from server
     */
    private void getProductsData()
    {
        if (NetworkHelper.getNetConnectionStatus(MerchandizingProductsActivity.this)) {

                swipeRefreshLayout.setRefreshing(true);
                //Get data from server


                final boolean[] tokenError = {false};
                final boolean[] connectionError = {false};


            mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/products/load")
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    tokenError[0] = true;
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");

                                    //Hats off details list
                                    JSONArray products = mainData.getJSONArray("products");
                                    for (int i = 0; i < products.length(); i++) {
                                        JSONObject dataObj = products.getJSONObject(i);
                                        ProductsModel productsData = new ProductsModel();
                                        productsData.setType(dataObj.getString("type"));
                                        productsData.setProductUrl(dataObj.getString("productimgurl"));
                                        productsData.setEntityUrl(dataObj.getString("entityimgurl"));
                                        productsData.setProductID(dataObj.getString("productid"));
                                        productsData.setPrice(getArrayListFromJSON(dataObj.getJSONArray("price")));
                                        productsData.setColors(getArrayListFromJSON(dataObj.getJSONArray("colors")));
                                        productsData.setSizes(getArrayListFromJSON(dataObj.getJSONArray("sizes")));
                                        productsData.setQuanity(getArrayListFromJSON(dataObj.getJSONArray("quantity")));

                                        mDataList.add(productsData);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);
                                connectionError[0] = true;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            //Dismiss progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                            FirebaseCrash.report(e);
                            //Server error Snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            // Token status invalid
                            if (tokenError[0]) {
                                ViewHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                                //Dismiss progress indicator
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            //Error occurred
                            else if (connectionError[0]) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                //Dismiss progress indicator
                                swipeRefreshLayout.setRefreshing(false);
                            } else {

                                headerProducts.setVisibility(View.VISIBLE);

                                //Dismiss indicator
                                swipeRefreshLayout.setRefreshing(false);
                                //Apply 'Slide Up' animation
                                int resId = R.anim.layout_animation_from_bottom;
                                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(MerchandizingProductsActivity.this, resId);
                                recyclerView.setLayoutAnimation(animation);

                                //Notify changes
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    })
            );
            }

         else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }



        /*ProductsModel productsModel = new ProductsModel();

        mDataList.add(productsModel);

        mAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
*/    }

    /**
     *
     * @param array JSON array to convert to array list
     * @return
     */
    private ArrayList<String> getArrayListFromJSON(JSONArray array)
    {
        int i = 0;
        ArrayList<String> arrayList = new ArrayList<>();
        while (i < array.length())
        {
            try {
                arrayList.add(array.getString(i));
                i++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return  arrayList;
    }



    private io.reactivex.Observable<JSONObject> getObservableFromServer(String url)
    {
        JSONObject jsonObject = new JSONObject();
        try {

            SharedPreferenceHelper spHelper = new SharedPreferenceHelper(MerchandizingProductsActivity.this);

            jsonObject.put("uuid", spHelper.getUUID());
            jsonObject.put("authkey", spHelper.getAuthToken());
            // TODO update dynamic entity id from intent
            jsonObject.put("entityid", "abc");
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(url)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    private void initBuyButtonListener()
    {
        mAdapter.setBuyButtonClickedListener(new OnBuyButtonClickedListener() {
            @Override
            public void onBuyButtonClicked(String type, String size, String color, String quantity, String price, String productID) {

              /*  Intent intent = new Intent(MerchandizingProductsActivity.this,);
                Bundle bundle = new Bundle();
                bundle.putString("type", type);
                bundle.putString("size",size);
                bundle.putString("color", color);
                bundle.putString("quantity", quantity);
                bundle.putString("price",price);
                bundle.putString("productID", productID);
                bundle.putString("entityID", mEntityID);
                intent.putExtras(bundle);
                startActivity(intent);*/
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_products,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_terms:
                new MaterialDialog.Builder(MerchandizingProductsActivity.this)
                        .customView(R.layout.dialog_products_terms, false)
                        .positiveText("Ok")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build()
                        .show();

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

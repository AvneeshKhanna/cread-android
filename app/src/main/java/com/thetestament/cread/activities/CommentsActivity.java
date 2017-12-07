package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
import com.thetestament.cread.adapters.CommentsAdapter.HeaderViewHolder;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.CommentsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;

/**
 * Class which Shows comments for the particular story.
 */

public class CommentsActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.editTextComment)
    EditText editTextComment;
    @BindView(R.id.buttonPost)
    ImageView buttonPost;
    @BindView(R.id.addCommentViewProgress)
    View addCommentViewProgress;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;


    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    List<CommentsModel> mCommentsList = new ArrayList<>();
    CommentsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    SharedPreferenceHelper mHelper;


    @State
    String mEntityID, mLastIndexKey = null;
    @State
    boolean mRequestMoreData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        //initialize textWatcher
        initTextWatcher(editTextComment);

        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    /**
     * Functionality to save user comment.
     */
    @OnClick(R.id.buttonPost)
    public void onButtonPostClicked(View view) {

        // check net status
        if (NetworkHelper.getNetConnectionStatus(CommentsActivity.this)) {
            saveComment(editTextComment.getText().toString().trim());
            //Clear edit text
            editTextComment.getText().clear();
            //Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }


    }


    /**
     * Method to set text watcher on edit text.
     */
    private void initTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                charSequence = charSequence.toString().trim();
                count = charSequence.length();

                if (count == 0) {
                    buttonPost.setVisibility(View.INVISIBLE);
                } else {
                    buttonPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Method to initialize screen
     */
    private void initView() {
        //Retrieve data from intent
        mEntityID = getIntent().getStringExtra(EXTRA_ENTITY_ID);
        //Set layout manger for recyclerView
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //Set adapter
        mAdapter = new CommentsAdapter(mCommentsList, this, mHelper.getUUID(), recyclerView);
        recyclerView.setAdapter(mAdapter);

        initSwipeRefreshLayout();

        //Initialize listeners
        initLoadMoreCommentsListener();
        initDeleteCommentListener(mAdapter);
        initEditCommentListener(mAdapter);
    }


    /**
     * Initialize delete comment listener.
     *
     * @param adapter CommentsAdapter reference.
     */
    private void initDeleteCommentListener(CommentsAdapter adapter) {
        adapter.setOnDeleteListener(new listener.OnCommentDeleteListener() {
            @Override
            public void onDelete(int index, String commentID) {
                //Delete comment
                deleteComment(commentID, index);
            }
        });
    }

    /**
     * Initialize edit comment listener.
     *
     * @param adapter CommentsAdapter reference.
     */
    private void initEditCommentListener(CommentsAdapter adapter) {
        adapter.setOnEditListener(new listener.OnCommentEditListener() {
            @Override
            public void onEdit(final int index, final CommentsModel commentsModel) {
                new MaterialDialog.Builder(CommentsActivity.this)
                        .title("Edit ")
                        .autoDismiss(false)
                        .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                        .input(null, commentsModel.getComment(), false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String s = String.valueOf(input).trim();
                                if (s.length() < 1) {
                                    ViewHelper.getToast(CommentsActivity.this, "This field can't be empty");
                                } else {
                                    //Dismiss
                                    dialog.dismiss();
                                    //save edited comment
                                    saveEditedComment(commentsModel.getCommentId()
                                            , String.valueOf(input)
                                            , index
                                            , commentsModel);
                                }
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    /**
     * Initialize load more comments listener.
     */
    private void initLoadMoreCommentsListener() {
        mAdapter.setOnViewLoadMoreListener(new listener.OnLoadMoreClickedListener() {
            @Override
            public void onLoadMoreClicked() {
                loadMoreData();
            }
        });
    }


    /**
     * Method to initialize SwipeRefreshLayout
     */
    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this
                , R.color.colorPrimary));

        loadCommentsData();
    }


    /**
     * This method loads comments data from server if user device is connected to internet.
     */
    private void loadCommentsData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            swipeRefreshLayout.setRefreshing(true);
            viewNoData.setVisibility(View.GONE);
            //Get data from server
            getCommentsData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }


    }

    /**
     * RxJava2 implementation for retrieving comment data
     */
    private void getCommentsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getCommentObservableFromServer(BuildConfig.URL + "/comment/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mEntityID
                , mLastIndexKey
                , true)
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
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Comments list
                                JSONArray commentsArray = mainData.getJSONArray("comments");
                                for (int i = 0; i < commentsArray.length(); i++) {
                                    JSONObject dataObj = commentsArray.getJSONObject(i);
                                    CommentsModel commentsData = new CommentsModel();
                                    commentsData.setUuid(dataObj.getString("uuid"));
                                    commentsData.setFirstName(dataObj.getString("firstname"));
                                    commentsData.setLastName(dataObj.getString("lastname"));
                                    commentsData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    commentsData.setComment(dataObj.getString("comment"));
                                    commentsData.setCommentId(dataObj.getString("commid"));
                                    commentsData.setEdited(dataObj.getBoolean("edited"));
                                    mCommentsList.add(commentsData);
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
                        swipeRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //No data
                        else if (mCommentsList.size() == 0) {
                            //SHow no data layout
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {

                            // show header
                            if (mRequestMoreData) {
                                mAdapter.setLoadMoreViewVisibility((HeaderViewHolder) recyclerView.
                                        findViewHolderForAdapterPosition(0), View.VISIBLE);

                            }
                            //Notify for data set changes
                            mAdapter.notifyDataSetChanged();

                            // scroll to last item in the recycler view
                            recyclerView.smoothScrollToPosition(mCommentsList.size());
                        }
                    }
                })
        );
    }

    /**
     * Method to load next set of data from server.
     */
    private void loadMoreData() {

        // hide load comments and show loading icon
        mAdapter.setLoadMoreViewVisibility((HeaderViewHolder) recyclerView.
                findViewHolderForAdapterPosition(0), View.GONE);
        mAdapter.setLoadingIconVisibility((HeaderViewHolder) recyclerView.
                findViewHolderForAdapterPosition(0), View.VISIBLE);

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getCommentObservableFromServer(BuildConfig.URL + "/comment/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mEntityID
                , mLastIndexKey
                , true)
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

                                List<CommentsModel> tempList = new ArrayList<>();

                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Comments list
                                JSONArray commentsArray = mainData.getJSONArray("comments");
                                for (int i = 0; i < commentsArray.length(); i++) {
                                    JSONObject dataObj = commentsArray.getJSONObject(i);
                                    CommentsModel commentsData = new CommentsModel();
                                    commentsData.setUuid(dataObj.getString("uuid"));
                                    commentsData.setFirstName(dataObj.getString("firstname"));
                                    commentsData.setLastName(dataObj.getString("lastname"));
                                    commentsData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    commentsData.setComment(dataObj.getString("comment"));
                                    commentsData.setCommentId(dataObj.getString("commid"));
                                    commentsData.setEdited(dataObj.getBoolean("edited"));

                                    tempList.add(commentsData);

                                }

                                mCommentsList.addAll(0, tempList);
                                // 1 because header is present at 0
                                mAdapter.notifyItemRangeInserted(1, tempList.size());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }

                        // hide loading icon  and show load comments image
                        if (mRequestMoreData) {
                            mAdapter.setLoadMoreViewVisibility((HeaderViewHolder) recyclerView.
                                    findViewHolderForAdapterPosition(0), View.VISIBLE);

                        }
                        mAdapter.setLoadingIconVisibility((HeaderViewHolder) recyclerView.
                                findViewHolderForAdapterPosition(0), View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {

                        // hide loading icon  and show load comments image
                        if (mRequestMoreData) {
                            mAdapter.setLoadMoreViewVisibility((HeaderViewHolder) recyclerView.
                                    findViewHolderForAdapterPosition(0), View.VISIBLE);

                        }
                        mAdapter.setLoadingIconVisibility((HeaderViewHolder) recyclerView.
                                findViewHolderForAdapterPosition(0), View.GONE);

                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {

                        }
                    }
                })
        );

    }

    /**
     * Method to save user comment on server.
     *
     * @param textComment Comment text to be saved on server.
     */
    private void saveComment(final String textComment) {
        //Show progress view
        addCommentViewProgress.setVisibility(View.VISIBLE);

        List<CommentsModel> otherCommentorsList = new ArrayList<>();

        // if comments are more than 10
        if (mCommentsList.size() >= 10) {
            otherCommentorsList = mCommentsList.subList(mCommentsList.size() - 10
                    , mCommentsList.size());
        } else {
            otherCommentorsList.addAll(mCommentsList);
        }

        List<String> otherCommentorsUUID = new ArrayList<>();

        for (CommentsModel otherCommentors : otherCommentorsList) {
            otherCommentorsUUID.add(otherCommentors.getUuid());
        }

        // removing all occurrences own uuid
        otherCommentorsUUID.removeAll(Collections.singleton(mHelper.getUUID()));
        // getting unique uuids
        Set<String> uniqueCommentors = new HashSet<String>(otherCommentorsUUID);

        JSONArray otherCommentorsJSON = new JSONArray(uniqueCommentors);


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("entityid", mEntityID);
            jsonObject.put("comment", textComment);
            jsonObject.put("othercommenters", otherCommentorsJSON);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            //Hide progress view
            addCommentViewProgress.setVisibility(View.GONE);
        }
        AndroidNetworking.post(BuildConfig.URL + "/comment/add")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        //Hide progress view
                        addCommentViewProgress.setVisibility(View.GONE);
                        try {
                            //Token in not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Hide No data view
                                    viewNoData.setVisibility(View.GONE);

                                    JSONObject commentObject = mainData.getJSONObject("comment");
                                    final CommentsModel commentsData = new CommentsModel();
                                    commentsData.setUuid(mHelper.getUUID());
                                    commentsData.setFirstName(commentObject.getString("firstname"));
                                    commentsData.setLastName(commentObject.getString("lastname"));
                                    commentsData.setComment(textComment);
                                    commentsData.setProfilePicUrl(commentObject.getString("profilepicurl"));
                                    commentsData.setCommentId(commentObject.getString("commid"));

                                    new Handler()
                                            .post(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          //Add data and notify
                                                          mCommentsList.add(commentsData);
                                                          mAdapter.notifyItemInserted(mCommentsList.size());

                                                          //Scroll to bottom
                                                          recyclerView.smoothScrollToPosition(mCommentsList.size());


                                                      }
                                                  }
                                            );

                                    //set result ok
                                    setResult(RESULT_OK);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Hide progress view
                        addCommentViewProgress.setVisibility(View.GONE);
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to delete comment.
     *
     * @param commentID ID of the comment to be deleted.
     * @param itemIndex index position of current item.
     */
    private void deleteComment(String commentID, final int itemIndex) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Deleting your comment")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("commid", commentID);
        } catch (JSONException e) {
            dialog.dismiss();
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        AndroidNetworking.post(BuildConfig.URL + "/comment/delete")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //Token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                //Token status is valid
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Remove item from list
                                    mCommentsList.remove(itemIndex);
                                    //Update adapter
                                    mAdapter.notifyItemRemoved(itemIndex);
                                    //set result ok
                                    setResult(RESULT_OK);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss dialog
                        dialog.dismiss();
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to save edited comment on server.
     *
     * @param commentID     ID of the comment to be updated.
     * @param textComment   edited comment text.
     * @param itemIndex     index of this item in adapter.
     * @param commentsModel Model of current item.
     */
    private void saveEditedComment(final String commentID, final String textComment, final int itemIndex, final CommentsModel commentsModel) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Updating your comment")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("commid", commentID);
            jsonObject.put("comment", textComment);
        } catch (JSONException e) {
            dialog.dismiss();
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        AndroidNetworking.post(BuildConfig.URL + "/comment/update")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Dismiss dialog
                        dialog.dismiss();
                        try {
                            //Token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Update data
                                    commentsModel.setEdited(true);
                                    commentsModel.setComment(textComment);
                                    //Notify changes
                                    mAdapter.notifyItemChanged(itemIndex);
                                    //set result ok
                                    setResult(RESULT_OK);
                                } else {
                                    //Show  error message
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss dialog
                        dialog.dismiss();
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }


}

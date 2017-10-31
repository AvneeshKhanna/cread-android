package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.CommentsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;

/**
 * Class which Shows comments for the particular story.
 */

public class CommentsActivity extends AppCompatActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.editTextComment)
    EditText editTextComment;
    @BindView(R.id.viewProgress)
    View viewProgress;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @State
    String mEntityID;
    List<CommentsModel> mCommentsList = new ArrayList<>();
    CommentsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    @BindView(R.id.buttonPost)
    ImageView buttonPost;


    private int mPageNumber = 0;
    private boolean mRequestMoreData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);

        initView();

        initSwipeRefreshLayout();

        initTextWatcher(editTextComment);
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
        saveComment(editTextComment.getText().toString().trim());
        //Clear edit text
        editTextComment.getText().clear();
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

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

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/comment/load", mEntityID, "", "", mPageNumber)
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
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                            //Hide progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            //Hide progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        //No data
                        else if (mCommentsList.size() == 0) {
                            //Hide indicator
                            swipeRefreshLayout.setRefreshing(false);
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            //Hide indicator
                            swipeRefreshLayout.setRefreshing(false);
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(CommentsActivity.this, resId);
                            recyclerView.setLayoutAnimation(animation);

                            //Notify for data set changes
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Method to load next set of data from server.
     */
    private void loadMoreData() {

        //Todo changes
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
            jsonObject.put("cmid", "");
            jsonObject.put("page", mPageNumber);
            jsonObject.put("loadAll", true);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }
        AndroidNetworking.post(BuildConfig.URL + "/comment/load")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        //Remove loading item
                        mCommentsList.remove(mCommentsList.size() - 1);
                        mAdapter.notifyItemRemoved(mCommentsList.size());
                        try {
                            //Token in not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper
                                        .getSnackBar(rootView, getString(R.string.error_msg_server));
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
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
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                            mAdapter.setLoaded();


                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Remove loading item
                        mCommentsList.remove(mCommentsList.size() - 1);
                        mAdapter.notifyItemRemoved(mCommentsList.size());
                        FirebaseCrash.report(anError);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to delete comment.
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
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
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
                                ViewHelper.getSnackBar(rootView,
                                        getString(R.string.error_msg_invalid_token));
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
                                } else {
                                    //Show  error message
                                    ViewHelper
                                            .getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            dialog.dismiss();
                            ViewHelper
                                    .getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dialog.dismiss();
                        ViewHelper
                                .getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to save user comment on server.
     */
    private void saveComment(final String textComment) {
        //Show progress view
        viewProgress.setVisibility(View.VISIBLE);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
            jsonObject.put("cmid", mEntityID);
            jsonObject.put("comment", textComment);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            //Show progress view
            viewProgress.setVisibility(View.GONE);
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }
        AndroidNetworking.post(BuildConfig.URL + "/comment/add")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        //Show progress view
                        viewProgress.setVisibility(View.GONE);
                        try {
                            //Token in not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper
                                        .getSnackBar(rootView, getString(R.string.error_msg_server));
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Hide no data view
                                    viewNoData.setVisibility(View.GONE);

                                    JSONObject commentObject = mainData.getJSONObject("comment");
                                    final CommentsModel commentsData = new CommentsModel();
                                    commentsData.setUuid("AccountManagerUtils.getUserID(CommentsActivity.this)");
                                    commentsData.setFirstName("AccountManagerUtils.getUserName(CommentsActivity.this)");
                                    commentsData.setLastName("");
                                    commentsData.setComment(textComment);
                                    commentsData.setProfilePicUrl(commentObject.getString("profilepicurl"));
                                    commentsData.setCommentId(commentObject.getString("commid"));

                                    new Handler()
                                            .post(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          //Add data and notify
                                                          mCommentsList.add(0, commentsData);
                                                          mAdapter.notifyItemInserted(0);

                                                          //Scroll to top
                                                          RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(CommentsActivity.this) {
                                                              @Override
                                                              protected int getVerticalSnapPreference() {
                                                                  return LinearSmoothScroller.SNAP_TO_START;
                                                              }
                                                          };
                                                          smoothScroller.setTargetPosition(0);
                                                          mLayoutManager.startSmoothScroll(smoothScroller);


                                                      }
                                                  }
                                            );

                                    //set result ok
                                    setResult(RESULT_OK);

                                } else {
                                    ViewHelper
                                            .getSnackBar(rootView, getString(R.string.error_msg_internal));
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
                        FirebaseCrash.report(anError);
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to save
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
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
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
                        dialog.dismiss();
                        try {
                            //Token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView,
                                        getString(R.string.error_msg_invalid_token));
                            } else {
                                //Token status is valid
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Update data
                                    commentsModel.setEdited(true);
                                    commentsModel.setComment(textComment);
                                    //Notify
                                    mAdapter.notifyItemChanged(itemIndex);
                                    //set result ok
                                    setResult(RESULT_OK);
                                } else {
                                    //Show  error message
                                    ViewHelper
                                            .getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            dialog.dismiss();
                            ViewHelper
                                    .getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dialog.dismiss();
                        ViewHelper
                                .getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
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
        mEntityID = getIntent().getStringExtra("entityID");
        //Set layout manger for recyclerView
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //Set adapter
        mAdapter = new CommentsAdapter(mCommentsList, this);
        recyclerView.setAdapter(mAdapter);

        //Set load more listener
        initLoadMoreListener(mAdapter);
        //Set delete campaign listener
        initDeleteCommentListener(mAdapter);
        //Set edit campaign listener
        initEditCommentListener(mAdapter);
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter CommentsAdapter reference.
     */
    private void initLoadMoreListener(CommentsAdapter adapter) {
        adapter.setOnLoadMoreListener(new listener.OnCommentsLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler()
                            .post(new Runnable() {
                                      @Override
                                      public void run() {
                                          mCommentsList.add(null);
                                          mAdapter.notifyItemInserted(mCommentsList.size() - 1);
                                      }
                                  }
                            );

                    //Increment page counter
                    mPageNumber += 1;
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
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
            public void onEdit(final int index, final String commentID, String comment, final CommentsModel commentsModel) {
                new MaterialDialog.Builder(CommentsActivity.this)
                        .title("Edit ")
                        .autoDismiss(false)
                        .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                        .input(null, comment, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                String s = String.valueOf(input).trim();
                                if (s.length() < 1) {
                                    ViewHelper.getToast(CommentsActivity.this, "This field can't be empty");
                                } else {
                                    //Dismiss
                                    dialog.dismiss();
                                    //save comment
                                    saveEditedComment(commentID, String.valueOf(input), index, commentsModel);
                                }
                            }
                        })
                        .build()
                        .show();
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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mCommentsList.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mPageNumber = 0;
                loadCommentsData();
            }
        });
        loadCommentsData();
    }


}

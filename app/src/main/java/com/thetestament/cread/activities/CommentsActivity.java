package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
import com.linkedin.android.spyglass.mentions.MentionSpan;
import com.linkedin.android.spyglass.suggestions.SuggestionsResult;
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager;
import com.linkedin.android.spyglass.tokenization.QueryToken;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer;
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
import com.thetestament.cread.adapters.CommentsAdapter.HeaderViewHolder;
import com.thetestament.cread.adapters.PersonMentionAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.PersonMentionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
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
import io.reactivex.subjects.PublishSubject;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COMMENTS;
import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getSearchObservableServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.BUCKET;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.convertToMentionsFormat;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.getMentionSpanConfig;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.setProfileMentionsForEditing;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.tokenizerConfig;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PEOPLE;

/**
 * Class which Shows comments for the particular story.
 */

public class CommentsActivity extends BaseActivity implements QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.editTextComment)
    MentionsEditText editTextComment;
    @BindView(R.id.buttonPost)
    ImageView buttonPost;
    @BindView(R.id.addCommentViewProgress)
    View addCommentViewProgress;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    @BindView(R.id.recyclerViewMentions)
    RecyclerView recyclerViewMentions;


    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    List<CommentsModel> mCommentsList = new ArrayList<>();
    List<PersonMentionModel> mSuggestionsList = new ArrayList<>();
    CommentsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    SharedPreferenceHelper mHelper;
    PersonMentionAdapter mMentionsAdapter;

    @State
    String mEntityID, mLastIndexKey = null, mSuggestionsLastIndexKey;
    @State
    boolean mRequestMoreData, mRequestMoreSuggestionsData;

    // variables for comment editing
    boolean isCommentEditMode;
    CommentsModel editedCommentModel;
    int editedCommentIndex = -1;

    QueryToken mQueryToken;
    PublishSubject<QueryToken> subject = PublishSubject.create();


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


    @Override
    public void onReceiveSuggestionsResult(@NonNull SuggestionsResult result, @NonNull String bucket) {

        List<? extends Suggestible> suggestions = result.getSuggestions();
        boolean display = suggestions != null && suggestions.size() > 0;
        displaySuggestions(display);
    }

    // --------------------------------------------------
    // SuggestionsManager Implementation
    // --------------------------------------------------

    @Override
    public void displaySuggestions(boolean display) {
        if (display) {
            recyclerViewMentions.setVisibility(RecyclerView.VISIBLE);
            viewNoData.setVisibility(View.GONE);
        } else {
            recyclerViewMentions.setVisibility(RecyclerView.GONE);
        }
    }

    @Override
    public boolean isDisplayingSuggestions() {
        return recyclerViewMentions.getVisibility() == RecyclerView.VISIBLE;
    }

    @Override
    public List<String> onQueryReceived(@NonNull QueryToken queryToken) {

        List<String> buckets = Arrays.asList(BUCKET);
        // init query token
        mQueryToken = queryToken;

        subject.onNext(mQueryToken);

        return buckets;
    }

    /**
     * Functionality to save user comment.
     */
    @OnClick(R.id.buttonPost)
    public void onButtonPostClicked(View view) {

        // check net status
        if (NetworkHelper.getNetConnectionStatus(CommentsActivity.this)) {

            String mentionFormattedString = convertToMentionsFormat(editTextComment);
            // check whether comment has been edited or is a new comment
            if (isCommentEditMode) {
                saveEditedComment(editedCommentModel.getCommentId()
                        , mentionFormattedString
                        , editedCommentIndex
                        , editedCommentModel);
            } else {
                saveComment(mentionFormattedString);
            }

            /*editTextComment.getText().clearSpans();*/
            List<MentionSpan> mentionSpans = editTextComment.getMentionsText().getMentionSpans();

            for (MentionSpan mentionSpan : mentionSpans) {
                editTextComment.getText().removeSpan(mentionSpan);
            }

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
        mAdapter = new CommentsAdapter(mCommentsList, this, mHelper.getUUID(), true);
        recyclerView.setAdapter(mAdapter);

        mMentionsAdapter = new PersonMentionAdapter(mSuggestionsList, this);
        recyclerViewMentions.setAdapter(mMentionsAdapter);
        recyclerViewMentions.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));


        editTextComment.setTokenizer(new WordTokenizer(tokenizerConfig));
        editTextComment.setQueryTokenReceiver(this);
        editTextComment.setSuggestionsVisibilityManager(this);

        initSuggestionsView();

        initSwipeRefreshLayout();

        //Initialize listeners
        initLoadMoreCommentsListener();
        initLoadMoreSuggestionsListener(mMentionsAdapter);
        initSuggestionsClickListener(mMentionsAdapter);
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
                // initialize comment editing variables
                isCommentEditMode = true;
                editedCommentIndex = index;
                editedCommentModel = commentsModel;

                // init comment box with the original comment
                editTextComment.setText(commentsModel.getComment());
                setProfileMentionsForEditing(CommentsActivity.this, commentsModel.getComment(), editTextComment);


                // request focus and show keyboard
                editTextComment.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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


    private void initLoadMoreSuggestionsListener(PersonMentionAdapter adapter) {
        adapter.setLoadMoreSuggestionsListener(new listener.onSuggestionsLoadMore() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreSuggestionsData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mSuggestionsList.add(null);
                                               mMentionsAdapter.notifyItemInserted(mSuggestionsList.size() - 1);
                                           }
                                       }
                    );

                    getMoreSuggestions();
                }
            }
        });
    }

    private void initSuggestionsClickListener(PersonMentionAdapter adapter) {
        adapter.setSuggestionsClickListener(new listener.OnPeopleSuggestionsClick() {
            @Override
            public void onPeopleSuggestionsClick(PersonMentionModel person) {

                editTextComment.setMentionSpanConfig(getMentionSpanConfig(CommentsActivity.this));
                editTextComment.insertMention(person);
                recyclerViewMentions.setAdapter(mMentionsAdapter);
                displaySuggestions(false);
                editTextComment.requestFocus();
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
                                    commentsData.setTopArtist(dataObj.getBoolean("topartist"));
                                    mCommentsList.add(commentsData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "CommentsActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_COMMENTS = false;

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

                                // hide loading icon  and show load comments image
                                if (mRequestMoreData) {
                                    mAdapter.setLoadMoreViewVisibility((HeaderViewHolder) recyclerView.
                                            findViewHolderForAdapterPosition(0), View.VISIBLE);

                                }
                                mAdapter.setLoadingIconVisibility((HeaderViewHolder) recyclerView.
                                        findViewHolderForAdapterPosition(0), View.GONE);


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
                                    commentsData.setTopArtist(dataObj.getBoolean("topartist"));

                                    tempList.add(commentsData);

                                }

                                mCommentsList.addAll(0, tempList);
                                // 1 because header is present at 0
                                mAdapter.notifyItemRangeInserted(0, tempList.size());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            connectionError[0] = true;
                        }
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

                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "CommentsActivity");
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
            Crashlytics.logException(e);
            Crashlytics.setString("className", "CommentsActivity");
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

                                    //set response flag
                                    GET_RESPONSE_FROM_NETWORK_COMMENTS = true;


                                    //set result ok
                                    setResult(RESULT_OK);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Hide progress view
                        addCommentViewProgress.setVisibility(View.GONE);
                        anError.printStackTrace();
                        Crashlytics.logException(anError);
                        Crashlytics.setString("className", "CommentsActivity");
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
            Crashlytics.logException(e);
            Crashlytics.setString("className", "CommentsActivity");
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
                                    mCommentsList.remove(itemIndex - 1);
                                    //Update adapter
                                    mAdapter.notifyItemRemoved(itemIndex);

                                    //set response flag
                                    GET_RESPONSE_FROM_NETWORK_COMMENTS = true;

                                    //set result ok
                                    setResult(RESULT_OK);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss dialog
                        dialog.dismiss();
                        anError.printStackTrace();
                        Crashlytics.logException(anError);
                        Crashlytics.setString("className", "CommentsActivity");
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
            Crashlytics.logException(e);
            Crashlytics.setString("className", "CommentsActivity");
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

                                    ViewHelper.getSnackBar(rootView, getString(R.string.msg_success_comment_edit));

                                    // set to false
                                    isCommentEditMode = false;

                                    //set response flag
                                    GET_RESPONSE_FROM_NETWORK_COMMENTS = true;

                                    //set result ok
                                    setResult(RESULT_OK);
                                } else {
                                    //Show  error message
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss dialog
                        dialog.dismiss();
                        anError.printStackTrace();
                        Crashlytics.logException(anError);
                        Crashlytics.setString("className", "CommentsActivity");
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }


    private void initSuggestionsView() {

        subject = PublishSubject.create();

        subject.debounce(1, TimeUnit.SECONDS)
                .distinctUntilChanged()
                //Emit only those items from an Observable that pass a predicate test
                .filter(new Predicate<QueryToken>() {
                    @Override
                    public boolean test(QueryToken queryToken) throws Exception {
                        if (queryToken.getKeywords().trim().isEmpty()) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                })
                //transform the items emitted by an Observable into Observables,
                // then flatten the emissions from those into a single Observable
                .switchMap(new Function<QueryToken, ObservableSource<JSONObject>>() {
                    @Override
                    public ObservableSource<JSONObject> apply(final QueryToken queryToken) throws Exception {


                        mSuggestionsList.clear();
                        mSuggestionsLastIndexKey = null;

                        mQueryToken = queryToken;


                        return getSearchObservableServer(queryToken.getKeywords()
                                , mSuggestionsLastIndexKey
                                , SEARCH_TYPE_PEOPLE);
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
                        mSuggestionsList.clear();
                        mMentionsAdapter.notifyDataSetChanged();
                        mMentionsAdapter.setLoaded();

                        try {

                            parseSuggestionsData(false, jsonObject);

                            mMentionsAdapter.notifyDataSetChanged();

                            SuggestionsResult result = new SuggestionsResult(mQueryToken, mSuggestionsList);
                            // Have suggestions, now call the listener (which is this activity)
                            onReceiveSuggestionsResult(result, BUCKET);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "CommentsActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    private void getMoreSuggestions() {

        requestServer(mCompositeDisposable,
                getSearchObservableServer(mQueryToken.getKeywords(), mSuggestionsLastIndexKey, SEARCH_TYPE_PEOPLE)
                , this, new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        //Remove loading item
                        mSuggestionsList.remove(mSuggestionsList.size() - 1);
                        mMentionsAdapter.notifyItemRemoved(mSuggestionsList.size());

                        try {

                            parseSuggestionsData(true, jsonObject);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CommentsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }

                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        //Remove loading item
                        mSuggestionsList.remove(mSuggestionsList.size() - 1);
                        mMentionsAdapter.notifyItemRemoved(mSuggestionsList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "CommentsActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));


                    }

                    @Override
                    public void onCompleteCalled() {

                        mMentionsAdapter.setLoaded();
                    }
                });
    }


    private void parseSuggestionsData(boolean isCalledFromLoadMore
            , JSONObject jsonObject)
            throws JSONException {

        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreSuggestionsData = mainData.getBoolean("requestmore");
        mSuggestionsLastIndexKey = mainData.getString("lastindexkey");
        //Data list
        JSONArray dataArray = mainData.getJSONArray("items");
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObj = dataArray.getJSONObject(i);
            PersonMentionModel data = new PersonMentionModel();
            data.setUserUUID(dataObj.getString("uuid"));
            data.setmName(dataObj.getString("name"));
            data.setmPictureURL(dataObj.getString("profilepicurl"));


            mSuggestionsList.add(data);
            //Notify changes
            if (isCalledFromLoadMore) {
                mMentionsAdapter.notifyItemInserted(mSuggestionsList.size() - 1);
            }
        }

    }
}

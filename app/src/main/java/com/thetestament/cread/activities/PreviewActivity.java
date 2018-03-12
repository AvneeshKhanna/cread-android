package com.thetestament.cread.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.linkedin.android.spyglass.suggestions.SuggestionsResult;
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager;
import com.linkedin.android.spyglass.tokenization.QueryToken;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer;
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FilterAdapter;
import com.thetestament.cread.adapters.PersonMentionAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.CaptureHelper;
import com.thetestament.cread.helpers.CustomFilters;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ProfileMentionsHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FilterModel;
import com.thetestament.cread.models.PersonMentionModel;
import com.zomato.photofilters.imageprocessors.Filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.OkHttpClient;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_INSPIRATION;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.CreadApp.IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION;
import static com.thetestament.cread.CreadApp.IMAGE_LOAD_FROM_NETWORK_ME;
import static com.thetestament.cread.dialog.DialogHelper.showCollabInvitationDialog;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getSearchObservableServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.BUCKET;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.getMentionSpanConfig;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.setProfileMentionsForEditing;
import static com.thetestament.cread.helpers.ProfileMentionsHelper.tokenizerConfig;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHORT_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_AUTH_KEY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BG_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BOLD;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_CAPTURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_COLLABORATION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CONTENT_IMAGE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_FONT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMAGE_TINT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMG_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ITALIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SIGNATURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_GRAVITY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_SIZE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_HEIGHT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_UUID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_X_POSITION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_Y_POSITION;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COLLABORATION_INVITATION;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PEOPLE;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_NO;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_YES;

/**
 * AppcompatActivity to show preview and option to write caption.
 */

public class PreviewActivity extends BaseActivity implements QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

    //Required for photo filters
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    //region :Butter knife view binding
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imagePreview)
    ImageView imagePreview;
    @BindView(R.id.etCaption)
    MentionsEditText etCaption;
    @BindView(R.id.textWaterMark)
    TextView textWaterMark;
    @BindView(R.id.filterBottomSheetView)
    NestedScrollView filterBottomSheetView;
    @BindView(R.id.filterRecyclerView)
    RecyclerView filterRecyclerView;
    @BindView(R.id.recyclerViewMentions)
    RecyclerView recyclerViewMentions;
    //endregion

    //region :Fields and constants
    /**
     * BottomSheet behaviour for filters.
     */
    BottomSheetBehavior filterSheetBehavior;

    FilterAdapter adapter;

    /**
     * Field to store bundle data from intent.
     */
    Bundle mBundle;

    /**
     * Flag to maintain screen called from data.
     */
    @State
    String mCalledFrom;
    Bitmap bmp = null;

    /**
     * Flag to store water mark text
     */
    @State
    String mWaterMarkText = "";

    /**
     * Flag to store image filter name
     */
    @State
    String mFilterName = "original";

    @State
    String mCapInMentionFormat;

    @State
    boolean mRequestMoreSuggestionsData = false;
    @State
    String mSuggestionsLastIndexKey;

    SharedPreferenceHelper mHelper;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    QueryToken mQueryToken;
    PublishSubject<QueryToken> subject = PublishSubject.create();
    List<PersonMentionModel> mSuggestionsList = new ArrayList<>();
    PersonMentionAdapter mMentionsAdapter;
    FragmentActivity mContext = PreviewActivity.this;

    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        //Get sharedPreference
        mHelper = new SharedPreferenceHelper(this);
        //initialize this screen
        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        getMenuInflater().inflate(R.menu.menu_preview, menu);

        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE)) {
            //Hide filter menu option
            menu.findItem(R.id.action_filter).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Show prompt dialog
                CustomDialog.getBackNavigationDialog(PreviewActivity.this
                        , "Discard changes?"
                        , "If you go back now, you will loose your changes.");
                return true;

            case R.id.action_filter:
                toggleBottomSheet();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == REQUEST_CODE_COLLABORATION_INVITATION) {   // finish activity
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            //Show prompt dialog
            CustomDialog.getBackNavigationDialog(PreviewActivity.this
                    , "Discard changes?"
                    , "If you go back now, you will loose your changes.");
        }
    }

    @Override
    public void onReceiveSuggestionsResult(@NonNull SuggestionsResult result, @NonNull String bucket) {

        List<? extends Suggestible> suggestions = result.getSuggestions();
        boolean display = suggestions != null && suggestions.size() > 0;
        displaySuggestions(display);
    }

    @Override
    public void displaySuggestions(boolean display) {

        if (display) {
            recyclerViewMentions.setVisibility(RecyclerView.VISIBLE);

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


    //endregion

    //region :Click functionality

    /**
     * Root view click functionality hide filter bottom sheet if its expanded
     */
    @OnClick({R.id.rootView, R.id.imageContainer})
    void rootOnClick() {
        if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Update button click functionality to upload content on server.
     */
    @OnClick(R.id.buttonUpdate)
    void updateOnClick() {
        if (NetworkHelper.getNetConnectionStatus(PreviewActivity.this)) {


            // get caption in mentions format
            mCapInMentionFormat = ProfileMentionsHelper.convertToMentionsFormat(etCaption);

            // edit capture
            if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE)) {
                uploadEditedCapture(mCapInMentionFormat
                        , mHelper.getUUID()
                        , mHelper.getAuthToken()
                        , mBundle.getString(PREVIEW_EXTRA_ENTITY_ID));
            } else {
                checkRuntimePermission();
            }
        } else {
            //Show no connection message
            ViewHelper.getToast(this, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Close button click functionality to hide filter bottom sheet.
     */
    @OnClick(R.id.buttonClose)
    void closeOnClick() {
        //Hide bottom sheet
        filterSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize screen data.
     */
    private void initScreen() {
        //Retrieve data from intent
        mBundle = getIntent().getBundleExtra(PREVIEW_EXTRA_DATA);
        //Set variable
        mCalledFrom = mBundle.getString(PREVIEW_EXTRA_CALLED_FROM);
        //Check content type
        checkContentType();

        mMentionsAdapter = new PersonMentionAdapter(mSuggestionsList, this);
        recyclerViewMentions.setAdapter(mMentionsAdapter);
        recyclerViewMentions.setLayoutManager(new LinearLayoutManager(mContext));


        etCaption.setTokenizer(new WordTokenizer(tokenizerConfig));
        etCaption.setQueryTokenReceiver(this);
        etCaption.setSuggestionsVisibilityManager(this);

        initSuggestionsView();

        initLoadMoreSuggestionsListener(mMentionsAdapter);
        initSuggestionsClickListener(mMentionsAdapter);
    }

    /**
     * Method to check the content type and perform operations accordingly.
     */
    private void checkContentType() {
        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_CAPTURE)) {
            //Load capture pic
            loadPreviewImage(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC), imagePreview);
            //initialize filter screen
            initFilterView();
            checkWatermarkStatus(mHelper);
        }
        //For capture editing
        else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE)) {
            //Load capture pic
            loadPreviewImage(Uri.parse(mBundle.getString(PREVIEW_EXTRA_CONTENT_IMAGE)), imagePreview);
            //Setup bottom sheets
            filterSheetBehavior = BottomSheetBehavior.from(filterBottomSheetView);
            filterSheetBehavior.setPeekHeight(0);
            //Set caption text
            etCaption.setText(mBundle.getString(PREVIEW_EXTRA_CAPTION_TEXT));
            setProfileMentionsForEditing(mContext, mBundle.getString(PREVIEW_EXTRA_CAPTION_TEXT), etCaption);
        } else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT)) {
            //initialize filter screen
            initFilterView();
            //Load short pic
            loadPreviewImage(getImageUri(IMAGE_TYPE_USER_SHORT_PIC), imagePreview);
            //Set caption text
            etCaption.setText(mBundle.getString(PREVIEW_EXTRA_CAPTION_TEXT));
            setProfileMentionsForEditing(mContext, mBundle.getString(PREVIEW_EXTRA_CAPTION_TEXT), etCaption);
        } else {
            //initialize filter screen
            initFilterView();
            //Load short pic
            loadPreviewImage(getImageUri(IMAGE_TYPE_USER_SHORT_PIC), imagePreview);
        }
    }

    /**
     * Method to load preview image.
     *
     * @param imageUri Uri of image to be loaded.
     * @param image    ImageView where image to be loaded.
     */
    private void loadPreviewImage(Uri imageUri, ImageView image) {
        Picasso.with(this)
                .load(imageUri)
                .error(R.drawable.image_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(image);
    }

    /**
     * Method to check  whether 'Upload a capture' is clicked for first time or not. If yes then show the appropriate dialog.
     *
     * @param helper SharedPreference reference.
     */
    private void checkWatermarkStatus(SharedPreferenceHelper helper) {
        //Check for first time status
        if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_YES)) {
            mWaterMarkText = helper.getCaptureWaterMarkText();
            textWaterMark.setText(mWaterMarkText);
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_NO)) {
            //Hide watermark
            textWaterMark.setVisibility(View.GONE);
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_ASK_ALWAYS)) {
            //Show watermark dialog
            getWaterWorkDialog();
        }
    }

    /**
     * Method to show watermark dialog.
     */
    private void getWaterWorkDialog() {
        new MaterialDialog.Builder(this)
                .content("Do you wish to add your signature? It will be visible on the bottom left of the image.")
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_no)
                .checkBoxPrompt("Remember this", false, null)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //If checkbox is selected
                        if (dialog.isPromptCheckBoxChecked()) {
                            //Save watermark status
                            mHelper.setWatermarkStatus(WATERMARK_STATUS_YES);
                        }
                        //Dismiss this dialog
                        dialog.dismiss();
                        //Show input dialog
                        showWaterMarkInputDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //If checkbox is selected
                        if (dialog.isPromptCheckBoxChecked()) {
                            //Save watermark status
                            mHelper.setWatermarkStatus(WATERMARK_STATUS_NO);
                        }
                        //Hide watermark
                        textWaterMark.setVisibility(View.GONE);
                        //Dismiss this dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Method to show input dialog where user enters his/her watermark.
     */
    private void showWaterMarkInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Signature")
                .autoDismiss(false)
                .inputRange(1, 20, ContextCompat.getColor(PreviewActivity.this, R.color.red))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(PreviewActivity.this, "This field can't be empty");
                        } else {
                            //Dismiss
                            dialog.dismiss();
                            mWaterMarkText = s;
                            //Set watermark
                            textWaterMark.setVisibility(View.VISIBLE);
                            textWaterMark.setText(mWaterMarkText);
                            //Save watermark text
                            mHelper.setCaptureWaterMarkText(mWaterMarkText);
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * Method to update the requested data on server.
     */
    private void performUpdateOperation() {
        // cpshort
        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_COLLABORATION)) {
            updateData(new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath())
                    , new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                    , mBundle.getString(PREVIEW_EXTRA_SHORT_ID)
                    , mBundle.getString(PREVIEW_EXTRA_UUID)
                    , mBundle.getString(PREVIEW_EXTRA_AUTH_KEY)
                    , mBundle.getString(PREVIEW_EXTRA_X_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_Y_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_TV_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_TV_HEIGHT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_SIZE)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_GRAVITY)
                    , mBundle.getString(PREVIEW_EXTRA_IMG_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_SIGNATURE)
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE)
                    , mBundle.getString(PREVIEW_EXTRA_FONT)
                    , mBundle.getString(PREVIEW_EXTRA_BOLD)
                    , mBundle.getString(PREVIEW_EXTRA_ITALIC)
                    , mCapInMentionFormat
                    , mBundle.getString(PREVIEW_EXTRA_IMAGE_TINT_COLOR)
            );
        }
        // short, shcapture
        else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_SHORT)) {
            updateShort(new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                    , mBundle.getString(PREVIEW_EXTRA_CAPTURE_ID)
                    , mBundle.getString(PREVIEW_EXTRA_UUID)
                    , mBundle.getString(PREVIEW_EXTRA_AUTH_KEY)
                    , mBundle.getString(PREVIEW_EXTRA_X_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_Y_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_TV_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_TV_HEIGHT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_SIZE)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_GRAVITY)
                    , mBundle.getString(PREVIEW_EXTRA_IMG_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE)
                    , mBundle.getString(PREVIEW_EXTRA_FONT)
                    , mBundle.getString(PREVIEW_EXTRA_BG_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_BOLD)
                    , mBundle.getString(PREVIEW_EXTRA_ITALIC)
                    , mCapInMentionFormat
                    , mBundle.getString(PREVIEW_EXTRA_IMAGE_TINT_COLOR)
            );
        }
        // capture
        else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_CAPTURE)) {
            uploadCapture(new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath())
                    , mCapInMentionFormat
                    , mHelper.getUUID()
                    , mHelper.getAuthToken()
                    , mWaterMarkText
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE));
        }
        // edit short
        else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT)) {
            updateEditedShort(new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                    , mBundle.getString(PREVIEW_EXTRA_ENTITY_ID)
                    , mBundle.getString(PREVIEW_EXTRA_CAPTURE_ID)
                    , mBundle.getString(PREVIEW_EXTRA_SHORT_ID)
                    , mBundle.getString(PREVIEW_EXTRA_UUID)
                    , mBundle.getString(PREVIEW_EXTRA_AUTH_KEY)
                    , mBundle.getString(PREVIEW_EXTRA_X_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_Y_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_TV_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_TV_HEIGHT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_SIZE)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_GRAVITY)
                    , mBundle.getString(PREVIEW_EXTRA_IMG_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE)
                    , mBundle.getString(PREVIEW_EXTRA_FONT)
                    , mBundle.getString(PREVIEW_EXTRA_BG_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_BOLD)
                    , mBundle.getString(PREVIEW_EXTRA_ITALIC)
                    , mCapInMentionFormat
                    , mBundle.getString(PREVIEW_EXTRA_IMAGE_TINT_COLOR)
            );
        } else {
            //do nothing
        }
    }

    /**
     * Method to update capture/collaboration details on server.
     */
    private void updateData(File imgHighRes, File imgLowRes, String shortID, final String uuid, final String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String signature, String merchantable, String font, String bold, String italic, String captionText, String imageTintColor) {

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your capture")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();
        dialog.show();


        Rx2AndroidNetworking.upload(BuildConfig.URL + "/capture-upload/collaborated")
                .setOkHttpClient(okHttpClient)
                .addMultipartFile("capture-img-high", imgHighRes)
                .addMultipartFile("capture-img-low", imgLowRes)
                .addMultipartParameter("shoid", shortID)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("txt_width", tvWidth)
                .addMultipartParameter("txt_height", tvHeight)
                .addMultipartParameter("img_width", imgWidth)
                .addMultipartParameter("img_height", imgWidth)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("watermark", signature)
                .addMultipartParameter("merchantable", merchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
                .addMultipartParameter("caption", captionText)
                .addMultipartParameter("imgtintcolor", imageTintColor)
                .addMultipartParameter("filtername", mFilterName)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Capture uploaded successfully.");
                                    setResult(RESULT_OK);

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = true;

                                    // open collaboration invitation dialog
                                    showCollabInvitationDialog(mContext
                                            , mCompositeDisposable
                                            , rootView
                                            , uuid
                                            , authToken
                                            , dataObject.getString("entityid")
                                            , dataObject.getString("captureurl")
                                            , mHelper.getFirstName()
                                            , CONTENT_TYPE_CAPTURE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

    /**
     * Update short image and other details on server.
     */
    private void updateShort(File file, String captureID, final String uuid, final String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String merchantable, String font, String bgColor, String bold, String italic, String captionText, String imageTintColor) {

        String mMerchantable = null;

        if (merchantable.equals("true")) {
            mMerchantable = "1";
        } else {
            mMerchantable = "0";
        }

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your writing")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();
        dialog.show();


        Rx2AndroidNetworking.upload(BuildConfig.URL + "/short-upload")
                .setOkHttpClient(okHttpClient)
                .addMultipartFile("short-image", file)
                .addMultipartParameter("captureid", captureID)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("txt_width", tvWidth)
                .addMultipartParameter("txt_height", tvHeight)
                .addMultipartParameter("img_width", imgWidth)
                .addMultipartParameter("img_height", imgWidth)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("merchantable", mMerchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bgcolor", bgColor)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
                .addMultipartParameter("caption", captionText)
                .addMultipartParameter("imgtintcolor", imageTintColor)
                .addMultipartParameter("filtername", mFilterName)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Writing uploaded successfully.");
                                    setResult(RESULT_OK);

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = true;

                                    // open collaboration invitation dialog
                                    showCollabInvitationDialog(mContext
                                            , mCompositeDisposable
                                            , rootView
                                            , uuid
                                            , authToken
                                            , dataObject.getString("entityid")
                                            , dataObject.getString("shorturl")
                                            , mHelper.getFirstName()
                                            , CONTENT_TYPE_SHORT);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

    /**
     * Update edited short image and other details on server.
     */
    private void updateEditedShort(final File file, String entityID, String captureID, String shortID, String uuid, String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String merchantable, String font, String bgColor, String bold, String italic, final String captionText, String imageTintColor) {

        String mMerchantable;

        if (merchantable.equals("true")) {
            mMerchantable = "1";
        } else {
            mMerchantable = "0";
        }

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your writing")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();
        dialog.show();

        Rx2AndroidNetworking.upload(BuildConfig.URL + "/short-upload/edit")
                .setOkHttpClient(okHttpClient)
                .addMultipartFile("short-image", file)
                .addMultipartParameter("entityid", entityID)
                .addMultipartParameter("captureid", captureID)
                .addMultipartParameter("shoid", shortID)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("txt_width", tvWidth)
                .addMultipartParameter("txt_height", tvHeight)
                .addMultipartParameter("img_width", imgWidth)
                .addMultipartParameter("img_height", imgWidth)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("merchantable", mMerchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bgcolor", bgColor)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
                .addMultipartParameter("caption", captionText)
                .addMultipartParameter("imgtintcolor", imageTintColor)
                .addMultipartParameter("filtername", mFilterName)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Changes updated successfully.");

                                    //fixme chandna G.............
                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;


                                    // to invalidate image cache
                                    IMAGE_LOAD_FROM_NETWORK_ME = true;
                                    IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION = true;

                                    Picasso.with(PreviewActivity.this).invalidate(file);


                                    //finish this activity and set result ok
                                    setResult(RESULT_OK, getIntent().putExtra(PREVIEW_EXTRA_CAPTION_TEXT
                                            , captionText));
                                    finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

    /**
     * Method to upload capture/graphic art on server.
     *
     * @param file         File to be saved i.e image file .
     * @param captionText  Caption text
     * @param uuid         UUID of the user.
     * @param authToken    AuthToken of user.
     * @param waterMark    Watermark of user.
     * @param merchantable Whether product is merchantable or not. 1 if merchantable 0 otherwise.
     */
    private void uploadCapture(File file, String captionText, final String uuid, final String authToken, String waterMark, String merchantable) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your graphic art")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        //if watermark is not empty
        if (!TextUtils.isEmpty(waterMark)) {
            CaptureHelper.generateSignatureOnCapture(mWaterMarkText
                    , textWaterMark.getWidth()
                    , textWaterMark.getHeight()
                    , imagePreview.getWidth());
        }

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        AndroidNetworking.upload(BuildConfig.URL + "/capture-upload")
                .addMultipartFile("captured-image", file)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("watermark", waterMark)
                .addMultipartParameter("merchantable", merchantable)
                .addMultipartParameter("caption", captionText)
                .addMultipartParameter("filtername", mFilterName)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Graphic art uploaded successfully");

                                    // Set feeds data to be loaded from network instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_INSPIRATION = true;

                                    // open collaboration invitation dialog
                                    showCollabInvitationDialog(mContext
                                            , mCompositeDisposable
                                            , rootView
                                            , uuid
                                            , authToken
                                            , dataObject.getString("entityid")
                                            , dataObject.getString("captureurl")
                                            , mHelper.getFirstName()
                                            , CONTENT_TYPE_CAPTURE);
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
                        dialog.dismiss();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });

    }

    /**
     * Method to upload edited capture.
     *
     * @param captionText Caption text
     * @param uuid        UUID of the user.
     * @param authToken   AuthToken of user.
     * @param entityID    Entity ID of capture.
     */
    private void uploadEditedCapture(String captionText, String uuid, String authToken, String entityID) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Updating your caption")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        AndroidNetworking.post(BuildConfig.URL + "/entity-manage/edit-caption")
                .addBodyParameter("uuid", uuid)
                .addBodyParameter("authkey", authToken)
                .addBodyParameter("caption", captionText)
                .addBodyParameter("entityid", entityID)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Caption updated successfully");

                                    // Set feeds data to be loaded from network instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;

                                    //finish this activity and set result ok
                                    setResult(RESULT_OK, getIntent().putExtra(PREVIEW_EXTRA_CAPTION_TEXT
                                            , mCapInMentionFormat));
                                    finish();
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
                        dialog.dismiss();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to toggle visibility of filter bottomSheet.
     */
    private void toggleBottomSheet() {
        if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            //Show bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }


    /**
     * Method to initialize filter view.
     */
    private void initFilterView() {
        //Setup bottom sheets
        filterSheetBehavior = BottomSheetBehavior.from(filterBottomSheetView);
        filterSheetBehavior.setPeekHeight(0);

        //Create filter data list
        final List<FilterModel> filterDataList = new ArrayList<>();
        //Create filter adapter
        adapter = new FilterAdapter(filterDataList);
        //Set listener
        adapter.setOnFilterSelectListener(new listener.OnFilterSelectListener() {
            @Override
            public void onFilterSelected(Bitmap bitmap, String filterNAme) {
                //Set bitmap
                imagePreview.setImageBitmap(bitmap);
                //Update flag
                mFilterName = filterNAme;
            }
        });
        //Set layout manager
        filterRecyclerView.setLayoutManager(new LinearLayoutManager(PreviewActivity.this
                , LinearLayoutManager.HORIZONTAL
                , false));
        filterRecyclerView.setHasFixedSize(true);
        //Set adapter
        filterRecyclerView.setAdapter(adapter);
        //Method call
        addFilters(filterDataList, adapter);
    }

    /**
     * Method to add filter to filterDataList.
     *
     * @param dataList Filter data list.
     * @param adapter  FilterAdapter reference.
     */
    private void addFilters(final List<FilterModel> dataList, final FilterAdapter adapter) {
        String path;
        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_CAPTURE)) {
            path = ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).toString();
        } else {
            path = ImageHelper.getImageUri(IMAGE_TYPE_USER_SHORT_PIC).toString();
        }

        path = Uri.parse(path).getPath();
        if (path != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inMutable = true;
            opts.inJustDecodeBounds = false;

            bmp = BitmapFactory.decodeFile(path, opts);
            opts.inSampleSize = calculateInSampleSize(opts, 300, 300);
            bmp = BitmapFactory.decodeFile(path, opts);

            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    FilterModel original = new FilterModel("Original", bmp, null);
                    FilterModel starLit = new FilterModel("Star Lit", bmp, CustomFilters.getStarLitFilter());
                    FilterModel blueMess = new FilterModel("Blue Mess", bmp, CustomFilters.getBlueMessFilter());
                    FilterModel aweStruckVibe = new FilterModel("Awe Struck Vibe", bmp, CustomFilters.getAweStruckVibeFilter());
                    FilterModel limeStutter = new FilterModel("Lime Stutter", bmp, CustomFilters.getLimeStutterFilter());
                    FilterModel nightWhisper = new FilterModel("Night Whisper", bmp, CustomFilters.getNightWhisperFilter());
                    FilterModel blackAndWhite = new FilterModel("Black & White", bmp, CustomFilters.getBlackAndWhiteFilter());
                    FilterModel sepia = new FilterModel("Sepia", bmp, CustomFilters.getSepiaFilter());
                    FilterModel amazon = new FilterModel("Amazon", bmp, CustomFilters.getAmazonFilter());
                    FilterModel adele = new FilterModel("Adele", bmp, CustomFilters.getAdeleFilter());
                    FilterModel cruz = new FilterModel("Cruz", bmp, CustomFilters.getCruzFilter());
                    FilterModel metropolis = new FilterModel("Metropolis", bmp, CustomFilters.getMetropolisFilter());
                    FilterModel audrey = new FilterModel("Audrey", bmp, CustomFilters.getAudreyFilter());
                    FilterModel rise = new FilterModel("Rise", bmp, CustomFilters.getRiseFilter(PreviewActivity.this));
                    FilterModel mars = new FilterModel("Mars", bmp, CustomFilters.getMarsFilter());
                    FilterModel april = new FilterModel("April", bmp, CustomFilters.getAprilFilter(PreviewActivity.this));
                    FilterModel han = new FilterModel("Han", bmp, CustomFilters.getHanFilter(PreviewActivity.this));
                    FilterModel oldMan = new FilterModel("Old Man", bmp, CustomFilters.getOldManFilter(PreviewActivity.this));
                    FilterModel clarendon = new FilterModel("Clarendon", bmp, CustomFilters.getClarendonFilter());

                    dataList.clear();
                    dataList.add(original); // Original Image
                    dataList.add(starLit);
                    dataList.add(blueMess);
                    dataList.add(aweStruckVibe);
                    dataList.add(limeStutter);
                    dataList.add(nightWhisper);
                    dataList.add(blackAndWhite);
                    dataList.add(sepia);
                    dataList.add(amazon);
                    dataList.add(adele);
                    dataList.add(cruz);
                    dataList.add(metropolis);
                    dataList.add(audrey);
                    dataList.add(rise);
                    dataList.add(mars);
                    dataList.add(april);
                    dataList.add(han);
                    dataList.add(oldMan);
                    dataList.add(clarendon);

                    adapter.notifyDataSetChanged();
                }
            };
            handler.post(r);
        }
    }

    /**
     * Method to calculate a sample size value that is a power of two based on a target width and height:
     */
    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //For more information
        // https://developer.android.com/topic/performance/graphics/load-bitmap.html#read-bitmap

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Method to check for write external storage permission and perform required operation.
     */
    private void checkRuntimePermission() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            saveBmpToFile();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(PreviewActivity.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                //Show error message
                ViewHelper.getToast(PreviewActivity.this
                        , getString(R.string.error_msg_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(PreviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, writePermission);
            }
        }
    }

    /**
     * Used to handle result of askForPermission for storage.
     */
    PermissionCallback writePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            saveBmpToFile();
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(PreviewActivity.this
                    , getString(R.string.error_msg_permission_denied));
        }
    };

    /**
     * Method to save bitmap to file and upload it on server.
     */
    private void saveBmpToFile() {
        String s;
        Uri imageUri;

        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_CAPTURE)) {
            imageUri = ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC);
            s = "/Cread/Capture/capture_pic.jpg";
        } else {
            imageUri = ImageHelper.getImageUri(IMAGE_TYPE_USER_SHORT_PIC);
            s = "/Cread/Short/short_pic.jpg";
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inMutable = true;
        //Decode bitmap from file
        Bitmap bmp = BitmapFactory.decodeFile(imageUri.getPath(), opts);

        Filter selectedFilter = adapter.getFilterSelected();

        if (selectedFilter != null) {
            //Apply filter on  bitmap
            bmp = selectedFilter.processFilter(bmp);

            try {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + s);
                file.getParentFile().mkdirs();

                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
                out.close();
                performUpdateOperation();

            } catch (IOException e) {
                e.printStackTrace();
                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
            }
        } else {
            performUpdateOperation();
        }

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

                etCaption.setMentionSpanConfig(getMentionSpanConfig(mContext));
                etCaption.insertMention(person);
                recyclerViewMentions.setAdapter(mMentionsAdapter);
                displaySuggestions(false);
                etCaption.requestFocus();
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
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

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
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }

                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        //Remove loading item
                        mSuggestionsList.remove(mSuggestionsList.size() - 1);
                        mMentionsAdapter.notifyItemRemoved(mSuggestionsList.size());
                        FirebaseCrash.report(e);
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

    //endregion

}

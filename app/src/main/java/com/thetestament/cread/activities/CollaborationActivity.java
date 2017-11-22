package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.utils.SquareView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * This class shows the preview of collaboration.
 */

public class CollaborationActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageContainer)
    SquareView squareView;
    @BindView(R.id.imageCapture)
    ImageView imageShort;
    @BindView(R.id.textShort)
    TextView textShort;
    @BindView(R.id.progressView)
    View viewProgress;

    /**
     * Flag to maintain gravity status i.e 0 for center , 1 for right and 2 for left.
     */
    @State
    int mGravityFlag = 0;

    //ENUM for text gravity
    private enum TextGravity {
        Center, East, West
    }

    //Initially text gravity is "CENTER"
    TextGravity textGravity = TextGravity.Center;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setContentView(R.layout.activity_collaboration);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collaboration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                finish();
                return true;
            case R.id.action_next:
                //todo functionality
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        //Change short text color
        textShort.setTextColor(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    /**
     * Functionality to toggle the text gravity.
     */
    @OnClick(R.id.btnLAlignText)
    public void onBtnLAlignTextClicked(ImageView btnAlignText) {

        switch (mGravityFlag) {
            case 0:
                //Set text gravity
                textShort.setGravity(Gravity.RIGHT);
                //Change button drawable
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_right_32));
                //Change gravity flag
                mGravityFlag = 1;
                //Set gravity variable
                textGravity = TextGravity.East;
                break;
            case 1:
                textShort.setGravity(Gravity.LEFT);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_left_32));
                mGravityFlag = 2;
                textGravity = TextGravity.West;
                break;
            case 2:
                textShort.setGravity(Gravity.CENTER);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_center_32));
                mGravityFlag = 0;
                textGravity = TextGravity.Center;
                break;
        }
    }

    /**
     * Click functionality to increase 'Short text' size by five unit..
     */
    @OnClick(R.id.btnFormatTextSizePlus)
    public void onBtnFormatTextSizePlusClicked() {
        int ts = (int) textShort.getTextSize() + 5;
        //Increase text size by 5 unit
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    /**
     * Click functionality to decrease 'Short text' by five unit.
     */
    @OnClick(R.id.btnFormatTextSizeMinus)
    public void onBtnFormatTextSizeMinusClicked() {
        int ts = (int) textShort.getTextSize() - 5;
        //Decrease text size by five unit
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    public void onBtnFormatTextColorClicked() {
        // Pass a context, along with the title of the dialog
        new ColorChooserDialog.Builder(this, R.string.text_color)
                // title of dialog when viewing shades of a color
                .titleSub(R.string.text_color)
                // when true, will display accent palette instead of primary palette
                .accentMode(false)
                // changes label of the done button
                .doneButton(R.string.md_done_label)
                // changes label of the cancel button
                .cancelButton(R.string.md_cancel_label)
                // changes label of the back button
                .backButton(R.string.md_back_label)
                // defaults to true, false will disable changing action buttons' color to currently selected color
                .dynamicButtonColor(true)
                .show(); // an AppCompatActivity which implements ColorCallback
    }


    /**
     *
     * */
    private void loadShortData() {
        //Show progress view
        viewProgress.setVisibility(View.VISIBLE);

        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("uuid", mHelper.getUUID());
            requestObject.put("authkey", mHelper.getAuthToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Rx2AndroidNetworking.post(BuildConfig.URL + "/dummyUrl")
                .addJSONObjectBody(requestObject)
                .build()
                .getJSONObjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //Add composite disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                    }
                });


    }

}

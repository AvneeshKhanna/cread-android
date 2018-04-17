package com.thetestament.cread.helpers;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ViewLongShortActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.ShortModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_VIEW_LONG_SHORT;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.EXTRA_SHORT_DATA;

/**
 * Created by prakharchandna on 29/03/18.
 */

public class LongShortHelper {

    public void getLongShortData(final FragmentActivity context,
                                 CompositeDisposable compositeDisposable,
                                 String entityid,
                                 final listener.OnLongShortDataRequestedListener onLongShortDataRequestedListener) {


        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Loading")
                .content(context.getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable, NetworkHelper.getLoadLongShortObservable(BuildConfig.URL + "/entity-manage/load-data-long-form",
                spHelper.getUUID()
                , spHelper.getAuthToken()
                , entityid
                , GET_RESPONSE_FROM_NETWORK_VIEW_LONG_SHORT)
                , context
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        dialog.dismiss();
                        onLongShortDataRequestedListener.onLongShortDataFailiure(context
                                .getString(R.string.error_msg_no_connection));

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        dialog.dismiss();

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Set listener
                                onLongShortDataRequestedListener.onLongShortDataFailiure((context.getString(R.string.error_msg_invalid_token)));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");

                                ShortModel shortModel = new ShortModel();


                                shortModel.setContentText(mainData.getString("text"));
                                shortModel.setBold(ShortModel.convertIntToBool(mainData.getInt("bold")));
                                shortModel.setItalic(ShortModel.convertIntToBool(mainData.getInt("italic")));
                                shortModel.setBgcolor(mainData.getString("bgcolor"));
                                shortModel.setFont(mainData.getString("font"));
                                shortModel.setImgTintColor(mainData.getString("imgtintcolor"));
                                shortModel.setTextSize(mainData.getDouble("textsize"));
                                shortModel.setTextColor(mainData.getString("textcolor"));
                                shortModel.setTextGravity(mainData.getString("textgravity"));
                                shortModel.setTextShadow(ShortModel.convertIntToBool(mainData.getInt("textshadow")));
                                shortModel.setImageURL(mainData.getString("entityurl"));
                                shortModel.setImgWidth(mainData.getDouble("img_width"));
                                shortModel.setBgSound(mainData.getString("bg_sound"));

                                //Set listener
                                onLongShortDataRequestedListener.onLongShortDataSuccess(shortModel);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Set listener
                            onLongShortDataRequestedListener.onLongShortDataFailiure((context.getString(R.string.error_msg_internal)));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Set listener
                        onLongShortDataRequestedListener.onLongShortDataFailiure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                        // update status
                        GET_RESPONSE_FROM_NETWORK_VIEW_LONG_SHORT = false;
                    }
                });
    }


    /**
     * Checks whether writing is long form
     * and toggles the preview option visibility
     *
     * @param containerLongShortPreview
     * @param data
     */
    public static void checkLongFormStatus(View containerLongShortPreview, FeedModel data) {
        if (data.isLongForm()) {
            containerLongShortPreview.setVisibility(View.VISIBLE);
        } else {
            containerLongShortPreview.setVisibility(View.GONE);
        }
    }

    /**
     * sets preview on click listener
     *
     * @param view
     * @param data
     */
    public static void initLongFormPreviewClick(View view, final FeedModel data, final FragmentActivity context, final CompositeDisposable compositeDisposable) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LongShortHelper longShortHelper = new LongShortHelper();
                longShortHelper.getLongShortData(context, compositeDisposable, data.getEntityID(), new listener.OnLongShortDataRequestedListener() {
                    @Override
                    public void onLongShortDataSuccess(ShortModel shortModel) {
                        // open activity and pass data
                        Intent intent = new Intent(context, ViewLongShortActivity.class);
                        intent.putExtra(EXTRA_SHORT_DATA, shortModel);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onLongShortDataFailiure(String errorMsg) {

                        ViewHelper.getToast(context, errorMsg);

                    }
                });
            }
        });
    }


    // Long form sounds
    public static final String LONG_FORM_SOUND_NONE = "none";
}

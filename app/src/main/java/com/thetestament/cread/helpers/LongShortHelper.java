package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ShortModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.requestServer;

/**
 * Created by prakharchandna on 29/03/18.
 */

public class LongShortHelper {

    public void getLongShortData(final FragmentActivity context,
                                 CompositeDisposable compositeDisposable,
                                 String entityid,
                                 final listener.OnLongShortDataRequestedListener onLongShortDataRequestedListener) {
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable, NetworkHelper.getLoadLongShortObservable(BuildConfig.URL + "/entity-manage/load-data-long-form",
                spHelper.getUUID()
                , spHelper.getAuthToken()
                , entityid
                , true)
                , context
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        onLongShortDataRequestedListener.onLongShortDataFailiure(context
                                .getString(R.string.error_msg_no_connection));

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {


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

                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Set listener
                        onLongShortDataRequestedListener.onLongShortDataFailiure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {


                    }
                });
    }
}

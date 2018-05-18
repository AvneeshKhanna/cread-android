package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.SuggestedArtistsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS;
import static com.thetestament.cread.helpers.NetworkHelper.geSuggestedArtistsDataFromServer;

/**
 * A helper class to provide utility methods for Artist suggestion.
 */

public class SuggestionHelper {

    /**
     * Method to retrieve SuggestedList data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param listener            OnSuggestedArtistLoadListener reference
     */
    public void getSuggestedArtist(final FragmentActivity context
            , CompositeDisposable compositeDisposable
            , final listener.OnSuggestedArtistLoadListener listener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        final List<SuggestedArtistsModel> dataLIst = new ArrayList<>();

        compositeDisposable.add(geSuggestedArtistsDataFromServer(BuildConfig.URL + "/recommend-users/load"
                , spHelper.getUUID()
                , spHelper.getAuthToken())
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
                                listener.onFailure(context.getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                //Suggested artists list
                                JSONArray jsonArray = mainData.getJSONArray("items");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject dataObj = jsonArray.getJSONObject(i);
                                    SuggestedArtistsModel artistsModel = new SuggestedArtistsModel();
                                    //Set artists property
                                    artistsModel.setArtistUUID(dataObj.getString("uuid"));
                                    artistsModel.setArtistName(dataObj.getString("name"));
                                    artistsModel.setArtistProfilePic(dataObj.getString("profilepicurl"));
                                    dataLIst.add(artistsModel);
                                }
                                listener.onSuccess(dataLIst);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "SuggestionHelper");
                            listener.onFailure(context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "SuggestionHelper");
                        listener.onFailure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Set to false
                        GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS = false;
                    }
                }));
    }

}

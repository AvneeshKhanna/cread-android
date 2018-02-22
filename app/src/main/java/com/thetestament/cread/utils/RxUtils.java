package com.thetestament.cread.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.thetestament.cread.database.NotificationsDBFunctions;

import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by prakharchandna on 16/02/18.
 */

public class RxUtils {

    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> subscriber) {
                        T observed = null;
                        try {
                            observed = func.call();

                            if (observed != null) { // to make defaultIfEmpty work
                                subscriber.onNext(observed);
                            }
                            subscriber.onComplete();
                        } catch (Exception e) {

                            subscriber.onError(e);

                        }

                    }
                }
        );
    }


    // and finally,
    public static Observable getUserActionsDataObservable(FragmentActivity context, String entityid, String actionType) {

        NotificationsDBFunctions dbFunctions = new NotificationsDBFunctions(context);
        dbFunctions.accessNotificationsDatabase();
        return makeObservable(dbFunctions.getData(entityid, actionType));
    }
}

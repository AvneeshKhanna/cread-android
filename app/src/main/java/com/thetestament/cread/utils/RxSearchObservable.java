package com.thetestament.cread.utils;


import android.support.v7.widget.SearchView;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Reactive Observable for searchView.
 */

public class RxSearchObservable {

    /**
     * Method to create observables from searchView.
     *
     * @param searchView SearchView reference.
     */
    public static Observable<String> fromView(SearchView searchView) {

        final PublishSubject<String> subject = PublishSubject.create();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //subject.onComplete();
                if (!s.isEmpty()) {
                    subject.onNext(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (!text.isEmpty()) {
                    subject.onNext(text);
                }
                //subject.onNext(text);
                return true;
            }
        });
        return subject;
    }

}

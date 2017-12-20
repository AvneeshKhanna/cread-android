package com.thetestament.cread.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Content provider for search suggestion.
 */

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.thetestament.cread.providers.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}

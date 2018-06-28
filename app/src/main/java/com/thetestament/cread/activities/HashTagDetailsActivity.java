package com.thetestament.cread.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.HashTagDetailsFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import butterknife.ButterKnife;

import static com.thetestament.cread.utils.Constant.BUNDLE_HASHTAG_NAME;
import static com.thetestament.cread.utils.Constant.TAG_HASH_TAG_DETAILS_FRAGMENT;


/**
 * Reference : http://sourabhsoni.com/implementing-hashtags-in-android-application/
 */
public class HashTagDetailsActivity extends BaseActivity {

    SharedPreferenceHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tag_details);
        ButterKnife.bind(this);

        spHelper = new SharedPreferenceHelper(this);
        // initialize view
        initView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize view
     */
    private void initView() {
        //Get the content URI
        Uri uri = getIntent().getData();
        //strip off hashtag from the URI
        String hashTagData = uri.toString().split("/")[3];
        String hashTagPostCount = hashTagData.split(":")[0];
        String hashTag = hashTagData.split(":")[1];

        long postsCount = Long.parseLong(hashTagPostCount);

        // posts count is not equal to -1 when hash tag screen is opened by clicking hash tag of the day
        if (postsCount != -1) {
            spHelper.setHTagCount(postsCount);
            spHelper.setHTagNewPostsIndicatorVisibility(false);
        }

        // set title
        getSupportActionBar().setTitle(hashTag);

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_HASHTAG_NAME, hashTag);

        HashTagDetailsFragment hashTagFragment = new HashTagDetailsFragment();
        hashTagFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.containerHashTagActivity, hashTagFragment, TAG_HASH_TAG_DETAILS_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}

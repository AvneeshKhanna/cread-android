package com.thetestament.cread.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.HashTagDetailsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.thetestament.cread.utils.Constant.BUNDLE_HASHTAG_NAME;
import static com.thetestament.cread.utils.Constant.TAG_HASH_TAG_DETAILS_FRAGMENT;

public class HashTagDetailsActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tag_details);
        ButterKnife.bind(this);

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
        String hashTag = uri.toString().split("/")[3];
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

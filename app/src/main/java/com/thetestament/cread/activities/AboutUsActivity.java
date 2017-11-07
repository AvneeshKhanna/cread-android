package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Class to show description about app.
 */

public class AboutUsActivity extends BaseActivity {

    @BindView(R.id.app_version_code)
    TextView textAppVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ButterKnife.bind(this);

        //Set version coded
        String versionName = BuildConfig.VERSION_NAME;
        textAppVersionCode.setText("Version " + versionName);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}

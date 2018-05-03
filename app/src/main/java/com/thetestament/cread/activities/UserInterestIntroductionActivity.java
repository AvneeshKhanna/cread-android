package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.thetestament.cread.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInterestIntroductionActivity extends BaseActivity {


    @BindView(R.id.textWelcome)
    TextView textWelcome;
    @BindView(R.id.buttonOpenUserInterests)
    TextView buttonOpenUserInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interest_introduction);
        ButterKnife.bind(this);

        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        a.reset();
        TextView tv = (TextView) findViewById(R.id.textWelcome);
        tv.clearAnimation();
        tv.startAnimation(a);

    }


    @OnClick(R.id.buttonOpenUserInterests)
    void onChooseClick() {
        Intent intent = new Intent(this, UserInterestsActivity.class);
        startActivity(intent);
    }
}

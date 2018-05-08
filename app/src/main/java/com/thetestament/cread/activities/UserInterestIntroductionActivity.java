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

import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.USER_INTERESTS_CALLED_FROM_LOGIN;

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
        intent.putExtra(EXTRA_USER_INTERESTS_CALLED_FROM, USER_INTERESTS_CALLED_FROM_LOGIN);
        startActivity(intent);
    }
}

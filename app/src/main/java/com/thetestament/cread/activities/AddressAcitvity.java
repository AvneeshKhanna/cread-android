package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_COLOR;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_DELIVERY_CHARGE;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_DELIVERY_TIME;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_ENTITYID;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_PRICE;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_PRODUCTID;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_QUANTITY;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_SIZE;
import static com.thetestament.cread.utils.Constant.EXTRA_PRODUCT_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_ADDR1;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_ADDR2;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_ALT_PHONE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_CITY;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_PHONE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_PINCODE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHIPPING_STATE;
import static com.thetestament.cread.utils.Constant.PAYMENT_STATUS_CONNECTION_TERMINATED;
import static com.thetestament.cread.utils.Constant.PAYMENT_STATUS_INVALID_TOKEN;
import static com.thetestament.cread.utils.Constant.PAYMENT_STATUS_SERVER_ERROR;
import static com.thetestament.cread.utils.Constant.PAYMENT_STATUS_SUCCESS;

public class AddressAcitvity extends BaseActivity implements PaymentResultListener {

    @BindView(R.id.productName)
    TextView productNameTV;
    @BindView(R.id.prodSpecs)
    TextView prodSpecs;
    @BindView(R.id.quantity)
    TextView quantityTV;
    @BindView(R.id.deliveryTime)
    TextView deliveryTimeTV;
    @BindView(R.id.amtItem)
    TextView amtItem;
    @BindView(R.id.amtDelivery)
    TextView amtDelivery;
    @BindView(R.id.amtTotal)
    TextView amtTotal;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.tiName)
    TextInputLayout tiName;
    @BindView(R.id.etPhone)
    EditText etPhone;
    @BindView(R.id.tiPhone)
    TextInputLayout tiPhone;
    @BindView(R.id.etAltPhone)
    EditText etAltPhone;
    @BindView(R.id.tiAltPhone)
    TextInputLayout tiAltPhone;
    @BindView(R.id.etCity)
    EditText etCity;
    @BindView(R.id.tiCity)
    TextInputLayout tiCity;
    @BindView(R.id.etAddr1)
    EditText etAddr1;
    @BindView(R.id.tiAddr1)
    TextInputLayout tiAddr1;
    @BindView(R.id.etAddr2)
    EditText etAddr2;
    @BindView(R.id.tiAddr2)
    TextInputLayout tiAddr2;
    @BindView(R.id.etPincode)
    EditText etPincode;
    @BindView(R.id.tiPincode)
    TextInputLayout tiPincode;
    @BindView(R.id.etState)
    EditText etState;
    @BindView(R.id.tiState)
    TextInputLayout tiState;
    @BindView(R.id.pay)
    TextView payButton;
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    public final String TAG = getClass().getSimpleName();
    String productName, size, color, quantity, price, productID, entityID, deliveryTime, deliveryCharge, shipName, shipPhone, shipAltPhone, shipAddr1, shipAddr2, shipCity, shipState, shipPincode;
    int totalAmount;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_acitvity);
        ButterKnife.bind(this);

        // to prevent keyboard from popping up when screen is opened
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        Bundle bundle = getIntent().getExtras();

        // getting product details from products listing activity
        productName = bundle.getString(EXTRA_PRODUCT_TYPE);
        size = bundle.getString(EXTRA_PRODUCT_SIZE);
        color = bundle.getString(EXTRA_PRODUCT_COLOR);
        quantity = bundle.getString(EXTRA_PRODUCT_QUANTITY);
        price = bundle.getString(EXTRA_PRODUCT_PRICE);
        productID = bundle.getString(EXTRA_PRODUCT_PRODUCTID);
        entityID = bundle.getString(EXTRA_PRODUCT_ENTITYID);
        deliveryTime = bundle.getString(EXTRA_PRODUCT_DELIVERY_TIME);
        deliveryCharge = bundle.getString(EXTRA_PRODUCT_DELIVERY_CHARGE);

        // getting shipping details from products listing activity
        // can be empty string
        shipName = bundle.getString(EXTRA_SHIPPING_NAME);
        shipPhone = bundle.getString(EXTRA_SHIPPING_PHONE);
        shipAltPhone = bundle.getString(EXTRA_SHIPPING_ALT_PHONE);
        shipCity = bundle.getString(EXTRA_SHIPPING_CITY);
        shipAddr1 = bundle.getString(EXTRA_SHIPPING_ADDR1);
        shipAddr2 = bundle.getString(EXTRA_SHIPPING_ADDR2);
        shipPincode = bundle.getString(EXTRA_SHIPPING_PINCODE);
        shipState = bundle.getString(EXTRA_SHIPPING_STATE);

        totalAmount = getTotalAmount();


        // For product details
        productNameTV.setText(productName);
        prodSpecs.setText(size + ", " + color);
        quantityTV.setText("X " + quantity);
        deliveryTimeTV.setText(deliveryTime);

        // For price details
        amtItem.setText(getString(R.string.Rs) + price + " X" + quantity);
        amtDelivery.setText(getString(R.string.Rs) + deliveryCharge);
        amtTotal.setText(getString(R.string.Rs) + String.valueOf(totalAmount));

        // For shipping To
        etName.setText(shipName);
        etPhone.setText(shipPhone);
        etAltPhone.setText(shipAltPhone);

        // For shipping address
        etCity.setText(shipCity);
        etAddr1.setText(shipAddr1);
        etAddr2.setText(shipAddr2);
        etPincode.setText(shipPincode);
        etState.setText(shipState);


    }

    /**
     * calculates the total amount based on the price and quantity of the product including the delivery charges
     *
     * @return integer total amount
     */
    private int getTotalAmount() {
        return Integer.parseInt(price) * Integer.parseInt(quantity) + Integer.parseInt(deliveryCharge);
    }

    @OnClick(R.id.pay)
    public void onViewClicked() {

        shipName = etName.getText().toString().trim();
        shipPhone = etPhone.getText().toString().trim();
        shipAltPhone = etAltPhone.getText().toString().trim();

        shipCity = etCity.getText().toString().trim();
        shipAddr1 = etAddr1.getText().toString().trim();
        shipAddr2 = etAddr2.getText().toString().trim();
        shipPincode = etPincode.getText().toString().trim();
        shipState = etState.getText().toString().trim();

        if (shipName.isEmpty()) {
            tiName.setError("Name " + getString(R.string.error_field_empty));
            tiName.setFocusableInTouchMode(true);
            tiName.requestFocus();
            //scrollView.smoothScrollTo(0, tiName.getBottom() - tiName.getHeight());

        } else if (shipPhone.isEmpty()) {
            tiPhone.setError("Phone " + getString(R.string.error_field_empty));
            tiPhone.setFocusableInTouchMode(true);
            tiPhone.requestFocus();
            //scrollView.smoothScrollTo(0, tiPhone.getBottom() - tiPhone.getHeight());
        } else if (shipAddr1.isEmpty()) {
            tiAddr1.setError("Address Line 1 " + getString(R.string.error_field_empty));
            tiAddr1.setFocusableInTouchMode(true);
            tiAddr1.requestFocus();
            //scrollView.smoothScrollTo(0, tiAddr1.getBottom() - tiAddr1.getHeight());
        } else if (shipCity.isEmpty()) {
            tiCity.setError("City " + getString(R.string.error_field_empty));
            tiCity.setFocusableInTouchMode(true);
            tiCity.requestFocus();
            //scrollView.smoothScrollTo(0, tiCity.getBottom() - tiCity.getHeight());
        } else if (shipState.isEmpty()) {
            tiState.setError("State " + getString(R.string.error_field_empty));
            tiState.setFocusableInTouchMode(true);
            tiState.requestFocus();
            //scrollView.smoothScrollTo(0, tiState.getBottom() - tiState.getHeight());
        } else if (shipPincode.isEmpty()) {
            tiPincode.setError("Pincode " + getString(R.string.error_field_empty));
            tiPincode.setFocusableInTouchMode(true);
            tiPincode.requestFocus();
            //scrollView.smoothScrollTo(0, tiPincode.getBottom() - tiPincode.getHeight());
        }

        else
        {
            startPayment();
        }



    }

    private void startPayment() {

        // Instantiate Checkout
        Checkout checkout = new Checkout();


        try {
            JSONObject options = new JSONObject();

            options.put("name", "Cread");
            options.put("currency", "INR");
            options.put("amount", String.valueOf(totalAmount * 100));

            JSONObject theme = new JSONObject();
            JSONObject prefill = new JSONObject();

            prefill.put("contact", shipPhone);
            theme.put("color", "#F44336");
            options.put("prefill", prefill);
            options.put("theme", theme);
            checkout.open(AddressAcitvity.this, options);

        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
            FirebaseCrash.report(e);
        }
    }


    @Override
    public void onPaymentSuccess(String paymentID) {


        try {
            if (NetworkHelper.getNetConnectionStatus(AddressAcitvity.this)) {

                updatePaymentStatus(paymentID);

            } else

            {
                showPaymentStatusDialog(PAYMENT_STATUS_CONNECTION_TERMINATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(e);

            showPaymentStatusDialog(PAYMENT_STATUS_SERVER_ERROR);
        }

    }

    @Override
    public void onPaymentError(int code, String response) {

        if (code == Checkout.NETWORK_ERROR) {
            showPaymentStatusDialog(PAYMENT_STATUS_CONNECTION_TERMINATED);
        } else if (code == Checkout.PAYMENT_CANCELED) {
            ViewHelper.getSnackBar(rootView, "Payment Cancelled");
        } else if (code == Checkout.INVALID_OPTIONS) {
            ViewHelper.getSnackBar(rootView, "Some error occured");
        }

    }


    private void showPaymentStatusDialog(String status) {
        MaterialDialog dialog = new MaterialDialog.Builder(AddressAcitvity.this)
                .customView(R.layout.dialog_payment_status, false)
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {

                        startActivity(new Intent(AddressAcitvity.this, MerchandizingProductsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        finish();
                    }
                })
                .show();

        TextView titleText = (TextView) dialog.getCustomView().findViewById(R.id.text_status);
        TextView descText = (TextView) dialog.getCustomView().findViewById(R.id.text_desc);
        ImageView statusImage = (ImageView) dialog.getCustomView().findViewById(R.id.image_status);

        if (status.equals(PAYMENT_STATUS_CONNECTION_TERMINATED)) {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_conn_term));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this, R.drawable.ic_error));
        } else if (status.equals(PAYMENT_STATUS_INVALID_TOKEN)) {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_invalid_token));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this, R.drawable.ic_error));

        } else if (status.equals(PAYMENT_STATUS_SERVER_ERROR)) {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_server_error));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this, R.drawable.ic_error));
        } else if (status.equals(PAYMENT_STATUS_SUCCESS)) {
            titleText.setText(getString(R.string.payment_success_title));
            descText.setText(getString(R.string.payment_success_message));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this, R.drawable.ic_check_48dp));
        }

    }

    /**
     * updates the payment details on the server
     *
     * @param paymentid razorpay payment id
     */
    private void updatePaymentStatus(String paymentid) {

        final MaterialDialog dialog = new MaterialDialog.Builder(AddressAcitvity.this)
                .title(getString(R.string.processing_title))
                .content(getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();


        JSONObject data = new JSONObject();

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(AddressAcitvity.this);

        try {

            data.put("uuid", spHelper.getUUID());
            data.put("authkey", spHelper.getAuthToken());
            data.put("productid", productID);
            data.put("entityid", entityID);
            data.put("paymentid", paymentid);
            data.put("amount", String.valueOf(totalAmount * 100));
            data.put("color", color);
            data.put("size", size);
            data.put("price", price);
            data.put("quantity", quantity);
            data.put("billing_name", shipName);
            data.put("billing_alt_contact", shipAltPhone);

            JSONObject shipmentDetails = new JSONObject();
            shipmentDetails.put("ship_addr_1",shipAddr1);
            shipmentDetails.put("ship_addr_2", shipAddr2);
            shipmentDetails.put("ship_city", shipCity);
            shipmentDetails.put("ship_state", shipState);
            shipmentDetails.put("ship_pincode", shipPincode);

            data.put("shipmentdetails", shipmentDetails);


        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/order/place")
                .addJSONObjectBody(data)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull JSONObject jsonObject) {

                        dialog.dismiss();

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {

                                showPaymentStatusDialog(PAYMENT_STATUS_INVALID_TOKEN);
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {

                                    showPaymentStatusDialog(PAYMENT_STATUS_SUCCESS);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            showPaymentStatusDialog(PAYMENT_STATUS_SERVER_ERROR);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        dialog.dismiss();

                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        showPaymentStatusDialog(PAYMENT_STATUS_SERVER_ERROR);
                    }

                    @Override
                    public void onComplete() {

                        // do nothing
                    }
                });

    }
}

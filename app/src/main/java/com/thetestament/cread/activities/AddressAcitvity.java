package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.priceDetails)
    TextView priceDetails;
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
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    public final String TAG = getClass().getSimpleName();
    String productName, size, color, quantity, price, productID, entityID, deliveryTime, deliveryCharge, shipName, shipPhone, shipAltPhone, shipAddr1, shipAddr2, shipCity, shipState, shipPincode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_acitvity);
        ButterKnife.bind(this);



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


        // For product details
        productNameTV.setText(productName);
        prodSpecs.setText(size + ", " + color);
        quantityTV.setText("X " + quantity);
        deliveryTimeTV.setText(deliveryTime);

        // For price details
        amtItem.setText(getString(R.string.Rs) + price + " X" + quantity);
        amtDelivery.setText(deliveryCharge);
        amtTotal.setText(getString(R.string.Rs) + String.valueOf(getTotalAmount()));

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
        // TODO change delivery charge
        return Integer.parseInt(price) * Integer.parseInt(quantity) + Integer.parseInt(/*deliveryCharge*/"300");
    }

    @OnClick(R.id.pay)
    public void onViewClicked() {

       /* if (etName.getText().toString().isEmpty()) {
            tiName.setError("Name " + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiName.getBottom() - tiName.getHeight());

        } else if (etPhone.getText().toString().isEmpty()) {
            tiPhone.setError("Phone " + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiPhone.getBottom() - tiPhone.getHeight());
        } else if (etAddr1.getText().toString().isEmpty()) {
            tiAddr1.setError("Address Line 1 " + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiAddr1.getBottom() - tiAddr1.getHeight());
        } else if (etCity.getText().toString().isEmpty()) {
            tiCity.setError("City" + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiCity.getBottom() - tiCity.getHeight());
        } else if (etState.getText().toString().isEmpty()) {
            tiState.setError("State" + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiState.getBottom() - tiState.getHeight());
        } else if (etPincode.getText().toString().isEmpty()) {
            tiPincode.setError("Pincode" + getString(R.string.error_field_empty));
            nestedScrollView.smoothScrollBy(0, tiPincode.getBottom() - tiPincode.getHeight());
        }

        else
        {
            startPayment();
        }*/

       startPayment();

    }

    private void startPayment()
    {

        // Instantiate Checkout
        Checkout checkout = new Checkout();


        try
        {
            JSONObject options = new JSONObject();

            options.put("name", "Cread");
            options.put("currency", "INR");
            options.put("amount",String.valueOf(getTotalAmount() * 100));

            JSONObject theme = new JSONObject();

            theme.put("color","#F44336");
            options.put("theme",theme);
            checkout.open(AddressAcitvity.this, options);

        }
        catch (Exception e)
        {   e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
            FirebaseCrash.report(e);
        }
    }


    @Override
    public void onPaymentSuccess(String paymentID) {

        if(NetworkHelper.getNetConnectionStatus(AddressAcitvity.this))
        {

        }

        else

        {
            showPaymentStatusDialog(PAYMENT_STATUS_CONNECTION_TERMINATED);
        }

    }

    @Override
    public void onPaymentError(int code, String response) {

        if(code == Checkout.NETWORK_ERROR)
        {
            showPaymentStatusDialog(PAYMENT_STATUS_CONNECTION_TERMINATED);
        }

        else if(code == Checkout.PAYMENT_CANCELED)
        {
            ViewHelper.getSnackBar(rootView, "Payment Cancelled");
        }

        else if(code == Checkout.INVALID_OPTIONS)
        {
            ViewHelper.getSnackBar(rootView, "Some error occured");
        }

    }


    private void showPaymentStatusDialog(String status)
    {
        MaterialDialog dialog = new MaterialDialog.Builder(AddressAcitvity.this)
                .customView(R.layout.dialog_payment_status, false)
                .positiveText("Ok")
                .show();

        TextView titleText = (TextView) dialog.getCustomView().findViewById(R.id.text_status);
        TextView descText = (TextView) dialog.getCustomView().findViewById(R.id.text_desc);
        ImageView statusImage = (ImageView) dialog.getCustomView().findViewById(R.id.image_status);

        if(status.equals(PAYMENT_STATUS_CONNECTION_TERMINATED))
        {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_conn_term));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this,R.drawable.ic_error));
        }

        else if(status.equals(PAYMENT_STATUS_INVALID_TOKEN))
        {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_invalid_token));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this,R.drawable.ic_error));

        }
        else if(status.equals(PAYMENT_STATUS_SERVER_ERROR))
        {
            titleText.setText(getString(R.string.payment_failed_title));
            descText.setText(getString(R.string.payment_failed_server_error));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this,R.drawable.ic_error));
        }


        else if(status.equals(PAYMENT_STATUS_SUCCESS))
        {
            titleText.setText(getString(R.string.payment_success_title));
            descText.setText(getString(R.string.payment_success_message));
            statusImage.setImageDrawable(ContextCompat.getDrawable(AddressAcitvity.this,R.drawable.ic_check_48dp));
        }

    }

}

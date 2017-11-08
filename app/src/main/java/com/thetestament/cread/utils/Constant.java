package com.thetestament.cread.utils;


public class Constant {
    //Request codes
    public static final int REQUEST_CODE_UPDATES_ACTIVITY = 1000;
    public static final int REQUEST_CODE_FB_ACCOUNT_KIT = 1001;
    public static final int REQUEST_CODE_COMMENTS_ACTIVITY = 1002;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1003;
    public static final int REQUEST_CODE_UPDATE_PROFILE_PIC = 1004;
    public static final int REQUEST_CODE_UPDATE_PROFILE_DETAILS = 1005;
    public static final int REQUEST_CODE_OPEN_GALLERY = 1006;
    public static final int REQUEST_CODE_INSPIRATION_ACTIVITY = 1007;

    //Tag value for fragments
    public static final String TAG_FEED_FRAGMENT = "TagFeedFragment";
    public static final String TAG_EXPLORE_FRAGMENT = "TagExploreFragment";
    public static final String TAG_ME_FRAGMENT = "TagMeFragment";
    public static final String FRAGMENT_TAG_UPDATES_FRAGMENT = "TagUpdatesFragment";

    //Content Types
    public static final String CONTENT_TYPE_SHORT = "SHORT";
    public static final String CONTENT_TYPE_CAPTURE = "CAPTURE";

    //Image Types
    public static final String IMAGE_TYPE_USER_PROFILE_PIC = "userProfilePic";
    public static final String IMAGE_TYPE_USER_CAPTURE_PIC = "UserCapturePic";
    public static final String IMAGE_TYPE_USER_SHORT_PIC = "UserShortPic";
    //User Activity type
    public static final String USER_ACTIVITY_TYPE_ALL = "typeAll";
    public static final String USER_ACTIVITY_TYPE_SHORT = "typeShort";
    public static final String USER_ACTIVITY_TYPE_CAPTURE = "typeCapture";

    //Extra data
    public static final String EXTRA_ENTITY_ID = "entityID";
    public static final String EXTRA_CAPTURE_ID = "captureID";
    public static final String EXTRA_CAPTURE_URL = "captureURL";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_PROFILE_UUID = "profileUUID";
    public static final String EXTRA_FEED_DESCRIPTION_DATA = "dataFeedDescription";

    public static final String EXTRA_USER_FIRST_NAME = "userFirstName";
    public static final String EXTRA_USER_LAST_NAME = "userLastName";
    public static final String EXTRA_USER_EMAIL = "userEmail";
    public static final String EXTRA_USER_BIO = "userBio";
    public static final String EXTRA_USER_CONTACT = "userContact";
    public static final String EXTRA_USER_WATER_MARK_STATUS = "userWaterMarkStatus";
    public static final String EXTRA_USER_IMAGE_PATH = "userImagePath";

    public static final String EXTRA_FOLLOW_REQUESTED_UUID = "followRequestedUUID";
    public static final String EXTRA_FOLLOW_TYPE = "followType";

    public static final String EXTRA_SHIPPING_ADDR1 = "addr1";
    public static final String EXTRA_SHIPPING_ADDR2 = "addr2";
    public static final String EXTRA_SHIPPING_CITY = "city";
    public static final String EXTRA_SHIPPING_STATE = "state";
    public static final String EXTRA_SHIPPING_PINCODE = "pincode";
    public static final String EXTRA_SHIPPING_NAME = "name";
    public static final String EXTRA_SHIPPING_PHONE = "phone";
    public static final String EXTRA_SHIPPING_ALT_PHONE = "altPhone";
    public static final String EXTRA_PRODUCT_TYPE = "type";
    public static final String EXTRA_PRODUCT_SIZE = "size";
    public static final String EXTRA_PRODUCT_COLOR = "color";
    public static final String EXTRA_PRODUCT_QUANTITY = "quantity";
    public static final String EXTRA_PRODUCT_PRICE = "price";
    public static final String EXTRA_PRODUCT_PRODUCTID = "productID";
    public static final String EXTRA_PRODUCT_ENTITYID = "entityID";
    public static final String EXTRA_PRODUCT_DELIVERY_TIME = "deliveryTime";
    public static final String EXTRA_PRODUCT_DELIVERY_CHARGE = "deliveryCharge";

    public static final String PAYMENT_STATUS_SUCCESS = "paymentSuccess";
    public static final String PAYMENT_STATUS_INVALID_TOKEN = "paymentInvalidToken";
    public static final String PAYMENT_STATUS_CONNECTION_TERMINATED = "paymentConnectionTerminated";
    public static final String PAYMENT_STATUS_SERVER_ERROR = "paymentServerError";

    //Action
    public static final String ACTION_LOG_OUT = "com.thetestament.cread.ACTION_LOGOUT";

    //Notifications Cread category
    public static final String NOTIFICATION_CATEGORY_CREAD_FOLLOW = "follow";
    public static final String NOTIFICATION_CATEGORY_CREAD_COLLABORATE = "collaborate";
    public static final String NOTIFICATION_CATEGORY_CREAD_HATSOFF = "hatsoff";
    public static final String NOTIFICATION_CATEGORY_CREAD_COMMENT = "comment";
    public static final String NOTIFICATION_CATEGORY_CREAD_BUY = "buy";
    public static final String NOTIFICATION_CATEGORY_CREAD_GENERAL = "general";

    //Notification id cread
    public static final int NOTIFICATION_ID_CREAD_FOLLOW = 1000;
    public static final int NOTIFICATION_ID_CREAD_COLLABORATE = 1001;
    public static final int NOTIFICATION_ID_CREAD_HATSOFF = 1002;
    public static final int NOTIFICATION_ID_CREAD_COMMENT = 1003;
    public static final int NOTIFICATION_ID_CREAD_BUY = 1004;
    public static final int NOTIFICATION_ID_CREAD_GENERAL = 1005;

    public static final String NOTIFICATION_CHANNEL_GENERAL = "generalNotificationChannel";


    // Bundle data keys for notification
    public static final String NOTIFICATION_BUNDLE_DATA_MESSAGE = "message";
    public static final String NOTIFICATION_BUNDLE_DATA_CATEGORY = "category";
    public static final String NOTIFICATION_BUNDLE_DATA_ACTOR_ID = "actorID";
    public static final String NOTIFICATION_BUNDLE_DATA_ENTITY_ID = "entityID";
    public static final String NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE = "actorImage";
    public static final String NOTIFICATION_BUNDLE_DATA_PERSISTABLE = "persistable";





}

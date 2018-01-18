package com.thetestament.cread.utils;


public class Constant {
    public static final String MINIMUM_APP_VERSION_KEY = "minimum_app_version";

    //Request codes
    public static final int REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE = 1000;
    public static final int REQUEST_CODE_FB_ACCOUNT_KIT = 1001;
    public static final int REQUEST_CODE_COMMENTS_ACTIVITY = 1002;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1003;
    public static final int REQUEST_CODE_UPDATE_PROFILE_PIC = 1004;
    public static final int REQUEST_CODE_UPDATE_PROFILE_DETAILS = 1005;
    public static final int REQUEST_CODE_OPEN_GALLERY = 1006;
    public static final int REQUEST_CODE_INSPIRATION_ACTIVITY = 1007;
    public static final int REQUEST_CODE_CASH_IN = 1008;
    public static final int REQUEST_CODE_ROYALTIES_ACTIVITY = 1009;
    public static final int REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY = 1010;
    public static final int REQUEST_CODE_PREVIEW_ACTIVITY = 1011;
    public static final int REQUEST_CODE_EDIT_CAPTURE = 1012;
    public static final int REQUEST_CODE_EDIT_SHORT = 1013;


    //Tag value for fragments
    public static final String TAG_FEED_FRAGMENT = "TagFeedFragment";
    public static final String TAG_EXPLORE_FRAGMENT = "TagExploreFragment";
    public static final String TAG_ME_FRAGMENT = "TagMeFragment";
    public static final String TAG_UPDATES_FRAGMENT = "TagUpdatesFragment";
    public static final String TAG_ROYALTIES_FRAGMENT = "TagRoyaltiesFragment";
    public static final String TAG_HASH_TAG_DETAILS_FRAGMENT = "HashTagDetailsFragment";
    public static final String TAG_SEARCH_PEOPLE_FRAGMENT = "SearchPeopleFragment";
    public static final String TAG_SEARCH_HASH_TAG_FRAGMENT = "SearchHashTagFragment";

    //Content Types
    public static final String CONTENT_TYPE_SHORT = "SHORT";
    public static final String CONTENT_TYPE_CAPTURE = "CAPTURE";

    public static final String SEARCH_TYPE_PEOPLE = "USER";
    public static final String SEARCH_TYPE_HASHTAG = "HASHTAG";
    public static final String SEARCH_TYPE_NO_RESULTS = "NORESULTS";
    public static final String SEARCH_TYPE_PROGRESS = "PROGRESS";

    //Image Types
    public static final String IMAGE_TYPE_USER_PROFILE_PIC = "userProfilePic";
    public static final String IMAGE_TYPE_USER_CAPTURE_PIC = "userCapturePic";
    public static final String IMAGE_TYPE_USER_SHORT_PIC = "userShortPic";
    public static final String IMAGE_TYPE_USER_SHARED_PIC = "sharedPic";

    //Photo watermark status
    public static final String WATERMARK_STATUS_YES = "statusYes";
    public static final String WATERMARK_STATUS_NO = "statusNo";
    public static final String WATERMARK_STATUS_ASK_ALWAYS = "StatusAskAlways";

    //Extra data
    public static final String EXTRA_ENTITY_ID = "entityID";
    public static final String EXTRA_ENTITY_TYPE = "entityType";
    public static final String EXTRA_CAPTURE_ID = "captureID";
    public static final String EXTRA_SHORT_ID = "shortID";
    public static final String EXTRA_SHORT_UUID = "shortUUID";
    public static final String EXTRA_CAPTURE_UUID = "captureUUID";
    public static final String EXTRA_CAPTURE_URL = "captureURL";
    public static final String EXTRA_MERCHANTABLE = "merchantable";
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

    public static final String EXTRA_WEB_VIEW_URL = "webViewUrl";
    public static final String EXTRA_WEB_VIEW_TITLE = "webViewTitle";

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
    public static final String EXTRA_PRODUCT_SHORTUUID = "shortID";
    public static final String EXTRA_PRODUCT_CAPTUREUUID = "captureID";
    public static final String EXTRA_PRODUCT_DELIVERY_TIME = "deliveryTime";
    public static final String EXTRA_PRODUCT_DELIVERY_CHARGE = "deliveryCharge";
    public static final String EXTRA_IS_PROFILE_EDITABLE = "isProfileEditable";
    public static final String EXTRA_MIN_CASH_AMT = "minCashInData";
    public static final String EXTRA_CASH_IN_AMOUNT = "cashInAmount";

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
    public static final String NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER = "other-comment";
    public static final String NOTIFICATION_CATEGORY_CREAD_TOP_POST = "top-post";

    //Notification id cread
    public static final int NOTIFICATION_ID_CREAD_FOLLOW = 1000;
    public static final int NOTIFICATION_ID_CREAD_COLLABORATE = 1001;
    public static final int NOTIFICATION_ID_CREAD_HATSOFF = 1002;
    public static final int NOTIFICATION_ID_CREAD_COMMENT = 1003;
    public static final int NOTIFICATION_ID_CREAD_BUY = 1004;
    public static final int NOTIFICATION_ID_CREAD_GENERAL = 1005;
    public static final int NOTIFICATION_ID_CREAD_COMMENT_OTHER = 1006;
    public static final int NOTIFICATION_ID_CREAD_TOP_POST = 1007;

    public static final String NOTIFICATION_CHANNEL_GENERAL = "generalNotificationChannel";


    // Bundle data keys for notification
    public static final String NOTIFICATION_BUNDLE_DATA_MESSAGE = "message";
    public static final String NOTIFICATION_BUNDLE_DATA_CATEGORY = "category";
    public static final String NOTIFICATION_BUNDLE_DATA_ACTOR_ID = "actorID";
    public static final String NOTIFICATION_BUNDLE_DATA_ENTITY_ID = "entityID";
    public static final String NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE = "actorImage";
    public static final String NOTIFICATION_BUNDLE_DATA_ENTITY_IMAGE = "entityImage";
    public static final String NOTIFICATION_BUNDLE_DATA_PERSISTABLE = "persistable";
    public static final String NOTIFICATION_BUNDLE_DATA_OTHER_COLLABORATOR = "otherCollaborator";


    // bundle data key for hashtag
    public static final String BUNDLE_HASHTAG_NAME = "hashTagName";


    //Firebase event types
    public static final String FIREBASE_EVENT_HAVE_CLICKED = "have_clicked";
    public static final String FIREBASE_EVENT_BUY_CLICKED = "buy_clicked";
    public static final String FIREBASE_EVENT_MAKE_PAYMENT_CLICKED = "make_payment_clicked";
    public static final String FIREBASE_EVENT_WRITE_CLICKED = "write_clicked";
    public static final String FIREBASE_EVENT_CAPTURE_CLICKED = "capture_clicked";
    public static final String FIREBASE_EVENT_FEED_CLICKED = "feed_clicked";
    public static final String FIREBASE_EVENT_EXPLORE_CLICKED = "explore_clicked";
    public static final String FIREBASE_EVENT_INSPIRATION_CLICKED = "inspire_clicked";
    public static final String FIREBASE_EVENT_NOTIFICATION_CLICKED = "notification_clicked";
    public static final String FIREBASE_EVENT_FIND_FRIENDS = "find_friends";
    public static final String FIREBASE_EVENT_FOLLOW_FROM_EXPLORE = "follow_from_explore";
    public static final String FIREBASE_EVENT_FOLLOW_FROM_FEED_DESCRIPTION = "follow_from_feed_description";
    public static final String FIREBASE_EVENT_FOLLOW_FROM_PROFILE = "follow_from_profile";
    public static final String FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION = "shared_from_feed_description";
    public static final String FIREBASE_EVENT_SHARED_FROM_MAIN_FEED = "shared_from_main_feed";
    public static final String FIREBASE_EVENT_SHARED_FROM_PROFILE = "shared_from_profile";
    public static final String FIREBASE_EVENT_RATE_US_CLICKED = "rate_us_clicked";


    public enum GratitudeNumbers {
        POSTS, FOLLOWERS, FOLLOWING,
        HATSOFF, COMMENT, COLLABORATIONS
    }

    public enum ITEM_TYPES {
        LIST, GRID, COLLABLIST;

        public static ITEM_TYPES toItemType(String myEnumString) {
            try {
                return valueOf(myEnumString);
            } catch (Exception ex) {
                // For error cases
                return GRID;
            }
        }
    }

    public static final String URI_HASH_TAG_ACTIVITY = "content://com.thetestament.cread.hashtagdetailsactivity/";


    //Data for preview Screen
    public static final String PREVIEW_EXTRA_DATA = "extraData";
    public static final String PREVIEW_EXTRA_CALLED_FROM = "calledFrom";
    public static final String PREVIEW_EXTRA_UUID = "uuid";
    public static final String PREVIEW_EXTRA_AUTH_KEY = "authKey";
    public static final String PREVIEW_EXTRA_ENTITY_ID = "entityID";
    public static final String PREVIEW_EXTRA_SHORT_ID = "shortID";
    public static final String PREVIEW_EXTRA_CAPTURE_ID = "captureID";
    public static final String PREVIEW_EXTRA_X_POSITION = "xPosition";
    public static final String PREVIEW_EXTRA_Y_POSITION = "yPosition";
    public static final String PREVIEW_EXTRA_TV_WIDTH = "tvWidth";
    public static final String PREVIEW_EXTRA_TV_HEIGHT = "tvHeight";
    public static final String PREVIEW_EXTRA_TEXT = "text";
    public static final String PREVIEW_EXTRA_TEXT_SIZE = "textSize";
    public static final String PREVIEW_EXTRA_TEXT_COLOR = "textColor";
    public static final String PREVIEW_EXTRA_TEXT_GRAVITY = "textGravity";
    public static final String PREVIEW_EXTRA_IMG_WIDTH = "imgWidth";
    public static final String PREVIEW_EXTRA_SIGNATURE = "signature";
    public static final String PREVIEW_EXTRA_MERCHANTABLE = "merchantable";
    public static final String PREVIEW_EXTRA_BG_COLOR = "bgColor";
    public static final String PREVIEW_EXTRA_FONT = "font";
    public static final String PREVIEW_EXTRA_BOLD = "bold";
    public static final String PREVIEW_EXTRA_ITALIC = "italic";
    public static final String PREVIEW_EXTRA_IMAGE_TINT_COLOR = "imageTintColor";
    public static final String PREVIEW_EXTRA_CAPTION_TEXT = "captionText";
    public static final String PREVIEW_EXTRA_CONTENT_IMAGE = "contentImage";
    public static final String PREVIEW_EXTRA_CALLED_FROM_COLLABORATION = "collaboration";
    public static final String PREVIEW_EXTRA_CALLED_FROM_SHORT = "short";
    public static final String PREVIEW_EXTRA_CALLED_FROM_CAPTURE = "capture";
    public static final String PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE = "editCapture";
    public static final String PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT = "editShort";
}

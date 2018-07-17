package com.thetestament.cread.utils;


public class Constant {
    public static final String MINIMUM_APP_VERSION_KEY = "minimum_app_version";
    public static final String EXPLORE_CATEGORY_VIEW_VISIBILITY = "explore_category_view_visibility";
    public static final String EXPLORE_CATEGORY_VIEW_VISIBILITY_VISIBLE = "visible";
    public static final String EXPLORE_CATEGORY_VIEW_VISIBILITY_INVISIBLE = "invisible";

    //region :Platform key and value constant
    public static final String PLATFORM_KEY = "platform";
    public static final String PLATFORM_VALUE = "android";
    //endregion

    // region :Login types
    public static final String LOGIN_TYPE_FACEBOOK = "facebook";
    public static final String LOGIN_TYPE_GOOGLE = "google";
    //endregion

    //region :Inspiration item view types
    public static final String INSPIRATION_ITEM_TYPE_SMALL = "itemSmall";
    public static final String INSPIRATION_ITEM_TYPE_DETAIL = "itemDetail";
    //endregion

    //region :Request codes
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
    public static final int REQUEST_CODE_EDIT_POST = 1012;
    public static final int REQUEST_CODE_COMMENT_MENTION = 1013;
    public static final int REQUEST_CODE_CHAT_DETAILS = 1014;
    public static final int REQUEST_CODE_CHAT_DETAILS_FROM_CHAT_REQUEST = 1015;
    public static final int REQUEST_CODE_CHAT_REQUEST = 1016;
    public static final int REQUEST_CODE_CHAT_DETAILS_FROM_USER_PROFILE = 1016;
    public static final int REQUEST_CODE_COLLABORATION_INVITATION = 1017;
    public static final int REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED = 1018;
    public static final int REQUEST_CODE_USER_PROFILE_FROM_FEED = 1019;
    public static final int REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED_ADAPTER = 1020;
    public static final int REQUEST_CODE_USER_PROFILE_FROM_SUGGESTED_ADAPTER = 1021;
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1022;
    public static final int REQUEST_CODE_USER_INTERESTS = 1023;
    //endregion

    //region :Tag value for fragments
    public static final String TAG_FEED_FRAGMENT = "TagFeedFragment";
    public static final String TAG_EXPLORE_FRAGMENT = "TagExploreFragment";
    public static final String TAG_ME_FRAGMENT = "TagMeFragment";
    public static final String TAG_HELP_FRAGMENT = "TagHelpFragment";
    public static final String TAG_UPDATES_FRAGMENT = "TagUpdatesFragment";
    public static final String TAG_ROYALTIES_FRAGMENT = "TagRoyaltiesFragment";
    public static final String TAG_HASH_TAG_DETAILS_FRAGMENT = "HashTagDetailsFragment";
    public static final String TAG_SEARCH_PEOPLE_FRAGMENT = "SearchPeopleFragment";
    public static final String TAG_VIEW_LONG_SHORT_FRAGMENT = "ViewLongShortFragment";
    //endregion

    //region :Content Types
    public static final String CONTENT_TYPE_SHORT = "SHORT";
    public static final String CONTENT_TYPE_CAPTURE = "CAPTURE";
    //endregion

    //region :Text gravity types
    public static final String TEXT_GRAVITY_TYPE_CENTER = "Center";
    public static final String TEXT_GRAVITY_TYPE_LEFT = "Left";
    public static final String TEXT_GRAVITY_TYPE_RIGHT = "Right";
    //endregion

    //region :Search item view type
    public static final String SEARCH_TYPE_PEOPLE = "USER";
    public static final String SEARCH_TYPE_HASHTAG = "HASHTAG";
    public static final String SEARCH_TYPE_NO_RESULTS = "NORESULTS";
    public static final String SEARCH_TYPE_PROGRESS = "PROGRESS";
    //endregion

    //region :Image Types
    public static final String IMAGE_TYPE_USER_PROFILE_PIC = "userProfilePic";
    public static final String IMAGE_TYPE_USER_CAPTURE_PIC = "userCapturePic";
    public static final String IMAGE_TYPE_USER_SHORT_PIC = "userShortPic";
    public static final String IMAGE_TYPE_USER_SHARED_PIC = "sharedPic";
    //endregion

    //region Photo watermark status
    public static final String WATERMARK_STATUS_YES = "statusYes";
    public static final String WATERMARK_STATUS_NO = "statusNo";
    public static final String WATERMARK_STATUS_ASK_ALWAYS = "StatusAskAlways";
    //endregion

    //Extra data
    public static final String EXTRA_ENTITY_ID = "entityID";
    public static final String EXTRA_ENTITY_TYPE = "entityType";
    public static final String EXTRA_CAPTURE_ID = "captureID";
    public static final String EXTRA_SHORT_ID = "shortID";
    public static final String EXTRA_SHORT_UUID = "shortUUID";
    public static final String EXTRA_CAPTURE_UUID = "captureUUID";
    public static final String EXTRA_CAPTURE_URL = "captureURL";
    public static final String EXTRA_MERCHANTABLE = "merchantable";
    public static final String EXTRA_IMAGE_WIDTH = "imageWidth";
    public static final String EXTRA_IMAGE_HEIGHT = "imageHeight";
    public static final String EXTRA_LIVE_FILTER = "liveFilter";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_PROFILE_UUID = "profileUUID";
    public static final String EXTRA_FEED_DESCRIPTION_DATA = "dataFeedDescription";

    public static final String EXTRA_USER_FIRST_NAME = "userFirstName";
    public static final String EXTRA_USER_LAST_NAME = "userLastName";
    public static final String EXTRA_USER_EMAIL = "userEmail";
    public static final String EXTRA_USER_BIO = "userBio";
    public static final String EXTRA_USER_CONTACT = "userContact";
    public static final String EXTRA_USER_WEB_STORE_LINK = "userWebStoreLink";
    public static final String EXTRA_USER_WATER_MARK_STATUS = "userWaterMarkStatus";
    public static final String EXTRA_USER_IMAGE_PATH = "userImagePath";
    public static final String EXTRA_TOP_USER_INTERESTS = "topUserInterests";
    public static final String EXTRA_USER_INTERESTS_COUNT = "userInterestsCount";

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

    public static final String PAYMENT_MODE_PAY_PORTAL = "PAY_PORTAL";
    public static final String PAYMENT_MODE_COD = "CASH_ON_DELIVERY";

    public static final String EXTRA_FROM_UPDATES_COMMENT_MENTION = "fromUpdatesCommentMention";

    public static final String PAYMENT_STATUS_SUCCESS = "paymentSuccess";
    public static final String PAYMENT_STATUS_INVALID_TOKEN = "paymentInvalidToken";
    public static final String PAYMENT_STATUS_CONNECTION_TERMINATED = "paymentConnectionTerminated";
    public static final String PAYMENT_STATUS_SERVER_ERROR = "paymentServerError";

    public static final String EXTRA_OPEN_SPECIFIC_BOTTOMNAV_FRAGMENT = "openSpecificBottomNavFragment";

    public static final String EXTRA_SHORT_DATA = "extraShortData";

    public static final String EXTRA_USER_INTERESTS_CALLED_FROM = "userInterestsCalledFrom";
    public static final String EXTRA_PROFILE_PIC_URL = "userProfilePicUrl";
    public static final String EXTRA_USER_INTERESTS_DATA = "userInterestsData";


    //Action
    public static final String ACTION_LOG_OUT = "com.thetestament.cread.ACTION_LOGOUT";

    //region :Notifications category
    public static final String NOTIFICATION_CATEGORY_CREAD_FOLLOW = "follow";
    public static final String NOTIFICATION_CATEGORY_CREAD_COLLABORATE = "collaborate";
    public static final String NOTIFICATION_CATEGORY_CREAD_HATSOFF = "hatsoff";
    public static final String NOTIFICATION_CATEGORY_CREAD_COMMENT = "comment";
    public static final String NOTIFICATION_CATEGORY_CREAD_BUY = "buy";
    public static final String NOTIFICATION_CATEGORY_CREAD_GENERAL = "general";
    public static final String NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER = "other-comment";
    public static final String NOTIFICATION_CATEGORY_CREAD_TOP_POST = "top-post";
    public static final String NOTIFICATION_CATEGORY_PROFILE_MENTION_POST = "profile-mention-post";
    public static final String NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT = "profile-mention-comment";
    public static final String NOTIFICATION_CATEGORY_ENGAGEMENT_NOTIFICATIONS = "engagement-notification";
    public static final String NOTIFICATION_CATEGORY_CREAD_FB_FRIEND = "fb-friend-new";
    public static final String NOTIFICATION_CATEGORY_PERSONAL_CHAT_MESSAGE = "personal-chat";
    public static final String NOTIFICATION_CATEGORY_PERSONAL_CHAT_REQUEST = "personal-chat-request";
    public static final String NOTIFICATION_CATEGORY_FEATURED_ARTIST = "featured-artist";
    public static final String NOTIFICATION_CATEGORY_FEATURED_ARTIST_FOLLOWER = "featured-artist-follower";
    public static final String NOTIFICATION_CATEGORY_REFERRAL_SUCCESS = "join-referral";
    public static final String NOTIFICATION_CATEGORY_POST_AFTER_GAP = "post-after-gap";
    public static final String NOTIFICATION_CATEGORY_FIRST_POST_USERS = "first-post-users";
    //endregion

    //Notification id cread
    public static final int NOTIFICATION_ID_CREAD_FOLLOW = 1000;
    public static final int NOTIFICATION_ID_CREAD_COLLABORATE = 1001;
    public static final int NOTIFICATION_ID_CREAD_HATSOFF = 1002;
    public static final int NOTIFICATION_ID_CREAD_COMMENT = 1003;
    public static final int NOTIFICATION_ID_CREAD_BUY = 1004;
    public static final int NOTIFICATION_ID_CREAD_GENERAL = 1005;
    public static final int NOTIFICATION_ID_CREAD_COMMENT_OTHER = 1006;
    public static final int NOTIFICATION_ID_CREAD_TOP_POST = 1007;
    public static final int NOTIFICATION_ID_PROFILE_MENTION_POST = 1008;
    public static final int NOTIFICATION_ID_PROFILE_MENTION_COMMENT = 1009;
    public static final int NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE = 1010;
    public static final int NOTIFICATION_ID_ENGAGEMENT_NOTIFICATIONS = 1011;
    public static final int NOTIFICATION_ID_CREAD_FB_FRIEND = 1012;
    public static final int NOTIFICATION_ID_FEATURED_ARTIST = 1013;
    public static final int NOTIFICATION_ID_FEATURED_ARTIST_FOLLOWER = 1014;
    public static final int NOTIFICATION_ID_REFERRAL_SUCCESS = 1015;
    public static final int NOTIFICATION_ID_POST_AFTER_GAP = 1016;
    public static final int NOTIFICATION_ID_FIRST_POST_USERS = 1017;


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
    public static final String NOTIFICATION_EXTRA_ENTITY_ID_LIST = "entityIDList";


    // bundle data key for hashtag
    public static final String BUNDLE_HASHTAG_NAME = "hashTagName";


    //region :Firebase event types
    public static final String FIREBASE_EVENT_HAVE_CLICKED = "have_clicked";
    public static final String FIREBASE_EVENT_BUY_CLICKED = "buy_clicked";
    public static final String FIREBASE_EVENT_MAKE_PAYMENT_CLICKED = "make_payment_clicked";
    public static final String FIREBASE_EVENT_WRITE_CLICKED = "write_clicked";
    public static final String FIREBASE_EVENT_CAPTURE_CLICKED = "capture_clicked";
    public static final String FIREBASE_EVENT_FEED_CLICKED = "feed_clicked";
    public static final String FIREBASE_EVENT_EXPLORE_CLICKED = "explore_clicked";
    public static final String FIREBASE_EVENT_HELP_CLICKED = "help_question_clicked";
    public static final String FIREBASE_EVENT_EXPLORE_CATEGORY_CLICKED = "explore_category_clicked";
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
    public static final String FIREBASE_EVENT_COLLAB_INVITE_CLICKED = "collab_invite_clicked";
    public static final String FIREBASE_EVENT_DEEP_LINK_USED = "deep_link_open";
    public static final String FIREBASE_EVENT_POST_SAVED = "post_saved";
    public static final String FIREBASE_EVENT_POST_UPLOADED = "post_uploaded";
    public static final String FIREBASE_EVENT_EXPLORE_TAB_SELECTED = "explore_selected_tab";
    //endregion


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


    //region :Data for preview Screen
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
    public static final String PREVIEW_EXTRA_IMG_HEIGHT = "imgHeight";
    public static final String PREVIEW_EXTRA_SIGNATURE = "signature";
    public static final String PREVIEW_EXTRA_MERCHANTABLE = "merchantable";
    public static final String PREVIEW_EXTRA_BG_COLOR = "bgColor";
    public static final String PREVIEW_EXTRA_FONT = "font";
    public static final String PREVIEW_EXTRA_TEMPLATE_NAME = "templateName";
    public static final String PREVIEW_EXTRA_IS_SHADOW_SELECTED = "shadowSelected";
    public static final String PREVIEW_EXTRA_BG_SOUND = "bgSound";
    public static final String PREVIEW_EXTRA_LONG_TEXT = "longText";
    public static final String PREVIEW_EXTRA_IMAGE_URL = "imageURL";
    public static final String PREVIEW_EXTRA_BOLD = "bold";
    public static final String PREVIEW_EXTRA_ITALIC = "italic";
    public static final String PREVIEW_EXTRA_IMAGE_TINT_COLOR = "imageTintColor";
    public static final String PREVIEW_EXTRA_CAPTION_TEXT = "captionText";
    public static final String PREVIEW_EXTRA_CONTENT_IMAGE = "contentImage";
    public static final String PREVIEW_EXTRA_LIVE_FILTER = "liveFilter";
    public static final String PREVIEW_EXTRA_CALLED_FROM_COLLABORATION = "collaboration";
    public static final String PREVIEW_EXTRA_CALLED_FROM_SHORT = "short";
    public static final String PREVIEW_EXTRA_CALLED_FROM_CAPTURE = "capture";
    public static final String PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE = "editCapture";
    public static final String PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT = "editShort";

    //endregion

    //region :Data for  Short activity
    public static final String SHORT_EXTRA_CALLED_FROM = "shortCalledFrom";
    public static final String SHORT_EXTRA_CALLED_FROM_EDIT_SHORT = "calledFromEditShort";
    public static final String SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT = "calledFromCollaboration";
    public static final String SHORT_EXTRA_CAPTION_TEXT = "shortCaptionText";
    //endregion


    public static final String USER_ACTION_TYPE_VIEW = "ENTITY_VIEW";

    //region :Data for chat details screen
    public static final String EXTRA_CHAT_UUID = "chatUUID";
    public static final String EXTRA_CHAT_USER_NAME = "chatUserName";
    public static final String EXTRA_CHAT_ITEM_POSITION = "chatItemPosition";
    public static final String EXTRA_CHAT_DETAILS_DATA = "chatDetailsData";
    public static final String EXTRA_CHAT_LAST_MESSAGE = "chatLastMessage";
    public static final String EXTRA_CHAT_FOLLOW_STATUS = "chatFollowStatus";
    public static final String EXTRA_CHAT_ID = "chatID";
    public static final String EXTRA_CHAT_LIST_CALLED_FROM = "chatListFrom";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM = "chatCalledFrom";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_LIST = "calledFromChatList";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST = "calledFromChatRequest";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE = "calledFromChatProfile";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_WITH_US = "calledFromChatWithUs";
    public static final String EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION = "calledFromChatNotification";

    //endregion

    public static final String SHARE_SOURCE_FROM_SHARE = "fromShare";
    public static final String SHARE_SOURCE_FROM_CREATE = "fromCreate";

    //region : Share options
    public static final String SHARE_OPTION_WHATSAPP = "shareOptionWhatsapp";
    public static final String SHARE_OPTION_FACEBOOK = "shareOptionFacebook";
    public static final String SHARE_OPTION_INSTAGRAM = "shareOptionInstagram";
    public static final String SHARE_OPTION_OTHER = "shareOptionOther";

    //region :Aspect ration
    public static final String ASPECT_RATIO_ONE_TO_ONE = "1.00";
    public static final String ASPECT_RATIO_FOUR_TO_FIVE = "0.80";
    public static final String ASPECT_RATIO_FIVE_TO_FOUR = "1.25";
    public static final String ASPECT_RATIO_FOUR_TO_THREE = "1.33";
    public static final String ASPECT_RATIO_THREE_TO_FOUR = "0.75";

    public static final float ASPECT_RATIO_ONE_TO_ONE_VALUE = 1.00f;
    public static final float ASPECT_RATIO_FOUR_TO_FIVE_VALUE = 0.80f;
    public static final float ASPECT_RATIO_FIVE_TO_FOUR_VALUE = 1.25f;
    public static final float ASPECT_RATIO_FOUR_TO_THREE_VALUE = 1.33f;
    public static final float ASPECT_RATIO_THREE_TO_FOUR_VALUE = 0.75f;

    //endregion

    //Region :ContentPreview extra data
    public static final String CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH = "contentPreviewImageWidth";
    public static final String CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT = "contentPreviewImageHeight";
    public static final String CONTENT_PREVIEW_EXTRA_IMAGE_URL = "contentPreviewImageUrl";
    public static final String CONTENT_PREVIEW_EXTRA_DATA = "contentPreviewData";
    public static final String CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT = "contentPreviewSignatureText";
    public static final String CONTENT_PREVIEW_EXTRA_LIVE_FILTER_NAME = "contentPreviewLiveFilterName";
    //endregion

    //region Content type for labels
    public static final String LABEL_TYPE_WRITING = "WRITING";
    public static final String LABEL_TYPE_GRAPHIC = "GRAPHIC";
    public static final String LABEL_TYPE_ALL = "ALL";

    public static final String USER_INTERESTS_CALLED_FROM_LOGIN = "userInterestsCalledFromLogin";
    public static final String USER_INTERESTS_CALLED_FROM_PROFILE = "userInterestsCalledFromProfile";
    //endregion

    //region
    public static final String EXPLORE_CATEGORY_DEFAULT = "DEFAULT";
    public static final String EXPLORE_CATEGORY_ART = "ART";
    public static final String EXPLORE_CATEGORY_FEEL = "FEEL";
    //endregion

    //region :Live filter name
    public static final String LIVE_FILTER_NONE = "none";
    public static final String LIVE_FILTER_SNOW = "snow";
    public static final String LIVE_FILTER_RAIN = "rain";
    public static final String LIVE_FILTER_BUBBLE = "bubble";
    public static final String LIVE_FILTER_CONFETTI = "confetti";
    //endregion

    //region  :Apps package name
    public static final String PACKAGE_NAME_WHATSAPP = "com.whatsapp";
    public static final String PACKAGE_NAME_FACEBOOK = "com.facebook.katana";
    public static final String PACKAGE_NAME_INSTAGRAM = "com.instagram.android";
    //endregion


    //region : Tabs
    public static final String EXPLORE_SELECTED_TAB_RECENT = "recent";
    public static final String EXPLORE_SELECTED_TAB_POPULAR = "popular";
    public static final String EXPLORE_SELECTED_TAB_ALL_STAR = "allstar";
    //endregion
}

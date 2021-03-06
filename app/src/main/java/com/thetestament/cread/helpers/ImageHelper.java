package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationActivity;
import com.thetestament.cread.utils.Constant;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;

import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHARED_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHORT_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;

/**
 * A helper class for providing utility method for image related operations.
 */

public class ImageHelper {

    /**
     * Return the Uri of picture stored in phone storage.
     *
     * @param imageType Type of image i.e IMAGE_TYPE_USER_PROFILE_PIC , IMAGE_TYPE_USER_CAPTURE_PIC and IMAGE_TYPE_USER_SHORT_PIC
     * @return uri of Picture.
     **/
    public static Uri getImageUri(String imageType) {
        String s;
        if (imageType.equals(IMAGE_TYPE_USER_PROFILE_PIC)) {
            s = "/Cread/Profile/user_profile_pic.jpg";
        } else if (imageType.equals(IMAGE_TYPE_USER_CAPTURE_PIC)) {
            s = "/Cread/Capture/capture_pic.jpg";
        } else if (imageType.equals(IMAGE_TYPE_USER_SHORT_PIC)) {
            s = "/Cread/Short/short_pic.jpg";
        } else if (imageType.equals(Constant.IMAGE_TYPE_USER_SHARE_BADGE)) {
            s = "/Cread/Share/badge_pic.png";
        } else if (imageType.equals(Constant.IMAGE_TYPE_USER_MEME)) {
            s = "/Cread/Meme/meme_pic.jpg";
        } else if (imageType.equals(Constant.IMAGE_TYPE_USER_MEME_ONE)) {
            s = "/Cread/Meme/meme_pic_one.jpg";
        } else if (imageType.equals(Constant.IMAGE_TYPE_USER_MEME_TWO)) {
            s = "/Cread/Meme/meme_pic_two.jpg";
        } else {
            s = "/Cread/Share/share_pic.png";
        }

        File outFile = new File(Environment.getExternalStorageDirectory().getPath() + s);
        outFile.getParentFile().mkdirs();

        if (!outFile.exists()) {
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(outFile);
    }


    /**
     * This Method is used to compress the cropped image stored in cache and store it to phone storage.
     *
     * @param context   Context where to be use.
     * @param imageType Type of image i.e IMAGE_TYPE_USER_PROFILE_PIC or
     * @param sourceUri Uri of the cached image.
     */
    public static Uri compressCroppedImg(Uri sourceUri, Context context, String imageType) throws IOException {
        File sourceFile = new File(sourceUri.getPath());
        //For more information please visit "https://github.com/zetbaitsu/Compressor"
        //To compress the profile pic
        File compressedFile = new Compressor(context).compressToFile(sourceFile);

        File destFile = new File(ImageHelper.getImageUri(imageType).getPath());

        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(compressedFile));
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(destFile));

        int numBytes;

        byte[] buffer = new byte[2048];

        while ((numBytes = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, numBytes);
        }
        fis.close();
        fos.close();

        return Uri.fromFile(destFile);
    }

    /**
     * This Method is used to compress the cropped image stored in cache and store it to phone storage.
     *
     * @param context   Context where to be use.
     * @param imageType Type of image i.e IMAGE_TYPE_USER_PROFILE_PIC or
     * @param sourceUri Uri of the cached image.
     */
    public static Uri compressSpecific(Uri sourceUri, Context context, String imageType) throws IOException {
        File sourceFile = new File(sourceUri.getPath());
        //For more information please visit "https://github.com/zetbaitsu/Compressor"
        //To compress the profile pic
        //  File compressedFile = new Compressor(context).compressToFile(sourceFile);
        File compressedFile = new Compressor(context)
                .setMaxWidth(5000)
                .setMaxHeight(5000)
                .setQuality(40)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .compressToFile(sourceFile);

        File destFile = new File(ImageHelper.getImageUri(imageType).getPath());

        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(compressedFile));
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(destFile));

        int numBytes;

        byte[] buffer = new byte[2048];

        while ((numBytes = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, numBytes);
        }
        fis.close();
        fos.close();

        return Uri.fromFile(destFile);
    }


    /**
     * Method to open image cropper screen with all aspect ration from activities.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     */
    public static void startImageCropping(FragmentActivity context, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"

        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        //Set gestures
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE);
        //Set max resolution size
        options.withMaxResultSize(5000, 5000);
        //Set aspect ratio
        options.setAspectRatioOptions(0,
                new AspectRatio("1:1", 1, 1),
                new AspectRatio("4:5", 4, 5),
                new AspectRatio("5:4", 5, 4),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("3:4", 3, 4)
        );


        //Launch image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(context);
    }


    /**
     * Method to open image cropper screen from fragment.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     * @param fragment       Fragment reference.
     */
    public static void startImageCropping(Context context, Fragment fragment, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"
        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        //Set gestures
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE);
        //Set max resolution size
        options.withMaxResultSize(5000, 5000);
        //Set aspect ratio
        options.setAspectRatioOptions(0,
                new AspectRatio("1:1", 1, 1),
                new AspectRatio("4:5", 4, 5),
                new AspectRatio("5:4", 5, 4),
                new AspectRatio("4:3", 4, 3),
                new AspectRatio("3:4", 3, 4)
        );
        //Launch image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(context, fragment, UCrop.REQUEST_CROP);
    }


    /**
     * Method to open image cropper screen with 1:1 aspect ration.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     */
    public static void startImageCroppingWithSquare(FragmentActivity context, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"

        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(context);
    }


    /**
     * Method to open image cropper screen with 1:1 aspect ration.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     */
    public static void startImageCroppingWithSquare(Context context, Fragment fragment, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"

        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(context, fragment);
    }

    /**
     * Method to open image cropper screen with 9:18 aspect ration.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     */
    public static void startImageCroppingWith918(Context context, Fragment fragment, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"

        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(9, 18)
                .withOptions(options)
                .start(context, fragment);
    }

    /**
     * Method to open image cropper screen with 4:3 aspect ration.
     *
     * @param sourceUri      Uri of image to be cropped.
     * @param destinationUri Where image will be saved.
     * @param context        Context of use usually activity or application.
     */
    public static void startImageCroppingWith43(Context context, Fragment fragment, Uri sourceUri, Uri destinationUri) {
        //For more information please visit "https://github.com/Yalantis/uCrop"

        UCrop.Options options = new UCrop.Options();
        //Change toolbar color
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        //Change status bar color
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(4, 3)
                .withOptions(options)
                .start(context, fragment);
    }


    /**
     * Method to convert bitmap into Uri.
     *
     * @param bmp     Bitmap to be converted.
     * @param context Context to use.
     * @return Uri of image.
     */
    public static Uri getLocalBitmapUri(Bitmap bmp, Context context) {
        //Create bitmap
        Bitmap src = Bitmap.createBitmap(bmp);
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        //Create canvas
        Canvas canvas = new Canvas(dest);
        //Draw image bitmap on canvas
        canvas.drawBitmap(src, 1f, 1f, null);

        //Create bitmap from watermark drawable
        Bitmap bitmapSrc = BitmapFactory.decodeResource(context.getResources(), R.drawable.cread_logo_watermark_share);

        //Scale factor for width and height
        int watermarkScaledWidth = src.getWidth() / 6;
        int watermarkScaledHeight = bitmapSrc.getHeight() / (bitmapSrc.getWidth() / watermarkScaledWidth);

        //Create scaled bitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapSrc, watermarkScaledWidth, watermarkScaledHeight, false);
        //Create watermark drawable from bitmap
        Drawable waterMarkDrawable = new BitmapDrawable(context.getResources(), scaledBitmap);
        //Set watermark image bounds
        waterMarkDrawable.setBounds(
                src.getWidth() - waterMarkDrawable.getMinimumWidth()
                , src.getHeight() - waterMarkDrawable.getMinimumHeight()
                , src.getWidth()
                , src.getHeight()
        );
        //Draw watermark canvas
        waterMarkDrawable.draw(canvas);

        //Uri to be returned
        Uri bmpUri = null;
        try {
            // File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            File file = new File(ImageHelper.getImageUri(IMAGE_TYPE_USER_SHARED_PIC).getPath());
            FileOutputStream out = new FileOutputStream(file);
            dest.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    /**
     * Open gallery so user can choose his/her capture for uploading.
     *
     * @param fragment Fragment reference.
     */
    public static void chooseImageFromGallery(Fragment fragment) {
        //Launch gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE);
    }

    /**
     * Open gallery so user can choose his/her image for meme.
     *
     * @param fragment Fragment reference.
     */
    public static void chooseImageFromGallery(Fragment fragment, int requestCode) {
        //Launch gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Method to perform square image manipulation.
     *
     * @param croppedImageUri Uri of cropped image
     * @param context         Context of use.
     * @param rootView        Layout parent view reference.
     * @param entityID        entity id of content.
     * @param entityType      Type oif entity.
     */
    public static void performSquareImageManipulation(Uri croppedImageUri, Context context, View rootView, String entityID, String entityType) {
        try {
            //Compress image
            compressSpecific(croppedImageUri, context, IMAGE_TYPE_USER_CAPTURE_PIC);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "ImageHelper");
        }
        //Decode image file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        int scaleFactor;
        boolean isLandscape;

        //Image is not available in square from
        if (imageWidth != imageHeight) {
            if (imageWidth > imageHeight) {
                scaleFactor = imageWidth;
                isLandscape = true;
            } else {
                scaleFactor = imageHeight;
                isLandscape = false;
            }

            //Decode bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(new File(croppedImageUri.getPath()).getAbsolutePath());

            //Method call
            CaptureHelper.createSquareBlurBitmap(context
                    , bitmap
                    , scaleFactor
                    , isLandscape);

            //Method call
            processCroppedImage(croppedImageUri, context, rootView, entityID, entityType);
        }
        //Image is available in square form
        else {
            //Method called
            processCroppedImage(croppedImageUri, context, rootView, entityID, entityType);
        }

    }


    /**
     * Method to perform required operation on cropped image.
     *
     * @param uri        Uri of cropped image.
     * @param context    Context of use.
     * @param rootView   Layout parent view reference.
     * @param entityID   entity id of content.
     * @param entityType Type oif entity.
     */
    public static void processCroppedImage(Uri uri, Context context, View rootView, String entityID, String entityType) {
        try {
            //Decode image file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            //If resolution of image is greater than 3000x3000 then compress this image

            if (Math.min(imageHeight, imageWidth) >= 3000) {
                //Compress image
                compressCroppedImg(uri, context, IMAGE_TYPE_USER_CAPTURE_PIC);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_ENTITY_ID, entityID);
                bundle.putString(EXTRA_ENTITY_TYPE, entityType);
                bundle.putString(EXTRA_MERCHANTABLE, "1");
                //Open preview screen
                Intent intent = new Intent(context, CollaborationActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                context.startActivity(intent);
            } else if (Math.min(imageHeight, imageWidth) >= 1800) {
                //Open preview screen
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_ENTITY_ID, entityID);
                bundle.putString(EXTRA_ENTITY_TYPE, entityType);
                bundle.putString(EXTRA_MERCHANTABLE, "1");
                //Open preview screen
                Intent intent = new Intent(context, CollaborationActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                context.startActivity(intent);
            } else {
                getMerchantableDialog(context, entityID, entityType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, context.getString(R.string.error_img_not_cropped));
        }
    }

    /**
     * Method to show dialog when user uploads low resolution image.
     *
     * @param context  Context of use.
     * @param entityID entity id of content.
     */
    private static void getMerchantableDialog(final Context context, final String entityID, final String entityType) {
        new MaterialDialog.Builder(context)
                .title("Note")
                .content("The resolution of this image is not printable. Other users won't be able to order a print version of it. Do you wish to proceed?")
                .positiveText("Proceed")
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_ENTITY_ID, entityID);
                        bundle.putString(EXTRA_ENTITY_TYPE, entityType);
                        bundle.putString(EXTRA_MERCHANTABLE, "0");
                        //open preview screen
                        Intent intent = new Intent(context, CollaborationActivity.class);
                        intent.putExtra(EXTRA_DATA, bundle);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .show();
    }


    /**
     * Method to load image from url to imageView.
     *
     * @param context     Context to use.
     * @param imageView   View where image to be loaded.
     * @param url         Url of image to be loaded.
     * @param placeholder Resource ID of error placeholder drawable.
     */
    public static void loadImageFromPicasso(Context context, ImageView imageView, String url, int placeholder) {
        Picasso.with(context)
                .load(url)
                .error(placeholder)
                .into(imageView);
    }

    public static String getAWSS3ProfilePicUrl(String uuid) {
        return "https://s3-ap-northeast-1.amazonaws.com/" + BuildConfig.S3BUCKET + "/Users/" + uuid + "/Profile/display-pic-small.jpg";
    }


    /**
     * Method to load image progressively.
     *
     * @param uri              Uri of image to be loaded.
     * @param simpleDraweeView View where image to be loaded.
     */
    public static void loadProgressiveImage(Uri uri, SimpleDraweeView simpleDraweeView) {

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(simpleDraweeView.getController())
                .setTapToRetryEnabled(true)
                .build();
        simpleDraweeView.setController(controller);
    }

    /**
     * @param filepath  Path of the file to be checked whether it exists or not.
     * @param context   Context to use.
     * @param imagePath Uri of the image to be loaded.
     * @param imageView ImageView where image to be loaded.
     */
    public static void loadImageIfExist(String filepath, Context context, Uri imagePath, ImageView imageView) {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filepath);

        //If image exits
        if (!file.exists()) {
            //do nothing
        } else {
            //Load image
            Picasso.with(context)
                    .load(imagePath)
                    .error(R.drawable.image_placeholder)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }
    }

    /**
     * Method to load preview image without cache.
     *
     * @param context  Context to use.
     * @param imageUri Uri of image to be loaded.
     * @param image    ImageView where image to be loaded.
     */
    public static void loadImage(Context context, Uri imageUri, ImageView image) {
        Picasso.with(context)
                .load(imageUri)
                .error(R.drawable.image_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(image);
    }
}

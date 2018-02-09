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
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationActivity;
import com.yalantis.ucrop.UCrop;

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
                .setQuality(100)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
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
     * Method to open image cropper screen.
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
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionQuality(100);

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                /*.useSourceImageAspectRatio()*/
                .start(context);
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
     * Method to open image cropper screen.
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
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionQuality(100);


        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
               /* .useSourceImageAspectRatio()*/
                .start(context, fragment, UCrop.REQUEST_CROP);
    }

    /**
     * Method to perform required operation on cropped image.
     *
     * @param uri      Uri of cropped image.
     * @param context  Context of use.
     * @param rootView Layout parent view reference.
     * @param entityID entity id of content.
     */
    public static void processCroppedImage(Uri uri, Context context, View rootView, String entityID, String entityType) {
        try {
            //Decode image file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            //If resolution of image is greater than 1800x1800 then compress this image
            if (imageHeight >= 1800 && imageWidth >= 1800) {
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
            } else {
                getMerchantableDialog(context, entityID, entityType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
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


    public static void loadImageFromPicasso(Context context, ImageView imageView, String url, int placeholder) {
        Picasso.with(context)
                .load(url)
                .error(placeholder)
                .into(imageView);
    }


}

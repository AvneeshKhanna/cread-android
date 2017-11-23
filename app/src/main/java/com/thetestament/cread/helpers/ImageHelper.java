package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;

/**
 * A helper class for providing utility method for profile pic related operations
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
        } else {
            s = "/Cread/Short/short_pic.jpg";
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
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
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
                .start(context, fragment, UCrop.REQUEST_CROP);
    }

    /**
     * Method to perform required operation on cropped image.
     *
     * @param uri      Uri of cropped image.
     * @param context  Context of use.
     * @param rootView Layout parent view reference.
     * @param shortID  Short id if the content.
     */
    public static void processCroppedImage(Uri uri, Context context, View rootView, String shortID) {
        try {
            //Decode image file
            Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
            //If resolution of image is greater than 2000x2000 then compress this image
            if (bitmap.getWidth() > 1800 && bitmap.getWidth() > 1800) {
                //Compress image
                compressCroppedImg(uri, context, IMAGE_TYPE_USER_CAPTURE_PIC);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_SHORT_ID, shortID);
                bundle.putString(EXTRA_MERCHANTABLE, "1");
                //Open preview screen
                Intent intent = new Intent(context, CollaborationActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                context.startActivity(intent);
            } else {
                getMerchantableDialog(context, shortID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
        }
    }

    /**
     * Method to show dialog when user uploads low resolution image.
     *
     * @param context Context of use.
     * @param shortID Short id if the content.
     */
    private static void getMerchantableDialog(final Context context, final String shortID) {
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
                        bundle.putString(EXTRA_SHORT_ID, shortID);
                        bundle.putString(EXTRA_MERCHANTABLE, "1");
                        //open preview screen
                        Intent intent = new Intent(context, CollaborationActivity.class);
                        intent.putExtra(EXTRA_DATA, bundle);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .show();
    }


}

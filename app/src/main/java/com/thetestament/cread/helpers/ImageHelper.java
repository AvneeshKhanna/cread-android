package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.thetestament.cread.R;
import com.yalantis.ucrop.UCrop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;

import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_PROFILE_PIC;

/**
 * A helper class for providing utility method for profile pic related operations
 */

public class ImageHelper {

    /**
     * Return the Uri of picture stored in phone storage.
     *
     * @param imageType Type of image i.e IMAGE_TYPE_USER_PROFILE_PIC or IMAGE_TYPE_USER_CAPTURE_PIC
     * @return uri of Picture.
     **/
    public static Uri getImageUri(String imageType) {

        String s;
        if (imageType.equals(IMAGE_TYPE_USER_PROFILE_PIC)) {
            s = "/Cread/Profile/user_profile_pic.jpg";
        } else {
            s = "/Cread/Capture/capture_pic.jpg";
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

        //Launch  image cropping activity
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(context);
    }

}

package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;

/**
 * A helper class for providing utility method for profile pic related operations
 */

public class ImageHelper {

    /**
     * Return the Uri of the profile picture of the user stored in phone storage.
     *
     * @return uri of Profile picture
     **/
    public static Uri getProfilePicUri() {

        File outFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/Profile/user_profile_pic.jpg");
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
     * @param sourceUri Uri of the cached image.
     */
    public static Uri copyCroppedImg(Uri sourceUri, Context context) throws IOException {
        File sourceFile = new File(sourceUri.getPath());
        //For more information please visit "https://github.com/zetbaitsu/Compressor"
        //To compress the profile pic
        File compressedFile = new Compressor(context).compressToFile(sourceFile);

        File destFile = new File(ImageHelper.getProfilePicUri().getPath());

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

}

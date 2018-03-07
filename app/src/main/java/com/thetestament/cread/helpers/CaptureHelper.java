package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.Type;
import android.text.TextPaint;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileOutputStream;

import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;

/**
 * A helper class for providing utility method for capture related operations.
 */
public class CaptureHelper {

    /**
     * Method to generate signature on capture.
     *
     * @param signatureText Text to be drawn
     */
    public static void generateSignatureOnCapture(String signatureText, float signatureWidth, float signatureHeight, int screenWidth) {
        //Get bitmap
        Bitmap srcBitmap = BitmapFactory
                .decodeFile(ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath());
        //Image height and width
        int imageWidth = srcBitmap.getWidth();
        int imageHeight = srcBitmap.getHeight();

        float factor = (float) imageWidth / 1080;
        //Obtain bitmap config
        Bitmap.Config config = srcBitmap.getConfig();
        // set default bitmap config if none
        if (config == null) {
            config = android.graphics.Bitmap.Config.ARGB_8888;
        }
        //obtain mutable bitmap
        srcBitmap = srcBitmap.copy(config, true);

        //Create canvas
        Canvas canvas = new Canvas(srcBitmap);

        //Draw bitmap on canvas
        canvas.drawBitmap(srcBitmap, 0f, 0f, null);

        //Create paint for text
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);//text drawing style
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);//draw smooth edges
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(30 * factor);

        float textPadding = 10 * factor;
        //draw signature on watermark
        canvas.drawText(signatureText, textPadding, (imageHeight - textPadding), textPaint);

        //Save bitmap in device storage
        saveBitmap(srcBitmap, ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath());
    }


    /**
     * Method to create square shape blurred bitmap.
     *
     * @param context         Context to use.
     * @param bitmap          Image to blurred.
     * @param squareImageSide Side of square image.
     * @param isLandscape     True if image is landscape false otherwise.
     */
    public static void createSquareBlurBitmap(Context context, Bitmap bitmap, int squareImageSide, boolean isLandscape) {

        //Blur radius  varies b/w 0-25
        float BLUR_RADIUS = 25f;

        //Create scale down mutable bitmap of 400X400 from original bitmap
        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, false);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocation = Allocation.createFromBitmap(rs, inputBitmap);
        Type t = allocation.getType();
        //Create allocation with the same type
        Allocation blurredAllocation = Allocation.createTyped(rs, t);

        //Create script
        ScriptIntrinsicBlur scriptBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        //Set property
        scriptBlur.setRadius(BLUR_RADIUS);
        scriptBlur.setInput(allocation);
        scriptBlur.forEach(blurredAllocation);
        blurredAllocation.copyTo(inputBitmap);

        //Create scaled image of original size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(inputBitmap, squareImageSide, squareImageSide, false);

        //Destroy everything to free memory
        allocation.destroy();
        blurredAllocation.destroy();
        scriptBlur.destroy();
        t.destroy();
        rs.destroy();

        //if not null then call recycle so GC deallocate memory
        if (inputBitmap != null) {
            inputBitmap.recycle();
        }

        //Method called
        drawOverLayOnBlurred(squareImageSide, isLandscape, scaledBitmap, bitmap);
    }


    /**
     * Method to draw overLay image over blurred bitmap and save it on device as capture picture.
     *
     * @param squareImageSize Side of square image.
     * @param isLandscape     true if image is landscape false otherwise.
     * @param blurredBitmap   Blurred bitmap.
     * @param overLayBitMap   OverlayBitmap
     */
    private static void drawOverLayOnBlurred(final int squareImageSize, final boolean isLandscape
            , Bitmap blurredBitmap, Bitmap overLayBitMap) {

        float left, top;

        //if image is landscape
        if (isLandscape) {
            //Get x(left) ,y(top) position for overLay bitmap
            left = 0;
            top = (squareImageSize - overLayBitMap.getHeight()) / 2;
        } else {
            //Get x(left) ,y(top) position for overLay bitmap
            left = (squareImageSize - overLayBitMap.getWidth()) / 2;
            top = 0;
        }


        //Create canvas object here
        Canvas canvas = new Canvas(blurredBitmap);
        //Draw blurred bitmap on canvas
        canvas.drawBitmap(blurredBitmap, 1f, 1f, null);
        //Draw over lay bitmap over canvas
        canvas.drawBitmap(overLayBitMap
                , left, top, null);


        //Method call
        saveBitmap(blurredBitmap, ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath());


        //if not null then call recycle so GC deallocate memory
        if (blurredBitmap != null) {
            blurredBitmap.recycle();
        }
        //if not null then call recycle so GC deallocate memory
        if (overLayBitMap != null) {
            overLayBitMap.recycle();
        }
    }

    /**
     * Method to save image on device local storage as a capture image.
     *
     * @param bitmap    Bitmap  to be saved.
     * @param imageType Image type is IMAGE_TYPE_USER_CAPTURE_PIC
     */
    private static void saveBitmap(Bitmap bitmap, String imageType) {
        try {
            File file = new File(imageType);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            //Handle exception
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

}

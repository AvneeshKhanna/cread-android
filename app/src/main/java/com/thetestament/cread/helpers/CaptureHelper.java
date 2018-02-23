package com.thetestament.cread.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

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

/*
        //Create paint for rectangle background
        Paint paintRect = new Paint();
        //Set color
        paintRect.setColor(Color.BLACK);
        //Set text drawing style
        paintRect.setStyle(Paint.Style.FILL_AND_STROKE);
        //Set width
        paintRect.setStrokeWidth(10);
        //Set alpha
        paintRect.setAlpha(80);



        float left = textPadding;
        float top = textPadding + imageHeight - signatureHeight;
        float right = signatureWidth;
        float bottom = top + signatureHeight + textPadding;
        //Draw transparent rectangle
        canvas.drawRect(left, top, right, bottom, paintRect);*/


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
        saveBitmap(srcBitmap);
    }


    /**
     * Method to save image on device local storage.
     *
     * @param bitmap Image to be saved.
     */
    private static void saveBitmap(Bitmap bitmap) {
        try {
            File file = new File(ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath());
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            //Handle exception
            e.printStackTrace();
        }

    }

}

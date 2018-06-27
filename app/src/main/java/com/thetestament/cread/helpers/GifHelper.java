package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.utils.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class to create sharable GIF.
 */

public class GifHelper {

    //region:Fields and constants
    FragmentActivity mContext;
    Bitmap mBitmap;
    FrameLayout frameLayout;
    String mShareOption;
    MaterialDialog materialDialog;
    boolean mCreateForSharing;

    /**
     * Flag to maintain path of live filter GIF.
     */
    String mGifPath = null;
    //endregion

    public interface Listener {
        void listen();
    }


    /**
     * Required constructor.
     *
     * @param context          Context to use.
     * @param bitmap           Bitmap object.
     * @param frameLayout      FrameLayout reference.
     * @param shareOption      Medium where GIF to be shared.
     * @param createForSharing false if called from 'Save on your phone' false otherwise.
     */
    public GifHelper(FragmentActivity context, Bitmap bitmap, FrameLayout frameLayout, String shareOption, boolean createForSharing) {
        this.mContext = context;
        this.mBitmap = bitmap;
        this.frameLayout = frameLayout;
        this.mShareOption = shareOption;
        this.mCreateForSharing = createForSharing;
        materialDialog = CustomDialog.getProgressDialog(mContext, mContext.getString(R.string.title_gif_dialog));
    }

    /**
     * Method to create handler task.
     *
     * @param handler handler task
     * @param counter Counter value i.e 0.
     */
    public void startHandlerTask(final Handler handler, final int counter) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runBitmapOnUi(new Listener() {
                    @Override
                    public void listen() {
                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/LiveFilter/live_filter_pic" + counter + ".jpg");
                            file.getParentFile().mkdirs();

                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            FileOutputStream out = new FileOutputStream(file);
                            mBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                            out.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (counter < 72) {
                            startHandlerTask(handler, counter + 1);
                        } else {
                            initFFmpeg();
                        }
                    }
                });
            }
        }, 50);
    }

    /**
     * Method to take screenshot.
     *
     * @param listener Listener reference.
     */
    private void runBitmapOnUi(final Listener listener) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Disable drawing cache
                frameLayout.setDrawingCacheEnabled(false);
                frameLayout.setDrawingCacheEnabled(true);
                mBitmap = frameLayout.getDrawingCache();

                HandlerThread handlerThread = new HandlerThread("HandlerThread");
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listener.listen();
                    }
                }, 1);

            }
        });
    }

    /**
     * Method to initialize FFmpeg library for gif generation.
     */
    private void initFFmpeg() {
        final FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    //Hide progress dialog
                    materialDialog.dismiss();
                    //Show toast
                    ViewHelper.getShortToast(mContext
                            , mContext.getString(R.string.error_ffmpeg));
                }

                @Override
                public void onSuccess() {
                    //Method called
                    createGif(ffmpeg);
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            //Hide progress dialog
            materialDialog.dismiss();
            //Show toast
            ViewHelper.getShortToast(mContext
                    , mContext.getString(R.string.error_ffmpeg));
        }
    }

    /**
     * Method to create GIF from images.
     *
     * @param fFmpeg FFmpeg instance.
     */
    private void createGif(FFmpeg fFmpeg) {
        //Called from sharing
        if (mCreateForSharing) {
            mGifPath = "/Cread/output.gif";
        } else {
            double i =  Math.random();
            mGifPath = "/Cread/output" + i + ".gif";
        }

        String[] cmd = new String[8];
        cmd[0] = "-f";
        cmd[1] = "image2";
        cmd[2] = "-framerate";
        cmd[3] = "18";
        cmd[4] = "-y";
        cmd[5] = "-i";
        cmd[6] = Environment.getExternalStorageDirectory() + "/Cread/LiveFilter/live_filter_pic%d.jpg";
        cmd[7] = Environment.getExternalStorageDirectory() + mGifPath;
        try {
            fFmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Dismiss material dialog
                    materialDialog.dismiss();
                    //Method called
                    if (mCreateForSharing) {
                        launchShareIntent(mContext);
                    } else {
                        ViewHelper.getToast(mContext, "GIF saved to : " + mGifPath);
                    }
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                    //Hide progress dialog
                    materialDialog.dismiss();
                    //Show toast
                    ViewHelper.getShortToast(mContext
                            , mContext.getString(R.string.error_ffmpeg));
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {


                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            //Hide progress dialog
            materialDialog.dismiss();
            //Show toast
            ViewHelper.getShortToast(mContext
                    , mContext.getString(R.string.error_ffmpeg));
        }
    }

    /**
     * Method to launch required intent for GIF sharing.
     *
     * @param context Context to use.
     */
    private void launchShareIntent(Context context) {
        switch (mShareOption) {
            case Constant.SHARE_OPTION_WHATSAPP:
                if (ShareHelper.isAppInstalled(context, "com.whatsapp")) {
                    Intent shareWhatsAppIntent = new Intent();
                    shareWhatsAppIntent.setAction(Intent.ACTION_SEND);
                    shareWhatsAppIntent.setPackage("com.whatsapp");
                    shareWhatsAppIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + "/Cread/output.gif"));
                    shareWhatsAppIntent.setType("video/*");
                    shareWhatsAppIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.startActivity(shareWhatsAppIntent);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Whatsapp installed");
                }
                break;
            case Constant.SHARE_OPTION_FACEBOOK:
                if (ShareHelper.isAppInstalled(context, "com.facebook.katana")) {
                    Intent shareFacebookIntent = new Intent(Intent.ACTION_SEND);
                    shareFacebookIntent.setPackage("com.facebook.katana");
                    shareFacebookIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + "/Cread/output.gif"));
                    shareFacebookIntent.setType("image/*");
                    shareFacebookIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.startActivity(shareFacebookIntent);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Facebook installed");
                }
                break;
            case Constant.SHARE_OPTION_INSTAGRAM:
                if (ShareHelper.isAppInstalled(context, "com.instagram.android")) {
                    Intent shareInstagramIntent = new Intent();
                    shareInstagramIntent.setAction(Intent.ACTION_SEND);
                    shareInstagramIntent.setPackage("com.instagram.android");
                    shareInstagramIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + "/Cread/output.gif"));
                    shareInstagramIntent.setType("image/*");
                    shareInstagramIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mContext.startActivity(shareInstagramIntent);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Instagram installed");
                }

                break;
            case Constant.SHARE_OPTION_OTHER:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + "/Cread/output.gif"));
                intent.setType("video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
                break;
            default:
                //do nothing
                break;
        }
    }

    /**
     * Method to return True if live filter is present else return false.
     *
     * @param s Filter name.
     * @return true if filter is present false otherwise.
     */
    public static boolean hasLiveFilter(String s) {
        switch (s) {
            case Constant.LIVE_FILTER_SNOW:
            case Constant.LIVE_FILTER_BUBBLE:
            case Constant.LIVE_FILTER_CONFETTI:
                return true;
            case Constant.LIVE_FILTER_RAIN:
            case Constant.LIVE_FILTER_NONE:
                return false;
            default:
                return false;
        }
    }
}

package com.eason.grade.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Dpuntu on 2017/3/8.
 */
public class BitmapUtil {
    private final static int WIDTH = 384;
    private final static float SMALL_TEXT = 23;
    private final static float LARGE_TEXT = 35;
    private final static int START_RIGHT = WIDTH;
    private final static int START_LEFT = 0;
    private final static int START_CENTER = WIDTH / 2;

    /**
     * 特殊需求：
     */
    public final static int IS_LARGE = 10;
    public final static int IS_SMALL = 11;
    public final static int IS_RIGHT = 100;
    public final static int IS_LEFT = 101;
    public final static int IS_CENTER = 102;


    private static float x = START_LEFT, y;

    /**
     * 生成图片
     */
    public static Bitmap stringListtoBitmap(Context context, ArrayList<StringBitmapParameter> AllString) {
        if (AllString.size() <= 0) {
            return Bitmap.createBitmap(WIDTH, WIDTH / 4, Bitmap.Config.RGB_565);
        }
        ArrayList<StringBitmapParameter> mBreakString = new ArrayList<>();

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setTextSize(SMALL_TEXT);

        // 仿宋打不出汉字
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/songti.TTF");
        Typeface font = Typeface.create(typeface, Typeface.NORMAL);
        paint.setTypeface(font);

        for (StringBitmapParameter mParameter : AllString) {
            //检测一行多少字
            int aLineLength = paint.breakText(mParameter.getText(), true, WIDTH, null);
            int length = mParameter.getText().length();
            if (aLineLength < length) {

                int num = length / aLineLength;
                String aLineString;
                String remainString;

                for (int j = 0; j < num; j++) {
                    aLineString = mParameter.getText().substring(j * aLineLength, (j + 1) * aLineLength);
                    mBreakString.add(new StringBitmapParameter(aLineString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
                }

                remainString = mParameter.getText().substring(num * aLineLength, mParameter.getText().length());
                mBreakString.add(new StringBitmapParameter(remainString, mParameter.getIsRightOrLeft(), mParameter.getIsSmallOrLarge()));
            } else {
                mBreakString.add(mParameter);
            }
        }


        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int fontHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent);
        y = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);

        int bNum = 0;
        for (StringBitmapParameter mParameter : mBreakString) {
            String bStr = mParameter.getText();
            if (bStr.isEmpty() | bStr.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
                bNum++;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, fontHeight * (mBreakString.size() + bNum), Bitmap.Config.RGB_565);

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, Color.WHITE);
            }
        }

        Canvas canvas = new Canvas(bitmap);
        for (StringBitmapParameter mParameter : mBreakString) {
            String str = mParameter.getText();
            if (mParameter.getIsSmallOrLarge() == IS_SMALL) {
                paint.setTextSize(SMALL_TEXT);

            } else if (mParameter.getIsSmallOrLarge() == IS_LARGE) {
                paint.setTextSize(LARGE_TEXT);
            }

            if (mParameter.getIsRightOrLeft() == IS_RIGHT) {
                x = WIDTH - paint.measureText(str);
            } else if (mParameter.getIsRightOrLeft() == IS_LEFT) {
                x = START_LEFT;
            } else if (mParameter.getIsRightOrLeft() == IS_CENTER) {
                x = (WIDTH - paint.measureText(str)) / 2.0f;
            }

            if ((str != null && str.isEmpty()) | str.contains("\n") | mParameter.getIsSmallOrLarge() == IS_LARGE) {
                canvas.drawText(str, x, y + fontHeight / 2, paint);
                y = y + fontHeight;
            } else {
                canvas.drawText(str, x, y, paint);
            }
            y = y + fontHeight;
        }
//        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * 合并图片
     */
    public static Bitmap addBitmapInHead(Bitmap first, Bitmap second) {
        int width = Math.max(first.getWidth(), second.getWidth());
        int startWidth = (width - first.getWidth()) / 2;
        int height = first.getHeight() + second.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                result.setPixel(i, j, Color.WHITE);
            }
        }
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, startWidth, 0, null);
        canvas.drawBitmap(second, 0, first.getHeight(), null);
        return result;
    }

    /***
     * 使用两个方法的原因是：
     * logo标志需要居中显示，如果直接使用同一个方法是可以显示的，但是不会居中
     */
    public static Bitmap addBitmapInFoot(Bitmap bitmap, Bitmap image) {
        int width = Math.max(bitmap.getWidth(), image.getWidth());
        int startWidth = (width - image.getWidth()) / 2;
        int height = bitmap.getHeight() + image.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                result.setPixel(i, j, Color.WHITE);
            }
        }
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(image, startWidth, bitmap.getHeight(), null);
        return result;
    }

    /**
     * 保存文件到指定路径
     *
     * @param context
     * @param fileName 导出图片文件名称
     * @param bmp      图片
     * @return
     */
    public static boolean saveImageToGallery(Context context, String fileName, Bitmap bmp) {
        // 首先保存图片
        String storePath = FileUtils.getAppStorageDir();
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        fileName += ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return isSuccess;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

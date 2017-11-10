package com.gxh.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 *  合成图片  不规则图片  拖拽显示部分区域
 */
public class AlbumImageView extends ImageView {

    public int ALBUM_IMAGE_TYPE = 0;   //合成图片类型

    public static final int ALBUM_IMAGE_SHAPE = 1001;   // 图片类型: 形状
    public static final int ALBUM_IMAGE_FRAME = 1002;   // 图片类型: 带相框的高大上图片

    private Bitmap[] bitmapMask = new Bitmap[2];   // 图片模板: 形状的只有1个   带相框的则有两个(一个是相框,一个是相框的阴影截取范围)
    private Bitmap src;   // 原图片

    private float xOffset = 0.f;   // x轴偏移
    private float yOffset = 0.f;   //y轴偏移

    private float downX = 0.0f;    //手指所在x坐标
    private float downY = 0.0f;    //手指所在y坐标


    public AlbumImageView(Context context, int image_type, Bitmap src, Bitmap[] bitmapMask, float xOffset, float yOffset) {
        super(context);
        this.ALBUM_IMAGE_TYPE = image_type;
        this.bitmapMask = bitmapMask;
        this.src = src;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        init();
    }

    public AlbumImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 初始化处理原图片 保证图片不能小于相框或形状
     */
    private void init() {
        if (src.getWidth() < bitmapMask[0].getWidth()) {  //如果原图片宽小于模板宽 按照等比例拉伸原图片
            Matrix matrix = new Matrix();
            matrix.postScale((float) bitmapMask[0].getWidth() / (float) src.getWidth(), (float) bitmapMask[0].getWidth() / (float) src.getWidth());
            src = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }

        if (src.getHeight() < bitmapMask[0].getHeight()) { //如果原图片高小于模板搞 按照等比例拉伸原图片
            Matrix matrix2 = new Matrix();
            matrix2.postScale((float) bitmapMask[0].getHeight() / (float) src.getHeight(), (float) bitmapMask[0].getHeight() / (float) src.getHeight());
            src = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix2, true);
        }

        this.setImageBitmap(resolveBitmap(-xOffset, -yOffset)); //设置Bitmap
    }

    /**
     * 处理图片 合成图片
     *
     * @param xOffset x轴偏移量
     * @param yOffset y轴偏移量
     * @return 返回合成之后的Bitmap
     */
    private Bitmap resolveBitmap(float xOffset, float yOffset) {
        Bitmap bmp = Bitmap.createBitmap(bitmapMask[0].getWidth(), bitmapMask[0].getHeight(), Bitmap.Config.ARGB_4444); //用模板生成一个bmp
        Paint paint = new Paint();  //初始化画笔
        if (ALBUM_IMAGE_TYPE == ALBUM_IMAGE_SHAPE) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));  //PorterDuffXfermode算法  SRC_ATOP 取下层非交集部分与上层交集部分
            Canvas canvas = new Canvas(bmp);
            canvas.drawBitmap(bitmapMask[0], 0, 0, null);  // 画模板
            canvas.drawBitmap(src, xOffset, yOffset, paint); //画原图
            return bmp;
        } else {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));  //DST_ATOP 取上层非交集部分与下层交集部分
            Canvas canvas = new Canvas(bmp);
            canvas.drawBitmap(bitmapMask[0], 0, 0, null);
            canvas.drawBitmap(src, xOffset, yOffset, paint);

            Bitmap result = Bitmap.createBitmap(bitmapMask[1].getWidth(), bitmapMask[1].getHeight(), Bitmap.Config.ARGB_4444);  // 相框阴影部分图片
            Canvas canvas1 = new Canvas(result);
            Paint paint1 = new Paint();
            paint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));  // 然后再画阴影部分 SRC_OUT 取上层非交集部分 抠出相框和图片
            canvas1.drawBitmap(bitmapMask[1], 0, 0, null);
            canvas1.drawBitmap(bmp, 0, 0, paint1);
            bmp.recycle();
            return result;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("gxh", "aaa");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX = -((int) downX - (int) event.getX());
                int offsetY = -((int) downY - (int) event.getY());
                xOffset += offsetX;
                yOffset += offsetY;
                //判断是否滑到边界
                if ((xOffset <= 0 && yOffset <= 0) && (src.getWidth() - Math.abs(xOffset) > bitmapMask[0].getWidth() && src.getHeight() - Math.abs(yOffset) > bitmapMask[0].getHeight())) {
                    this.setImageBitmap(resolveBitmap(xOffset, yOffset));
                } else {
                    //else 里面判断xy某一坐标滑到边界  另一个坐标还得继续移动 否则会出现问题
                    if (xOffset > 0) {
                        xOffset = 0;
                    }

                    if (yOffset > 0) {
                        yOffset = 0;
                    }

                    if (src.getWidth() - Math.abs(xOffset) < bitmapMask[0].getWidth()) {
                        xOffset = bitmapMask[0].getWidth() - src.getWidth();
                    }
                    if (src.getHeight() - Math.abs(yOffset) < bitmapMask[0].getHeight()) {
                        yOffset = bitmapMask[0].getHeight() - src.getHeight();
                    }
                    setImageBitmap(resolveBitmap(xOffset, yOffset));
                }
                downX = event.getX();
                downY = event.getY();
                break;
        }
        return true;
    }
}

package kenta.android.apps.eshiritori;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View implements Serializable
{
    // SerializableインターフェースはMainActivityオブジェクトからDialogOfPaintingオブジェクトへ
    // このクラスのオブジェクトを渡すために実装しています

    private static final int SIZE_OF_QUEUE = 3; // Undo用のキューのサイズ

    private Paint mPaint;
    private Path mCurtPath;                 // 今描いている線を保存しておく
    private float mOldX = 0;                // 一つ前のmCurtPathの終点を保存する
    private float mOldY = 0;                // 一つ前のmCurtPathの終点を保存する
    private ArrayList<Bitmap> mBitmapQueue; // Redo用にbitmapを保存しておくキュー
    public Bitmap mInitialBitmap;           // 最初のcanvasの状態を保存する
    private Bitmap mOldBitmap;              // 一番戻った時の状態のbitmapを保存する
    private boolean mIsFirst = true;        // 初期状態をキャプチャするのに使う

    public PaintView(Context context)
    {
        super(context);
        mPaint = new Paint();
        mBitmapQueue = new ArrayList<Bitmap>();
        mCurtPath = new Path();

        setParamsToPaint(mPaint);
    }

    private void setParamsToPaint(Paint paint)
    {
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public Bitmap getCurrentCanvasBitmap()
    {
        int enqueued_recently = mBitmapQueue.size() - 1;

        if (!mBitmapQueue.isEmpty())
            return mBitmapQueue.get(enqueued_recently);
        else
            return null;
    }

    public boolean isWhite()
    {
        /* キャンバスに何も描かれていない(初期状態と等しい)かどうかを *
         * チェックして返すメソッド(空→true, 空じゃない→false)     */

        boolean result; // 返り値

        if(this.getCurrentCanvasBitmap() == null
                || this.getCurrentCanvasBitmap() == mInitialBitmap)
            result = true;
        else
            result = false;

        return result;
    }

	@Override
	protected void onDraw(Canvas canvas)
	{
		//キューにある最新のbitmapを描画する
		if(!mBitmapQueue.isEmpty())
		{
			int enqueued_recently = mBitmapQueue.size() - 1; 
			canvas.drawBitmap(mBitmapQueue.get(enqueued_recently), 0, 0, null);
		}
		else if(mOldBitmap != null)
		{
			canvas.drawBitmap(mOldBitmap, 0, 0, null);
		}
		//今描いている線を描画する
		if(!mCurtPath.isEmpty())
		{
			canvas.drawPath(mCurtPath, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{	
		if(mIsFirst)
		{
			this.setInitState();
			mIsFirst = false;
		}
		
		if(e.getAction() == MotionEvent.ACTION_DOWN)
		{
			mCurtPath.moveTo(e.getX(), e.getY());
			mOldX = e.getX();
			mOldY = e.getY();
		}
		if(e.getAction() == MotionEvent.ACTION_MOVE)
		{
			final float dx = Math.abs(e.getX() - mOldX);
			final float dy = Math.abs(e.getY() - mOldY);
			
			if(dx >= 15 || dy >= 15)
			{
				float endX = (e.getX() + mOldX) / 2;
				float endY = (e.getY() + mOldY) / 2;
				
				mCurtPath.quadTo(mOldX, mOldY, endX, endY);	
				
				mOldX = e.getX();
				mOldY = e.getY();
			}
			
			//Viewの再描画
			super.invalidate();
		}
		if(e.getAction() == MotionEvent.ACTION_UP)
		{
			mCurtPath.lineTo(e.getX(), e.getY());
			
			//Viewの再描画
			super.invalidate();
			
			//現在のcanvasの状態をキャプチャしてQUEUEに挿入
			setDrawingCacheEnabled(true);
			enqueueBitmap(Bitmap.createBitmap(getDrawingCache()));
			setDrawingCacheEnabled(false);
			
			mCurtPath.reset();
		}
		
		return true;
	}
	
	private void enqueueBitmap(Bitmap bmpToAdd)
	{
		//キューのサイズを超えたら一番古い要素[0]を削除してサイズを一定に保つ
		mBitmapQueue.add(bmpToAdd);

		if(mBitmapQueue.size() > SIZE_OF_QUEUE)
		{
			mOldBitmap = mBitmapQueue.get(0);
			mBitmapQueue.remove(0);
		}
	}
	
	private void dequeueBitmap()
	{
		if(!mBitmapQueue.isEmpty())
		{
			int END_OF_QUEUE = mBitmapQueue.size() - 1;
			mBitmapQueue.remove(END_OF_QUEUE);
		}
	}
	
	public void redo()
	{
		/*
		 * キューから最新のbitmapを削除する
		 */
		dequeueBitmap();
		super.invalidate();
		
	}
	
	public void clear()
	{
		//初期状態のキャンパスをENQUEUE
		if(mInitialBitmap != null)
			enqueueBitmap(mInitialBitmap);
		//更新
		super.invalidate();
	}
	
	private void setInitState()
	{
		//現在のcanvasの状態をキャプチャしてInitBitmapに保存
		setDrawingCacheEnabled(true);
		mInitialBitmap = Bitmap.createBitmap(getDrawingCache());
		setDrawingCacheEnabled(false);
	}
}

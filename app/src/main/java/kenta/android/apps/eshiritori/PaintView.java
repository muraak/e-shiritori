package kenta.android.apps.eshiritori;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

class PaintView extends View implements Serializable
{
    // SerializableインターフェースはMainActivityオブジェクトからDialogOfPaintingオブジェクトへ
    // このクラスのオブジェクトを渡すために実装しています

    private Paint mPaint;
    private Path mCurtPath;                 // 今描いている線を保存しておく
    private float mOldX = 0;                // 一つ前のmCurtPathの終点を保存する
    private float mOldY = 0;                // 一つ前のmCurtPathの終点を保存する

	private PictureHistory pictureHistory;

    public PaintView(Context context)
    {
        super(context);
        mPaint = new Paint();
        mCurtPath = new Path();
		pictureHistory = new PictureHistory();
		pictureHistory.load(context);
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
		if(pictureHistory.isLatestEmptyPicture())
			return  null;
		else
			return pictureHistory.getLatest();
    }

    public boolean isWhite()
	{
		return pictureHistory.isLatestEmptyPicture();
    }

	@Override
	protected void onDraw(Canvas canvas)
	{
		if(!pictureHistory.isNotInitialized())
			canvas.drawBitmap(pictureHistory.getLatest(), 0, 0, null);

		if(!mCurtPath.isEmpty())
			canvas.drawPath(mCurtPath, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		if(pictureHistory.isNotInitialized())
		{
			setDrawingCacheEnabled(true);
			pictureHistory.initialize(Bitmap.createBitmap(getDrawingCache()));
			setDrawingCacheEnabled(false);
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
			pictureHistory.putLatest(Bitmap.createBitmap(getDrawingCache()));
			setDrawingCacheEnabled(false);
			
			mCurtPath.reset();
		}
		
		return true;
	}
	
	public void undo()
	{
		pictureHistory.backToPrev();
		super.invalidate();
	}
	
	public void clear()
	{
		pictureHistory.putEmpty();
		//更新
		super.invalidate();
	}

	public void save(Context context)
	{
		if(pictureHistory != null)
			pictureHistory.save(context);
	}
}

class PictureHistory
{
	private static final int MAX_HISTORY_SIZE = 4;

	private Bitmap emptyPicture;

	private ArrayList<Bitmap> histories;

	public PictureHistory()
	{
		histories = new ArrayList<>();
	}

	public boolean isNotInitialized()
	{
		return (emptyPicture == null);
	}

	public void initialize(Bitmap empty_picture)
	{
		emptyPicture = empty_picture;
		if(histories.size() == 0)
		{
			putEmpty();
		}
	}

	public void putLatest(Bitmap latest_picture) {
		histories.add(latest_picture);

		if (histories.size() > MAX_HISTORY_SIZE) {
			histories.remove(0);
		}
	}

	public void putEmpty() {
		putLatest(emptyPicture);
	}

	public Bitmap getLatest() {
		if (histories.size() == 0) {
			return emptyPicture;
		} else {
			return histories.get(histories.size() - 1);
		}
	}

	public void backToPrev() {
		if (histories.size() > 1) {
			histories.remove(histories.size() - 1);
		}
	}

	public boolean isLatestEmptyPicture() {
		return (getLatest() == emptyPicture);
	}

	public void save(Context context)
	{
		if(getLatest() == emptyPicture)
		{
			return;
		}

		if(emptyPicture != null)
			saveBitmapToShrPref(context, emptyPicture, "empty");

		if(histories.size() > 0)
		{
			for(int i = 0; i < histories.size(); i++)
			{
				saveBitmapToShrPref(context, histories.get(i), "histories" + i);
			}
		}
	}

	private void saveBitmapToShrPref(Context context, Bitmap bitmap, String key)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

		String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

		SharedPreferences pref
				= context.getSharedPreferences("history", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, bitmapStr);
		editor.apply();
	}

	public void load(Context context)
	{
		Bitmap empty_picture = loadBitmapFromShrPref(context, "empty");
		if(empty_picture != null)
			emptyPicture = empty_picture;

		for(int i = 0; i < MAX_HISTORY_SIZE; i++)
		{
			Bitmap picture = loadBitmapFromShrPref(context, "histories" + i);

			if (picture == null)
			{
				break;
			}

			putLatest(picture);
		}

		clear(context);
	}

	private Bitmap loadBitmapFromShrPref(Context context, String key)
	{
		SharedPreferences pref = context.getSharedPreferences("history", Context.MODE_PRIVATE);
		String s = pref.getString(key, "");
		if (!s.equals(""))
		{
			byte[] b = Base64.decode(s, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);
		}
		else
		{
			return null;
		}
	}

	private void clear(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences("history", Context.MODE_PRIVATE);
		pref.edit().clear().apply();
	}
}

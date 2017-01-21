package  kenta.android.apps.eshiritori;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class ShadowView extends View
{

	public ShadowView(Context context)
	{
		super(context);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		this.dropShadow(canvas);
	}
	
	private void dropShadow(Canvas canvas)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setARGB(128, 0, 0, 0);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		BlurMaskFilter maskfilter = new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL);
		paint.setMaskFilter(maskfilter);
		
		Path path = new Path();
		path.moveTo(-4, this.getHeight() - 8);
		path.lineTo(this.getWidth() + 4, this.getHeight() - 8);
		canvas.drawPath(path, paint);
	}

}

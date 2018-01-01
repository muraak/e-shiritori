package  kenta.android.apps.eshiritori;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class ShadowView extends View
{
	private int strokeWidth;
	public ShadowView(Context context, int stroke_width)

	{
		super(context);
		strokeWidth = stroke_width;
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		this.dropShadow(canvas);
	}
	
	private void dropShadow(Canvas canvas)
	{
//		BlurMaskFilter maskfilter = new BlurMaskFilter(strokeWidth / 2, BlurMaskFilter.Blur.NORMAL);
//		paint.setMaskFilter(maskfilter);

		for(int i = 0; i < strokeWidth; i++)
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			paint.setARGB(((64/strokeWidth) * i), 0, 0, 0);

			Path path = new Path();
			// under shadow
			path.moveTo(strokeWidth, this.getHeight() - i);
			path.lineTo(this.getWidth() - i, this.getHeight() - i);
			// right shadow
			path.lineTo(this.getHeight() - i, strokeWidth);


			canvas.drawPath(path, paint);
		}
	}

}

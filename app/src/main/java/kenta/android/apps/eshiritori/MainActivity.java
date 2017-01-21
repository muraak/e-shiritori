package kenta.android.apps.eshiritori;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setPaintingWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        /*********************************************
         * キーが押されたときに呼ばれるメソッドです. *
         * 何か処理を行ったときは"true"を返し        *
         * 何もしなかったときは"false"を返す         *
         * 決まりらしいです．                        *
         *********************************************/

        //戻るボタンが押されたときの処理
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            this.clearDB();
            this.finish();
            return true;
        }

        return false;
    }

    private void setPaintingWindow()
    {
        super.setContentView(R.layout.painting);

        PaintView paintView = new PaintView(this);

        // ペイント画面のボタンにイベント割り当て
        // コンストラクタでpaintViewを渡す必要があるので
        // 無名クラスでのリスナ設定は使用しません．
        Button btBack  = (Button) this.findViewById(R.id.btBackOfPaint);
        Button btClear = (Button) this.findViewById(R.id.btClearOfPaint);
        Button btDone  = (Button) this.findViewById(R.id.btDoneOfPaint);

        btBack .setOnClickListener(new BtBackOfPaintListener(paintView));
        btClear.setOnClickListener(new BtClearOfPaintListener(paintView));
        btDone .setOnClickListener(new BtDoneOfPaintListener(this, paintView));

        // 画面の横幅を取得する
        int dispWidth_px = getDispSize().x;

        // 絵を描く紙とその影から成るレイアウト
        // サイズは画面横幅の正方形と等しい(影は16px下に長い)
        RelativeLayout rlPaper = new RelativeLayout(this);

        //影ビューを作成
        ShadowView shadowView = new ShadowView(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                dispWidth_px, dispWidth_px + 16);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        rlPaper.addView(shadowView, lp);

        //絵を描くビューを作成
        setRepeatBackground(paintView, R.drawable.whitepaper);
        lp = new RelativeLayout.LayoutParams(dispWidth_px, dispWidth_px);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        rlPaper.addView(paintView, lp);

        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        super.addContentView(rlPaper, lp);
    }

    public void setRepeatBackground(View v, int id)
    {
        // このメソッドは一般に使用することが可能です
        // 第2引数で指定した画像(drawable)を第1引数の背景に
        // リピートモードで設定します
        BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(id);
        bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            v.setBackgroundDrawable(bg);
        else
            v.setBackground(bg);
    }

    private Point getDispSize()
    {
        // このメソッドは一般に使用することが可能です
        // 画面サイズを取得しPoint型で返します
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 14)
        {
            display.getSize(size);
        } else
        {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }

        return size;
    }

    private class BtBackOfPaintListener implements View.OnClickListener
    {
        private PaintView mPaintView;

        BtBackOfPaintListener(PaintView v)
        {
            mPaintView = v;
        }

        @Override
        public void onClick(View v)
        {
            mPaintView.redo();
        }
    }

    private class BtClearOfPaintListener implements View.OnClickListener
    {
        private PaintView mPaintView;

        BtClearOfPaintListener(PaintView v)
        {
            mPaintView = v;
        }

        @Override
        public void onClick(View v)
        {
            mPaintView.clear();
        }
    }

    private class BtDoneOfPaintListener implements View.OnClickListener
    {
        private Context mContext;
        private PaintView mPaintView;

        BtDoneOfPaintListener(Context context, PaintView paintView)
        {
            mContext = context;
            mPaintView = paintView;
        }

        @Override
        public void onClick(View v)
        {
            // 白紙だったら戻す
            if(mPaintView.isWhite())
            {
                makeCustomToast("何か絵を描いてね!", mContext);
//                Log.d("KMShiritori", "Canvas was empty!");
                return;
            }

            // 題名記入用のダイアログをポップアップ
            DialogOfPainting dialogOfPainting = new DialogOfPainting();
            // BundleでmPaintViewをDialogPaintingに渡すために保存します.
            Bundle bundle = new Bundle();
            bundle.putSerializable("PaintView", mPaintView);
            // DialogFragmentクラスのsetArguments(Bundle b)で
            // 値を保存したBundleを渡します.
            dialogOfPainting.setArguments(bundle);

            dialogOfPainting.show(getFragmentManager(), "inputTitle"/*TAG*/);
        }
    }

    // カスタムしたトーストでメッセージを表示
    public void makeCustomToast(String message , Context context)
    {
        Toast result = new Toast(context);

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.custom_toast, null);

        TextView textView = (TextView) view.findViewById(R.id.message);
        textView.setText(message);

        result.setView(view);
        // 表示時間
        result.setDuration(Toast.LENGTH_SHORT/*2sec*/);
        // 位置調整
        result.setGravity(Gravity.CENTER, 0, 180);

        result.show();
    }

    private void clearDB()
    {
        DBHelper helper = new DBHelper(this);
        helper.clear();
        helper.close();
        super.onDestroy();
    }
}

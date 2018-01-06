package kenta.android.apps.eshiritori;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kenta on 2015/05/03.
 */
public class AnswerActivity extends Activity
{
    enum Mode
    {
        Normal,
        ShowAnswer,
        ShowHint
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setWindow(Mode.Normal, 0);
    }

    private void setWindow(Mode mode, int page_number)
    {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(getResources().getColor(R.color.oliveDrab));
        super.setContentView(sv);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        DBHelper helper = new DBHelper(this);

        // 絵が空だったら以下の処理はキャンセル
        if(helper.getRecordCount() == 0)
        {
            LinearLayout ll_row1 = new LinearLayout(this);
            ll_row1.setOrientation(LinearLayout.HORIZONTAL);
            ll_row1.setGravity(Gravity.CENTER_HORIZONTAL);
            ll_row1.setPadding(0, 20, 0, 0);

            LinearLayout ll_row2 = new LinearLayout(this);
            ll_row2.setOrientation(LinearLayout.HORIZONTAL);
            ll_row2.setGravity(Gravity.CENTER_HORIZONTAL);
            ll_row2.setPadding(0, 20, 0, 0);

            Button btAnswer = new Button(this);
            Button btResult = new Button(this);
            setButtonOfAnswerWindow(btAnswer, new BtAnswerOnClickListener(), "絵を描く", ll_row1, 2);
            setButtonOfAnswerWindow(btResult, new BtChangeModeOnClickListener(Mode.ShowAnswer), "答え合わせ", ll_row2, 2);

            ll.addView(ll_row1);
            ll.addView(ll_row2);

            return;
        }

        ArrayList<Integer> ids = new ArrayList<>(5);
        ArrayList<Bitmap> pictures = new ArrayList<>(5);

        if(mode == Mode.Normal)
            helper.getPictures(page_number, ids, pictures);
        else
            helper.getPicturesOfAnswer(page_number, ids, pictures);

        boolean isLastPage = false;
        //  □□□…□□□
        //  ↑のページかどうか調べる
        if(ids.get(0) == 1)
            isLastPage = true;

        for(int i = 0; i < 5; i++)
        {
            //picturesの要素数が5個以下だったら抜ける
            if(i > (pictures.size() - 1))
                break;
            ImageView ivTmp = new ImageView(this);
            Bitmap pic = pictures.get(i);
            ivTmp.setImageBitmap(pic);
            ivTmp.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            getDispSize().x - (getDispSize().x / 20),
                            getDispSize().x - (getDispSize().x / 20));
            lp.topMargin = 20;
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            ivTmp.setLayoutParams(lp);
            ll.addView(ivTmp);

            if(mode == Mode.ShowAnswer || mode == Mode.ShowHint)
            {
                TextView tvTmp = new TextView(this);

                if(mode == Mode.ShowHint)
                    tvTmp.setText(helper.getHiddenTitle(ids.get(i)));
                else
                    tvTmp.setText(helper.getTitle(ids.get(i)));

                tvTmp.setTextSize(20);
                tvTmp.setTextColor(getResources().getColor(R.color.saddleBrown));
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = 20;
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                tvTmp.setLayoutParams(lp);
                ll.addView(tvTmp);
            }
        }

        LinearLayout ll_row1 = new LinearLayout(this);
        ll_row1.setOrientation(LinearLayout.HORIZONTAL);
        ll_row1.setGravity(Gravity.CENTER_HORIZONTAL);
        ll_row1.setPadding(0, 20, 0, 0);

        LinearLayout ll_row2 = new LinearLayout(this);
        ll_row2.setOrientation(LinearLayout.HORIZONTAL);
        ll_row2.setGravity(Gravity.CENTER_HORIZONTAL);
        ll_row2.setPadding(0, 20, 0, 0);

        if(!isLastPage)
        {
            Button btOlder = new Button(this);
            setButtonOfAnswerWindow(btOlder,
                    new BtOlderOnClickListener(mode, page_number), "←", ll_row1, 1);
        }

        Button btAnswer = new Button(this);
        Button btHint   = new Button(this);
        Button btResult = new Button(this);

        setButtonOfAnswerWindow(btAnswer, new BtAnswerOnClickListener(), "絵を描く", ll_row1, 2);

        if(mode == Mode.Normal) {
            setButtonOfAnswerWindow(btHint,
                    new BtChangeModeOnClickListener(Mode.ShowHint),
                    "ヒントちょうだい！", ll_row1, 2);
        }
        else if(mode == Mode.ShowHint){
            setButtonOfAnswerWindow(btHint,
                    new BtChangeModeOnClickListener(Mode.Normal),
                    "ヒントをかくす", ll_row1, 2);
        }

        if(mode != Mode.ShowAnswer) {
            setButtonOfAnswerWindow(btResult,
                    new BtChangeModeOnClickListener(Mode.ShowAnswer),
                    "答え合わせ", ll_row2, 2);
        }
        else
        {
            setButtonOfAnswerWindow(btResult,
                    new BtChangeModeOnClickListener(Mode.Normal),
                    "答えをかくす", ll_row2, 2);
        }

        //全ページ数をデータベースのレコード数から算出 ToDo: もっと簡単な算出方法を検討する
        int numberOfPages = helper.getRecordCount() / 5;
        if(helper.getRecordCount() % 5 != 0)
            numberOfPages++;
        numberOfPages--;

        helper.close();

        //  □□□…□□□
        //        ↑のページかどうか調べる → そうだったらbtNewerは表示しない
        if((mode == Mode.Normal) && (page_number > 0)
                || (mode != Mode.Normal) && (page_number < numberOfPages))
        {
            Button btNewer = new Button(this);
            setButtonOfAnswerWindow(btNewer,
                    new BtNewerOnClickListener(mode, page_number), "→", ll_row1, 1);
        }

        ll.addView(ll_row1);
        ll.addView(ll_row2);
    }

    private void setButtonOfAnswerWindow(Button bt, View.OnClickListener listener,
                                         String txt, ViewGroup l, int weight)
    {
        bt.setText(txt);
        bt.setOnClickListener(listener);
        //Resources res = getResources();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 10, 10);
        lp.weight = weight;
        bt.setBackgroundResource(R.drawable.selector_bt_paint);
        bt.setTextColor(Color.WHITE);
        bt.setLayoutParams(lp);
        l.addView(bt);
    }

    private Point getDispSize()
    {
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

    private class BtOlderOnClickListener implements View.OnClickListener
    {
        private int mPageNumber;
        private Mode mMode;

        BtOlderOnClickListener(Mode mode, int page_number)
        {
            mMode = mode;
            mPageNumber = page_number;
        }

        @Override
        public void onClick(View v)
        {
            if(mMode == Mode.Normal)
                mPageNumber++;
            else
                mPageNumber--;
            setWindow(mMode, mPageNumber);
        }
    }

    private class BtNewerOnClickListener implements View.OnClickListener
    {
        private int mPageNumber;
        private Mode mMode;

        BtNewerOnClickListener(Mode mode, int page_number)
        {
            mMode = mode;
            mPageNumber = page_number;
        }

        @Override
        public void onClick(View v)
        {
            if(mMode == Mode.Normal)
                mPageNumber--;
            else
                mPageNumber++;
            setWindow(mMode, mPageNumber);
        }
    }

    private class BtChangeModeOnClickListener implements View.OnClickListener
    {
        private Mode  _Mode;

        public BtChangeModeOnClickListener(Mode mode)
        {
            _Mode = mode;
        }

        @Override
        public void onClick(View v)
        {
            setWindow(_Mode, 0);
        }
    }


    private class BtAnswerOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent();
            intent.setClassName("kenta.android.apps.eshiritori",
                    "kenta.android.apps.eshiritori.MainActivity");
            startActivity(intent);
            finish();
        }
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

    private void clearDB()
    {
        DBHelper helper = new DBHelper(this);
        helper.clear();
        helper.close();
        super.onDestroy();
    }
}

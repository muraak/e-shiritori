package kenta.android.apps.eshiritori;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setWindow(false, 0);
    }

    private void setWindow(boolean isCheckMode, int page_number)
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
            LinearLayout lltmp = new LinearLayout(this);
            lltmp.setOrientation(LinearLayout.HORIZONTAL);
            lltmp.setGravity(Gravity.CENTER_HORIZONTAL);
            lltmp.setPadding(0, 20, 0, 0);

            Button btAnswer = new Button(this);
            Button btResult = new Button(this);
            setButtonOfAnswerWindow(btAnswer, new BtAnswerOnClickListener(), "絵を描く", lltmp, 2);
            setButtonOfAnswerWindow(btResult, new BtResultOnClickListener(), "答え合わせ", lltmp, 2);

            ll.addView(lltmp);

            return;
        }

        ArrayList<Integer> ids = new ArrayList<>(5);
        ArrayList<Bitmap> pictures = new ArrayList<>(5);

        if(!isCheckMode)
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

            if(isCheckMode)
            {
                TextView tvTmp = new TextView(this);
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

        LinearLayout lltmp = new LinearLayout(this);
        lltmp.setOrientation(LinearLayout.HORIZONTAL);
        lltmp.setGravity(Gravity.CENTER_HORIZONTAL);
        lltmp.setPadding(0, 20, 0, 0);

        if(!isLastPage)
        {
            Button btOlder = new Button(this);
            setButtonOfAnswerWindow(btOlder,
                    new BtOlderOnClickListener(isCheckMode, page_number), "←", lltmp, 1);
        }

        Button btAnswer = new Button(this);
        Button btResult = new Button(this);
        setButtonOfAnswerWindow(btAnswer, new BtAnswerOnClickListener(), "絵を描く", lltmp, 2);
        setButtonOfAnswerWindow(btResult, new BtResultOnClickListener(), "答え合わせ", lltmp, 2);

        //全ページ数をデータベースのレコード数から算出 ToDo: もっと簡単な算出方法を検討する
        int numberOfPages = helper.getRecordCount() / 5;
        if(helper.getRecordCount() % 5 != 0)
            numberOfPages++;
        numberOfPages--;

        helper.close();

        //  □□□…□□□
        //        ↑のページかどうか調べる → そうだったらbtNewerは表示しない
        if(!isCheckMode && page_number > 0 || isCheckMode && page_number < numberOfPages)
        {
            Button btNewer = new Button(this);
            setButtonOfAnswerWindow(btNewer,
                    new BtNewerOnClickListener(isCheckMode, page_number), "→", lltmp, 1);
        }

        ll.addView(lltmp);
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
        private boolean mCheckMode;

        BtOlderOnClickListener(boolean isCheckMode, int page_number)
        {
            mCheckMode = isCheckMode;
            mPageNumber = page_number;
        }

        @Override
        public void onClick(View v)
        {
            if(!mCheckMode)
                mPageNumber++;
            else
                mPageNumber--;
            setWindow(mCheckMode, mPageNumber);
        }
    }

    private class BtNewerOnClickListener implements View.OnClickListener
    {
        private int mPageNumber;
        private boolean mCheckMode;

        BtNewerOnClickListener(boolean isCheckMode, int page_number)
        {
            mCheckMode = isCheckMode;
            mPageNumber = page_number;
        }

        @Override
        public void onClick(View v)
        {
            if(!mCheckMode)
                mPageNumber--;
            else
                mPageNumber++;
            setWindow(mCheckMode, mPageNumber);
        }
    }

    private class BtResultOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            setWindow(/* チェックモードで実行 */true, 0);
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

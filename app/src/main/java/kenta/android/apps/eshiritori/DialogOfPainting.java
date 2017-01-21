package kenta.android.apps.eshiritori;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kenta on 2015/05/02.
 */
public class DialogOfPainting extends DialogFragment
{

    public DialogOfPainting(){}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = new Dialog(getActivity());
        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        // レイアウト指定
        dialog.setContentView(R.layout.dialog_of_painting);
        // 背景を透明にする
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // TODO: ボタンにイベント処理を割り当てる
        dialog.findViewById(R.id.ButtonCancelOfDialogDone)
                .setOnClickListener(new BtCancelOfDialogListener(dialog));
        // getSerializable("PaintView")によってMainActivityで
        // このクラスがnewされた後に保存された(渡された)PaintViewオブジェクト
        // を取り出しています.
        dialog.findViewById(R.id.ButtonOKOfDialogDone)
                .setOnClickListener(new BtOKOfDialogListener(dialog,
                        (PaintView) getArguments().getSerializable("PaintView")));

        return dialog;
    }

    private class BtOKOfDialogListener implements View.OnClickListener
    {
        Dialog    mDialog;
        PaintView mPaintView;

        BtOKOfDialogListener(Dialog dialog, PaintView paintView)
        {
            mDialog    = dialog;
            mPaintView = paintView;
        }

        @Override
        public void onClick(View v)
        {
            EditText editTitle = (EditText) mDialog.findViewById(R.id.editTitle);
            String title	= editTitle.getText().toString();

            // タイトルが入力されているかをチェック
            if(title.length() == 0)
            {
                makeCustomToast("何か入力してね!",
                        getActivity().getApplicationContext());
//                Log.d("KMShiritori", "Title was not inputed!");
                return;
            }

            //入力文字列がひらがな or "ー" or 全角カタカナ か調べるための準備
            String regfilter = "^[ぁ-んーァ-ヶ]+$";
            Pattern p        = Pattern.compile(regfilter);
            Matcher m        = p.matcher(title);

            // タイトルがひらがな or "ー" or 全角カタカナ であるかチェック
            if(!m.find())
            {
                makeCustomToast("タイトルは「ひらがな」か\n「カタカナ」で入力してね!",
                        getActivity().getApplicationContext());
//                Log.d("KMShiritori", "Title was not HIRAGANA!");
                return;
            }

            mDialog.dismiss();
//            Log.d("KMShiritori", "Title was accepted!");

            // 絵をDBに格納(キー(連番)，タイトル，画像からなる)
            DBHelper helper = new DBHelper(getActivity().getApplicationContext());
            helper.insertData(title, mPaintView.getCurrentCanvasBitmap());
            helper.close();

            // 絵一覧を表示する画面に遷移
            Intent intent = new Intent();
            intent.setClassName("kenta.android.apps.eshiritori",
                    "kenta.android.apps.eshiritori.AnswerActivity");
            startActivity(intent);
            getActivity().finish();
        }
    }

    private class BtCancelOfDialogListener implements View.OnClickListener
    {
        private Dialog mDialog;

        BtCancelOfDialogListener(Dialog dialog)
        {
            mDialog = dialog;
        }

        @Override
        public void onClick(View v)
        {
            mDialog.dismiss();
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
}

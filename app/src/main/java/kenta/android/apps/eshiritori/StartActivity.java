package kenta.android.apps.eshiritori;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_start);
        super.onCreate(savedInstanceState);

        /* Start Button */
        Button btStart = (Button) this.findViewById(R.id.btStart);
        btStart.setOnClickListener(new StartActivity.BtStartOnClickListener());

        /* Continue Button */
        Button btContinue = (Button) this.findViewById(R.id.btContinue);

        DBHelper helper = new DBHelper(this);
        int count = helper.getRecordCount();
        helper.close();

        if(count > 0)
        {
            btContinue.setOnClickListener(new StartActivity.BtContinueOnClickListener());
        }
        else
        {
            btContinue.setVisibility(View.INVISIBLE);
        }
    }

    private class BtStartOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            /* Clear all pictures on DB. */
            DBHelper helper = new DBHelper(getBaseContext());
            helper.clear();
            helper.close();

            /* Move to painting screen. */
            Intent intent = new Intent();
            intent.setClassName("kenta.android.apps.eshiritori",
                    "kenta.android.apps.eshiritori.MainActivity");
            startActivity(intent);
            finish();
        }
    }

    private class BtContinueOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent();
            intent.setClassName("kenta.android.apps.eshiritori",
                    "kenta.android.apps.eshiritori.AnswerActivity");
            startActivity(intent);
            finish();
        }
    }
}

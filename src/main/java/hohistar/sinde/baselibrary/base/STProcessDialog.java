package hohistar.sinde.baselibrary.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import hohistar.sinde.baselibrary.R;

/**
 * Created by sinde on 15/12/7.
 */
public class STProcessDialog extends Dialog {

    private TextView mTextTV;

    public STProcessDialog(Context context) {
        super(context, R.style.STDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_stprocess);
        mTextTV = (TextView)findViewById(R.id.layout_stprocessTV);
    }

    public void setMessage(final String msg){
        if (mTextTV != null) mTextTV.setText(msg);
        else mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTextTV != null) mTextTV.setText(msg);
            }
        },100);
    }

    private Handler mHandler = new Handler();

}

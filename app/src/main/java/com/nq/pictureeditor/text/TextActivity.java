package com.nq.pictureeditor.text;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nq.pictureeditor.R;

public class TextActivity extends Activity implements View.OnClickListener {

    private Button mCancel;
    private Button mSelect;
    private EditText mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text);

        mCancel = findViewById(R.id.cancel_text);
        mSelect = findViewById(R.id.select_text);
        mText = findViewById(R.id.text);

        mCancel.setOnClickListener(this);
        mSelect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_text:
                setResult(0);
                break;
            case R.id.select_text:
                Intent intent = new Intent();
                intent.putExtra("text", mText.getText().toString());
                setResult(1, intent);
                break;
        }

        finish();
    }
}

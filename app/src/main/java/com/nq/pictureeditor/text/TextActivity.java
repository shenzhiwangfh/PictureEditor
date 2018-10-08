package com.nq.pictureeditor.text;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.mode.TextMode;

public class TextActivity extends Activity implements View.OnClickListener {

    private Button mCancel;
    private Button mSelect;
    private EditText mText;

    private int x, y;
    private String text;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text);

        Intent intent = getIntent();
        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);
        text = intent.getStringExtra("text");
        index = intent.getIntExtra("index", -1);

        mCancel = findViewById(R.id.cancel_text);
        mSelect = findViewById(R.id.select_text);
        mText = findViewById(R.id.text);

        mCancel.setOnClickListener(this);
        mSelect.setOnClickListener(this);

        if(text != null && !text.isEmpty()) mText.setText(text);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_text:
                setResult(TextMode.RESULT_CANCEL);
                break;
            case R.id.select_text:
                Intent intent = new Intent();
                if(x != 0) intent.putExtra("x", x);
                if(y != 0) intent.putExtra("y", y);
                intent.putExtra("text", mText.getText().toString());
                intent.putExtra("index", index);
                setResult(TextMode.RESULT_SELECT, intent);
                break;
        }

        finish();
    }
}

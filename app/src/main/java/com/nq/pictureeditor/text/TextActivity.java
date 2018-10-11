package com.nq.pictureeditor.text;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.nq.pictureeditor.R;
import com.nq.pictureeditor.mode.TextMode;
import com.nq.pictureeditor.view.ArcColorPicker;
import com.nq.pictureeditor.view.ArcSeekBar;

public class TextActivity extends Activity implements View.OnClickListener,
        ArcColorPicker.OnPickListener,
        ArcSeekBar.OnSlideListener {

    private Button mCancel;
    private Button mSelect;
    private Button mDelete;
    private EditText mText;
    private ArcColorPicker mTextColorPicker;
    private ArcSeekBar mTextSizePicker;

    private int x, y;
    private String text;
    private int index;
    private int color;
    private int size;

    private float zoom = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text);

        Intent intent = getIntent();
        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);
        text = intent.getStringExtra("text");
        index = intent.getIntExtra("index", -1);
        color = intent.getIntExtra("color", TextMode.DEFAULT_COLOR);
        size = intent.getIntExtra("size", TextMode.DEFAULT_SIZE);
        zoom = intent.getFloatExtra("zoom", 1.0f);

        mCancel = findViewById(R.id.cancel_text);
        mSelect = findViewById(R.id.select_text);
        mDelete = findViewById(R.id.delete_text);
        mText = findViewById(R.id.text);
        mTextColorPicker = findViewById(R.id.text_color);
        mTextSizePicker = findViewById(R.id.text_size);

        mCancel.setOnClickListener(this);
        mSelect.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mTextColorPicker.setOnPickListener(this);
        mTextSizePicker.setOnSlideListener(this);

        mTextColorPicker.setColor(color);
        mTextSizePicker.setSize(size);
        if (text != null && !text.isEmpty()) mText.setText(text);
        mDelete.setEnabled(index != -1);
        mDelete.setTextColor(index != -1 ? Color.RED : Color.DKGRAY);

        mText.setTextColor(color);
        mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size * zoom);
        showSoftInputFromWindow(this, mText);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_text:
                setResult(TextMode.RESULT_CANCEL);
                finish();
                break;
            case R.id.select_text:
                Intent intent = new Intent();
                if (x != 0) intent.putExtra("x", x);
                if (y != 0) intent.putExtra("y", y);
                intent.putExtra("text", mText.getText().toString());
                intent.putExtra("index", index);
                intent.putExtra("color", color);
                intent.putExtra("size", size);
                setResult(TextMode.RESULT_SELECT, intent);
                finish();
                break;
            case R.id.delete_text:
                showDeleteDailog();
                break;
        }
    }

    public void showSoftInputFromWindow(Activity activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onPick(View view, int color) {
        int id = view.getId();
        if (id == R.id.text_color) {
            this.color = color;
            mText.setTextColor(color);
        }
    }

    @Override
    public void onSlide(View view, int size) {
        int id = view.getId();
        if (id == R.id.text_size) {
            this.size = size;
            mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size * zoom);
        }
    }

    private void showDeleteDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_warning);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.delete_tips);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.putExtra("index", index);
                setResult(TextMode.RESULT_DELETE, intent);
                TextActivity.this.finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}

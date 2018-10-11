package com.nq.pictureeditor.record;

import android.util.Log;
import android.util.SparseArray;
import android.widget.EditText;

import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.EditMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordManager {

    private final static String TAG = "DrawView";

    private List<EditMode> records = new ArrayList<>();
    private int index = -1;
    private boolean changed = false;

    private static RecordManager mInstance;

    public RecordManager() {

    }

    public static RecordManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecordManager();
        }
        return mInstance;
    }

    public void clear() {
        records.clear();
    }

    public void addRecord(EditMode record, boolean force) {
        //if(!force || !changed) return;

        switch (record.getMode()) {
            case EditMode.MODE_CLIP:
                if(records.size() > 0) {
                    EditMode mode = getCurrentMode();
                    if (mode instanceof ClipMode) {
                        ClipMode clipMode1 = (ClipMode) mode;
                        ClipMode clipMode2 = (ClipMode) record;
                        boolean same = clipMode1.pictureRect.equals(clipMode2.pictureRect) &&
                                clipMode1.clipPictureRect.equals(clipMode2.clipPictureRect);
                        if (same) break;
                    }
                }
                add(record);
                break;
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
            case EditMode.MODE_TEXT:
                add(record);
                break;
        }
    }

    private void add(EditMode mode) {
        while (records.size() > (index + 1)) {
            records.remove(index + 1);
        } //remove end records

        if (mode.index != -1) {
            records.remove(mode.index);
            for (int i = 0; i < index; i++) {
                EditMode record = records.get(i);
                record.index = i;
            }
        }
        mode.index = index = records.size();
        records.add(mode);
    }

    public void remove(int removeIndex) {
        if(removeIndex >= 0 && removeIndex <= index) {
            records.remove(removeIndex);
            index--;

            for (int i = 0; i < index; i++) {
                EditMode record = records.get(i);
                record.index = i;
            }
        }
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int size() {
        return records.size();
    }

    public EditMode getCurrentMode() {
        return records.get(index);
    }

    public EditMode getPreMode() {
        return records.get(index - 1);
    }

    public EditMode getNextMode() {
        return records.get(index + 1);
    }

    public List<EditMode> getRecords() {
        return records;
    }

    public boolean canBack() {
        return index > 0;
    }

    public boolean canForward() {
        return index < (records.size() - 1);
    }

    public EditMode back() {
        index--;
        return records.get(index);
    }

    public EditMode forward() {
        index++;
        return records.get(index);
    }

    public boolean doLoop(ModeLoopInterface modeLoop) {
        for (int i = 0; i <= index; i++) {
            EditMode mode = records.get(i);
            if (modeLoop.pickMode(mode)) return false; //if break, doLoop return false;
        }
        return true;
    }
}

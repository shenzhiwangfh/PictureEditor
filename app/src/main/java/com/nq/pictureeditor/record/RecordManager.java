package com.nq.pictureeditor.record;

import android.util.SparseArray;
import android.widget.EditText;

import com.nq.pictureeditor.mode.ClipMode;
import com.nq.pictureeditor.mode.EditMode;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordManager {

    private final static String TAG = "DrawView";

    private SparseArray<EditMode> records = new SparseArray<>();
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

    public void addRecord(EditMode record, boolean force) {
        //if(!force || !changed) return;

        switch (record.getMode()) {
            case EditMode.MODE_CLIP:
                /*
                if (records.isEmpty()) {
                    ClipMode nowClipMode = (ClipMode) record;
                    //nowClipMode.status = 0;
                    add(nowClipMode);
                } else {
                    EditMode editMode = getCurrentMode();
                    if(editMode instanceof ClipMode) {
                        ClipMode nowClipMode = (ClipMode) mode;
                        ClipMode lastClipMode = (ClipMode) editMode;

                        if (nowClipMode.pictureRect.equals(lastClipMode.pictureRect) &&
                                nowClipMode.clipPictureRect.equals(lastClipMode.clipPictureRect)) {
                            //
                        } else {
                            if(lastClipMode.status) {
                                //nowClipMode.status = lastClipMode.status;
                                //replace(nowClipMode);
                                add(nowClipMode);
                            } else {
                                //nowClipMode.status = lastClipMode.status + 1;
                                //add(nowClipMode);
                                replace(nowClipMode);
                            }
                        }
                    } else {
                        ClipMode nowClipMode = (ClipMode) mode;
                        //nowClipMode.status = 0;
                        add(nowClipMode);
                    }
                }
                */
                break;
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
            case EditMode.MODE_TEXT:
                add(record);
                break;
        }
    }

    private void add(EditMode mode) {
        if(mode.index != -1) {
            records.remove(mode.index);
        }
        mode.index = ++index;
        records.put(mode.index, mode);
    }

    private void replace(EditMode mode) {
        records.put(mode.index, mode);

        //for (int i = index; i < records.size(); i++) {
        //    records.remove(i);
        //}
    }

    /*
    private ClipMode getLastClipMode() {
        ClipMode clipMode = null;
        for (int i = (records.size() - 1); i >= 0; i--) {
            EditMode mode = records.get(i);
            if (mode.getMode() == EditMode.MODE_CLIP) {
                clipMode = (ClipMode) mode;
            }
        }
        return clipMode;
    }
    */

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
        int i = index - 1;
        return records.get(i);
    }

    public EditMode getNextMode() {
        int i = index + 1;
        return records.get(i);
    }

    public SparseArray<EditMode> getRecords() {
        return records;
    }

    public void back() {
        index--;
    }

    public void forward() {
        index++;
    }

    public boolean doLoop(ModeLoopInterface modeLoop) {
        for (int i = 0; i <= index; i++) {
            EditMode mode = records.get(i);
            if (mode == null) continue;
            if (modeLoop.pickMode(mode)) return false; //if break, doLoop return false;
        }

        return true;
    }
}

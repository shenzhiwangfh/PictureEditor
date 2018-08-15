package com.nq.pictureeditor.mode;

import android.util.Log;

import java.util.ArrayList;

public class RecordAction {

    private final static String TAG = "DrawView";

    private ArrayList<EditMode> records = new ArrayList<>();
    private int index = -1;
    private boolean changed = false;

    public RecordAction() {

    }

    public void addRecord(EditMode mode, boolean force) {
        //if(!force || !changed) return;

        switch (mode.getMode()) {
            case EditMode.MODE_CLIP:
                if (records.isEmpty()) {
                    ClipMode nowClipMode = (ClipMode) mode;
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
                break;
            case EditMode.MODE_PEN:
            case EditMode.MODE_MOSAICS:
                add(mode);
                break;
        }
    }

    private void add(EditMode mode) {
        index++;
        for (int i = index; i < records.size(); i++) {
            records.remove(i);
        }

        Log.e(TAG, "index=" + index + ",getMode=" + mode.getMode());
        records.add(mode);
    }

    private void replace(EditMode mode) {
        //index++;
        for (int i = index; i < records.size(); i++) {
            records.remove(i);
        }

        Log.e(TAG, "index=" + index + ",getMode=" + mode.getMode());
        records.add(mode);
    }

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

    public ArrayList<EditMode> getRecords() {
        return records;
    }

    public void back() {
        index--;
    }

    public void forward() {
        index++;
    }

    public void doLoop(ModeLoopInterface modeLoop) {
        for (int i = 0; i <= index; i++) {
            EditMode mode = records.get(i);
            modeLoop.pickMode(mode);
        }
    }
}

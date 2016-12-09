package com.cosc341.bre.slidingpuzzle;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Bre on 2016-12-08.
 */

public class SquareImageView extends ImageView {
    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isBlankImage() {
        return blankImage;
    }

    public void setBlankImage(boolean blankImage) {
        this.blankImage = blankImage;
    }

    private int column;
    private int origColumn;
    private int row;
    private int origRow;
    private boolean blankImage;

    public SquareImageView(Context context, int row, int column) {
        super(context);
        this.row = row;
        this.origRow = row;
        this.column = column;
        this.origColumn = column;
        this.blankImage = false;
    }

    public int getOrigColumn() {
        return origColumn;
    }

    public int getOrigRow() {
        return origRow;
    }
}

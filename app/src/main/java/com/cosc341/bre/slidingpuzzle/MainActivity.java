package com.cosc341.bre.slidingpuzzle;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    GridLayout gridLayout;
    SquareImageView[][] gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = (GridLayout) findViewById(R.id.gridView);

        imageView = new ImageView(getApplicationContext());
        imageView.setImageResource(R.drawable.drwhosquare);
        imageView.setAdjustViewBounds(true);


        ViewTreeObserver vto = gridLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = gridLayout.getMeasuredWidth();
                int height = gridLayout.getMeasuredHeight();

                ArrayList<ArrayList<Bitmap>> slicedImages = sliceImage(4);
                addImagesToGridLayout(slicedImages, 4);
            }
        });

    }

    private void addImagesToGridLayout(ArrayList<ArrayList<Bitmap>> slicedImages, int squares) {
        int imageWidth = gridLayout.getMeasuredWidth() / squares;
        SquareImageView[][] gameState = new SquareImageView[squares][squares];

        for(int i = 0; i < squares; i++) {
            SquareImageView[] column = new SquareImageView[squares];
            for (int y = 0; y < squares; y++) {
                SquareImageView slicedView = new SquareImageView(getApplicationContext(), y, i);
                Drawable slicedDrawable = new BitmapDrawable(getResources(), slicedImages.get(i).get(y));
                slicedView.setMaxWidth(imageWidth);

                if ((i == squares - 1) && (y == squares - 1)) {
                    //set last square to blank image
                    slicedView.setMinimumWidth(imageWidth - 2);
                    slicedView.setMinimumHeight(imageWidth - 2);
                    slicedView.setBackgroundColor(Color.WHITE);
                    slicedView.setBlankImage(true);
                } else {
                    slicedView.setImageDrawable(slicedDrawable);
                }

                slicedView.setAdjustViewBounds(true);
                slicedView.setPadding(2, 2, 2, 2);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(y),
                        GridLayout.spec(i));

                slicedView.setOnClickListener(new OnClickSquare());
                gridLayout.addView(slicedView, params);

                column[y] = slicedView;
            }
            gameState[i] = column;
        }
        this.gameState = gameState;
    }

    private class OnClickSquare implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SquareImageView square = (SquareImageView) v;
            SquareImageView squareSwap = getAdjacentBlankSquare(square);

            if(squareSwap != null) {
                int row = square.getRow();
                int col = square.getColumn();

                square.setRow(squareSwap.getRow());
                square.setColumn(squareSwap.getColumn());
                gameState[squareSwap.getColumn()][squareSwap.getRow()] = square;

                squareSwap.setRow(row);
                squareSwap.setColumn(col);
                gameState[col][row] = squareSwap;

                gridLayout.removeView(squareSwap);
                gridLayout.removeView(square);
                gridLayout.addView(square, new GridLayout.LayoutParams(GridLayout.spec(square.getRow()), GridLayout.spec(square.getColumn())));
                gridLayout.addView(squareSwap, new GridLayout.LayoutParams(GridLayout.spec(squareSwap.getRow()), GridLayout.spec(squareSwap.getColumn())));
            }
        }
    }

    private SquareImageView getAdjacentBlankSquare(SquareImageView square) {
        int col = square.getColumn();
        int row = square.getRow();

        int colSize = gameState[col].length - 1;
        int rowSize = gameState.length - 1;

        if (col > 0 && gameState[col - 1][row].isBlankImage()) {
            return gameState[col - 1][row];
        } else if (col + 1 <= colSize && gameState[col + 1][row].isBlankImage()) {
            return gameState[col + 1][row];
        } else if (row > 0 && gameState[col][row - 1].isBlankImage()) {
            return gameState[col][row - 1];
        } else if (row + 1 <= rowSize && gameState[col][row + 1].isBlankImage()) {
            return gameState[col][row + 1];
        }

        return null;
    }

    private ArrayList<ArrayList<Bitmap>> sliceImage(int squares) {
        Bitmap bitmapImage = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        int sideSizeX = bitmapImage.getWidth() / squares;
        int sideSizeY = bitmapImage.getHeight() / squares;

        ArrayList<ArrayList<Bitmap>> slicedImages = new ArrayList<>();

        for(int i = 0; i < squares; i++) {
            ArrayList<Bitmap> column = new ArrayList<>();
            for (int y = 0; y < squares; y++) {
                int fromX = i * sideSizeX;
                int fromY = y * sideSizeY;
                int toX = sideSizeX;
                int toY = sideSizeY;
                Bitmap sliced = Bitmap.createBitmap(bitmapImage, fromX, fromY, toX, toY);
                column.add(sliced);
            }
            slicedImages.add(column);
        }

        return slicedImages;
    }
}

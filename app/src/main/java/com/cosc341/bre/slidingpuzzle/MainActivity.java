package com.cosc341.bre.slidingpuzzle;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    GridLayout gridLayout;
    SquareImageView[][] gameState;
    int moves;
    TextView movesTextView;
    TextView messageTextView;
    Button solveButton;
    Button newGameButton;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("GameState", this.gameState);
        outState.putSerializable("Moves", this.moves);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = (GridLayout) findViewById(R.id.gridView);

        imageView = new ImageView(getApplicationContext());
        imageView.setImageResource(R.drawable.drwhosquare);
        imageView.setAdjustViewBounds(true);

        movesTextView = (TextView) findViewById(R.id.movesNumberTextView);
        messageTextView = (TextView) findViewById(R.id.messageTextView);

        solveButton = (Button) findViewById(R.id.solvePuzzleButton);
        solveButton.setOnClickListener(new OnClickSolve());
        solveButton.setEnabled(false);

        newGameButton = (Button) findViewById(R.id.newPuzzleButton);
        newGameButton.setOnClickListener(new OnClickNewGame());

        if(savedInstanceState == null) {
            ViewTreeObserver vto = gridLayout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ArrayList<ArrayList<Bitmap>> slicedImages = sliceImage(4);
                    addImagesToGridLayout(slicedImages, 4);
                }
            });
        } else {
            final SquareImageView[][] oldGameState = (SquareImageView[][]) savedInstanceState.getSerializable("GameState");
            this.moves = (int) savedInstanceState.getSerializable("Moves");

            ViewTreeObserver vto = gridLayout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ArrayList<ArrayList<Bitmap>> slicedImages = sliceImage(4);
                    addImagesToGridLayout(slicedImages, 4);

                    initializeGridLayout(oldGameState);
                    movesTextView.setText(String.valueOf(moves));
                }
            });
        }
    }

    private void initializeGridLayout(SquareImageView[][] oldGameState) {
        SquareImageView[][] initializedGameState = new SquareImageView[oldGameState.length][oldGameState.length];

        for(int i = 0; i < gameState.length; i++){
            for(int j = 0; j < gameState.length; j++) {
                SquareImageView origLocation = getOriginal(i, j, oldGameState);

                initializedGameState[i][j] = gameState[origLocation.getColumn()][origLocation.getRow()];
                initializedGameState[i][j].setColumn(i);
                initializedGameState[i][j].setRow(j);

                gridLayout.removeView(initializedGameState[i][j]);
                gridLayout.addView(initializedGameState[i][j], new GridLayout.LayoutParams(GridLayout.spec(j), GridLayout.spec(i)));
            }
        }

        this.gameState = initializedGameState;
    }

    private SquareImageView getOriginal(int column, int row, SquareImageView[][] oldGameState) {
        for(int i = 0; i < gameState.length; i++){
            for(int j = 0; j < gameState.length; j++) {
                if (oldGameState[i][j].getOrigColumn() == column && oldGameState[i][j].getOrigRow() == row) {
                    return oldGameState[i][j];
                }
            }
        }

        return null;
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
                moves ++;
                swap(square, squareSwap);
                movesTextView.setText(String.valueOf(moves));
                messageTextView.setText(" ");
                checkWin();
            } else {
                messageTextView.setText("Illegal Move");
            }
        }
    }

    private class OnClickSolve implements View.OnClickListener {

        @Override
        public void onClick(View v) {
                solvePuzzle();
                moves = 0;
                movesTextView.setText(String.valueOf(moves));
                solveButton.setEnabled(false);
        }
    }

    private class OnClickNewGame implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            solvePuzzle();
            scrambleGame();
            moves = 0;
            movesTextView.setText(String.valueOf(moves));
            solveButton.setEnabled(true);
        }
    }

    private void scrambleGame() {
        while(anyTileIsAtOriginal()) {
            makeRandomMove();
        }
    }

    private boolean anyTileIsAtOriginal(){
        for(int i = 0; i < gameState.length; i++){
            for(int j = 0; j < gameState.length; j++){
                if(gameState[i][j].getColumn() == gameState[i][j].getOrigColumn() && gameState[i][j].getRow() == gameState[i][j].getOrigRow()){
                    return true;
                }
            }
        }
        return false;
    }

    private void makeRandomMove() {
        SquareImageView blankSquare = getBlankSquare();
        if (blankSquare != null) {
            ArrayList<SquareImageView> adjacentTiles = getAdjacentSquares(blankSquare);
            Random random = new Random();
            SquareImageView toSwap = adjacentTiles.get(random.nextInt(adjacentTiles.size()));
            swap(blankSquare, toSwap);
        }
    }

    private SquareImageView getBlankSquare() {
        for(int i = 0; i < gameState.length; i++) {
            for (int j = 0; j < gameState[i].length; j++){
                if(gameState[i][j].isBlankImage()){
                    return gameState[i][j];
                }
            }
        }
        return null;
    }


    private void solvePuzzle() {
        while(!gameIsWon()){
            swapToCorrectPositions();
        }
    }

    private void swapToCorrectPositions() {
        for(int i = 0; i < gameState.length; i++){
            for(int j = 0; j < gameState.length; j++){
                if(gameState[i][j].getColumn() != gameState[i][j].getOrigColumn() || gameState[i][j].getRow() != gameState[i][j].getOrigRow()){
                    int origColumn = gameState[i][j].getOrigColumn();
                    int origRow = gameState[i][j].getOrigRow();
                    swap(gameState[i][j], gameState[origColumn][origRow]);
                }
            }
        }
    }

    private void swap(SquareImageView square, SquareImageView squareSwap) {
        if (square.getRow() == squareSwap.getRow() && square.getColumn() == squareSwap.getColumn()) {
            //do nothing, same square
        } else {
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

    private void checkWin() {
        if (gameIsWon()) {
            messageTextView.setText(R.string.gameWon);
        } else {
            messageTextView.setText(" ");
        }
    }

    private boolean gameIsWon() {
        for(int i = 0; i < gameState.length; i++){
            for(int j = 0; j < gameState.length; j++){
                if(gameState[i][j].getColumn() != gameState[i][j].getOrigColumn() || gameState[i][j].getRow() != gameState[i][j].getOrigRow()){
                    return false;
                }
            }
        }

        return true;
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

    private ArrayList<SquareImageView> getAdjacentSquares(SquareImageView square) {
        int col = square.getColumn();
        int row = square.getRow();

        int colSize = gameState[col].length - 1;
        int rowSize = gameState.length - 1;

        ArrayList<SquareImageView> adjacentSquares = new ArrayList<>();

        if (col > 0) {
            adjacentSquares.add(gameState[col - 1][row]);
        }

        if (col + 1 <= colSize) {
            adjacentSquares.add(gameState[col + 1][row]);
        }

        if (row > 0) {
            adjacentSquares.add(gameState[col][row - 1]);
        }

        if (row + 1 <= rowSize) {
            adjacentSquares.add(gameState[col][row + 1]);
        }

        return adjacentSquares;
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

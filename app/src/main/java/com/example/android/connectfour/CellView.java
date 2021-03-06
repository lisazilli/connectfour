package com.example.android.connectfour;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class CellView extends View {

    int color;
    int row = 0;
    int column = 0;

    private RectF rect;
    private Paint paint;
    MainActivity activity;

    public CellView(MainActivity activity) {
        super(activity);
        this.activity = activity;
        color = new Integer(GameService.NONE);
    }

    public CellView(MainActivity activity, @Nullable AttributeSet attrs) {
        super(activity, attrs);
        this.activity = activity;
        color = new Integer(GameService.NONE);
    }

    public CellView(MainActivity activity, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(activity, attrs, defStyleAttr);
        this.activity = activity;
        color = new Integer(GameService.NONE);
    }

    public void setPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getRow(){
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    public boolean onTouchEvent( MotionEvent event ) {
        if ( event.getAction() != MotionEvent.ACTION_UP )
            return true;

        if (!GameService.getInstance().getGameInplay()){
            Toast.makeText(this.getContext(),
                    "Game is over, please restart the game",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        /*Toast.makeText(this.getContext(),
                String.format("Test Square[%d,%d]",row,column),
                Toast.LENGTH_LONG).show();*/
        if ( color != GameService.NONE || !GameService.getInstance().isAvailable(row,column)) {
            Toast.makeText(this.getContext(),
                    "Square not available, please select an available square.",
                    Toast.LENGTH_LONG).show();
            return true;
        }

        color = new Integer(GameService.getInstance().getColor());
        invalidate();

        activity.progressDialog = ProgressDialog.show(this.getContext(), "Status Check",
                "Checking game status", true, false);
        if (GameService.getInstance().isWon(color)) {
            Toast.makeText(this.getContext(),
                    String.format("%s Player wins!", GameService.getColorName(color)),
                    Toast.LENGTH_LONG).show();
            activity.progressDialog.dismiss();
        } else if (GameService.getInstance().isFull()) {
            Toast.makeText(this.getContext(),
                    String.format("Board is full. No winners."),
                    Toast.LENGTH_LONG).show();
            activity.progressDialog.dismiss();
        }
        if (activity.progressDialog.isShowing() ) {
            // Game still going...
            activity.progressDialog.dismiss();
            activity.progressDialog = ProgressDialog.show(this.getContext(), activity.getString(R.string.waiting_title),
                    activity.getString(R.string.waiting_body), true, false);
        }
        if (color == GameService.YELLOW){
            new WriteThread (activity, activity.p2pClientAddress, MainActivity.P2P_ClIENT_PORT,
                    MainActivity.DATA_MSG, String.format("%d:%d:%d",row,column,color)).start();
        } else if (color == GameService.RED){
            new WriteThread (activity, activity.p2pInfo.groupOwnerAddress.getHostAddress(), MainActivity.P2P_HOST_PORT,
                    MainActivity.DATA_MSG, String.format("%d:%d:%d",row,column,color)).start();
        }

        return true;
    }

    protected void onDraw(Canvas canvas) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(), paint);

        //Mark as yellow
        if (color == GameService.YELLOW){
            //Mark as empty
            paint.setColor(Color.YELLOW);
            rect = new RectF( 10, 10,getWidth() - 20, getHeight() - 20);
            canvas.drawOval(rect, paint);
        }
        //Mark as red
        else if (color == GameService.RED){
            paint.setColor(Color.RED);
            rect = new RectF( 10, 10,getWidth() - 20, getHeight() - 20);
            canvas.drawOval(rect, paint);
        }
        //Mark as empty
        else{
            paint.setColor(Color.WHITE);
            rect = new RectF( 10, 10,getWidth() - 20, getHeight() - 20);
            canvas.drawOval(rect, paint);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

}

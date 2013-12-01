package com.prince.reversi.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.prince.reversi.R;
import com.prince.reversi.bean.Point;
import com.prince.reversi.bean.Step;
import com.prince.reversi.service.ReversiBoard;

public class ReversiView extends View{
	private Context context;
	
	private Paint paint;
	
	private Bitmap boardBitmap;
	private Bitmap whiteChess;
	private Bitmap blackChess;
	private Bitmap canputChess;
	private Bitmap blackWhiteChess;
	
	private int marginLeft=21;
	private int marginTop=21;
	private int chessWhith=56;
	private int chessHeight=56;
	
	private int boardWhith;
	
	private Bitmap outCanvasBitmap;	
	private Bitmap realBitmap;		//正式绘制的图片
	private Canvas outCanvas;
	private Canvas realCanvas;
	
	private boolean hasMeasured = false;
	private int viewWidth;
	private int viewHeight;
	
	private Map<String,Integer> hasPutChessType;
	
	public boolean canOnclick=true;
	
	private List<ViewStateListenner> viewStateListennerList;
	public ReversiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
		initPreDrawListenner();
	}
	private void init(){
		viewStateListennerList = new ArrayList<ReversiView.ViewStateListenner>();
		hasPutChessType = new HashMap<String, Integer>();
		initResouce();
		initPaint();
	}

	private void initPaint(){
		paint = new Paint();
	}
	private void initResouce(){
		boardBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.board1);
		boardWhith = boardBitmap.getWidth();
		blackChess = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.blackchess);
		whiteChess = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.whitechess);
		canputChess = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.lastchess);
		blackWhiteChess = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.blackwhitechess);
	}
	
	private void initPreDrawListenner(){
		ViewTreeObserver vto = this.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){  
            public boolean onPreDraw(){
                if (hasMeasured == false){  
                	hasMeasured = true;
                	viewWidth = ReversiView.this.getWidth();
            		viewHeight = ReversiView.this.getHeight();
            		initBackPhoto();
            		excuteViewPrepared();
                }  
                return true;  
            }  
        });
	}
	private void initBackPhoto(){
		outCanvasBitmap = Bitmap.createBitmap(489,492, Config.ARGB_8888);
		realBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
		outCanvas = new Canvas();
		realCanvas = new Canvas();
		outCanvas.setBitmap(outCanvasBitmap);
		realCanvas.setBitmap(realBitmap);
	}
	@Override
    protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       if(outCanvasBitmap!=null){
    	   canvas.drawBitmap(outCanvasBitmap, null,new Rect(0, 0, viewWidth, viewWidth), paint);
       }
    }
	public void drawReversiViewByBoard(ReversiBoard board,int currentChess){
		drawBoard();//绘制棋盘
		drawChess(board);//绘制棋子
		drawCanput(board,currentChess);//绘制可以下子的位置
		drawLastChess(board);
		postInvalidate();
	}
	private void drawBoard(){
		outCanvas.drawBitmap(boardBitmap,0,0, paint);
	}
	private void drawChess(ReversiBoard board){
		final List<Point> changChessList = new ArrayList<Point>();
		int willChangType=-1;
		Step s = board.getLastStep();
		Point p = null;
		if(s!=null){
			p = s.getP();
		}
		for(int i=0;i<ReversiBoard.BOARD_LENGTH;i++){
			for(int j=0;j<ReversiBoard.BOARD_LENGTH;j++){
				int chess = board.getChessByPosition(i, j);
				Integer chessIn = hasPutChessType.get(i+"_"+j);
				if(chessIn!=null){
					if(chess!=chessIn){
						if(p!=null&&i==p.getY()&&j==p.getX()){
							
						}else{
							changChessList.add(new Point(j, i));
							willChangType=chess;
							chess=chessIn;
							synchronized (hasPutChessType) {
								hasPutChessType.put(i+"_"+j, willChangType);
							}
						}
					}
				}
				drawChess(chess, j, i);
			}
		}
		final int willChangTypeReal = willChangType;
		new Thread(new Runnable() {
			@Override
			public void run() {
				changeChess(changChessList,willChangTypeReal);
			}
		}).start();
	}
	private void changeChess(List<Point> changChessList,int willChangeType){
		int size = changChessList.size();
		boolean isLastChess = false;
		for(int i=0;i<size;i++){
			Point p = changChessList.get(i);
			//drawChess(willChangeType, p.getX(), p.getY());
			Log.e("changeChess", "替换"+p.getX()+"_"+p.getY()+"位置棋子");
			if(i==size-1){
				isLastChess = true;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			new ChangeChessThread(p.getX(), p.getY(), willChangeType,isLastChess).start();
		}
	}
	private void drawChess(int chess,int j,int i){
		if(chess==ReversiBoard.BLACK){
			drawChess(blackChess, j, i);
		}else if(chess==ReversiBoard.WHITE){
			drawChess(whiteChess, j, i);
		}
		synchronized (hasPutChessType) {
			hasPutChessType.put(i+"_"+j, chess);
		}
	}
	private void drawLastChess(ReversiBoard board){
		Step step = board.getLastStep();
		if(step==null)return;
		Point p =step.getP();
		int x = p.getX();
		int y = p.getY();
		drawChess(canputChess, x, y);
	}
	private void drawChess(Bitmap chess,int x,int y){
		synchronized (outCanvas) {
			outCanvas.drawBitmap(chess, marginLeft+chessWhith*x,marginTop+chessHeight*y, paint);
		}
	}
	private void drawCanput(ReversiBoard board,int currentChess){
		List<Point> canputList = board.getPutableList(currentChess);
		int size = canputList.size();
		for(int i=0;i<size;i++){
			Point p = canputList.get(i);
			int x = p.getX();
			int y = p.getY();
			drawChess(canputChess,x,y);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!hasMeasured)return super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_MOVE:
			return true;
		case MotionEvent.ACTION_UP:
			if(canOnclick){
				float upX = event.getX();
				float upY = event.getY();
				int x = (int)(upX*boardWhith/viewWidth-marginLeft)/chessWhith;
				int y = (int)(upY*boardWhith/viewWidth-marginTop)/chessHeight;
				excuteViewOnclick(x, y);
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private void excuteViewPrepared(){
		if(viewStateListennerList!=null){
			int size = viewStateListennerList.size();
			for(int i=0;i<size;i++){
				ViewStateListenner vl = viewStateListennerList.get(i);
				vl.onPrepared();
			}
		}
	}
	
	private void excuteLastChessDraw(int desChess){
		if(viewStateListennerList!=null){
			int size = viewStateListennerList.size();
			for(int i=0;i<size;i++){
				ViewStateListenner vl = viewStateListennerList.get(i);
				vl.onLastChessDraw(desChess);
			}
		}
	}
	
	private void excuteViewOnclick(int x,int y){
		canOnclick=false;
		if(viewStateListennerList!=null){
			int size = viewStateListennerList.size();
			for(int i=0;i<size;i++){
				ViewStateListenner vl = viewStateListennerList.get(i);
				vl.onBoardClick(x, y);
			}
		}
	}
	
	public void removeViewStateListenner(ViewStateListenner vl){
		if(viewStateListennerList!=null){
			viewStateListennerList.remove(vl);
		}
	}
	
	public void addViewStateListenner(ViewStateListenner vl){
		if(viewStateListennerList!=null){
			viewStateListennerList.add(vl);
		}
	}
	public interface ViewStateListenner{
		public void onPrepared();
		public void onBoardClick(int x,int y);
		public void onLastChessDraw(int deschess);
	}
	private class ChangeChessThread extends Thread{
		private int chessX;
		private int chessY;
		private int desChess;
		private boolean isLastChess;
		public ChangeChessThread(int x,int y,int chess,boolean isLastChess){
			chessX = x;
			chessY = y;
			desChess = chess;
			this.isLastChess = isLastChess;
		}
		public void run(){
			int left = marginLeft+chessWhith*chessX;
			int top = marginTop+chessHeight*chessY;
			int right = left+chessWhith;
			int bottom = top+chessHeight;
			synchronized(outCanvas){
				outCanvas.save();
				outCanvas.clipRect(new Rect(left, top, right, bottom));
				drawBoard();
				outCanvas.restore();
			}
			drawChess(blackWhiteChess, chessX, chessY);
			postInvalidate();
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			drawChess(desChess, chessX, chessY);
			postInvalidate();
			if(isLastChess){
				excuteLastChessDraw(desChess);
			}
		}
	}
}

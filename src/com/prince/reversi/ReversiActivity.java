package com.prince.reversi;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prince.reversi.bean.Point;
import com.prince.reversi.bean.Step;
import com.prince.reversi.service.ReversiAi;
import com.prince.reversi.service.ReversiBoard;
import com.prince.reversi.view.ReversiView;
import com.prince.reversi.view.ReversiView.ViewStateListenner;

public class ReversiActivity extends Activity {
	private ReversiView reversiView;
	private ReversiBoard board;
	private ReversiAi ai;
	private Handler handler;
	
	private LinearLayout gameBeginLinear;
	private LinearLayout gameMsgLinear;
	private TextView gameOverMsg;
	private TextView blackChessCount;
	private TextView whiteChessCount;
	private Button robotxian;
	private Button playerxian;
	private Button resetGameButton;
	private Button regretButton;
	
	private int selfchess=ReversiBoard.BLACK;
	private int robotChess = ReversiBoard.WHITE;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initBoard();
        initView();
        addListenner();
        initHandler();
    }
    private void initHandler(){
    	handler = new Handler() {  
	        public void handleMessage(Message message) {
	        	switch (message.arg1){
	        		case 1:
	        			reversiView.drawReversiViewByBoard(board,selfchess);
	        			break;
	        		case 2:
	        			String gamemsg = (String)message.obj;
	        			parseGameMsg(gamemsg);
	        			break;
	        		case 3:
	        			String msg = (String)message.obj;
	        			parseGameMsg(msg);
	        			break;
	        		case 4:
	        			String blackWhiteStr = (String)message.obj;
	        			String[] strs = blackWhiteStr.split("_");
	        			blackChessCount.setText(strs[0]);
	        	    	whiteChessCount.setText(strs[1]);
	        	}
	        }  
	    };
    }
    public void initView(){
    	reversiView = (ReversiView)findViewById(R.id.reversiView);
    	gameBeginLinear = (LinearLayout)findViewById(R.id.gameBeginLinear);
    	gameMsgLinear = (LinearLayout)findViewById(R.id.gameMsgLinear);
    	playerxian = (Button)findViewById(R.id.playerxian);
    	robotxian = (Button)findViewById(R.id.robotxian);
    	gameOverMsg = (TextView)findViewById(R.id.gameOverMsg);
    	blackChessCount = (TextView)findViewById(R.id.blackChessCount);
    	whiteChessCount = (TextView)findViewById(R.id.whiteChessCount);
    	resetGameButton = (Button)findViewById(R.id.resetGameButton);
    	regretButton = (Button)findViewById(R.id.regretButton);
    }
    private void addListenner(){	
    	reversiView.addViewStateListenner(new ViewStateListenner() {
			@Override
			public void onPrepared() {
				reversiView.drawReversiViewByBoard(board,ReversiBoard.BLACK);
			}
			@Override
			public void onBoardClick(int x, int y) {
				if(x>=ReversiBoard.BOARD_LENGTH||y>=ReversiBoard.BOARD_LENGTH){
					reversiView.canOnclick = true;
					return ;
				}
				boolean flag = board.putChessInPosition(y, x, selfchess);
				if(flag){
					reversiView.drawReversiViewByBoard(board,robotChess);
				}else{
					reversiView.canOnclick = true;
				}
			}
			@Override
			public void onLastChessDraw(int deschess) {
				parseChessCount();
				if(board.isGameOver()){
					sendMessage(3, "游戏结束");
					return;
				}
				if(deschess==selfchess){
					if(board.getPutableList(robotChess).size()!=0){
						sendMessage(2, "机器人思考中");
						aiCalcular();
					}else{
						sendMessage(2, "机器人无子可下，请您继续");
					}
				}else{
					if(board.getPutableList(selfchess).size()==0){
						sendMessage(2, "机器人思考中");
						aiCalcular();
					}
				}
			}
		});
    	robotxian.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selfchess=ReversiBoard.WHITE;
				robotChess = ReversiBoard.BLACK;
				aiCalcular();
				hideGameBeginLinear();
				showGameMsgLinear();
			}
		});
    	playerxian.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reversiView.canOnclick = true;
				sendMessage(2, "请落子..");
				hideGameBeginLinear();
				showGameMsgLinear();
			}
		});
    	resetGameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetGame();
			}
		});
    	regretButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				regretGame();
				reversiView.drawReversiViewByBoardNoMovie(board,selfchess);
			}
		});
    }
    private void resetGame(){
    	board.resetGame();
    	reversiView.drawReversiViewByBoardNoMovie(board,selfchess);
    	reversiView.canOnclick = false;
    	hideGameMsgLinear();
    	showGameBeginLinear();
    }
    private void regretGame(){
    	board.undo();
    	Step s=board.getLastStep();
    	if(s!=null){
    		int chessType = s.getChessType();
        	if(chessType!=robotChess){
        		regretGame();
        	}
    	}else{
    		if(selfchess==ReversiBoard.WHITE){
    			aiCalcular();
    		}
    	}
    }
    private void showGameBeginLinear(){
    	gameBeginLinear.setVisibility(View.VISIBLE);
    }
    private void hideGameBeginLinear(){
    	gameBeginLinear.setVisibility(View.INVISIBLE);
    }
    private void showGameMsgLinear(){
    	gameMsgLinear.setVisibility(View.VISIBLE);
    	parseGameMsg("");
    	parseChessCount();
    }
    private void hideGameMsgLinear(){
    	gameMsgLinear.setVisibility(View.INVISIBLE);
    }
    private void parseGameMsg(String gameMsg){
    	gameOverMsg.setText(gameMsg);
    }
    private void parseChessCount(){
    	int blackNum = board.getChessCount(ReversiBoard.BLACK);
    	int whiteNum = board.getChessCount(ReversiBoard.WHITE);
    	sendMessage(4, blackNum+"_"+whiteNum);
    }
    private void aiCalcular(){
    	int chessType =robotChess;
    	if(board.getPutableList(chessType).size()>0){
    		long timestart = new Date().getTime();
			Point p = ai.findBestStep(board, chessType);
			long timeend = new Date().getTime();
			if((timeend-timestart)<500){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			sendMessage(2, "该你了");
			int y=p.getY();
			int x=p.getX();
			boolean flag = board.putChessInPosition(y, x, chessType);
			if(flag){
				sendMessage(1);
			}
			if(board.getPutableList(selfchess).size()!=0){
				reversiView.canOnclick = true;
			}
		}
    }
    private void sendMessage(int caseindex){
    	Message message = new Message();
       	message.arg1=caseindex;
        handler.sendMessage(message);
    }
    private void sendMessage(int caseindex,Object obj){
    	Message message = handler.obtainMessage(0, obj);
       	message.arg1=caseindex;
        handler.sendMessage(message);
    }
    public void initBoard(){
    	board = new ReversiBoard();
    	board.resetGame();
    	ai = new ReversiAi();
    }
}
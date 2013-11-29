package com.prince.reversi.service;

import java.util.ArrayList;
import java.util.List;

import com.prince.reversi.bean.Point;
import com.prince.reversi.bean.Step;

public class ReversiBoard {
	public static final int BOARD_LENGTH=8;
	public static final int BLACK=1;
	public static final int WHITE=2;
	public static final int BLANK=0;
	private int[] boardPanel = new int[BOARD_LENGTH*BOARD_LENGTH];
	private List<Step> chessStepList;
	
	public ReversiBoard(){
		chessStepList = new ArrayList<Step>();
	}
	/**
	 * 获取第i行 第j列的棋子
	 * @param i
	 * @param j
	 * @return 
	 */
	public int getChessByPosition(int i,int j){
		int position = j+i*BOARD_LENGTH;
		return boardPanel[position];
	}
	public void setChessByPosition(int i,int j,int chessType){
		int position = j+i*BOARD_LENGTH;
		boardPanel[position]=chessType;
	}
	public void putInStep(Point p,int chessType,List<Point> plist){
		Step s = new Step(p, chessType,plist);
		chessStepList.add(s);
	}
	public Step popStep(){
		int size = chessStepList.size();
		Step s=null;
		if(size>0){
			s = chessStepList.remove(size-1);
		}
		return s;
	}
	/**
	 * 在第i行 第j列下子 chessType
	 * @param i
	 * @param j
	 * @param chessType
	 */
	public boolean putChessInPosition(int i,int j,int chessType){
		Point p = new Point(j, i);
		if(isCanPutChess(p, chessType)){
			setChessByPosition(i, j, chessType);
			List<Point> plist = changeChess(p,chessType);
			putInStep(p, chessType,plist);
			return true;
		}
		return false;
	}
	public boolean undo(){
		Step s = popStep();
		if(s!=null){
			Point p = s.getP();
			int x = p.getX();
			int y = p.getY();
			int nowChess = s.getChessType();
			List<Point> plist = s.getPlist();
			setChessByPosition(y, x, BLANK);
			int size = plist.size();
			for(int i=0;i<size;i++){
				Point pr = plist.get(i);
				undoByDir(p,pr,nowChess);
			}
		}
		return false;
	}
	private void undoByDir(Point p,Point pr,int chessType){
		int xbegin = p.getX();
		int ybegin = p.getY();
		int xend = pr.getX();
		int yend = pr.getY();
		int xdir = xend-xbegin;
		int ydir = yend-ybegin;
		xdir=(xdir==0?0:xdir/Math.abs(xdir));
		ydir=(ydir==0?0:ydir/Math.abs(ydir));
		int x = xbegin;
		int y = ybegin;
		int desChessType = chessType==BLACK?WHITE:BLACK;
		while(x>=0&&x<BOARD_LENGTH&&y>=0&&y<BOARD_LENGTH){
			x = x+xdir;
			y = y+ydir;
			if((x!=xend||y!=yend)){
				setChessByPosition(y, x, desChessType);
			}else{
				break;
			}
		}
	}
	public List<Point> getPutableList(int chessType){
		List<Point> pointList = new ArrayList<Point>();
		for(int i=0;i<BOARD_LENGTH;i++){
			for(int j=0;j<BOARD_LENGTH;j++){
				Point p = new Point(j, i);
				if(isCanPutChess(p, chessType)){
					pointList.add(p);
				}
			}
		}
		return pointList;
	}
	
	/**
	 * 检测当前位置是否可以放chessType
	 * @return
	 */
	private boolean isCanPutChess(int i,int j,int chessType){
		return isCanPutChess(new Point(j, i), chessType);
	}
	private boolean isCanPutChess(Point p,int chessType){
		int x = p.getX();
		int y = p.getY();
		if(getChessByPosition(y, x)!=BLANK)return false;
		boolean isCanput = isCanPutChessByDir(p, 0, 1, chessType)
						 ||isCanPutChessByDir(p, 0, -1, chessType)
						 ||isCanPutChessByDir(p, 1, 0, chessType)
						 ||isCanPutChessByDir(p, -1, 0, chessType)
						 ||isCanPutChessByDir(p, 1, 1, chessType)
						 ||isCanPutChessByDir(p, 1, -1, chessType)
						 ||isCanPutChessByDir(p, -1, -1, chessType)
						 ||isCanPutChessByDir(p, -1, 1, chessType);
		return isCanput;
	}
	/**
	 * 切换棋子
	 * @param p
	 * @param chessType
	 * @return
	 */
	private List<Point> changeChess(Point p,int chessType){
		List<Point> plist = new ArrayList<Point>();
		for(int i=-1;i<=1;i++){
			for(int j=-1;j<=1;j++){
				if(i==0&&j==0)continue;
				Point pr = changeChessByDir(p,i,j,chessType);
				if(pr!=null){
					plist.add(pr);
				}
			}
		}
		return plist;
	}
	private Point changeChessByDir(Point p,int x,int y,int chessType){
		if(isCanPutChessByDir(p, x, y, chessType)){
			int i=p.getY();
			int j=p.getX();
			while(true){
				i+=x;
				j+=y;
				if(i>=0&&i<BOARD_LENGTH&&j>=0&&j<BOARD_LENGTH){
					int chess = getChessByPosition(i, j);
					if(chess==BLANK)break;
					if(chess==chessType){
						return new Point(j, i);
					}
					setChessByPosition(i, j, chessType);
				}else{
					break;
				}
			}
		}
		return null;
	}
	
	public boolean isGameOver(){
		int winner = getWinner();
		if(winner==BLANK){
			return false;
		}
		return true;
	}
	public int getWinner(){
		List<Point> wplist = getPutableList(WHITE);
		if(wplist.size()==0){
			List<Point> bplist = getPutableList(BLACK);
			if(bplist.size()==0){
				int wnum=0;
				int bnum=0;
				for(int i=0;i<BOARD_LENGTH;i++){
					for(int j=0;j<BOARD_LENGTH;j++){
						int chess = getChessByPosition(i, j);
						if(chess==BLACK){
							bnum++;
						}else if(chess==WHITE){
							wnum++;
						}
					}
				}
				if(wnum>bnum){
					return WHITE;
				}else if(wnum<bnum){
					return BLACK;
				}else{
					return -100;
				}
			}
		}
		return BLANK;
	}
	/**
	 * 判断 在当前i,j位置下子chessType ，在 x y方向是否有子被消除 
	 * @param i
	 * @param j
	 * @param x
	 * @param y
	 * @param chessType
	 * @return
	 */
	private boolean isCanPutChessByDir(Point p,int x,int y,int chessType){
		boolean iscanPut = false;
		int dis=0;
		int i=p.getY();
		int j=p.getX();
		while(true){
			i+=x;
			j+=y;
			if(i>=0&&i<BOARD_LENGTH&&j>=0&&j<BOARD_LENGTH){
				int chess = getChessByPosition(i, j);
				if(chess==BLANK)break;
				if(chess==chessType){
					iscanPut = true;
					break;
				}
				dis++;
			}else{
				break;
			}
		}
		if(iscanPut&&dis>0)return true;
		return false;
	}
	
	public int getChessCount(){
		int size = boardPanel.length;
		int num=0;
		for(int i=0;i<size;i++){
			int chessIn = boardPanel[i];
			if(WHITE==chessIn||BLACK==chessIn){
				num++;
			}
		}
		return num;
	}
	
	public int getChessCount(int chess){
		int size = boardPanel.length;
		int num=0;
		for(int i=0;i<size;i++){
			int chessIn = boardPanel[i];
			if(chess==chessIn){
				num++;
			}
		}
		return num;
	}
	
	public int[] getData(){
		return boardPanel;
	}
	
	/**
	 * 重置游戏
	 */
	public void resetGame(){
		chessStepList.clear();
		for(int i=0;i<boardPanel.length;i++){
			boardPanel[i]=BLANK;
		}
		setChessByPosition(3, 3, WHITE);
		setChessByPosition(4, 4, WHITE);
		setChessByPosition(3, 4, BLACK);
		setChessByPosition(4, 3, BLACK);
	}
}

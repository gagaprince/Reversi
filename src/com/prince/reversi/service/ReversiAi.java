package com.prince.reversi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.util.Log;

import com.prince.reversi.bean.Point;
import com.prince.reversi.bean.ScorePoint;

public class ReversiAi {
	public static final int INFINITE = 999999;
	public static final int MIN_NODES = 10000;
	public static final int MIN_TICK = 1000;
	public static final int[] EVAL_SCORES = {500, -150, 30, 10, 10, 30, -150, 500, -150, -250, 0, 0, 0, 0, -250, -150, 30, 0, 1, 2, 2, 1, 0, 30, 10, 0, 2, 16, 16, 2, 0, 10, 10, 0, 2, 16, 16, 2, 0, 10, 30, 0, 1, 2, 2, 1, 0, 30, -150, -250, 0, 0, 0, 0, -250, -150, 500, -150, 30, 10, 10, 30, -150, 500};
	public static final int BONUS_SCORE = 30;
	public static final int LIBERTY_SCORE = 8;
	public static final int YU_CHESSCOUNT = 8;//终局遍历时剩余棋子数
	public static final int DEEP_LENGTH=4;//普通情况下的搜索深度
	private int boardLength;
	
	public ReversiAi(){
		boardLength = ReversiBoard.BOARD_LENGTH;
	}
	/**
	 * 
	 * @param board 棋盘
	 * @param chessType 当前要下子的玩家
	 * @return
	 */
	public Point findBestStep(ReversiBoard board,int chessType){
		List<Point> plist = board.getPutableList(chessType);	//获取可下子列表
		int allChessCount = board.getChessCount();	//获取黑白子 数量列表
		if (plist.size()<= 0) {
			return null;
		}
		if (allChessCount <= ((boardLength - 4) * (boardLength - 4))) {//走的步数少于16步//遍历走法 选择最接近内部的走法 任选一种
			Log.e("findBestStep","步数太少，可以随意选则可选位置下子");
			List<Point> canputList=new ArrayList<Point>();
			for (int i = 0; i < plist.size(); i++) {
				Point p = plist.get(i);
				int x = p.getX();
				int y = p.getY();
				if (x>=2&&x<= (boardLength-3)&&y>=2&&y<=(boardLength-3)){
					canputList.add(p);
				}
			}
			int size = canputList.size();
			if (size>0) {//如果不存在最接近走法 进行深度搜索
				int randomNum = new Random().nextInt(size);
				return canputList.get(randomNum);
			}
			Log.e("findBestStep","没有最近走法 采取探索走法");
		}
		Log.e("findBestStep", "当前共有棋子"+allChessCount);
		if (allChessCount>=boardLength*boardLength-YU_CHESSCOUNT) {//如果剩余18个子位置 进入终局遍历状态
			Log.e("findBestStep","开始终局搜索");
			int deeplength = boardLength*boardLength-allChessCount;
			Log.e("findBestStep","终局搜索深度："+deeplength);
			ScorePoint sp = deepSearch(board, chessType,chessType, deeplength, -INFINITE, INFINITE,false);
			Log.e("findBestStep","终局搜索结果,最佳点x="+sp.getP().getX()+",y="+sp.getP().getY());
			if (sp.getScore()!=(-INFINITE)) {
				return sp.getP();
			}
		}
		Log.e("findBestStep","开始普通深度搜索");
		int deeplength = DEEP_LENGTH;
		Log.e("findBestStep","普通深度搜索，深度："+deeplength);
		//开始深度搜索 棋盘状态 当前下子玩家 探索深度 
		ScorePoint sp = deepSearch(board, chessType,chessType, deeplength, -INFINITE, INFINITE,true); 
		Log.e("findBestStep","普通深度搜索结果,最佳点x="+sp.getP().getX()+",y="+sp.getP().getY());
		return sp.getP();
	}
	/**
	 * 当四角出现一方棋子时 处理得分
	 * @return
	 */
	private int[] culcularScoreInSijiao(int[] chessDataArray,int[] boardData,int[] scores,int nowChess){
		int meScore = scores[0];
		int youScore = scores[1];
		int index = chessDataArray[0];
		int chessType = boardData[index];
		if (chessType!=ReversiBoard.BLANK) {
			for (int i=1;i<=3;i++) {
				int chessInSlidType = boardData[chessDataArray[i]];
				if (chessInSlidType == ReversiBoard.BLANK) {
					continue;
				} else {
					if (chessInSlidType==nowChess) {
						meScore -= EVAL_SCORES[chessDataArray[i]];
					} else {
						youScore -= EVAL_SCORES[chessDataArray[i]];
					}
				}
			}
			int indexTemp = index;
			for (int i = 0; i<boardLength-2;i++){
				indexTemp+=chessDataArray[4];
				if (boardData[indexTemp]!= chessType) {
					break;
				}
				if (nowChess == chessType) {
					meScore += BONUS_SCORE;
				} else {
					youScore += BONUS_SCORE;
				}
			}
			indexTemp = index;
			for (int i=0;i<boardLength-2;i++) {
				indexTemp+=(chessDataArray[5]*boardLength);
				if (boardData[indexTemp]!= chessType) {
					break;
				}
				if (nowChess == chessType) {
					meScore += BONUS_SCORE;
				} else {
					youScore += BONUS_SCORE;
				}
			}
		}
		scores[0]=meScore;
		scores[1]=youScore;
		return scores;
	}
	
	//n棋盘 w当前玩家下完子后 计算出此时棋盘得分
	private int calcularScore(ReversiBoard board,int chessType){
		int meChessCount = 0,
		youChessCount = 0;
		int meScore = 0,youScore = 0;
		int[] boardData = board.getData();
		for (int i=0; i<boardLength; i++) {
			for (int j=0; j<boardLength;j++) {
				int index = j+i*boardLength;
				int chessIn = boardData[index];
				if (chessIn == ReversiBoard.BLANK) {
					continue;
				}
				int hitNum = 0;
				for (int r=-1;r<=1;r++) {
					for (int q=-1;q<=1;q++) {
						int ir= i + r;
						int jq= j + q;
						int dataIndex = jq+ir*boardLength;
						//即当前棋子的分数 如果旁边有空白棋子 则要减分 
						//如果是对方棋子 则减对方积分
						if (ir>=0&&ir<boardLength&&jq>=0&&jq<boardLength&&boardData[dataIndex]==ReversiBoard.BLANK) {
							++hitNum;
						}
					}
				}
				if (chessIn == chessType) {
					++meChessCount;
					meScore+= EVAL_SCORES[index];
					meScore-=hitNum*LIBERTY_SCORE;
				} else {
					++youChessCount;
					youScore+=EVAL_SCORES[index];
					youScore-=hitNum*LIBERTY_SCORE;
				}
			}
		}
		if (meChessCount == 0) {
			return -INFINITE;
		}
		if (youChessCount == 0) {
			return INFINITE;
		}
		if (meChessCount + youChessCount == boardLength*boardLength) {
			if (meChessCount>youChessCount) {
				return INFINITE;
			} else {
				if (youChessCount>meChessCount) {
					return -INFINITE;
				}
			}
		}
		int[] scores=culcularScoreInSijiao(new int[]{0, 1, 8, 9, 1, 1},boardData,new int[]{meScore,youScore},chessType);
		scores=culcularScoreInSijiao(new int[]{7, 6, 14, 15, -1, 1},boardData,scores,chessType);
		scores=culcularScoreInSijiao(new int[]{56, 48, 49, 57, 1, -1},boardData,scores,chessType);
		scores=culcularScoreInSijiao(new int[]{63, 54, 55, 62, -1, -1},boardData,scores,chessType);
		meScore = scores[0];
		youScore = scores[1];
		return (meScore-youScore);
	}
	/**
	 * 在p位置下子后 棋盘得分
	 * @return
	 */
	public int getScoreIf(ReversiBoard board,int chessType,int nowChess,Point p) {
		board.putChessInPosition(p.getY(), p.getX(), nowChess);
		int score = calcularScore(board, chessType);
		board.undo();
		return score;
	};
	
	public int getScoreWhenCanPass(ReversiBoard board, int chessType) {
		int whiteNum = board.getChessCount(ReversiBoard.WHITE);
		int blackNum = board.getChessCount(ReversiBoard.BLACK);
		int l = 0;
		if ( blackNum>whiteNum) {
			l = INFINITE;
		} else {
			if (whiteNum>blackNum) {
				l = -INFINITE;
			}
		}
		if (chessType ==ReversiBoard.WHITE) {
			l = -l;
		}
		return l;
};
	
	/**
	 * 
	 * @param board 棋盘
	 * @param chessType	机器下子方
	 * @param nowChessType 递归下子方
	 * @param deepLength
	 * @param meScore
	 * @param youScore
	 * @return
	 */
	private ScorePoint deepSearch(ReversiBoard board,int chessType,int nowChessType,int deepLength,int meScore,int youScore,boolean isSort){
		if (deepLength == 0) {//搜索深度为0 直接返回
			if(isSort){
				return new ScorePoint(getScoreWhenCanPass(board, chessType), null);
			}else{
				return new ScorePoint(calcularScore(board, chessType), null);
			}
		}
		final boolean isRobot = (nowChessType==chessType);	//判断是否是机器人玩家
		int score = isRobot ? ( - INFINITE - 1) : (INFINITE + 1);	//平分上下界
		List<Point> plist= board.getPutableList(nowChessType);		//获取当前棋盘可下子列表
		Point returnPoint = new Point(-1,-1);
		int psize = plist.size();
		if (psize>0){
			final Map<Point,Integer> scoreMap = new HashMap<Point,Integer>();
			if(isSort){
				for (int i=0; i<psize;i++){
					Point pTemp=plist.get(i);
					scoreMap.put(pTemp,getScoreIf(board, chessType,nowChessType, pTemp));	//获取如此下子的得分 
				}
				Collections.sort(plist,new Comparator<Point>(){
					@Override
					public int compare(Point lhs,Point rhs) {
						int lhscore = scoreMap.get(lhs);
						int rhscore = scoreMap.get(rhs);
						return isRobot?rhscore-lhscore:lhscore-rhscore;
					}
				});
			}
			if (deepLength == 1&&isSort) {//如果思考深度为1 返回u走法
				returnPoint =plist.get(0);
				score = scoreMap.get(returnPoint);
			} else {//如果思考深度不为1 则递归此方法
				for (int i=0;i<plist.size();i++) {
					Point p = plist.get(i);
					board.putChessInPosition(p.getY(), p.getX(),nowChessType);
					Log.e("deepSearch","普通深度搜索，深度："+(deepLength-1));
					ScorePoint scoreN_1=deepSearch(board, chessType, (nowChessType==ReversiBoard.WHITE)?ReversiBoard.BLACK:ReversiBoard.WHITE, deepLength-1, meScore,youScore,isSort);
					board.undo();
					Log.e("deepSearch","scoreN_1.getScore："+scoreN_1.getScore());
					if (isRobot) {
						if (scoreN_1.getScore()>score) {
							score = scoreN_1.getScore();
							returnPoint = p;
						}
						meScore = (meScore > score ? meScore: score);
						if (meScore >= youScore) {
							break;
						}
					} else {
						if (scoreN_1.getScore()<score) {
							score = scoreN_1.getScore();
							returnPoint = p;
						}
						youScore = (youScore < score ? youScore: score);
						if (meScore >= youScore) {
							break;
						}
					}
				}
			}
		} else {
			if (!board.isGameOver()) {
				ScorePoint scoreN=deepSearch(board, chessType,(nowChessType==ReversiBoard.WHITE)?ReversiBoard.BLACK:ReversiBoard.WHITE, deepLength, meScore, youScore,isSort);
				score = scoreN.getScore();
				returnPoint = null;
			} else {
				score = getScoreWhenCanPass(board, chessType);
				returnPoint = null;
			}
		}
		return new ScorePoint(score, returnPoint);
	}
	
	/*private ScorePoint deepSearchAll(ReversiBoard board,int chessType,int deepLength,int meScore,int youScore){
		return null;
	}*/
}

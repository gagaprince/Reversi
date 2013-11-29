package com.prince.reversi.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.prince.reversi.bean.Point;
import com.prince.reversi.bean.ScorePoint;

public abstract class AI {
	/*public static final int INFINITE = 999999;
	*//**
	 * 当前棋盘局势对 evalLabel方的评分
	 * @param board
	 * @param evalLabel
	 * @return
	 *//*
	protected abstract int calcularScore(Board board,int evalLabel);
	*//**
	 * 在p下了nowChessLabel 子后 棋盘局势对evalLabel放的评分
	 * 要处理棋盘的回溯
	 * @param board
	 * @param evalLabel
	 * @param nowChessLabel
	 * @param p
	 * @return
	 *//*
	protected abstract int getScoreIf(Board board,int evalLabel,int nowChessLabel,Point p);
	
	*//**
	 * 递归搜索算法 min max 算法
	 * 假设己方和对手都足够聪明  总是可以将对方的优势降到最低
	 * 所以己方会从对己方有利的局势中选最好的结局
	 * 而对方则会从选出的每个分支中最好的结局中 选择最差的结局
	 * @param board 棋盘类
	 * @param evalLabel	估值函数作用方 即主动决策的那一方  或者说是  博弈树中最顶层的决策者
	 * @param nowChessLabel  博弈树中当前做决策的一方  一般是己方和对手轮流进行
	 * @param deepLength	递归深度 或 博弈树层级
	 * 接下来两个参数 辅助剪枝作用  由于递归遍历博弈树时采用的是深度遍历方式
	 * @param minMaxScore   己方决策时 如果发现当前分支中最大值 即此值已经比上层现在的最小值大，则由于对手足够聪明 ，已经不会选择当前分支，故可以不进行当前分支的继续计算 
	 * @param maxMinScore   对方决策时 如果发现当前分支中最小值 即此值已经比上层现在的最大值小    则由于己方足够聪明，已经不会选择当前分支，故可以不进行当前分支的继续计算
	 * @return  返回是一个决策点  封装一个最优点 还有选择当前点的对当前决策者的最佳得分
	 *//*
	protected ScorePoint deepSearch(Board board,int evalLabel,int nowChessLabel,int deepLength,int minMaxScore,int maxMinScore){
		if (deepLength == 0) {//搜索深度为0 直接返回
			return new ScorePoint(calcularScore(board, evalLabel), null);
		}
		final boolean isRobot = (nowChessLabel==evalLabel);	//判断是否是机器人玩家
		int score = isRobot ? ( - INFINITE - 1) : (INFINITE + 1);	//平分上下界
		List<Point> plist= board.getPutableList(nowChessLabel);		//获取当前棋盘可下子列表
		Point returnPoint = new Point(-1,-1);	//最终返回的决策棋子位置
		int psize = plist.size();
		if (psize>0){
			final Map<Point,Integer> scoreMap = new HashMap<Point,Integer>();
			for (int i=0; i<psize;i++){
				Point pTemp=plist.get(i);
				scoreMap.put(pTemp,getScoreIf(board, evalLabel,nowChessLabel, pTemp));	//获取如此下子的得分 
			}
			Collections.sort(plist,new Comparator<Point>(){
				@Override
				public int compare(Point lhs,Point rhs) {
					int lhscore = scoreMap.get(lhs);
					int rhscore = scoreMap.get(rhs);
					return isRobot?rhscore-lhscore:lhscore-rhscore;
				}
			});
			if (deepLength == 1) {//如果思考深度为1 返回u走法
				returnPoint =plist.get(0);
				score = scoreMap.get(returnPoint);
			} else {//如果思考深度不为1 则递归此方法
				for (int i=0;i<plist.size();i++) {
					Point p = plist.get(i);
					board.putChessInPosition(p.getY(), p.getX(),nowChessType);
					Log.e("deepSearch","普通深度搜索，深度："+(deepLength-1));
					ScorePoint scoreN_1=deepSearch(board, evalLabel, (nowChessType==ReversiBoard.WHITE)?ReversiBoard.BLACK:ReversiBoard.WHITE, deepLength-1, minMaxScore,maxMinScore);
					board.undo();
					Log.e("deepSearch","scoreN_1.getScore："+scoreN_1.getScore());
					if (isRobot) {
						if (scoreN_1.getScore()>score) {
							score = scoreN_1.getScore();
							returnPoint = p;
						}
						minMaxScore = (minMaxScore > score ? minMaxScore: score);
						if (minMaxScore >= maxMinScore) {
							break;
						}
					} else {
						if (scoreN_1.getScore()<score) {
							score = scoreN_1.getScore();
							returnPoint = p;
						}
						maxMinScore = (maxMinScore < score ? maxMinScore: score);
						if (minMaxScore >= maxMinScore) {
							break;
						}
					}
				}
			}
		} else {
			if (!board.isGameOver()) {
				ScorePoint scoreN=deepSearch(board, evalLabel,(nowChessType==ReversiBoard.WHITE)?ReversiBoard.BLACK:ReversiBoard.WHITE, deepLength, meScore, youScore);
				score = scoreN.getScore();
				returnPoint = null;
			} else {
				score = getScoreWhenCanPass(board, evalLabel);
				returnPoint = null;
			}
		}
		return new ScorePoint(score, returnPoint);
	}*/
}

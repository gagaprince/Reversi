package com.prince.reversi.service;

import java.util.List;

import com.prince.reversi.bean.Point;

public abstract class Board {
	/**
	 * 获取可下子位置列表
	 * @param nowChessLabel
	 * @return
	 */
	protected abstract List<Point> getPutableList(int nowChessLabel);
	/**
	 * 回溯上一步操作
	 */
	protected abstract void undo();
	/**
	 * 在第i行 第j列 防止 chess
	 * @param i
	 * @param j
	 * @param chess
	 * @return
	 */
	protected abstract boolean putChessInPosition(int i,int j,int chess);
	
	protected abstract boolean isGameOver();
}

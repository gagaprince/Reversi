package com.prince.reversi.service;

import java.util.List;

import com.prince.reversi.bean.Point;

public abstract class Board {
	protected abstract List<Point> getPutableList(int nowChessLabel);
}

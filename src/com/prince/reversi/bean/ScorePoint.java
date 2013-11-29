package com.prince.reversi.bean;
/**
 * ´ò·Öµã
 * @author gaga
 *
 */
public class ScorePoint {
	private int score;
	private Point p;
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public Point getP() {
		return p;
	}
	public void setP(Point p) {
		this.p = p;
	}
	public ScorePoint(int score, Point p) {
		super();
		this.score = score;
		this.p = p;
	}
	
}

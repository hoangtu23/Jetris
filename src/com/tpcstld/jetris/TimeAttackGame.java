package com.tpcstld.jetris;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;

public class TimeAttackGame extends MainGame {

	public TimeAttackGame(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onTick() {
		countDown();
	}

	@Override
	public void printAuxText(Canvas canvas) {
		// Drawing aux text box
		canvas.drawText("Time:", auxInfoXStarting + mainFieldShiftX,
				auxInfoYStarting + mainFieldShiftY, paint);
		changePaintSettings("info");
		canvas.drawText(auxText, numSquaresX * squareSide, auxInfoYStarting
				+ mainFieldShiftY, paint);
		changePaintSettings("normal");
	}

	@Override
	public int getHighScore(SharedPreferences settings) {
		return settings.getInt(Constants.TIME_ATTACK_SCORE, 0);
	}

	@Override
	public void onShapeLocked() {
	}

	@Override
	public void updateHighScore() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		editHighScore(settings, Constants.TIME_ATTACK_SCORE);
	}

}

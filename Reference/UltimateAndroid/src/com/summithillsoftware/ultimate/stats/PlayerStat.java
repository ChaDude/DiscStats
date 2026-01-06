package com.summithillsoftware.ultimate.stats;

import java.util.Locale;

import android.annotation.SuppressLint;
import com.summithillsoftware.ultimate.model.Player;

public class PlayerStat {
	private Player player;
	private float floatValue;
	private int intValue;
	private StatNumericType type;
	
	public PlayerStat(Player player, float floatValue) {
		super();
		this.player = player;
		setFloatValue(floatValue);
	}
	
	public PlayerStat(Player player, int intValue) {
		super();
		this.player = player;
		this.intValue = intValue;
		setIntValue(intValue);
	}
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public float getFloatValue() {
		return floatValue;
	}
	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
		this.type = StatNumericType.FLOAT;
	}
	public int getIntValue() {
		return intValue;
	}
	public void setIntValue(int intValue) {
		this.intValue = intValue;
		this.type = StatNumericType.INTEGER;
	}
	public void incrIntValue() {
		setIntValue(intValue+1);
	}
	public void decrIntValue() {
		setIntValue(intValue-1);
	}
	public void incrFloatValue() {
		setFloatValue(floatValue+1);
	}
	public void decrFloatValue() {
		setFloatValue(floatValue-1);
	}	
	public float sortValue() {
		return type == StatNumericType.INTEGER ? (float)intValue : floatValue;
	}
	public StatNumericType getType() {
		return type;
	}
	public void setType(StatNumericType type) {
		this.type = type;
	}
	
	@SuppressLint("DefaultLocale")
	public String statAsString() {
		return isFloat() ? String.format(Locale.getDefault(), "%.1f", getFloatValue()) : Integer.toString(getIntValue());
	}
	
	public boolean isFloat() {
		return type == StatNumericType.FLOAT; 
	}

}

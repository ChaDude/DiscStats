package com.summithillsoftware.ultimate.ui.stats;

import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.stats.PlayerStat;
import com.summithillsoftware.ultimate.stats.PlayerStatisticsCalculator;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class StatsCalculationAsyncTask extends AsyncTask<Void, Void, List<PlayerStat>> {
	private int statsTypeId;
	private boolean isTournamentIncluded;
	private StatsActivity statsActivity;

	public StatsCalculationAsyncTask(int statsTypeId,
			boolean isTournamentIncluded, StatsActivity statsActivity) {
		super();
		this.statsTypeId = statsTypeId;
		this.isTournamentIncluded = isTournamentIncluded;
		this.statsActivity = statsActivity;
	}

	@Override
	protected List<PlayerStat> doInBackground(Void... paramArrayOfParams) {
		List<PlayerStat> stats = Collections.emptyList();

		try {
			if (statsTypeId == R.id.button_stattype_plus_minus) {
				stats = PlayerStatisticsCalculator.plusMinusCountPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_points_played) {
				stats = PlayerStatisticsCalculator.pointsPerPlayer(game(), true, true, isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_opoints_played) {
				stats = PlayerStatisticsCalculator.pointsPerPlayer(game(), true, false, isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_dpoints_played) {
				stats = PlayerStatisticsCalculator.pointsPerPlayer(game(), false, true, isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_goals) {
				stats = PlayerStatisticsCalculator.goalsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_assists) {
				stats = PlayerStatisticsCalculator.assistsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_callahans) {
				stats = PlayerStatisticsCalculator.callahansPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_throws) {
				stats = PlayerStatisticsCalculator.throwsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_drops) {
				stats = PlayerStatisticsCalculator.dropsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_throwaways) {
				stats = PlayerStatisticsCalculator.throwawaysPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_stalled) {
				stats = PlayerStatisticsCalculator.stallsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_penalties) {
				stats = PlayerStatisticsCalculator.miscPenaltiesPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_callahaned) {
				stats = PlayerStatisticsCalculator.callahanedPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_ds) {
				stats = PlayerStatisticsCalculator.dsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_pulls) {
				stats = PlayerStatisticsCalculator.pullsPerPlayer(game(), isTournamentIncluded);
			} else if (statsTypeId == R.id.button_stattype_pullobs) {
				stats = PlayerStatisticsCalculator.pullsObPerPlayer(game(), isTournamentIncluded);
			} 
			if (isTournamentIncluded) { // pause long enough for animation on UI finish
				try { Thread.sleep(500); } catch (InterruptedException e) { /* no-op */ }
			}
		} catch (Exception e) {
			UltimateLogger.logError( "Unable to generated stats for statsTypeId=" + statsTypeId, e);
			stats = Collections.emptyList();
		}
		return stats;
	}

	@Override
	protected void onPostExecute(List<PlayerStat> result) {
		statsActivity.updatePlayerStats(result);
	}

	private Game game() {
		return Game.current();
	}


}

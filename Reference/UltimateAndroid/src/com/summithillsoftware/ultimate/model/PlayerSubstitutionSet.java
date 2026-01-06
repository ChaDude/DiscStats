package com.summithillsoftware.ultimate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * This class is not persistent.  It is used to help the UI deal with substitutions as a set instead of individual subs
 */
public class PlayerSubstitutionSet {
	
	private List<PlayerSubstitution> substitutions = new ArrayList<PlayerSubstitution>();
	
	public static List<PlayerSubstitutionSet> createSubstitutionSets(List<PlayerSubstitution> substitutions) {
		if (substitutions.isEmpty()) {
			return Collections.emptyList();
		}
		List<PlayerSubstitution> sortedSubstitutions = new ArrayList<PlayerSubstitution>(substitutions);
		Collections.sort(sortedSubstitutions, PlayerSubstitution.TimestampAndNameComparator);
		List<PlayerSubstitutionSet> sets = new ArrayList<PlayerSubstitutionSet>();
		List<PlayerSubstitution> playerSubsInSet = new ArrayList<PlayerSubstitution>();
		int lastTimestamp = -1;
		for (PlayerSubstitution playerSubstitution : sortedSubstitutions) {
			if (playerSubstitution.getTimestamp() != lastTimestamp) {
				playerSubsInSet = new ArrayList<PlayerSubstitution>();
				sets.add(new PlayerSubstitutionSet(playerSubsInSet));
			}
			playerSubsInSet.add(playerSubstitution);
			lastTimestamp = playerSubstitution.getTimestamp();
		}
		return sets;
	}
	
	public PlayerSubstitutionSet(List<PlayerSubstitution> substitutions) {
		super();
		this.substitutions = substitutions;
	}

	public List<PlayerSubstitution> getSubstitutions() {
		return substitutions;
	}

	public void setSubstitutions(List<PlayerSubstitution> substitutions) {
		this.substitutions = substitutions;
	}
	
	public String playerNames(boolean isOut) {
		if (substitutions.isEmpty()) {
			return "";
		}
		String description = "";
		for (PlayerSubstitution playerSub : substitutions) {
			if (!description.isEmpty()) {
				description += ", ";
			}
			description = description + (isOut ? playerSub.getFromPlayer().getName() : playerSub.getToPlayer().getName());
		}
		return description;
	}

}

package com.summithillsoftware.ultimate.model;

import java.util.Comparator;

public class TeamDescription {
	private String teamId;	
	private String name;
	private String cloudId;
	
	public TeamDescription(String teamId, String name, String cloudId) {
		super();
		this.teamId = teamId;
		this.name = name;
		this.cloudId = cloudId;
	}

	public String getTeamId() {
		return teamId;
	}
	public String getName() {
		return name;
	}
	public String getCloudId() {
		return cloudId;
	}
	public boolean isCurrentTeam() {
		return Team.isCurrentTeam(teamId);
	}
	
	public boolean canBeSafelyRemove() {
		return isDefaultTeamName() && Game.numberOfGamesForTeam(getTeamId()) == 0;
	}
	
	public boolean isDefaultTeamName() {
		return getName() != null && getName().equalsIgnoreCase(Team.DEFAULT_TEAM_NAME);
	}
	
	public static Comparator<TeamDescription> TeamDescriptionListComparator = new Comparator<TeamDescription>() {
		public int compare(TeamDescription team1, TeamDescription team2) {
			return team1.getName().compareTo(team2.getName());
		}
	};
	
}

package fft_battleground.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BattleGroundTeam {
	RED(new String[] {"red", "friends"}, (short) 0, "red"),
	BLUE(new String[] {"blue", "crew"}, (short) 1, "blue"),
	GREEN(new String[] {"green", "dream"}, (short) 2, "green"),
	YELLOW(new String[] {"yellow", "fellows"}, (short) 3, "yellow"),
	WHITE(new String[] {"white", "delight"}, (short) 4, "white"),
	BLACK(new String[] {"black", "pack"}, (short) 5, "black"),
	PURPLE(new String[] {"purple", "pals"}, (short) 6, "purple"),
	BROWN(new String[] {"brown", "town"}, (short) 7, "brown"),
	CHAMPION(new String[] {"champ", "champion", "champs", "orange"}, (short) 8, "champion"),
	LEFT(new String[] {"left", "p1", "player1", "t1", "team1"}, (short) -2, "left"),
	RIGHT(new String[] {"right", "p2", "player2", "t2", "team2"}, (short) -3, "right"),
	RANDOM(new String[] {"random"}, (short) -4, "random"), 
	NONE(new String[] {"none"}, (short) -1, "none");
	
	private static Random random;
	
	private Set<String> teamNames;
	private short teamCode;
	private String properName;
	
	static {
		random = new Random();
	}
	
	BattleGroundTeam(String[] teamNames, short teamCode, String properName) {
		List<String> teamNamesList = Arrays.asList(teamNames);
		this.teamNames = new HashSet<String>();
		this.teamNames.addAll(teamNamesList);
		
		this.teamCode = teamCode;
		this.properName = properName;
	}
	
	@JsonCreator
	public static BattleGroundTeam parse(String teamName) {
		String cleanedTeamName = StringUtils.lowerCase(teamName);
		if(cleanedTeamName != null) {
			for(BattleGroundTeam team: BattleGroundTeam.values()) {
				if(team.teamNames.contains(cleanedTeamName)) {
					return team;
				}
			}
		}
		return null;
		
	}
	
	public static BattleGroundTeam parse(int teamCode) {
		short teamCodeShort = (short) teamCode;
		for(BattleGroundTeam team : BattleGroundTeam.values()) {
			if(team.getTeamCode() >= 0 && team.getTeamCode() == teamCodeShort) {
				return team;
			}
		}
		
		return null;
	}
	
	public static boolean isBattleGroundTeamname(String teamName) {
		boolean result = false;
		if(teamName != null) {
			for(BattleGroundTeam team: BattleGroundTeam.values()) {
				if(team.teamNames.contains(teamName)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	public static String getTeamName(BattleGroundTeam team) {
		if(team != null) {
			return team.getTeamName();
		} else {
			return null;
		}
	}
	
	public static String getRandomTeamName(BattleGroundTeam team) {
		if(team != null) {
			int size = team.teamNames.size();
			int nextChoice = random.nextInt(size);
			String result = null;
			Iterator<String> it = team.teamNames.iterator();
			for(int i = 0 ; i < nextChoice && it.hasNext(); i++) {
				result = it.next();
			}
			return result;
		} else {
			return null;
		}
	}
	
	public static List<BattleGroundTeam> coreTeams() {
		List<BattleGroundTeam> teams = Arrays.asList(new BattleGroundTeam[] { BattleGroundTeam.RED,
				BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW, BattleGroundTeam.WHITE,
				BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN });
		return teams;
	}
	
	public static List<BattleGroundTeam> coreAndChampionTeams() {
		List<BattleGroundTeam> teams = Arrays.asList(new BattleGroundTeam[] { BattleGroundTeam.RED,
				BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW, BattleGroundTeam.WHITE,
				BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN, BattleGroundTeam.CHAMPION });
		return teams;
	}
	
	public Set<String> getTeamNames() {
		return this.teamNames;
	}
	
	public short getTeamCode() {
		return this.teamCode;
	}
	
	public String getTeamName() {
		return (String) this.teamNames.toArray()[0];
	}
	
	@JsonValue
	public String getProperName() {
		return this.properName;
	}
	
}

package fft_battleground.tournament.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.match.model.TeamAttributes;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class Teams {
	@JsonProperty("black")
	private Team black;
	@JsonProperty("blue")
	private Team blue;
	@JsonProperty("brown")
	private Team brown;
	@JsonProperty("champion")
	private Team champion;
	@JsonProperty("green")
	private Team green;
	@JsonProperty("purple")
	private Team purple;
	@JsonProperty("red")
	private Team red;
	@JsonProperty("white")
	private Team white;
	@JsonProperty("yellow")
	private Team yellow;
	
	public Teams() {}
	
	public Team getTeamByBattleGroundTeam(BattleGroundTeam battleGroundTeam) {
		switch(battleGroundTeam) {
		case BLACK:
			return this.black;
		case BLUE:
			return this.blue;
		case BROWN:
			return this.brown;
		case CHAMPION:
			return this.champion;
		case GREEN:
			return this.green;
		case PURPLE:
			return this.purple;
		case RED:
			return this.red;
		case WHITE:
			return this.white;
		case YELLOW:
			return this.yellow;
		default:
			return null;
		}
	}
}

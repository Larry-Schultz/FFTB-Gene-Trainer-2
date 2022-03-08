package fft_battleground.dump.model;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TournamentTeamValues {
	private Integer black;
	private Integer blue;
	private Integer brown;
	private Integer champion;
	private Integer green;
	private Integer purple;
	private Integer red;
	private Integer white;
	private Integer yellow;
	
	public TournamentTeamValues(Map<BattleGroundTeam, Integer> teamValueMap) {
		teamValueMap.forEach((key, value) -> {
			switch(key) {
			case BLACK:
				this.black = value;
				break;
			case BLUE:
				this.blue = value;
				break;
			case BROWN:
				this.brown = value;
				break;
			case CHAMPION:
				this.champion = value;
				break;
			case GREEN:
				this.green = value;
				break;
			case PURPLE:
				this.purple = value;
				break;
			case RED:
				this.red = value;
				break;
			case WHITE:
				this.white = value;
				break;
			case YELLOW:
				this.yellow = value;
				break;
			default:
				break;
			}
		});
	}
	
	public Integer getTeamByBattleGroundTeam(BattleGroundTeam battleGroundTeam) {
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

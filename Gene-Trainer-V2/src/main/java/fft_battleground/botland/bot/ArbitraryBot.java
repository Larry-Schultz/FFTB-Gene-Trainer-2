package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.botland.util.BetType;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArbitraryBot extends SimulatedBetBot {
	private final Random random = new Random(9872349L);
	
	private static final String BET_AMOUNT_PARAMETER = "betAmount";
	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String CHOICE_PARAMETER = "choice";
	
	private Integer betAmount;
	private BetType betType;
	private BetChoice choice;
	
	protected BattleGroundTeam pickedTeam;
	
	public ArbitraryBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BET_AMOUNT_PARAMETER)) {
			this.betAmount = Integer.valueOf(map.get(BET_AMOUNT_PARAMETER).getValue());
		}
		if(map.containsKey(BET_TYPE_PARAMETER)) {
			this.betType = BetType.getBetType(map.get(BET_TYPE_PARAMETER).getValue());
		}
		if(map.containsKey(CHOICE_PARAMETER)) {
			this.choice = BetChoice.getChoiceFromString(map.get(CHOICE_PARAMETER).getValue());
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected Bet generateBetAmount(Match match, long gil) {
		Bet bet = null;
		BattleGroundTeam chosenTeam = this.getTeamByChoice(match, this.choice);
		if(this.betType == BetType.FLOOR || this.betType == BetType.ALLIN) {
			bet = new Bet(chosenTeam, this.betType);
		} else if(this.betAmount != null) {
			bet = new Bet(chosenTeam, this.betAmount);
		} else if(this.betType == BetType.PERCENTAGE) { 
			bet = new Bet(chosenTeam, this.betAmount, this.betType); //if type is percentage, use amount as the percent
		} else if(this.betType == BetType.RANDOM) {
			Integer randomValue = (int) (random.nextLong(Math.min(gil, BET_MAX))); //get a number between 0 and either the smaller between gil or max bet
			randomValue = (int) Math.min(randomValue, gil); //ensure randomValue is below current gil
			randomValue = (int) Math.max(randomValue, GIL_FLOOR); //ensure randomValue is above gil floor
			bet = new Bet(chosenTeam, randomValue);
		} else {
			bet = new Bet(chosenTeam, (int) GIL_FLOOR);
		}
		
		return bet;
	}
	
	BattleGroundTeam getTeamByChoice(Match match, BetChoice choice) {
		switch(choice) {
		case LEFT:
			return match.leftTeam().team();
		case RIGHT:
			return match.rightTeam().team();
		default:
			Integer nextIndex = this.random.nextInt(BetChoice.sideChoices.length);
			//log.info("next index is: {}", nextIndex);
			return this.getTeamByChoice(match, BetChoice.sideChoices[nextIndex]);
		}
	}

}

enum BetChoice {
	LEFT("left"),
	RIGHT("right"),
	RANDOM("random")
	;
	
	public static BetChoice[] sideChoices = new BetChoice[] {BetChoice.LEFT, BetChoice.RIGHT};
	
	private String str;
	
	private BetChoice(String str) {
		this.str = str;
	}
	
	public String getString() {
		return this.str;
	}
	
	public static BetChoice getChoiceFromString(String parameter) {
		BetChoice result = null;
		for(BetChoice choice : BetChoice.values()) {
			if(StringUtils.equalsIgnoreCase(choice.getString(), parameter)) {
				result = choice;
				break;
			}
		}
		
		return result;
	}
	
	
}

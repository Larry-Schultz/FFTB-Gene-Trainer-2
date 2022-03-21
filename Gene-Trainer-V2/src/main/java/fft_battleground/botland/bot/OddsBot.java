package fft_battleground.botland.bot;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.botland.util.BetType;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;

public class OddsBot extends SimulatedBetBot {

	protected BetType type;
	protected String betAmountExpression;
	
	public OddsBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
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
		BattleGroundTeam chosenTeam = null;
		Integer result = null;
		Integer leftSum = (int) match.bets().botEstimatedPot().leftOdds();
		Integer rightSum = (int) match.bets().botEstimatedPot().rightOdds();
		if(leftSum >= rightSum) {
			Float betAmountFloat =  this.bettingPercentage(leftSum, rightSum) * ((float) BET_MAX);
			result = Collections.min(List.of((int) gil, betAmountFloat.intValue(), (int) BET_MAX));
			chosenTeam = match.leftTeam().team();
		} else {
			Float betAmountFloat =  this.bettingPercentage(rightSum, leftSum) * (BET_MAX);
			result = Collections.min(List.of((int) gil, betAmountFloat.intValue(), (int) BET_MAX));
			chosenTeam = match.rightTeam().team();
		}

		Bet bet = new Bet(chosenTeam, result);
		
		return bet;
	}
	
	Float bettingPercentage(Integer thisTeamAmount, Integer otherTeamAmount) {
		Float odds = 1f;
		try {
			odds = ((float) thisTeamAmount)/((float) thisTeamAmount + (float) otherTeamAmount);
		} catch(ArithmeticException e) {
			odds = 1f;
		}
		
		return odds;
	}

}

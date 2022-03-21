package fft_battleground.botland.bot;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;

public class DataDrivenBot extends SimulatedBetBot {
	private static final String PLAYER_SCORE_EXPRESSION_PARAMETER = "playerScoreExpression";
	
	private String playerScoreExpression;
	
	public DataDrivenBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(PLAYER_SCORE_EXPRESSION_PARAMETER)) {
			this.playerScoreExpression = map.get(PLAYER_SCORE_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

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
		Pair<Float, Float> scoresBySide = new ImmutablePair<>(this.generateLeftScore(match.bets().leftTeamEstimatedBets()), this.generateRightScore(match.bets().rightTeamEstimatedBets()));
		BattleGroundTeam chosenTeam = null;
		if(scoresBySide.getLeft() >= scoresBySide.getRight()) {
			chosenTeam = match.leftTeam().team();
		} else {
			chosenTeam = match.rightTeam().team();
		}
		
		int betAmount = 0;
		if(chosenTeam == match.leftTeam().team()) {
			float betRatio = scoresBySide.getLeft() / (scoresBySide.getLeft() + scoresBySide.getRight());
			betAmount = (int) (betRatio * (gil));
		} else {
			float betRatio = scoresBySide.getRight() / (scoresBySide.getLeft() + scoresBySide.getRight());
			betAmount = (int) (betRatio * (gil));
		}
		
		
		Bet result = new Bet(chosenTeam, betAmount);
		return result;
	}
	
	protected Float generateLeftScore(EstimatedPlayerBet[] leftSideBets) {
		Float score = this.findScoreOfBets(leftSideBets);
		return score;
	}

	protected Float generateRightScore(EstimatedPlayerBet[] rightSideBets) {
		Float score = this.findScoreOfBets(rightSideBets);
		return score;
	}
	
	protected Float findScoreOfBets(EstimatedPlayerBet[] bets) {
		float scoreSum = 0f;
		for(EstimatedPlayerBet bet: bets) {
			scoreSum += this.scoreByPlayer(this.playerBetRatios[bet.getPlayerBetId()], bet.getBetAmount(), bet.getBalanceAtTimeOfBet());
			
		}
		
		return scoreSum;
	}
	
	protected double scoreByPlayer(double winLossRatio, Integer betAmount, Integer totalAmountPlayer) {
		double score = 1f;
		if(betAmount != null) {
			double betRatio = 1f;
		    if(totalAmountPlayer != null) { 
		    	betRatio = (float) (betAmount.floatValue() + 1) /(totalAmountPlayer.floatValue() + 1); 
		    }
			  
			score = betAmount.floatValue() * winLossRatio * betRatio;
		}
		return score;
	}

}

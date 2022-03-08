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
		Pair<List<EstimatedPlayerBet>, List<EstimatedPlayerBet>> playerBetsBySide = new ImmutablePair<>(List.of(match.bets().leftTeamEstimatedBets()), 
				List.of(match.bets().rightTeamEstimatedBets()));
		return null;
	}
	
	protected Float generateLeftScore(List<EstimatedPlayerBet> leftSideBets) {
		Float score = this.findScoreOfBets(leftSideBets);
		return score;
	}

	protected Float generateRightScore(List<EstimatedPlayerBet> rightSideBets) {
		Float score = this.findScoreOfBets(rightSideBets);
		return score;
	}
	
	protected Float findScoreOfBets(List<EstimatedPlayerBet> bets) {
		float scoreSum = 0f;
		for(EstimatedPlayerBet bet: bets) {
			PlayerRecord playerRecord = null;
			try {
				playerRecord = this.playerBetRecords.get(bet.getPlayer());
			}catch(NullPointerException e) {
				if(this.playerBetRecords == null) {
					log.error("this*.playerBetRecords is null");
				}
				if(bet == null) {
					log.error("the current bet is null");
				}
				
			}
			Integer amount = GambleUtil.getMinimumBetForBettor(this.isBotSubscriber);
			if(playerRecord != null) {
				amount = GambleUtil.getBetAmountFromBetString(playerRecord, bet);
				scoreSum += this.scoreByPlayer(playerRecord.getWins(), playerRecord.getLosses(), amount, playerRecord.getLastKnownAmount());
			}
			
		}
		
		return scoreSum;
	}

}

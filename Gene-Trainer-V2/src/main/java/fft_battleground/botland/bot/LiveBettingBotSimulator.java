package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.util.Bet;
import fft_battleground.exception.BotConfigException;
import fft_battleground.match.model.ActualPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.simulation.model.SimulationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveBettingBotSimulator extends SimulatedBetBot {

	public LiveBettingBotSimulator(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
		// TODO Auto-generated constructor stub
	}

	private static final String BOT_NAME_KEY = "botname";
	
	private String botName;
	private Integer botPlayerBetId;
	
	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BOT_NAME_KEY)) {
			this.botName = map.get(BOT_NAME_KEY).getValue();
		}
	}

	@Override
	public void init() {
		this.botPlayerBetId = super.botBetIndexes.get(this.botName);
		if(this.botPlayerBetId == null) {
			log.warn("Bot {} not found in the known bot list; it will sit out every match", this.botName);
		}
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
		if(this.botPlayerBetId == null) {
			return new Bet(BattleGroundTeam.NONE, 0);
		}
		Optional<ActualPlayerBet> botBet = Stream.of(match.bets().leftTeamActualBets(), match.bets().rightTeamActualBets())
				.flatMap(Arrays::stream).filter(bet -> bet.playerBetId()== this.botPlayerBetId).findFirst();
		if(botBet.isEmpty()) {
			return new Bet(BattleGroundTeam.NONE, 0);
		}
		
		BattleGroundTeam chosenTeam = botBet.get().team();
		Integer betAmount = Math.min(botBet.get().value(), (int) gil);
		Bet bet = new Bet(chosenTeam, betAmount);
		return bet;
	}

}

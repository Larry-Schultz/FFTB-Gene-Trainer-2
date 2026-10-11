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

//mimics a real player's bot: bets exactly what the reference player actually bet on each match
@Slf4j
public class ReferenceBot extends SimulatedBetBot {
	private static final String REFERENCE_PLAYER_PARAMETER = "reference_player";

	private String referencePlayer;
	private Integer referencePlayerId;

	public ReferenceBot(SimulationConfig simulatorConfig) {
		super(simulatorConfig);
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(REFERENCE_PLAYER_PARAMETER)) {
			this.referencePlayer = map.get(REFERENCE_PLAYER_PARAMETER).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {
		this.referencePlayerId = super.botBetIndexes.get(this.referencePlayer);
		if(this.referencePlayerId == null) {
			log.warn("Reference player {} is not in the known bot list; bot {} will sit out every match", this.referencePlayer, this.name);
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
		if(this.referencePlayerId == null) {
			//viewer parity: no signal means the reference bots fall back to a 1 gil bet on the left side
			return new Bet(match.leftTeam().team(), 1);
		}
		Optional<ActualPlayerBet> referenceBet = Stream.of(match.bets().leftTeamActualBets(), match.bets().rightTeamActualBets())
				.flatMap(Arrays::stream).filter(bet -> bet.playerBetId() == this.referencePlayerId).findFirst();
		if(referenceBet.isEmpty()) {
			return new Bet(match.leftTeam().team(), 1);
		}

		BattleGroundTeam chosenTeam = referenceBet.get().team();
		int betAmount = Math.min(referenceBet.get().value(), (int) Math.min(gil, BET_MAX));
		return new Bet(chosenTeam, betAmount);
	}
}

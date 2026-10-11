package fft_battleground.botland;

import java.util.BitSet;
import java.util.Map;

import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.ActualPlayerBet;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MatchPot;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.match.model.TeamBetData;
import fft_battleground.model.BattleGroundTeam;

//hand-built two match dataset so the full botland roster can be exercised without any network
public final class SyntheticBotlandData {

	public static int geneId(CompleteBotGenome genome, String name) {
		Integer id = genome.getAttributeMap().get(name);
		return id != null ? id : genome.addMissingGene(name);
	}

	public static MultipleTournamentDataset dataset(CompleteBotGenome genome) {
		int chemistFemale = geneId(genome, "Chemist Female");
		int item = geneId(genome, "Item");
		int potionItem = geneId(genome, "Potion-Item");
		int knightMale = geneId(genome, "Knight Male");
		int mathSkill = geneId(genome, "MathSkill");
		genome.generateAttributeMap();

		TeamAttributes redTeam = new TeamAttributes(BattleGroundTeam.RED,
				new short[] {(short) chemistFemale, (short) item, (short) potionItem},
				new short[] {40, 50, 60, 70}, new short[] {45, 55, 65, 75}, 0,
				new double[] {0.5, 0.55, 0.6, 0.65}, 100);
		TeamAttributes blueTeam = new TeamAttributes(BattleGroundTeam.BLUE,
				new short[] {(short) knightMale, (short) mathSkill},
				new short[] {60, 65, 70, 75}, new short[] {40, 45, 50, 55}, 1,
				new double[] {0.44, 0.48, 0.52, 0.58}, 150);

		EstimatedPlayerBet[] redEstimatedBets = { new EstimatedPlayerBet(0, 100, 1000, BattleGroundTeam.RED),
				new EstimatedPlayerBet(1, 150, 2000, BattleGroundTeam.RED) };
		EstimatedPlayerBet[] blueEstimatedBets = { new EstimatedPlayerBet(2, 120, 1500, BattleGroundTeam.BLUE),
				new EstimatedPlayerBet(3, 80, 900, BattleGroundTeam.BLUE) };
		ActualPlayerBet[] redActualBets = { new ActualPlayerBet(0, 500, 1000, BattleGroundTeam.RED) };
		ActualPlayerBet[] blueActualBets = { new ActualPlayerBet(1, 300, 2000, BattleGroundTeam.BLUE) };
		MatchPot estimatedPot = new MatchPot(250, 200, 250.0 / 450.0, 200.0 / 450.0);
		MatchPot actualPot = new MatchPot(500, 300, 0.6, 0.4);
		TeamBetData bets = new TeamBetData(redEstimatedBets, blueEstimatedBets, redActualBets, blueActualBets, estimatedPot, actualPot);

		Match matchOne = new Match(100L, 0, 1, redTeam, blueTeam, bets, BattleGroundTeam.RED);
		Match matchTwo = new Match(100L, 1, 2, blueTeam, redTeam, bets, BattleGroundTeam.BLUE);

		double[] playerBetRatios = {0.5, 0.4, 0.6, 0.55, 0.45, 0.51};
		double[] playerFightRatios = {0.5, 0.4, 0.6, 0.55, 0.45, 0.51};
		BitSet subscribers = new BitSet(6);
		subscribers.set(4);
		Map<String, Integer> botBetIndexes = Map.of("birbbrainsbot", 0, "smokebowlingbot", 1, "getthemoneyz", 2);

		return new MultipleTournamentDataset(new Match[] {matchOne, matchTwo}, playerBetRatios, playerFightRatios,
				subscribers, botBetIndexes);
	}

	private SyntheticBotlandData() {
	}
}

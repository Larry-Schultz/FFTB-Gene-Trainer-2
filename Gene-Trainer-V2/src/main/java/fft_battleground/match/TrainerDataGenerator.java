package fft_battleground.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

import fft_battleground.cache.model.MatchCacheEntry;
import fft_battleground.dump.model.DumpData;
import fft_battleground.genetic.model.CompleteBotGenome;
import fft_battleground.match.model.ActualPlayerBet;
import fft_battleground.match.model.EstimatedPlayerBet;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MatchPot;
import fft_battleground.match.model.TeamAttributes;
import fft_battleground.match.model.TeamBetData;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Team;
import fft_battleground.tournament.model.TeamBet;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.util.GenericPairing;
import fft_battleground.viewer.model.BotBetData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
public class TrainerDataGenerator {
	//input data
	private List<MatchCacheEntry> matchCacheData;
	private Map<String, Double> playerBetWinRatios;
	private Map<String, Double> playerFightWinRatios;
	private CompleteBotGenome genome;
	
	//cache
	private Map<TeamAttributeCacheKey, TeamAttributes> teamAttributesCache;
	
	private Map<String, Integer> playerBetWinRatioMap;
	private List<Double> playerBetWinRatioList;
	private Set<String> playerNames;
	private Map<String, Integer> playerFightWinRatiosMap;
	private List<Double> playerFightWinRatioList;
	
	private List<Match> matches;
	
	public TrainerDataGenerator(CompleteBotGenome genome, List<MatchCacheEntry> matchCacheData, Map<String, Double> playerBetWinRatios, 
			Map<String, Double> playerFightWinRatios) {
		this.genome = genome;
		this.matchCacheData = matchCacheData;
		this.playerBetWinRatios = playerBetWinRatios;
		this.playerFightWinRatios = playerFightWinRatios;
		
		this.teamAttributesCache = new HashMap<>();
		
		this.playerBetWinRatioMap = new HashMap<>();
		this.playerBetWinRatioList = new LinkedList<>();
		this.populateWinRatioListAndMap(playerBetWinRatios, this.playerBetWinRatioMap, this.playerBetWinRatioList);
		this.playerFightWinRatiosMap = new HashMap<>();
		this.playerFightWinRatioList = new LinkedList<>();
		this.populateWinRatioListAndMap(playerFightWinRatios, this.playerFightWinRatiosMap, this.playerFightWinRatioList);
		
		this.playerNames = new HashSet<>();
		this.playerNames.addAll(this.playerBetWinRatioMap.keySet());
		this.playerNames.addAll(this.playerFightWinRatiosMap.keySet());
		
		this.matches = new ArrayList<>();
	}
	
	public Match[] generateDataset() {
		List<Match> matches = new ArrayList<>();
		List<Long> badTournaments = List.of(1639305296545L, 1639281050642L, 1639305296545L, 1639410234093L, 1639436642684L);
		for(int tournamentNumber = 1; tournamentNumber < this.matchCacheData.size(); tournamentNumber++) {
			try {
				MatchCacheEntry currentTournamentData = this.matchCacheData.get(tournamentNumber);
				if(!badTournaments.contains(currentTournamentData.getTournamentId())) {
					for(int matchNumber = 0; matchNumber <= 7; matchNumber++) {
						Tournament currentTournament = currentTournamentData.getTournament();
						MatchPairing matchPairing = this.getMatchByTournamentAndNumber(currentTournament, matchNumber);
						List<BotBetData> botBetData = currentTournamentData.getBotBetData();
						DumpData dumpData = currentTournamentData.getDumpData();
		
						Match match = this.gatherNecessaryDataAndGenerateMatchData(currentTournament, matchPairing, botBetData, dumpData, matchNumber);
						matches.add(match);
					}
				}
			} catch(NoSuchElementException e) {
				
			}
		}
		
		return matches.toArray(Match[]::new);
	}
	
	protected Match gatherNecessaryDataAndGenerateMatchData(Tournament currentTournament, MatchPairing matchPairing, List<BotBetData> botBetData, 
			DumpData dumpData, int matchNumber) throws NoSuchElementException {
		long tournamentId = currentTournament.getID();
		int map = matchPairing.mapNumber();
		Team leftTeam = currentTournament.getTeams().getTeamByBattleGroundTeam(matchPairing.leftTeam);
		Team rightTeam = currentTournament.getTeams().getTeamByBattleGroundTeam(matchPairing.rightTeam);
		TeamBet leftTeamPot = currentTournament.getPots().get(matchNumber).getLeft();
		TeamBet rightTeamPot = currentTournament.getPots().get(matchNumber).getRight();
		Integer leftTeamValue = dumpData.getTournamentTeamValues().getTeamByBattleGroundTeam(matchPairing.leftTeam);
		Integer rightTeamValue = dumpData.getTournamentTeamValues().getTeamByBattleGroundTeam(matchPairing.rightTeam);
		Integer champStreak = dumpData.getChampStreak();
		BattleGroundTeam winner = BattleGroundTeam.parse(currentTournament.getWinners().get(matchNumber));
		
		BotBetData estimatedBetData = null;
		try {
			estimatedBetData = botBetData.stream()
					.filter(betData -> betData.getTournamentId().equals(tournamentId) && betData.getLeftTeam() == matchPairing.leftTeam && betData.getRightTeam() == matchPairing.rightTeam)
					.findFirst().orElseThrow();
		} catch(NoSuchElementException e) {
			log.warn("Missing bot bet data for tournament {} left team {} right team {}", currentTournament.getID(), matchPairing.leftTeam, matchPairing.rightTeam, e);
			throw e;
		}
		Match result = this.generateMatchData(map, tournamentId, leftTeam, leftTeamPot, leftTeamValue, rightTeam, rightTeamPot, rightTeamValue, estimatedBetData, champStreak, winner);
		return result;
	}
	
	protected Match generateMatchData(int map, long tournamentId, Team leftTeam, TeamBet leftTeamPot, Integer leftTeamValue, Team rightTeam, TeamBet rightTeamPot, Integer rightTeamValue, 
			BotBetData estimatedBetData, Integer champStreak, BattleGroundTeam winner) {
		
		TeamAttributes leftTeamAttributes = this.getAttributesForTeam(tournamentId, leftTeamValue, leftTeam, champStreak);
		TeamAttributes rightTeamAttributes = this.getAttributesForTeam(tournamentId, rightTeamValue, rightTeam, champStreak);
		
		BattleGroundTeam leftBattleGroundTeam = leftTeam.getTeam();
		BattleGroundTeam rightBattleGroundTeam = rightTeam.getTeam();
		Pair<List<ActualPlayerBet>, List<ActualPlayerBet>> actualPlayerBets = new ImmutablePair<>(new ArrayList<>(), new ArrayList<>());
		leftTeamPot.getBets().forEach((bet) -> {
			String cleanedName = StringUtils.lowerCase(bet.getUser());
			String closestPlayerName = this.findClosestMatchingName(cleanedName, this.playerNames);
			Integer playerId = this.playerBetWinRatioMap.get(closestPlayerName);
			if(playerId == null) {
				log.error("missing player data for player {}", cleanedName);
			} else {
				actualPlayerBets.getLeft().add(new ActualPlayerBet(playerId, bet.getValue(), bet.getBalance(), leftBattleGroundTeam));
			}
		});
		rightTeamPot.getBets().forEach((bet) -> {
			String cleanedName = StringUtils.lowerCase(bet.getUser());
			String closestPlayerName = this.findClosestMatchingName(cleanedName, this.playerNames);
			Integer playerId = this.playerBetWinRatioMap.get(closestPlayerName);
			if(playerId == null) {
				log.error("missing player data for player {}", cleanedName);
			} else {
				actualPlayerBets.getRight().add(new ActualPlayerBet(playerId, bet.getValue(), bet.getBalance(), rightBattleGroundTeam));
			}
		});
		
		MatchPot actualPot = new MatchPot(leftTeamPot.potTotal(), rightTeamPot.potTotal(), leftTeamPot.getOdds(), rightTeamPot.getOdds());
		
		Pair<List<EstimatedPlayerBet>, List<EstimatedPlayerBet>> estimatedPlayerBets = new ImmutablePair<>(new ArrayList<>(), new ArrayList<>());
		;
		estimatedBetData.getLeftBets().getBets().forEach((playerName, betAmount) -> {
			String cleanedName = StringUtils.lowerCase(playerName);
			String closestPlayerName = this.findClosestMatchingName(cleanedName, this.playerNames);
			Integer playerId = this.playerBetWinRatioMap.get(closestPlayerName);
			if(playerId == null) {
				log.error("missing player data for player {}", cleanedName);
			} else {
				estimatedPlayerBets.getLeft().add(new EstimatedPlayerBet(playerId, betAmount, leftBattleGroundTeam));
			}
		});
		estimatedBetData.getRightBets().getBets().forEach((playerName, betAmount) -> {
			String cleanedName = StringUtils.lowerCase(playerName);
			String closestPlayerName = this.findClosestMatchingName(cleanedName, this.playerNames);
			Integer playerId = this.playerBetWinRatioMap.get(closestPlayerName);
			if(playerId == null) {
				log.error("missing player data for player {}", cleanedName);
			} else {
				estimatedPlayerBets.getRight().add(new EstimatedPlayerBet(playerId, betAmount, rightBattleGroundTeam));
			}
		});
		
		MatchPot estimatedPot = new MatchPot(estimatedBetData.leftPotTotal(), estimatedBetData.rightPotTotal(), estimatedBetData.leftOdds(), estimatedBetData.rightOdds());
		
		TeamBetData teamBetData = new TeamBetData(estimatedPlayerBets.getLeft().toArray(EstimatedPlayerBet[]::new), 
				estimatedPlayerBets.getRight().toArray(EstimatedPlayerBet[]::new),
				actualPlayerBets.getLeft().toArray(ActualPlayerBet[]::new), 
				actualPlayerBets.getRight().toArray(ActualPlayerBet[]::new), 
				estimatedPot, actualPot);
		
		Match result = new Match(map, leftTeamAttributes, rightTeamAttributes, teamBetData, winner);
		return result;
	}
	
	protected TeamAttributes getAttributesForTeam(long tournamentId, int teamValue, Team team, Integer champStreak) {
		TeamAttributeCacheKey key = new TeamAttributeCacheKey(tournamentId, team.getTeam());
		TeamAttributes teamAttributes = this.teamAttributesCache.containsKey(key) ? this.teamAttributesCache.get(key) : 
			this.getTeamAttributes(team, this.genome, teamValue, champStreak);
		return teamAttributes;
	}
	
	public TeamAttributes getTeamAttributes(Team team, CompleteBotGenome completeBotGenome, int teamValue, Integer champStreak) {
		BattleGroundTeam battleGroundTeam = team.getTeam();
		if(battleGroundTeam == null) {
			battleGroundTeam = team.getPalettes().getPalettes().getLeft();
		}
		Integer champStreakValue = team.getTeam() == BattleGroundTeam.CHAMPION && champStreak != null ? champStreak: null;
		double[] fightRatios = team.unitPlayerNames().stream().map(player -> {
			String cleanedName = StringUtils.lowerCase(player);
			String closestPlayerName = this.findClosestMatchingName(cleanedName, this.playerNames);
			Integer playerId = this.playerFightWinRatiosMap.get(closestPlayerName);
			if(playerId == null) {
				log.error("missing player data for player {}", cleanedName);
				return null;
			} else {
				return this.playerFightWinRatioList.get(this.playerFightWinRatiosMap.get(closestPlayerName));
			}
		}).filter(Objects::nonNull).mapToDouble(Double::doubleValue).toArray();
		return new TeamAttributes(battleGroundTeam, team.attributes(completeBotGenome), team.braves(), team.faiths(), team.raidBossCount(), teamValue, fightRatios, champStreakValue);
	}
	
	protected MatchPairing getMatchByTournamentAndNumber(Tournament tournament, int matchNumber) {
		MatchPairing match = null;
		BattleGroundTeam leftTeamName = BattleGroundTeam.NONE;
		BattleGroundTeam rightTeamName = BattleGroundTeam.NONE;
		Team leftTeam = null;
		Team rightTeam = null;
		Integer map = null;
		
		if(matchNumber == 0) {
			leftTeamName = BattleGroundTeam.RED; 
			rightTeamName = BattleGroundTeam.BLUE;
		} else if(matchNumber == 1) {
			leftTeamName = BattleGroundTeam.GREEN;
			rightTeamName = BattleGroundTeam.YELLOW;
		} else if(matchNumber == 2) {
			leftTeamName = BattleGroundTeam.WHITE;
			rightTeamName = BattleGroundTeam.BLACK;
		} else if(matchNumber == 3) {
			leftTeamName = BattleGroundTeam.PURPLE;
			rightTeamName = BattleGroundTeam.BROWN;
		} else if(matchNumber == 4) {
			BattleGroundTeam[] possibleSurvivors = new BattleGroundTeam[] {BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW};
			List<String> relevantWinners = this.getRelevantEntries(tournament.getWinners(), 0, 1);
			GenericPairing<BattleGroundTeam, BattleGroundTeam> teams = this.getTeamsByDeterminingSurvivors(relevantWinners, possibleSurvivors);
			if(teams != null) {
				leftTeamName = teams.getLeft();
				rightTeamName = teams.getRight();
			} 
		} else if(matchNumber == 5) {
			BattleGroundTeam[] possibleSurvivors = new BattleGroundTeam[] {BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN};
			List<String> relevantWinners = this.getRelevantEntries(tournament.getWinners(), 2, 3);
			GenericPairing<BattleGroundTeam, BattleGroundTeam> teams = this.getTeamsByDeterminingSurvivors(relevantWinners, possibleSurvivors);
			if(teams != null) {
				leftTeamName = teams.getLeft();
				rightTeamName = teams.getRight();
			} 
		} else if(matchNumber == 6) {
			BattleGroundTeam[] possibleSurvivors = new BattleGroundTeam[] {BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW,
					BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN};
			List<String> relevantWinners = this.getRelevantEntries(tournament.getWinners(), 4, 5);
			GenericPairing<BattleGroundTeam, BattleGroundTeam> teams = this.getTeamsByDeterminingSurvivors(relevantWinners, possibleSurvivors);
			if(teams != null) {
				leftTeamName = teams.getLeft();
				rightTeamName = teams.getRight();
			} 
		} else if(matchNumber == 7) {
			List<String> relevantWinners = this.getRelevantEntries(tournament.getWinners(), 6);
			if(relevantWinners.size() == 1) {
				leftTeamName = BattleGroundTeam.parse(relevantWinners.get(0));
				rightTeamName = BattleGroundTeam.CHAMPION;
			}
			
		}
		
		if(leftTeamName != null && rightTeamName != null) {
			leftTeam = tournament.getTeams().getTeamByBattleGroundTeam(leftTeamName);
			rightTeam = tournament.getTeams().getTeamByBattleGroundTeam(rightTeamName);
			map = mapNumber(tournament.getMaps().get(matchNumber));
			match = new MatchPairing(matchNumber, map, leftTeamName, leftTeam, rightTeamName, rightTeam);
		} else {
			match = new MatchPairing(matchNumber, 0, BattleGroundTeam.NONE, null, BattleGroundTeam.NONE, null);
		}
		
		return match;
	}
	
	protected List<String> getRelevantEntries(List<String> winners, int... indexes) {
		List<String> relevantWinners = new ArrayList<>();
		for(int index: indexes) {
			relevantWinners.add(winners.get(index));
		}
		
		return relevantWinners;
	}
	
	protected GenericPairing<BattleGroundTeam, BattleGroundTeam> getTeamsByDeterminingSurvivors(List<String> winners, BattleGroundTeam[] possibleSurvivors) {
		GenericPairing<BattleGroundTeam, BattleGroundTeam> teams = null;
		
		List<BattleGroundTeam> winnerTeams = winners.parallelStream().map(winner -> BattleGroundTeam.parse(winner)).collect(Collectors.toList());
		List<BattleGroundTeam> possibleSurvivorList = new ArrayList<>(Arrays.asList(possibleSurvivors));
		
		List<BattleGroundTeam> teamsToRemoveFromSurvivorList = new LinkedList<>();
		for(BattleGroundTeam possibleSurvivorTeam: possibleSurvivorList) {
			if(!winnerTeams.contains(possibleSurvivorTeam)) {
				teamsToRemoveFromSurvivorList.add(possibleSurvivorTeam);
			}
		}
		for(BattleGroundTeam teamToRemove: teamsToRemoveFromSurvivorList) {
			possibleSurvivorList.remove(teamToRemove);
		}
		
		if(possibleSurvivorList.size() == 2) {
			teams = new GenericPairing<>(possibleSurvivorList.get(0), possibleSurvivorList.get(1));
		}
		
		return teams;
	}
	
	protected static Integer mapNumber(String mapName) {
		String numberString = StringUtils.substringBefore(mapName, ")");
		Integer value = Integer.valueOf(numberString);
		return value;
	}
	
	protected void populateWinRatioListAndMap(Map<String, Double> playerData, Map<String, Integer> placementMap, List<Double> ratioData) {
		AtomicInteger i = new AtomicInteger(0);
		playerData.forEach((playerName, ratio) -> {
			ratioData.add(ratio);
			placementMap.put(StringUtils.lowerCase(playerName), i.get());
			i.getAndIncrement();
		});
	}
	
	protected String findClosestMatchingName(String playerName, Collection<String> entrants) {
		List<String> cleanedEntrants = Collections.unmodifiableList(entrants.parallelStream().collect(Collectors.toList()));
		LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
		Map<String, Integer> entrantDistanceMap = new ConcurrentHashMap<>();
		for(String entrant: cleanedEntrants) {
			Integer distance = distanceCalculator.apply(playerName, entrant);
			entrantDistanceMap.put(entrant, distance);
		}
		Optional<String> closestEntrant = entrantDistanceMap.keySet().parallelStream().min(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				Integer distance1 = entrantDistanceMap.get(o1);
				Integer distance2 = entrantDistanceMap.get(o2);
				return distance1.compareTo(distance2);
			}
		});
		
		String result = playerName;
		if(closestEntrant.isPresent()) {
			result = closestEntrant.get();
		}
		
		return result;
	}
	
	record MatchPairing(int matchNumber, int mapNumber, BattleGroundTeam leftTeam, Team leftTeamData, BattleGroundTeam rightTeam, Team rightTeamData) {};
	record TeamAttributeCacheKey(long tournamentId, BattleGroundTeam team) {};
}

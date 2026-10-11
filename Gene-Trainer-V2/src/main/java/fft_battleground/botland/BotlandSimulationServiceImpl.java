package fft_battleground.botland;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import fft_battleground.botland.model.BotlandProgress;
import fft_battleground.botland.model.BotlandRunResult;
import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.TrainerDataGenerator;
import fft_battleground.match.TrainerDataService;
import fft_battleground.match.model.Match;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tips;
import lombok.extern.slf4j.Slf4j;

//runs the same botland simulation the trainer uses, on demand, over the last N completed matches
@Service
@Slf4j
public class BotlandSimulationServiceImpl implements BotlandSimulationService {
	private static final String PROGRESS_DESTINATION = "/botland/progress";
	private static final String RESULTS_DESTINATION = "/botland/results";
	private static final int MATCHES_PER_TOURNAMENT = 8;
	//the generator skips the oldest cache entry and some matches are dropped for bad data, so fetch a few extra tournaments
	private static final int TOURNAMENT_HEADROOM = 3;

	@Autowired
	private TournamentService tournamentService;

	@Autowired
	private TrainerDataService trainerDataService;

	@Autowired
	private BotlandService botlandService;

	@Autowired
	private SimpMessagingTemplate template;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "botland-simulation");
		thread.setDaemon(true);
		return thread;
	});
	private volatile BotlandProgress progress = new BotlandProgress("idle", 0, "");
	private volatile BotlandRunResult lastResult = null;

	@Override
	public boolean start(int matchCount) {
		if(!this.running.compareAndSet(false, true)) {
			return false;
		}
		this.executor.submit(() -> {
			try {
				this.runSimulation(matchCount);
			} catch(Exception e) {
				log.error("Botland simulation failed", e);
				this.updateProgress("failed", 100, "simulation failed, see server logs");
			} finally {
				this.running.set(false);
			}
		});
		return true;
	}

	private void runSimulation(int matchCount) throws Exception {
		Instant startedAt = Instant.now();
		int tournamentCount = (int) Math.ceil(matchCount / (double) MATCHES_PER_TOURNAMENT) + TOURNAMENT_HEADROOM;

		this.updateProgress("loading tips", 5, "building genome from live tips");
		Tips tips = this.tournamentService.getCurrentTips();
		CompleteBotGenome genome = new CompleteBotGenome(tips);

		this.updateProgress("loading data", 10, "fetching up to " + tournamentCount + " tournaments");
		Consumer<String> dataProgress = message -> this.updateProgress("loading data", -1, message);
		MultipleTournamentDataset dataset = this.trainerDataService.generateDataset(tournamentCount, genome, dataProgress);

		//take the most recent matches, keeping chronological order since gil compounds across matches
		Match[] matches = TrainerDataGenerator.sliceLastN(dataset.matches(), matchCount);
		MultipleTournamentDataset slicedDataset = new MultipleTournamentDataset(matches, dataset.playerBetRatios(),
				dataset.playerFightRatios(), dataset.subscriberBitSet(), dataset.botBetIndexes());
		this.updateProgress("slicing", 85, "selected " + matches.length + " of " + dataset.matches().length + " available matches");

		this.updateProgress("simulating", 90, "running the botland");
		List<BotPlacement> leaderboard = this.botlandService.getBotleaderboard(genome, slicedDataset);

		Instant finishedAt = Instant.now();
		BotlandRunResult result = new BotlandRunResult(leaderboard, matchCount, matches.length, dataset.matches().length,
				Date.from(startedAt), Date.from(finishedAt), Duration.between(startedAt, finishedAt).toMillis());
		this.lastResult = result;
		this.updateProgress("complete", 100, matches.length + " matches simulated, " + leaderboard.size() + " bots ranked");
		this.template.convertAndSend(RESULTS_DESTINATION, result);
	}

	private void updateProgress(String phase, int percent, String detail) {
		int resolvedPercent = percent < 0 ? this.progress.percent() : percent;
		this.progress = new BotlandProgress(phase, resolvedPercent, detail);
		this.template.convertAndSend(PROGRESS_DESTINATION, this.progress);
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public BotlandProgress getStatus() {
		return this.progress;
	}

	@Override
	public BotlandRunResult getLastResult() {
		return this.lastResult;
	}
}

package fft_battleground.genetic;

import java.util.List;

import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.GenomeFile;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import io.jenetics.DoubleGene;
import io.jenetics.engine.EvolutionResult;

public interface GenomeManager {
	public Runnable logGeneration(final EvolutionResult<DoubleGene, Long> evolutionResult, long perfectScore, List<BotPlacement> botLeaderboard);
	public Runnable createGenomeFileCheckAndWriteRunnable(GenomeFile genomeData);
	public Runnable sendGenomeStatsUpdate(CompleteBotGenome genome, EvolutionResult<DoubleGene, Long> evolutionResult, 
			long perfectScore, List<BotPlacement> botLeaderboard, int matchesAnalyzed);
}

package fft_battleground.genetic;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.GenomeFile;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.simulation.Simulator;

import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Trainer {
	private Simulator simulator;
	private GenomeManager genomeFileManagerRef;
	private CompleteBotGenome genome;
	private int agentCount;
	private Duration duration;
	private List<BotPlacement> botlandLeaderboard;
	
	long perfectScore;
	int matchesAnalyzed;
	int geneCount;
	
	public Trainer(Simulator simulator, GenomeManager genomeFileManager, CompleteBotGenome data, List<BotPlacement> botlandLeaderboard, 
			int agentCount, long duration, int matchesAnalyzed, int geneCount) {
		this.genome = data;
		this.simulator = simulator;
		this.genomeFileManagerRef = genomeFileManager;
		this.botlandLeaderboard = botlandLeaderboard;
		this.agentCount = agentCount;
		this.duration = Duration.ofMillis(duration);
		
		this.perfectScore = this.simulator.perfectScoreGil();
		this.matchesAnalyzed = matchesAnalyzed;
		this.geneCount = geneCount;
	}
	
	public CompleteBotGenome train() {
		log.info("Starting training");
		Factory<Genotype<DoubleGene>> gtf = genome.generateGenome();
		
		int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService geneTrainerThreadPool = Executors.newFixedThreadPool(cores - 1);
        ExecutorService midTrainingThreadPool = Executors.newFixedThreadPool(1);
        // 3.) Create the execution environment.
        Engine<DoubleGene, Long> engine = Engine
            .builder(gt -> this.eval(gt), gtf)
            .survivorsSelector(new TournamentSelector<>())
            .offspringSelector(new StochasticUniversalSelector<>())
            //.alterers(new Mutator<>(0.5), new Mutator<>(0.15))
            .populationSize(agentCount)
            .executor(geneTrainerThreadPool)
            .build();
        
        Predicate<? super EvolutionResult<DoubleGene, Long>> winsLimit = Limits.byExecutionTime(duration);
        
        Genotype<DoubleGene> result = engine.stream()
        		.limit(winsLimit)
        		.parallel()
        		.peek(evolutionResult -> submitRunnables(evolutionResult, midTrainingThreadPool))
        		.collect(EvolutionResult.toBestGenotype());
        
        log.info("Training complete");
        this.genome.loadGeneData(result);
        
        return this.genome;
	}
	
	private long eval(Genotype<DoubleGene> genotype) {
		return this.simulator.gilResultFromSimulation(genotype);
    }
	
	private void submitRunnables(final EvolutionResult<DoubleGene, Long> evolutionResult, ExecutorService midTrainingThreadPool) {
		try {
			midTrainingThreadPool.submit(this.genomeFileManagerRef.logGeneration(evolutionResult, this.perfectScore, this.botlandLeaderboard));
			midTrainingThreadPool.submit(this.genomeFileManagerRef.createGenomeFileCheckAndWriteRunnable(this.createGenomeFile(evolutionResult)));
			midTrainingThreadPool.submit(this.genomeFileManagerRef.sendGenomeStatsUpdate(this.genome, evolutionResult, this.perfectScore, this.botlandLeaderboard, this.matchesAnalyzed));
		} catch(Exception e) {
			log.error("Error happened creating runnables that could have broken training", e);
		}
	}
	
	private GenomeFile createGenomeFile(final EvolutionResult<DoubleGene, Long> evolutionResult) {
		long currentGil = evolutionResult.bestFitness();
		long generation = evolutionResult.generation();
		Genotype<DoubleGene> bestFitnessGenotype = evolutionResult.bestPhenotype().genotype();
		Map<Integer, Double> percentiles = this.calculatePercentiles(bestFitnessGenotype);
		this.genome.loadGeneData(bestFitnessGenotype);
		GenomeFile genomeDate = new GenomeFile(this.botlandLeaderboard, this.matchesAnalyzed, currentGil, this.perfectScore, this.geneCount, 
				generation, this.genome, percentiles);
		return genomeDate;
	}
	
	private Map<Integer, Double> calculatePercentiles(final Genotype<DoubleGene> bestFitnessGenotype) {
		Map<Integer, Double> percentiles = this.simulator.calculatePercentiles(bestFitnessGenotype);
		return percentiles;
	}
}

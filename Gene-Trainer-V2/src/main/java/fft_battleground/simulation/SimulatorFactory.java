package fft_battleground.simulation;

import fft_battleground.genetic.model.CompleteBotGenome;
import fft_battleground.match.model.MultipleTournamentDataset;
import fft_battleground.simulation.model.SimulationConfig;

public interface SimulatorFactory {
	Simulator createSimulatorFromDataset(CompleteBotGenome genome, MultipleTournamentDataset dataset);
	SimulationConfig createSimulatorConfig(CompleteBotGenome genome, MultipleTournamentDataset dataset);
}

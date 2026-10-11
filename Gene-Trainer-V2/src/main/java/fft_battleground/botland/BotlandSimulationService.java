package fft_battleground.botland;

import fft_battleground.botland.model.BotlandProgress;
import fft_battleground.botland.model.BotlandRunResult;

public interface BotlandSimulationService {
	//returns false if a simulation is already running
	boolean start(int matchCount);
	boolean isRunning();
	BotlandProgress getStatus();
	BotlandRunResult getLastResult();
}

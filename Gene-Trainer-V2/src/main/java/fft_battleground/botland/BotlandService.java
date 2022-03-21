package fft_battleground.botland;

import java.util.List;

import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.MultipleTournamentDataset;

public interface BotlandService {
	List<BotPlacement> getBotleaderboard(CompleteBotGenome genome, MultipleTournamentDataset dataset);
}

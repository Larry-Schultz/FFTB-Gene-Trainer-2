package fft_battleground.genetic;

import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.CompleteBotGenome;

public interface TrainerService {
	CompleteBotGenome trainBot(int agentCount, long duration) throws TournamentApiException, ViewerException, DumpException, CacheException;
}

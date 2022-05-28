package fft_battleground.genetic;

import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;

public interface TrainerService {
	public static final int AGENT_COUNT = 150000;
	public static final int TOURNAMENT_COUNT = 500;
	public static final int DURATION_IN_HOURS = 7;
	
	CompleteBotGenome trainBot(int agentCount, long duration, int tournamentCount) throws TournamentApiException, ViewerException, DumpException, CacheException;
}

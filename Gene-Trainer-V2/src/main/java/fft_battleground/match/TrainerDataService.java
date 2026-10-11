package fft_battleground.match;

import java.util.function.Consumer;

import fft_battleground.exception.CacheException;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.exception.ViewerException;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.match.model.MultipleTournamentDataset;

public interface TrainerDataService {
	MultipleTournamentDataset generateDataset(int count, CompleteBotGenome genome) throws TournamentApiException, ViewerException, DumpException, CacheException;
	MultipleTournamentDataset generateDataset(int count, CompleteBotGenome genome, Consumer<String> progress) throws TournamentApiException, ViewerException, DumpException, CacheException;
}

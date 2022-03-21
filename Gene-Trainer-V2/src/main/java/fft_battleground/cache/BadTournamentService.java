package fft_battleground.cache;

import java.util.Collection;
import java.util.Set;

public interface BadTournamentService {
	void addBadTournament(Collection<Long> tournamentId);
	Set<Long> getBadTournamentIds();
}

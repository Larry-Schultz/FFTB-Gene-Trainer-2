package fft_battleground.tournament;

import java.util.List;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.model.Tips;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;

public interface TournamentService {
	List<Tournament> getTournaments(List<TournamentInfo> tournamentInfoList) throws TournamentApiException;
	List<TournamentInfo> getLatestTournamentInfo(Integer count) throws TournamentApiException;
	Tips getCurrentTips() throws TournamentApiException;
}

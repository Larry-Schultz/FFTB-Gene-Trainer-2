package fft_battleground.viewer.model;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class BotBetData {
	private Long botBetDataId;
	private Long tournamentId;
	private BattleGroundTeam leftTeam;
	private BattleGroundTeam rightTeam;
	private Integer leftSidePot;
	private Integer rightSidePot;
	
	private BotReceivedBets leftBets;
	
	private BotReceivedBets rightBets;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date createDateTime;
    
    public BotBetData() {
    	
    }
	
	public int leftPotTotal() {
		return this.potTotal(this.leftBets.getBets());
	}
	
	public double leftOdds() {
		return this.calculateOdds(this.leftPotTotal(), this.rightPotTotal());
	}
	
	public int rightPotTotal() {
		return this.potTotal(this.rightBets.getBets());
	}
	
	public double rightOdds() {
		return this.calculateOdds(this.rightPotTotal(), this.leftPotTotal());
	}
	
	protected int potTotal(List<BotReceivedBet> bets) {
		return bets.stream().mapToInt(BotReceivedBet::getBetAmount).sum();
	}
	
	protected double calculateOdds(int mySide, int otherSide) {
		if(mySide == 0 && otherSide > 0) {
			return 0;
		} else if(otherSide == 0 && mySide > 0) {
			return 1;
		} else if(mySide == 0 && otherSide == 0) {
			return 1;
		}
		
		double result = ((double) mySide) / ((double)(mySide + otherSide));
		return result;
	}



}
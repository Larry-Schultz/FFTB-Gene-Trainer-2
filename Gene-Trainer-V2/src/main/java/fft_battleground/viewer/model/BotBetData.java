package fft_battleground.viewer.model;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GenericPairing;
import fft_battleground.viewer.util.BotReceivedBetsJsonDeserializer;
import lombok.Data;

@Data
public class BotBetData {
	private Long botBetDataId;
	private Long tournamentId;
	private BattleGroundTeam leftTeam;
	private BattleGroundTeam rightTeam;
	private Integer leftSidePot;
	private Integer rightSidePot;
	
	@JsonDeserialize(using = BotReceivedBetsJsonDeserializer.class)
	@JsonSerialize(using = BotReceivedBetsJsonSerializer.class)
	private BotReceivedBets leftBets;
	
	@JsonDeserialize(using = BotReceivedBetsJsonDeserializer.class)
	@JsonSerialize(using = BotReceivedBetsJsonSerializer.class)
	private BotReceivedBets rightBets;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date createDateTime;
    
    public BotBetData() {
    	
    }
    
	public BotBetData(Long id, BattleGroundTeam leftTeam, BattleGroundTeam rightTeam, Integer leftSideTotal,
			Integer rightSideTotal, Map<String, Integer> leftBetsMap, Map<String, Integer> rightBetsMap) {
		this.tournamentId = id;
		this.leftTeam = leftTeam;
		this.rightTeam = rightTeam;
		this.leftSidePot = leftSideTotal;
		this.rightSidePot = rightSideTotal;
		this.leftBets = new BotReceivedBets(leftBetsMap);
		this.rightBets = new BotReceivedBets(rightBetsMap);
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
	
	protected int potTotal(Map<String, Integer> bets) {
		return bets.values().stream().mapToInt(i -> i).sum();
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

class BotReceivedBetsJsonSerializer extends StdSerializer<BotReceivedBets> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672752181097052631L;

	public BotReceivedBetsJsonSerializer() {
		super(BotReceivedBets.class);
	}
	
	protected BotReceivedBetsJsonSerializer(Class<BotReceivedBets> t) {
		super(t);
	}

	@Override
	public void serialize(BotReceivedBets value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		List<GenericPairing<String, Integer>> genericPair = GenericPairing.convertMapToGenericPairList(value.getBets());
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(genericPair);
		gen.writeString(json);
		
	}

}

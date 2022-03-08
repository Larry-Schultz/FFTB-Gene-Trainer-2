package fft_battleground.genetic.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class PlayerDataGeneAttributes extends GeneAttributes {
	public static final String BET_WIN_RATIO = "BetWinRatio";
	public static final String FIGHT_WIN_RATIO = "FightWinRatio";
	public static final String CURRENT_BALANCE_TO_BET_RATIO = "CurrentBalanceToBetRatio";
	public static final String HUMAN = "Human";
	public static final String ROBOT = "Robot";
	public static final String SUBSCRIBER = "Subscriber";
	public static final List<String> attributes = List.of(BET_WIN_RATIO, FIGHT_WIN_RATIO, CURRENT_BALANCE_TO_BET_RATIO, HUMAN, ROBOT, SUBSCRIBER);
	
	public PlayerDataGeneAttributes(AtomicInteger idTracker) {
		this.populateMapsFromAttributeList(idTracker, attributes);
	}
}

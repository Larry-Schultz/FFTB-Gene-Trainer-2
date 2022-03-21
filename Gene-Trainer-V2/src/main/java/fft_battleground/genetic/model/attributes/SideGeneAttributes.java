package fft_battleground.genetic.model.attributes;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class SideGeneAttributes extends GeneAttributes {
	public static final List<BattleGroundTeam> sides = List.of(BattleGroundTeam.LEFT, BattleGroundTeam.RIGHT,
			BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, 
			BattleGroundTeam.YELLOW, BattleGroundTeam.BLACK, BattleGroundTeam.WHITE, 
			BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN, BattleGroundTeam.CHAMPION);
	
	public SideGeneAttributes(AtomicInteger attributeNumber) {
		List<String> attributeNames = sides.stream().map(BattleGroundTeam::getProperName).collect(Collectors.toList());
		this.populateMapsFromAttributeList(attributeNumber, attributeNames);
	}
}

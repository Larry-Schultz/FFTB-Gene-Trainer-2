package fft_battleground.genetic.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class SideGeneAttributes extends GeneAttributes {
	private static final List<String> sideAttributes = List.of("Left", "Right", "Red", "Blue", "Green", "Yellow", "Black", "White", "Purple", "Brown", "Champion");
	
	public SideGeneAttributes(AtomicInteger attributeNumber) {
		this.populateMapsFromAttributeList(attributeNumber, sideAttributes);
	}
}

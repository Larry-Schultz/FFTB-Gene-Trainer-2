package fft_battleground.genetic.model;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class BraveFaithAttributes extends GeneAttributes {
	public static final String BRAVE = "Brave";
	public static final String FAITH = "Faith";
	public static final String BRAVEFAITH = "BraveFaith";
	public static final String RAIDBOSS = "RaidBoss";
	
	public static final List<String> braveFaith = Arrays.asList(new String[] {BRAVE, FAITH, BRAVEFAITH, RAIDBOSS});
	
	public BraveFaithAttributes(AtomicInteger idTracker) {
		this.populateMapsFromAttributeList(idTracker, braveFaith);
	}
}

package fft_battleground.genetic.model.attributes;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class BetGeneAttributes extends GeneAttributes {
	public static final Long GIL_FLOOR = 200L;
	public static final Long BET_MIN = 1L;
	public static final Integer BET_MAX = 1000;
	
	public static final String first = "1-bet";
	protected double minValue = GIL_FLOOR.doubleValue();
	protected double maxValue = BET_MAX.doubleValue();
	
	public BetGeneAttributes(AtomicInteger idTracker) {
		List<String> attributes = IntStream.range(1, 101).boxed().map(number -> number.toString() + "-bet").collect(Collectors.toList());
		this.populateMapsFromAttributeList(idTracker, attributes);
	}
	
	@Override
	public double getMinValue() {
		return this.minValue;
	}
	
	@Override
	public double getMaxValue() {
		return this.maxValue;
	}
}

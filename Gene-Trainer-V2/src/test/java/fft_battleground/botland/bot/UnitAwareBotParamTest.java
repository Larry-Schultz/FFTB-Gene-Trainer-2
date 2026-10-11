package fft_battleground.botland.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fft_battleground.botland.model.BotParam;

public class UnitAwareBotParamTest {

	@Test
	public void testParseUnitWeightsKeepsOnlyNumericNonControlParams() {
		Map<String, BotParam> params = new HashMap<>();
		params.put("personality", new BotParam("personality", "chemical", ""));
		params.put("betExpression", new BotParam("betExpression", "min(mxBet, balance)", ""));
		params.put("inverse", new BotParam("inverse", "true", ""));
		params.put("bravemultiplier", new BotParam("bravemultiplier", "2", ""));
		params.put("faithmuliplier", new BotParam("faithmuliplier", "1.5", "")); //the viewer's typo'd key must stay excluded
		params.put("Chemist Female", new BotParam("Chemist Female", "50", ""));
		params.put("RedChocobo", new BotParam("RedChocobo", "30", ""));
		params.put("Blind", new BotParam("Blind", "-5", ""));

		Map<String, Double> weights = UnitAwareBot.parseUnitWeights(params);

		assertEquals(3, weights.size());
		assertEquals(50.0, weights.get("chemist female"), "names must be lowercased to match the genome namespace");
		assertEquals(30.0, weights.get("redchocobo"));
		assertEquals(-5.0, weights.get("blind"));
		assertFalse(weights.containsKey("faithmuliplier"));
		assertFalse(weights.containsKey("personality"));
	}
}

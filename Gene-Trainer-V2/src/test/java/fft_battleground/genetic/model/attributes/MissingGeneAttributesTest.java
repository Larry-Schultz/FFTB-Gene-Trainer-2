package fft_battleground.genetic.model.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class MissingGeneAttributesTest {

	@Test
	public void testAddAttributeIsIdempotentForDuplicateNames() {
		AtomicInteger idTracker = new AtomicInteger(0);
		MissingGeneAttributes attributes = new MissingGeneAttributes(idTracker);

		//"Kikuichimonji" was the duplicate key that killed the live gene bot in April 2026
		int firstId = attributes.addAttribute("Kikuichimonji");
		int secondId = attributes.addAttribute("Kikuichimonji");

		assertEquals(firstId, secondId, "re-adding an existing attribute name must return the original id");
		assertEquals(2, attributes.getGeneAttributes().size(), "a duplicate name must not burn a gene slot");
		assertEquals(2, attributes.getIdNameMap().size());
		assertEquals(2, attributes.getAttributeOrderArray().size(), "the attribute order array must stay in sync with the name map");

		int otherId = attributes.addAttribute("Chirijiraden");
		assertEquals(3, attributes.getGeneAttributes().size());
		assertEquals(firstId + 1, otherId, "ids must keep advancing for genuinely new names");
	}

	@Test
	public void testPopulatedSectionsSkipDuplicateNames() {
		AtomicInteger idTracker = new AtomicInteger(0);
		PotGeneAttributes attributes = new PotGeneAttributes(idTracker);

		//the fixed attribute lists are unique, so building them must succeed cleanly
		assertEquals(3, attributes.getGeneAttributes().size());
		assertEquals(3, attributes.getAttributeOrderArray().size());
	}
}

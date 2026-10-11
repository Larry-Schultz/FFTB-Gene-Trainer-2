package fft_battleground.genetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WinnerFileValidationTest {

	@TempDir
	Path tempDir;

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testCleanWinnerFilePassesValidation() throws Exception {
		File file = this.writeFixture(tempDir.resolve("clean.txt"),
				"{\"genome\":{\"missingGeneAttributes\":{\"geneAttributes\":{\"attributes\":["
				+ "{\"key\":\"Kikuichimonji\",\"value\":27},"
				+ "{\"key\":\"Chirijiraden\",\"value\":-60}]}}}}");

		List<String> duplicates = this.newResultChecker().findDuplicateAttributeKeys(file, this.mapper);

		assertTrue(duplicates.isEmpty(), "clean file must have no duplicates: " + duplicates);
	}

	@Test
	public void testDuplicateKeyWithinSectionIsDetected() throws Exception {
		File file = this.writeFixture(tempDir.resolve("dupe-in-section.txt"),
				"{\"genome\":{\"missingGeneAttributes\":{\"geneAttributes\":{\"attributes\":["
				+ "{\"key\":\"Kikuichimonji\",\"value\":-60},"
				+ "{\"key\":\"Kikuichimonji\",\"value\":27}]}}}}");

		List<String> duplicates = this.newResultChecker().findDuplicateAttributeKeys(file, this.mapper);

		assertEquals(1, duplicates.size(), "within-section dupe should be reported exactly once: " + duplicates);
		assertTrue(duplicates.get(0).contains("duplicate within"), duplicates.get(0));
	}

	@Test
	public void testDuplicateKeyAcrossSectionsIsDetected() throws Exception {
		File file = this.writeFixture(tempDir.resolve("dupe-across-sections.txt"),
				"{\"genome\":{"
				+ "\"sideGeneAttributes\":{\"geneAttributes\":{\"attributes\":[{\"key\":\"left\",\"value\":10}]}},"
				+ "\"missingGeneAttributes\":{\"geneAttributes\":{\"attributes\":[{\"key\":\"left\",\"value\":20}]}}"
				+ "}}");

		List<String> duplicates = this.newResultChecker().findDuplicateAttributeKeys(file, this.mapper);

		assertEquals(1, duplicates.size(), "cross-section dupe should be reported: " + duplicates);
		assertTrue(duplicates.get(0).contains("multiple genome sections"), duplicates.get(0));
	}

	@Test
	public void testMissingGenomeSectionIsReported() throws Exception {
		File file = this.writeFixture(tempDir.resolve("no-genome.txt"), "{\"botLeaderboard\":[]}");

		List<String> duplicates = this.newResultChecker().findDuplicateAttributeKeys(file, this.mapper);

		assertEquals(1, duplicates.size(), "a file without a genome section cannot be validated: " + duplicates);
	}

	@Test
	public void testValidateUniqueAttributeKeysDeletesFileAndFailsLoudlyOnDuplicate() throws Exception {
		File file = this.writeFixture(tempDir.resolve("poison.txt"),
				"{\"genome\":{\"missingGeneAttributes\":{\"geneAttributes\":{\"attributes\":["
				+ "{\"key\":\"Kikuichimonji\",\"value\":-60},"
				+ "{\"key\":\"Kikuichimonji\",\"value\":27}]}}}}");

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> this.newResultChecker().validateUniqueAttributeKeys(file, this.mapper));

		assertTrue(failure.getMessage().contains("Kikuichimonji"), failure.getMessage());
		assertFalse(file.exists(), "poison file must be deleted so consumers never load it");
	}

	@Test
	public void testValidateUniqueAttributeKeysKeepsCleanFile() throws Exception {
		File file = this.writeFixture(tempDir.resolve("clean2.txt"),
				"{\"genome\":{\"missingGeneAttributes\":{\"geneAttributes\":{\"attributes\":[{\"key\":\"Kikuichimonji\",\"value\":27}]}}}}");

		this.newResultChecker().validateUniqueAttributeKeys(file, this.mapper);

		assertTrue(file.exists(), "clean file must be kept");
	}

	@Test
	public void testMoveAtomicallyReplacesTarget() throws Exception {
		Path source = tempDir.resolve("source.tmp");
		Path target = tempDir.resolve("target.txt");
		Files.writeString(source, "new content");
		Files.writeString(target, "old content");

		this.newResultChecker().moveAtomically(source.toFile(), target.toFile());

		assertEquals("new content", Files.readString(target), "atomic move must replace the target's contents");
		assertFalse(source.toFile().exists(), "temp file must be gone after the move");
	}

	private ResultChecker newResultChecker() {
		return new ResultChecker(null, new AtomicLong(0), new ReentrantLock());
	}

	private File writeFixture(Path path, String json) throws Exception {
		Files.writeString(path, json);
		return path.toFile();
	}
}

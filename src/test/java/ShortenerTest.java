import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShortenerTest {

	@Test
	void shorten() {
		assertEquals("[I][13:38:43.4628]...", Shortener.shorten("[INFO][2023-08-26 13:38:43.462862] ..."));
		assertEquals("...", Shortener.shorten("..."));
	}
}
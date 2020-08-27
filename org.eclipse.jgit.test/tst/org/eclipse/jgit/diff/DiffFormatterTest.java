 * Copyright (C) 2010, 2020 Google Inc. and others
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.jgit.api.Status;
	@Test
	public void testDefaultRenameDetectorSettings() throws Exception {
		RenameDetector rd = df.getRenameDetector();
		assertNull(rd);
		df.setDetectRenames(true);
		rd = df.getRenameDetector();
		assertNotNull(rd);
		assertEquals(400, rd.getRenameLimit());
		assertEquals(60, rd.getRenameScore());
	}

	@Test
	public void testTrackedFileInIgnoredFolderUnchanged()
			throws Exception {
		commitFile("empty/empty/foo", "", "master");
		commitFile(".gitignore", "empty/*", "master");
		try (Git git = new Git(db)) {
			Status status = git.status().call();
			assertTrue(status.isClean());
		}
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
				DiffFormatter dfmt = new DiffFormatter(os)) {
			dfmt.setRepository(db);
			dfmt.format(new DirCacheIterator(db.readDirCache()),
					new FileTreeIterator(db));
			dfmt.flush();

			String actual = os.toString("UTF-8");

			assertEquals("", actual);
		}
	}

	@Test
	public void testTrackedFileInIgnoredFolderChanged()
			throws Exception {
		String expectedDiff = "diff --git a/empty/empty/foo b/empty/empty/foo\n"
				+ "index e69de29..5ea2ed4 100644\n" //
				+ "--- a/empty/empty/foo\n" //
				+ "+++ b/empty/empty/foo\n" //
				+ "@@ -0,0 +1 @@\n" //
				+ "+changed\n";

		commitFile("empty/empty/foo", "", "master");
		commitFile(".gitignore", "empty/*", "master");
		try (Git git = new Git(db)) {
			Status status = git.status().call();
			assertTrue(status.isClean());
		}
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
				DiffFormatter dfmt = new DiffFormatter(os)) {
			writeTrashFile("empty/empty/foo", "changed\n");
			dfmt.setRepository(db);
			dfmt.format(new DirCacheIterator(db.readDirCache()),
					new FileTreeIterator(db));
			dfmt.flush();

			String actual = os.toString("UTF-8");

			assertEquals(expectedDiff, actual);
		}
	}

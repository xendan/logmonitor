package org.xendan.logmonitor.read;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Don't run it unless host available.
 *
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpDownloaderTest {

    @Test
    public void testDownloadToLocal() throws Exception {
        ScpDownloader downloader = new ScpDownloader("192.168.0.1", "someuser", new DummyUserInfo("somepassword"), "/home/grizun/aa.log", 21345);
        String localPath = downloader.downloadToLocal();

        File file = new File(localPath);
        assertTrue(file.exists());
        if (!file.delete()) {
            throw new IllegalStateException("Test file not deleted");
        }
    }
}

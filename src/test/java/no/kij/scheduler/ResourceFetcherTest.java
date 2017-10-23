package no.kij.scheduler;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Properties;

public class ResourceFetcherTest {

    @Test
    public void testGetCredentialsSuccessful() {
        Properties creds = ResourceFetcher.getCredentials();
        assertNotNull(creds);
    }

    @Test
    public void testGetFileSuccessful() {
        String sql = ResourceFetcher.getFile("database.sql");
        assertNotNull(sql);
    }

    @Test
    public void testGetFileWhereFileDoesNotExist() {
        String sql = ResourceFetcher.getFile("doesNotExist.txt");
        assertNull(sql);
    }


}

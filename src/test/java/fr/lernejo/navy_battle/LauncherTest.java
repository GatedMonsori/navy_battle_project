package fr.lernejo.navy_battle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LauncherTest {

    @Test
    void testPing() throws IOException {
        Launcher.main(new String[]{"9876"});
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:9876/ping").openConnection();
        connection.setRequestMethod("GET");
        assertEquals(200, connection.getResponseCode());
    }
}

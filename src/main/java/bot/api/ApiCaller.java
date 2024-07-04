package bot.api;

import java.io.IOException;

public interface ApiCaller {
    String makeApiCall(String url) throws IOException;
}
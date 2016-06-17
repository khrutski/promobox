package ee.promobox.promoboxandroid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.util.error.DeviceNotFoundError;


public class HttpPullRequest implements Callable<PullResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPullRequest.class);

    private PullRequest pullRequest;
    private AppState appState;

    private ObjectMapper mapper = new ObjectMapper();

    public HttpPullRequest(AppState appState, PullRequest pullRequest) {

        this.pullRequest = pullRequest;
        this.appState = appState;
        mapper.registerModule(new JodaModule());
    }

    @Override
    public PullResponse call() throws Exception {

        URL url = new URL(String.format("%s/service/device/%s/pull", appState.getSettings().getServer(), pullRequest.getUuid()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);

            String query = "json=" + URLEncoder.encode(mapper.writeValueAsString(pullRequest), "UTF-8");

            IOUtils.write(query.getBytes(), conn.getOutputStream());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                String response = IOUtils.toString(conn.getInputStream(),"UTF-8");

                LOGGER.debug("Response: {}", response);

                if (!response.isEmpty()) {
                    return mapper.readValue(response, PullResponse.class);
                }
            } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new DeviceNotFoundError();
            }

        } finally {
            conn.disconnect();
        }

        throw new Error("Error get response from server");
    }
}

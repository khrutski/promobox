package ee.promobox.promoboxandroid;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.service.NetworkService;
import ee.promobox.promoboxandroid.service.PullRequest;
import ee.promobox.promoboxandroid.service.PullResponse;


public class NetworkServiceTest {

    @Test
    public void testStart() {
        AppState state = new AppState();
        NetworkService networkService = new NetworkService(state);

        Futures.addCallback(networkService.pullRequest(new PullRequest()), new FutureCallback<PullResponse>(){

            @Override
            public void onSuccess(PullResponse result) {
                Assert.assertNotNull(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail();
            }
        });

    }
}

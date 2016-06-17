package ee.promobox.promoboxandroid;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;


public class MainServiceTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testWithBoundService() throws Exception {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(), MainService.class);


        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);


        // Get the reference to the service, or you can call public methods on the binder directly.
        AIDLInterface mainService = AIDLInterface.Stub.asInterface(binder);

        Assert.assertNotNull(mainService);

        Assert.assertNotNull(mainService.getUuid());

        Assert.assertEquals("fail", mainService.getUuid());
    }
}

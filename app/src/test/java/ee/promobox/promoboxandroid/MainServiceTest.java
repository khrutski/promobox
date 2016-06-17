package ee.promobox.promoboxandroid;


import android.content.Context;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MainServiceTest {

    @Mock
    Context mMockContext;




    @Test
    public void testStart() {
        Intent startIntent = new Intent();
        startIntent.setClass(mMockContext, MainService.class);
        MainService mainService = new MainService();
        mainService.onCreate();

    }

}

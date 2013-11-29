/*
 * Copyright Iconology, Inc. 2012. All rights reserved.
 */

package com.plausiblelabs.metrics.reporting;

import com.amazonaws.auth.AWSCredentials;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class RemoteCloudWatchTest {
    @Test
    public void testSendingToAmazon() throws IOException {
        MetricRegistry registry = new MetricRegistry();
        
        AWSCredentials creds = null;
        Timer timer = registry.timer(name(CloudWatchReporterTest.class, "TestTimer"));
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 50; j++) {
                timer.update(i, TimeUnit.MINUTES);
            }
        }
        registry.register(name("test.limits.NegSmall"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return -1E-109;
            }
        });
        registry.register(name("test.limits.PosSmall"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return 1E-109;
            }
        });
        registry.register(name("test.limits.NegLarge"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return -CloudWatchReporter.LARGEST_SENDABLE * 10;
            }
        });
        registry.register(name("test.limits.PosLarge"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return CloudWatchReporter.LARGEST_SENDABLE * 10;
            }
        });
        
        new CloudWatchReporter.Enabler("cxabf", creds)
            .withRegistry(registry)
            .withInstanceIdDimension("test").build().report();
    }
}

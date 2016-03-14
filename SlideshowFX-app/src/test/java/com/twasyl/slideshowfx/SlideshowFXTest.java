package com.twasyl.slideshowfx;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.app.SlideshowFXState;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Tests basic behaviors on the {@link com.twasyl.slideshowfx.app.SlideshowFX} class
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class SlideshowFXTest {
    @Test
    public void applicationStateEventRaised() throws NoSuchMethodException, InterruptedException, ExecutionException {

        final CompletableFuture<SlideshowFXState> results = new CompletableFuture<>();

        SlideshowFX.addPropertyChangeListener(event -> {
            if(event.getPropertyName().equals("APPLICATION_STATE")) {
                results.complete((SlideshowFXState) event.getNewValue());
            }
        });

        SlideshowFX.setApplicationState(SlideshowFXState.PRESENTING);

        assertEquals(SlideshowFXState.PRESENTING, results.get());
    }
}

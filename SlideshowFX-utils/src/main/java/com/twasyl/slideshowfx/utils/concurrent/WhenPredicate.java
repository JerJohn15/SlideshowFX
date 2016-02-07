package com.twasyl.slideshowfx.utils.concurrent;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.List;

/**
 * A WhenPredicate is created by {@link com.twasyl.slideshowfx.utils.concurrent.ForPredicate} instances and
 * add a listener on properties of a {@link javafx.concurrent.Task} that match a criterion.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class WhenPredicate {

    /**
     * The list of all DoPredicate generated by the methods in this class, like {@link #stateIs(javafx.concurrent.Worker.State)}.
     */
    protected final List<DoPredicate> doPredicates = new ArrayList<>();

    /**
     * The ForPredicate associated to this predicate.
     */
    protected ForPredicate forPredicate;

    protected WhenPredicate(final ForPredicate forPredicate) {
        this.forPredicate = forPredicate;
    }

    /**
     * Creates a {@link com.twasyl.slideshowfx.utils.concurrent.DoPredicate} for the state of the task.
     * When the state of the task matches the given {@code state}, the listener set on the {@link javafx.concurrent.Task#stateProperty()}
     * is triggered and the action performed is the one identified by the {@link com.twasyl.slideshowfx.utils.concurrent.DoPredicate#runnable}
     * runnable.
     * Note that {@link com.twasyl.slideshowfx.utils.concurrent.DoPredicate#perform(Runnable)} method should be called
     * on the instance of predicate returned by this method.
     *
     * @param state The state of the task to match to trigger the execution of the {@link com.twasyl.slideshowfx.utils.concurrent.DoPredicate#runnable}
     * @return The created DoPredicate.
     */
    public DoPredicate stateIs(final Worker.State state) {
        final DoPredicate doPredicate = new DoPredicate(this);
        this.doPredicates.add(doPredicate);

        this.forPredicate.task.stateProperty().addListener((value, oldValue, newValue) -> {
            if(newValue == state) {
                PlatformHelper.run(doPredicate.runnable);
            }
        });

        return doPredicate;
    }
}

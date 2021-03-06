/**
 * The MIT License
 * Copyright (c) 2014-2016 Ilkka Seppälä
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.promise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests Promise class.
 */
public class PromiseTest {

    private Executor executor;
    private Promise<Integer> promise;

    @BeforeEach
    public void setUp() {
        executor = Executors.newSingleThreadExecutor();
        promise = new Promise<>();
    }

    @Test
    public void promiseIsFulfilledWithTheResultantValueOfExecutingTheTask()
            throws InterruptedException, ExecutionException {
        promise.fulfillInAsync(new NumberCrunchingTask(), executor);

        assertEquals(NumberCrunchingTask.CRUNCHED_NUMBER, promise.get());
        assertTrue(promise.isDone());
        assertFalse(promise.isCancelled());
    }

    @Test
    public void promiseIsFulfilledWithAnExceptionIfTaskThrowsAnException()
            throws InterruptedException, ExecutionException, TimeoutException {
        testWaitingForeverForPromiseToBeFulfilled();
        testWaitingSomeTimeForPromiseToBeFulfilled();
    }

    private void testWaitingForeverForPromiseToBeFulfilled()
            throws InterruptedException, TimeoutException {
        Promise<Integer> promise = new Promise<>();
        promise.fulfillInAsync(() -> {
            throw new RuntimeException("Barf!");
        }, executor);

        try {
            promise.get();
            fail("Fetching promise should result in exception if the task threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }

        try {
            promise.get(1000, TimeUnit.SECONDS);
            fail("Fetching promise should result in exception if the task threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }
    }

    private void testWaitingSomeTimeForPromiseToBeFulfilled()
            throws InterruptedException, TimeoutException {
        Promise<Integer> promise = new Promise<>();
        promise.fulfillInAsync(() -> {
            throw new RuntimeException("Barf!");
        }, executor);

        try {
            promise.get(1000, TimeUnit.SECONDS);
            fail("Fetching promise should result in exception if the task threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }

        try {
            promise.get();
            fail("Fetching promise should result in exception if the task threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }

    }

    @Test
    public void dependentPromiseIsFulfilledAfterTheConsumerConsumesTheResultOfThisPromise()
            throws InterruptedException, ExecutionException {
        Promise<Void> dependentPromise = promise
                .fulfillInAsync(new NumberCrunchingTask(), executor)
                .thenAccept(value -> {
                    assertEquals(NumberCrunchingTask.CRUNCHED_NUMBER, value);
                });


        dependentPromise.get();
        assertTrue(dependentPromise.isDone());
        assertFalse(dependentPromise.isCancelled());
    }

    @Test
    public void dependentPromiseIsFulfilledWithAnExceptionIfConsumerThrowsAnException()
            throws InterruptedException, ExecutionException, TimeoutException {
        Promise<Void> dependentPromise = promise
                .fulfillInAsync(new NumberCrunchingTask(), executor)
                .thenAccept(value -> {
                    throw new RuntimeException("Barf!");
                });

        try {
            dependentPromise.get();
            fail("Fetching dependent promise should result in exception "
                    + "if the action threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }

        try {
            dependentPromise.get(1000, TimeUnit.SECONDS);
            fail("Fetching dependent promise should result in exception "
                    + "if the action threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }
    }

    @Test
    public void dependentPromiseIsFulfilledAfterTheFunctionTransformsTheResultOfThisPromise()
            throws InterruptedException, ExecutionException {
        Promise<String> dependentPromise = promise
                .fulfillInAsync(new NumberCrunchingTask(), executor)
                .thenApply(value -> {
                    assertEquals(NumberCrunchingTask.CRUNCHED_NUMBER, value);
                    return String.valueOf(value);
                });


        assertEquals(String.valueOf(NumberCrunchingTask.CRUNCHED_NUMBER), dependentPromise.get());
        assertTrue(dependentPromise.isDone());
        assertFalse(dependentPromise.isCancelled());
    }

    @Test
    public void dependentPromiseIsFulfilledWithAnExceptionIfTheFunctionThrowsException()
            throws InterruptedException, ExecutionException, TimeoutException {
        Promise<String> dependentPromise = promise
                .fulfillInAsync(new NumberCrunchingTask(), executor)
                .thenApply(value -> {
                    throw new RuntimeException("Barf!");
                });

        try {
            dependentPromise.get();
            fail("Fetching dependent promise should result in exception "
                    + "if the function threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }

        try {
            dependentPromise.get(1000, TimeUnit.SECONDS);
            fail("Fetching dependent promise should result in exception "
                    + "if the function threw an exception");
        } catch (ExecutionException ex) {
            assertTrue(promise.isDone());
            assertFalse(promise.isCancelled());
        }
    }

    @Test
    public void fetchingAnAlreadyFulfilledPromiseReturnsTheFulfilledValueImmediately()
            throws InterruptedException, ExecutionException, TimeoutException {
        Promise<Integer> promise = new Promise<>();
        promise.fulfill(NumberCrunchingTask.CRUNCHED_NUMBER);

        promise.get(1000, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void exceptionHandlerIsCalledWhenPromiseIsFulfilledExceptionally() {
        Promise<Object> promise = new Promise<>();
        Consumer<Throwable> exceptionHandler = mock(Consumer.class);
        promise.onError(exceptionHandler);

        Exception exception = new Exception("barf!");
        promise.fulfillExceptionally(exception);

        verify(exceptionHandler).accept(eq(exception));
    }

    private static class NumberCrunchingTask implements Callable<Integer> {

        private static final Integer CRUNCHED_NUMBER = Integer.MAX_VALUE;

        @Override
        public Integer call() throws Exception {
            // Do number crunching
            Thread.sleep(100);
            return CRUNCHED_NUMBER;
        }
    }
}

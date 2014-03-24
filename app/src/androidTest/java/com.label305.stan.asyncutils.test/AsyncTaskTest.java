package com.label305.safeasynctasktest.test;

import android.os.Looper;

import com.label305.stan.asyncutils.AsyncTask;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"AnonymousInnerClass", "AnonymousInnerClassWithTooManyMethods"})
public class AsyncTaskTest extends TestCase {

    public static final long ONE_SECOND = 1000L;
    public static final long HALF_SECOND = 500L;
    public static final String EXPECTED_EXCEPTION_MESSAGE = "Expected";

    @Mock
    private SafeAsyncTaskCallback<Object> mSafeAsyncTaskCallback;
    private CountDownLatch mCountDownLatch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        mCountDownLatch = new CountDownLatch(1);
    }

    /* Test normal behavior */

    public void testEmptyExecution() throws InterruptedException {
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch).execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onSuccess(any(Object.class));
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
    }

    public void testSlowExecution() throws InterruptedException {
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {
            @Override
            public Object call() throws Exception {
                super.call();
                Thread.sleep(ONE_SECOND);
                return null;
            }
        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onSuccess(any(Object.class));
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
    }

    public void testResultExecution() throws InterruptedException {
        final Object result = new Object();
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {
            @Override
            public Object call() throws Exception {
                super.call();
                return result;
            }
        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onSuccess(result);
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onCancelled(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
    }

    public void testCancelledExecution() throws InterruptedException {
        AsyncTask<Object> task = new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {
            @Override
            public Object call() throws Exception {
                super.call();
                Thread.sleep(ONE_SECOND);
                return null;
            }
        }.execute();

        Thread.sleep(HALF_SECOND);
        task.cancel();
        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onCancelled(any(Object.class));
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onSuccess(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
    }

    public void testCallExceptionThrown() throws InterruptedException {
        final Exception ex = new Exception(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @Override
            public Object call() throws Exception {
                super.call();
                throw ex;
            }

        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any());
        verify(mSafeAsyncTaskCallback).call(any());
        verify(mSafeAsyncTaskCallback).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any());
        verify(mSafeAsyncTaskCallback, never()).onSuccess(any());
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(InterruptedException.class));
    }

     /* Test whether RuntimeExceptions are actually thrown, and not caught */

    public void testCancelledInterruptedExecution() throws InterruptedException {
        AsyncTask<Object> task = new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {
            @Override
            public Object call() throws Exception {
                super.call();
                Thread.sleep(ONE_SECOND);
                return null;
            }
        }.execute();

        Thread.sleep(HALF_SECOND);
        task.cancelInterrupt();
        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onInterrupted(any(InterruptedException.class));
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onCancelled(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onSuccess(any(Object.class));
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onRuntimeException(any(RuntimeException.class));
    }

    public void testPreExecuteRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                throw rte;
            }
        }.execute();

        mCountDownLatch.await();
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);
    }

    public void testSuccessRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @Override
            protected void onSuccess(final Object o) {
                super.onSuccess(o);
                throw rte;
            }
        }.execute();

        mCountDownLatch.await();
        verify(mSafeAsyncTaskCallback).onPreExecute(any(Object.class));
        verify(mSafeAsyncTaskCallback).call(any(Object.class));
        verify(mSafeAsyncTaskCallback).onSuccess(any(Object.class));
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);
        verify(mSafeAsyncTaskCallback).onFinally(any(Object.class));

        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any(Object.class));
    }

    public void testCancelledRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        AsyncTask<Object> task = new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @Override
            public Object call() throws Exception {
                super.call();
                Thread.sleep(ONE_SECOND);
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                throw rte;
            }
        }.execute();

        Thread.sleep(HALF_SECOND);
        task.cancel();
        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any());
        verify(mSafeAsyncTaskCallback).call(any());
        verify(mSafeAsyncTaskCallback).onCancelled(any());
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onSuccess(any());
        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(InterruptedException.class));
    }


    public void testExceptionRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @Override
            public Object call() throws Exception {
                super.call();
                throw new Exception();
            }

            @Override
            protected void onException(final Exception e) {
                super.onException(e);
                throw rte;
            }
        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any());
        verify(mSafeAsyncTaskCallback).call(any());
        verify(mSafeAsyncTaskCallback).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onCancelled(any());
        verify(mSafeAsyncTaskCallback, never()).onSuccess(any());
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(InterruptedException.class));
    }

    public void testFinallyRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @SuppressWarnings("RefusedBequest")
            @Override
            protected void onFinally() {
                assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
                mSafeAsyncTaskCallback.onFinally(new Object());
                throw rte;
            }

            @Override
            protected void onRuntimeException(final RuntimeException e) {
                super.onRuntimeException(e);
                mCountDownLatch.countDown();
            }
        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any());
        verify(mSafeAsyncTaskCallback).call(any());
        verify(mSafeAsyncTaskCallback).onSuccess(any());
        verify(mSafeAsyncTaskCallback).onFinally(any());
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);

        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any());
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(InterruptedException.class));
    }

    public void testCallRuntimeExceptionThrown() throws InterruptedException {
        final RuntimeException rte = new RuntimeException(EXPECTED_EXCEPTION_MESSAGE);
        new TestAsyncTask(mSafeAsyncTaskCallback, mCountDownLatch) {

            @Override
            public Object call() throws Exception {
                super.call();
                throw rte;
            }

        }.execute();

        mCountDownLatch.await();

        verify(mSafeAsyncTaskCallback).onPreExecute(any());
        verify(mSafeAsyncTaskCallback).call(any());
        verify(mSafeAsyncTaskCallback).onRuntimeException(rte);
        verify(mSafeAsyncTaskCallback).onFinally(any());

        verify(mSafeAsyncTaskCallback, never()).onException(any(Exception.class));
        verify(mSafeAsyncTaskCallback, never()).onCancelled(any());
        verify(mSafeAsyncTaskCallback, never()).onSuccess(any());
        verify(mSafeAsyncTaskCallback, never()).onInterrupted(any(InterruptedException.class));
    }

    /**
     * A test AsyncTask which:
     * - Notifies a callback
     * - Counts down a CountDownLatch when finished
     * - Checks whether calls are made on the correct thread.
     */
    private static class TestAsyncTask extends AsyncTask<Object> {

        private final SafeAsyncTaskCallback<Object> mSafeAsyncTaskCallback;
        private final CountDownLatch mCountDownLatch;

        private TestAsyncTask(final SafeAsyncTaskCallback<Object> safeAsyncTaskCallback, final CountDownLatch countDownLatch) {
            mSafeAsyncTaskCallback = safeAsyncTaskCallback;
            mCountDownLatch = countDownLatch;
        }

        @Override
        public Object call() throws Exception {
            mSafeAsyncTaskCallback.call(new Object());
            assertThat(Looper.getMainLooper().getThread(), is(not(Thread.currentThread())));
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSafeAsyncTaskCallback.onPreExecute(new Object());

            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
        }

        @Override
        protected void onSuccess(final Object t) {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onSuccess(t);
        }

        @SuppressWarnings("RefusedBequest")
        @Override
        protected void onInterrupted(final InterruptedException e) {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onInterrupted(e);
        }

        @Override
        protected void onCancelled() {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onCancelled(new Object());
        }

        @Override
        protected void onException(final Exception e) {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onException(e);
        }

        @Override
        protected void onRuntimeException(final RuntimeException e) {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onRuntimeException(e);
        }

        @Override
        protected void onFinally() {
            assertThat(Looper.getMainLooper().getThread(), is(Thread.currentThread()));
            mSafeAsyncTaskCallback.onFinally(new Object());
            mCountDownLatch.countDown();
        }
    }

    @SuppressWarnings("InterfaceNeverImplemented")
    /** A callback interface to validate method calls using Mockito. */
    /* Extra Object parameters are added because dexmaker fails otherwise */
    private interface SafeAsyncTaskCallback<T> {
        public void call(Object o);

        public void onPreExecute(Object o);

        public void onSuccess(T t);

        public void onInterrupted(Exception e);

        public void onCancelled(Object o);

        public void onException(Exception e);

        public void onRuntimeException(RuntimeException e);

        public void onFinally(Object o);
    }
}
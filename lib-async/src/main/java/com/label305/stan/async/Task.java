package com.label305.stan.async;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("AnonymousInnerClass")
class Task<ResultT> implements Runnable {

    @NotNull
    private final AsyncTask<ResultT> mParent;

    @NotNull
    private final Handler mHandler;

    Task(@NotNull final AsyncTask<ResultT> parent) {
        mParent = parent;
        mHandler = parent.getHandler() != null ? parent.getHandler() : new Handler(Looper.getMainLooper());
    }

    @SuppressWarnings("NestedTryStatement")
    @Override
    public void run() {
        try {
            doPreExecute();

            boolean success = false;
            ResultT result = null;
            try {
                result = doCall();
                success = true;
            } catch (RuntimeException e) {
                doRuntimeException(e);
            } catch (@NotNull final Exception e) {
                doException(e);
            }

            if (success) {
                if (mParent.isCancelled()) {
                    doCancel();
                } else {
                    doSuccess(result);
                }
            }
        } catch (RuntimeException e) {
            doRuntimeException(e);
        } finally {
            try {
                doFinally();
            } catch (RuntimeException e) {
                doRuntimeException(e);
            }
        }
    }

    protected void doPreExecute() {
        postToUiThreadAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        mParent.onPreExecute();
                    }
                }
        );
    }

    @Nullable
    @SuppressWarnings("ProhibitedExceptionDeclared")
    protected ResultT doCall() throws Exception {
        return mParent.call();
    }

    protected void doSuccess(@Nullable final ResultT r) {
        postToUiThreadAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        mParent.onSuccess(r);
                    }
                }
        );
    }

    protected void doCancel() {
        postToUiThreadAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        mParent.onCancelled();
                    }
                }
        );
    }

    protected void doException(@NotNull final Exception e) {
        StackTraceElement[] launchLocation = mParent.getLaunchLocation();
        if (launchLocation != null) {
            final ArrayList<StackTraceElement> stack = new ArrayList<>(Arrays.asList(e.getStackTrace()));
            stack.addAll(Arrays.asList(launchLocation));
            e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
        }
        postToUiThreadAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof InterruptedException) {
                            mParent.onInterrupted((InterruptedException) e);
                        } else {
                            mParent.onException(e);
                        }
                    }
                }
        );
    }

    protected void doRuntimeException(@NotNull final RuntimeException e) {
        StackTraceElement[] launchLocation = mParent.getLaunchLocation();
        if (launchLocation != null) {
            final ArrayList<StackTraceElement> stack = new ArrayList<>(Arrays.asList(e.getStackTrace()));
            stack.addAll(Arrays.asList(launchLocation));
            e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
        }
        mHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        mParent.onRuntimeException(e);
                    }
                }
        );
    }

    protected void doFinally() {
        postToUiThreadAndWait(
                new Runnable() {
                    @Override
                    public void run() {
                        mParent.onFinally();
                    }
                }
        );
    }

    /**
     * Posts the specified runnable to the UI thread using a handler, and
     * waits for operation to finish.
     *
     * @param runnable the runnable to post
     */
    protected void postToUiThreadAndWait(@NotNull final Runnable runnable) {
        final RuntimeException[] exceptions = new RuntimeException[1];
        final CountDownLatch latch = new CountDownLatch(1);

        // Execute the runnable in the UI thread, but wait for it to complete.
        mHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runnable.run();
                        } catch (RuntimeException e) {
                            exceptions[0] = e;
                        } finally {
                            latch.countDown();
                        }
                    }
                }
        );

        // Wait for onSuccess to finish
        try {
            latch.await();
        } catch (@NotNull final InterruptedException e) {
            mHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            mParent.onInterrupted(e);
                        }
                    }
            );
        }

        if (exceptions[0] != null) {
            //noinspection ProhibitedExceptionThrown
            throw exceptions[0];
        }
    }

    @NotNull
    protected AsyncTask<ResultT> getParent() {
        return mParent;
    }
}
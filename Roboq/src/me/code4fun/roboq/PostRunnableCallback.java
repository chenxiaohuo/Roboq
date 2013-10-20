package me.code4fun.roboq;


import android.os.Handler;

/**
 * @since 0.1
 */
public abstract class PostRunnableCallback extends HandlerCallback {
    public PostRunnableCallback(Handler handler) {
        super(handler);
    }

    @Override
    public void onResponse(Request req, Response resp, Exception error) {
        Runnable r = createRunnable(req, resp, error);
        if (r != null)
            processRunnable(r);
    }

    protected abstract Runnable createRunnable(Request req, Response resp, Exception error);

    protected void processRunnable(Runnable r) {
        handler.post(r);
    }
}

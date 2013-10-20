package me.code4fun.roboq;


import android.os.Handler;
import android.os.Message;

/**
 * @since 0.1
 */
public abstract class SendMessageCallback extends HandlerCallback {
    public SendMessageCallback(Handler handler) {
        super(handler);
    }

    @Override
    public void onResponse(Request req, Response resp, Exception error) {
        Message msg = createMessage(req, resp, error);
        if (msg != null)
           processMessage(msg);
    }

    protected abstract Message createMessage(Request req, Response resp, Exception error);

    protected void processMessage(Message msg) {
        handler.sendMessage(msg);
    }
}

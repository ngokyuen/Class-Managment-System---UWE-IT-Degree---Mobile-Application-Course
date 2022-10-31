package general;

import android.app.Activity;
import android.content.Context;

import com.tedngok.classmanagementsystem.R;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class WebSocketIO {

    public static Socket _socket;
    private Context _context;
    private Activity _activity;

    public WebSocketIO(Context context){
        this._context = context;
        this.startSocketConnect();
    }

    private void startSocketConnect(){

        if (_socket == null){
            try {
                _socket = IO.socket(_context.getString(R.string.websocket_server));
                _socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        _socket.connect();
    }
}

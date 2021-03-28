package com.hichigo;

import com.hichigo.websocket.BackendWebSocket;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
    public static void main(String[] args) {
        BackendWebSocket ws = new BackendWebSocket(4242);
        ws.start();
    }
}

package com.rubix.core.Resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.rubix.TokenTransfer.TokenReceiver;
import org.json.JSONException;
public class Receiver implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                TokenReceiver.receive();
            } catch (JSONException e) {
                System.out.println("JSON Exception occured : "+e);
            }
              catch(NullPointerException nullPointerException){
                  System.out.println("Null Pointer Exception occured : "+nullPointerException);
              }
        }
    }
}

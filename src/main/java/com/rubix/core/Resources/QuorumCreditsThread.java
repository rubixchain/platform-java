package com.rubix.core.Resources;

import com.rubix.Ping.QuorumSendCredits;
import org.json.JSONException;

import static com.rubix.Resources.Functions.QUORUM_PORT;
import static com.rubix.Resources.Functions.pathSet;

public class QuorumCreditsThread implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                pathSet();
                QuorumSendCredits.sendCredits(QUORUM_PORT + 420);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

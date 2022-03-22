package com.rubix.core.Controllers;

import static com.rubix.Resources.APIHandler.*;
import static com.rubix.Resources.Functions.*;
import static com.rubix.core.Controllers.Basics.checkRubixDir;
import static com.rubix.core.Controllers.Basics.mutex;
import static com.rubix.core.Controllers.Basics.start;
import static com.rubix.core.Resources.CallerFunctions.mainDir;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.Functions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:1898")
@RestController

public class Wallet {

    @RequestMapping(value = "/getAccountInfo", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String getAccountInfo() throws JSONException, IOException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();

        JSONObject result = new JSONObject();

        String getAccountInfoValue = "";
        ExecutorService getAccountInfoEs = Executors.newSingleThreadExecutor();
        Future<String> getAccountInfoFutureResult = getAccountInfoEs.submit(new Callable<String>() {
            public String call() throws Exception {
                /*
                 * // the other thread
                 * JSONObject z= new JSONObject();
                 * z.put("test", "test value");
                 * return z.toString();
                 */

                JSONArray accountInfo = accountInformation();
                JSONObject accountObject = accountInfo.getJSONObject(0);
                accountObject.put("balance", getBalance());
                accountObject.put("credits", creditsInfo());

                JSONObject result = new JSONObject();
                JSONObject contentObject = new JSONObject();
                contentObject.put("response", accountObject);
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");

                return result.toString();
            }
        });
        try {
            getAccountInfoValue = getAccountInfoFutureResult.get();
        } catch (Exception e) {
            System.out.println("Error occured in /getAccountInfo");
            e.printStackTrace();
        }
        getAccountInfoEs.shutdown();
        return getAccountInfoValue;
    }

    @RequestMapping(value = "/getDashboard", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String getDashboard() throws JSONException, IOException, InterruptedException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();
        JSONObject result = new JSONObject();

        String getDashboardValue = "";
        ExecutorService getDashboardEs = Executors.newSingleThreadExecutor();
        Future<String> getDashboardFuture = getDashboardEs.submit(new Callable<String>() {
            public String call() throws Exception {
                JSONArray contactsObject = contacts();
                int contactsCount = contactsObject.length();
                System.out.println(contactsCount);

                JSONArray accountInfo = accountInformation();
                JSONObject accountObject = accountInfo.getJSONObject(0);
                System.out.println(accountObject);

                JSONArray dateTxn = txnPerDay();
                JSONObject dateTxnObject = dateTxn.getJSONObject(0);
                System.out.println(dateTxnObject);

                // To display the Mine Count of the wallet - Reading from
                // QuorumSignedTransactions
                String content = readFile(WALLET_DATA_PATH.concat("QuorumSignedTransactions.json"));
                JSONArray contentArray = new JSONArray(content);
                JSONArray finalArray = new JSONArray();
                for (int j = 0; j < contentArray.length(); j++) {
                    if (contentArray.getJSONObject(j).has("minestatus")) {
                        if (!contentArray.getJSONObject(j).getBoolean("minestatus"))
                            finalArray.put(contentArray.getJSONObject(j));
                    } else
                        finalArray.put(contentArray.getJSONObject(j));

                }
                System.out.println(finalArray);

                int totalTxn = accountObject.getInt("senderTxn") + accountObject.getInt("receiverTxn");
                accountObject.put("totalTxn", totalTxn);
                try {
                    accountObject.put("onlinePeers", onlinePeersCount());
                } catch (JSONException | IOException | InterruptedException e) {
                    System.out.println("Error occured " + e);
                    e.printStackTrace();
                }
                accountObject.put("contactsCount", contactsCount);
                accountObject.put("transactionsPerDay", dateTxnObject);
                accountObject.put("balance", getBalance());
                accountObject.put("proofCredits", finalArray.length());

                JSONObject contentObject = new JSONObject();
                contentObject.put("response", accountObject);
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");
                return result.toString();
            }
        });
        try {
            getDashboardValue = getDashboardFuture.get();
        } catch (Exception e) {
            System.out.println("Error occured in /getDashboard");
            e.printStackTrace();
        }
        getDashboardEs.shutdown();
        return getDashboardValue;

    }

    @RequestMapping(value = "/getOnlinePeers", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String getOnlinePeers() throws IOException, JSONException, InterruptedException {
        if (!mainDir())
            return checkRubixDir();

        if (!mutex)
            start();
        String getOnlinePeersResult = "";
        JSONObject result = new JSONObject();
        ExecutorService getOnlinePeersES = Executors.newSingleThreadExecutor();
        Future<String> getOnlinePeersResultFuture = getOnlinePeersES.submit(new Callable<String>() {
            public String call() throws Exception {
                JSONObject contentObject = new JSONObject();
                try {
                    contentObject.put("response", peersOnlineStatus());
                } catch (JSONException | IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Error occured for getOnlinePeers" + e);
                    e.printStackTrace();
                }
                try {
                    contentObject.put("count", peersOnlineStatus().length());
                } catch (JSONException | IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Error occured " + e);
                    e.printStackTrace();
                }
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");

                return result.toString();
            }
        });

        try {
            getOnlinePeersResult = getOnlinePeersResultFuture.get();

        } catch (Exception e) {
            System.out.println("Error occured in /getOnlinePeers");
            e.printStackTrace();
        }

        getOnlinePeersES.shutdown();
        return getOnlinePeersResult;
    }

    @RequestMapping(value = "/getContactsList", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String getContactsList() throws JSONException, IOException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();
        JSONObject result = new JSONObject();
        String getContactsListResult = "";
        ExecutorService getContactsListES = Executors.newSingleThreadExecutor();
        // Thread getContactsList =
        Future<String> getContactsListFuture = getContactsListES.submit(new Callable<String>() {
            public String call() throws Exception {

                String contactsTable = readFile(DATA_PATH + "Contacts.json");
                JSONArray contactsArray = new JSONArray(contactsTable);

                String DIDFile = readFile(DATA_PATH + "DID.json");
                JSONArray didArray = new JSONArray(DIDFile);
                String myDID = didArray.getJSONObject(0).getString("didHash");
                JSONArray finalArray = new JSONArray();

                for (int i = 0; i < contactsArray.length(); i++) {
                    if (!(contactsArray.getJSONObject(i).getString("did").equals(myDID)))
                        finalArray.put(contactsArray.getJSONObject(i));
                }

                JSONObject contentObject = new JSONObject();
                contentObject.put("response", finalArray);
                contentObject.put("count", finalArray.length());
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");

                return result.toString();
            }
        });
        try {
            getContactsListResult = getContactsListFuture.get();
        } catch (Exception e) {
            System.out.println("Error occured in /getContactsList");
            e.printStackTrace();
        }
        getContactsListES.shutdown();
        return getContactsListResult;

    }

    @RequestMapping(value = "/getNetworkNodes", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String getNetworkNodes() throws JSONException, IOException, InterruptedException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();

        JSONObject result = new JSONObject();
        // Thread getNetworkNodesThread =
        String getNetworkNodesValue = "";
        ExecutorService getNetworkNodeses = Executors.newSingleThreadExecutor();
        Future<String> getNetworkNodesfutureResult = getNetworkNodeses.submit(new Callable<String>() {
            public String call() throws Exception {
                JSONObject contentObject = new JSONObject();
                contentObject.put("response", contacts());
                contentObject.put("count", contacts().length());
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");
                return result.toString();
            }
        });
        try {
            getNetworkNodesValue = getNetworkNodesfutureResult.get();
        } catch (Exception e) {
            System.out.println("Error occured in /getNetworkNodes");
            e.printStackTrace();
        }
        getNetworkNodeses.shutdown();
        return getNetworkNodesValue;
    }

    @RequestMapping(value = "/viewTokens", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String viewTokens() throws JSONException, IOException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();
        JSONObject result = new JSONObject();

        // Thread viewTokensThread =
        String viewTokensValue = "";
        ExecutorService viewTokensES = Executors.newSingleThreadExecutor();
        Future<String> viewTokensfutureResult = viewTokensES.submit(new Callable<String>() {
            public String call() throws Exception {
                File directoryPath = new File(TOKENS_PATH);
                String[] contents = directoryPath.list();

                JSONArray returnTokens = new JSONArray();
                for (String content : contents)
                    returnTokens.put(content);

                JSONObject contentObject = new JSONObject();
                contentObject.put("response", returnTokens);
                contentObject.put("count", returnTokens.length());
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");
                return result.toString();
            }
        });
        try {
            viewTokensValue = viewTokensfutureResult.get();
        } catch (Exception e) {
            System.out.println("Error occured in /viewTokens");
            e.printStackTrace();
        }
        viewTokensES.shutdown();
        return viewTokensValue;

    }

    @RequestMapping(value = "/checkPartBalance", method = RequestMethod.GET, produces = { "application/json",
            "application/xml" })
    public String checkPartBalance(@RequestParam("token") String token) throws JSONException, IOException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();

        JSONObject result = new JSONObject();
        // Thread checkPartBalanceThread =
        String checkPartBalanceValue = "";
        ExecutorService checkPartBalanceEs = Executors.newSingleThreadExecutor();
        Future<String> checkPartBalancefuture = checkPartBalanceEs.submit(new Callable<String>() {
            public String call() throws Exception {

                JSONObject contentObject = new JSONObject();
                contentObject.put("response", checkTokenPartBalance(token));
                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");
                return result.toString();
            }
        });
        try {
            checkPartBalanceValue = checkPartBalancefuture.get();
        } catch (Exception e) {
            System.out.println("Error occured in /checkPartBalance");
            e.printStackTrace();
        }
        checkPartBalanceEs.shutdown();
        return checkPartBalanceValue;

    }

    @RequestMapping(value = "/addNickName", method = RequestMethod.POST, produces = { "application/json",
            "application/xml" })
    public static String addNickName(@RequestParam("did") String did, @RequestParam("nickname") String nickname)
            throws JSONException, IOException, InterruptedException {
        if (!mainDir())
            return checkRubixDir();
        if (!mutex)
            start();

        JSONObject result = new JSONObject();
        // Thread addNickNameThread =
        String addNickNameValue = "";
        ExecutorService addNickNameEs = Executors.newSingleThreadExecutor();
        Future<String> addNickNamefuture = addNickNameEs.submit(new Callable<String>() {
            public String call() throws Exception {

                JSONObject contentObject = new JSONObject();
                pathSet();
                String contactsFile = readFile(DATA_PATH + "Contacts.json");
                JSONArray contactsArray = new JSONArray(contactsFile);

                for (int i = 0; i < contactsArray.length(); i++) {
                    if (contactsArray.getJSONObject(i).getString("did").equals(did)) {
                        contentObject.put("response", "DID already assigned with same/another NickName");
                        contentObject.put("did", did);
                        contentObject.put("nickname", contactsArray.getJSONObject(i).getString("nickname"));
                        result.put("data", contentObject);
                        result.put("message", "");
                        result.put("status", "true");
                        return result.toString();
                        // return result.toString();
                    }
                }
                for (int i = 0; i < contactsArray.length(); i++) {
                    if (contactsArray.getJSONObject(i).getString("nickname").equals(nickname)) {
                        contentObject.put("response", "Nickname already assigned to same/another DID");
                        contentObject.put("did", nickname);
                        contentObject.put("nickname", contactsArray.getJSONObject(i).getString("did"));
                        result.put("data", contentObject);
                        result.put("message", "");
                        result.put("status", "true");
                        // return result.toString();
                        return result.toString();
                    }
                }

                JSONObject contactObject = new JSONObject();
                contactObject.put("did", did);
                contactObject.put("nickname", nickname);
                contactsArray.put(contactObject);

                writeToFile(DATA_PATH + "Contacts.json", contactsArray.toString(), false);
                contentObject.put("response", "Added");

                result.put("data", contentObject);
                result.put("message", "");
                result.put("status", "true");
                return result.toString();
            }
        });
        try {
            addNickNameValue = addNickNamefuture.get();
        } catch (Exception e) {
            System.out.println("Error occured in /addNickName");
            e.printStackTrace();
        }
        addNickNameEs.shutdown();
        return addNickNameValue;

    }
}

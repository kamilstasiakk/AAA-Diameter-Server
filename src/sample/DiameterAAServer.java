package sample;

import dk.i1.diameter.*;
import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.NodeManager;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static sample.Main.PrintedStrings;

/**
 * Created by Kamil on 2016-11-27.
 */
public class DiameterAAServer extends NodeManager {

    private List<IdentyficatingUserRecord> identyficatingUserRecords;
    private Dictionary<String, String> userToPasswordDict;
    private Dictionary<String, String> userToSecretDict;
    private Dictionary<String, ServicingUserEntry> curentlyServicing;
    char lastId;
    private String clusterAddress;

    public String getClusterAddress() {
        return clusterAddress;
    }

    public void setClusterAddress(String clusterAddress) {
        this.clusterAddress = clusterAddress;
    }

    public void register(PrintCallBack printCallBack){ printCallBack.printLogs(); }

    public DiameterAAServer(NodeSettings node_settings) {
        super(node_settings);
        identyficatingUserRecords = new ArrayList<IdentyficatingUserRecord>();
        userToPasswordDict = new Hashtable();
        userToSecretDict = new Hashtable<>();
        curentlyServicing = new Hashtable<>();
        lastId = 33;
    }


    protected void handleRequest(dk.i1.diameter.Message request, ConnectionKey connkey, Peer peer) {
        //this is not the way to do it, but fine for a lean-and-mean test server

        String log = "";
        Message answer = new Message();
        answer.prepareResponse(request);
        AVP avp;
        avp = request.find(ProtocolConstants.DI_SESSION_ID);
        if (avp != null)
            answer.add(avp);
        node().addOurHostAndRealm(answer);
        //avp = request.find(ProtocolConstants.DI_CC_REQUEST_TYPE); DIAMETER_COMMAND_AA
        avp = request.find(ProtocolConstants.DI_AUTH_REQUEST_TYPE);
        if (avp == null) {
            answerError(answer, connkey, ProtocolConstants.DIAMETER_RESULT_MISSING_AVP,
                    new AVP[]{new AVP_Grouped(ProtocolConstants.DI_FAILED_AVP, new AVP[]{new AVP(ProtocolConstants.DI_AUTH_REQUEST_TYPE, new byte[]{})})});
            return;
        }


        int aa_request_type = -1;
        try {
            aa_request_type = new AVP_Unsigned32(avp).queryValue();
        } catch (InvalidAVPLengthException ex) {
        }
        if (aa_request_type != ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHENTICATE &&
                aa_request_type != ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHENTICATE_ONLY &&
                aa_request_type != ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHORIZE_ONLY) {
            answerError(answer, connkey, ProtocolConstants.DIAMETER_RESULT_INVALID_AVP_VALUE,
                    new AVP[]{new AVP_Grouped(ProtocolConstants.DI_FAILED_AVP, new AVP[]{avp})});
            return;
        }



        avp = request.find(ProtocolConstants.DI_AUTH_APPLICATION_ID);
        if (avp != null)
            answer.add(avp);
        avp = request.find(ProtocolConstants.DI_AUTH_REQUEST_TYPE);
        if (avp != null)
            answer.add(avp);


        switch (aa_request_type) {
            case ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHENTICATE_ONLY:
            case ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHENTICATE:
                //grant whatever is requested
                avp = request.find(ProtocolConstants.DI_USER_NAME);
                answer.add(avp);
                String userName = new AVP_UTF8String(avp).queryValue();
                avp = request.find(ProtocolConstants.DI_USER_PASSWORD);
                String userPassword = new AVP_UTF8String(avp).queryValue();


                synchronized (PrintedStrings.stringsToPrint) {
                    System.out.println(log = "UserName: " + userName);
                    PrintedStrings.stringsToPrint.add(log);
                    System.out.println(log = "password: " + userPassword);
                    PrintedStrings.stringsToPrint.add(log);

                    //TODO Sprawdzic to jesli nie ma z w slowniku
                    String pass = userToPasswordDict.get(userName);
                    if (pass == null) {
                        System.out.println(log="nie ma takiego uzytkownika");

                        PrintedStrings.stringsToPrint.add(log);
                    }
                    if (userPassword.equals(pass)) {

                        avp = request.find(ProtocolConstants.DI_CHAP_AUTH);
                        if (avp != null) {
                            try {
                                AVP_Grouped chapAuth = new AVP_Grouped(avp);
                                AVP[] elements = chapAuth.queryAVPs();
                                byte[] idBytes = new AVP_OctetString(elements[1]).queryValue();
                                char id = (char) idBytes[0];
                                System.out.println(log = "id: " + id);
                                PrintedStrings.stringsToPrint.add(log);

                                byte[] chapResponseBytes = new AVP_OctetString(elements[2]).queryValue();
                                printBytesAsString(chapResponseBytes, "odebrane rozwiazanie ");
                                log = getBytesAsString(chapResponseBytes, "odebrane rozwiazanie ");
                                PrintedStrings.stringsToPrint.add(log);

                                byte[] chapChallengeBytes = new AVP_OctetString(elements[3]).queryValue();
                                printBytesAsString(chapChallengeBytes, "odebrane zadanie ");
                                log = getBytesAsString(chapResponseBytes, "odebrane zadanie ");
                                PrintedStrings.stringsToPrint.add(log);

                                //sprawdzenie czy nie ma ataku przez odtwarzanie
                                ServicingUserEntry sessionEntry = curentlyServicing.get(userName);
                                if (sessionEntry == null) {
                                    System.out.println(log = "Atak/ pakiet dotarl po upłynieciu czasu oczekiwania/ pakiet został powtórzony");
                                    //w takiej sytuacji  nie odpowiadamy wcale
                                    PrintedStrings.stringsToPrint.add(log);
                                    return;
                                }
                                Date now = new Date();
                                if (id != sessionEntry.getId() ||
                                        !Arrays.equals(chapChallengeBytes, sessionEntry.getChallenge()) ||
                                        (now.getTime() - sessionEntry.getTime().getTime()) > 5000) {

                                    System.out.println(log = "Atak/ pakiet dotarl po upłynieciu czasu oczekiwania/ przekłamanie pakietu");
                                    //w takiej sytuacji  nie odpowiadamy wcale
                                    PrintedStrings.stringsToPrint.add(log);
                                    return;
                                }
                                curentlyServicing.remove(userName);
                                byte[] md5 = caluculateMD5(idBytes, userToSecretDict.get(userName).getBytes("ASCII"), chapChallengeBytes);
                                printBytesAsString(chapResponseBytes, "obliczone rozwiazanie ");
                                log = getBytesAsString(chapResponseBytes, "obliczone rozwiazanie ");
                                PrintedStrings.stringsToPrint.add(log);

                                if (Arrays.equals(chapResponseBytes, md5)) {

                                    answer.add(new AVP_OctetString(ProtocolConstants.DI_FRAMED_IP_ADDRESS, clusterAddress.getBytes()));
                                    answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_SUCCESS));
                                    System.out.println(log = "Uwierzytelnionio, pozwalam na dołaczenie do klastra");
                                    PrintedStrings.stringsToPrint.add(log);
                                } else {
                                    answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_AUTHENTICATION_REJECTED));
                                    System.out.println(log = "Błędnie rozwiązane zadanie");
                                    PrintedStrings.stringsToPrint.add(log);
                                }
                            } catch (InvalidAVPLengthException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        } else {
                            answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_MULTI_ROUND_AUTH));
                            //zwiekszamy ostatnio uzywany index o 1 i korzystamy z niego
                            incrementIndex();
                            byte[] ind = new byte[]{(byte) lastId};
                            byte[] challenge = generateChallenge();
                            printBytesAsString(challenge, "wygenerowane zadanie ");
                            log = getBytesAsString(challenge, "wygenerowane zadanie ");
                            PrintedStrings.stringsToPrint.add(log);


                            curentlyServicing.put(userName, new ServicingUserEntry(userName, challenge, lastId));
                            //System.out.println("generated chalenge : " + challenge.toString());
                            answer.add(new AVP_Grouped(ProtocolConstants.DI_CHAP_AUTH,
                                    new AVP_Integer32(ProtocolConstants.DI_CHAP_ALGORITHM, 5),
                                    new AVP_OctetString(ProtocolConstants.DI_CHAP_IDENT, ind),
                                    new AVP_OctetString(ProtocolConstants.DI_CHAP_CHALLENGE, challenge)));


                            if(lastId==34)
                                serverGUIController.firstInClaster = true; //TODO obsluz wszsytkich
                            if(lastId==35)
                                serverGUIController.secondInClaster = true;
                            if(lastId==36)
                                serverGUIController.thirdInClaster = true;
                            if(lastId==37)
                                serverGUIController.fourthInClaster = true;
                        }
                    } else {
                        answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, ProtocolConstants.DIAMETER_RESULT_AUTHENTICATION_REJECTED));
                        log="podane hasło jest niepoprawne";
                        PrintedStrings.stringsToPrint.add(log);
                    }
                }
            case ProtocolConstants.DI_AUTH_REQUEST_TYPE_AUTHORIZE_ONLY:
                break;
        }

        Utils.setMandatory_RFC3588(answer);

        try {
            answer(answer, connkey);
        } catch (dk.i1.diameter.node.NotAnAnswerException ex) {
        }

        switch (lastId) {
            case 34: {
                serverGUIController.firstConnected = true;
                serverGUIController.firstTextClear = true;
                break;
            }
            case 35:{
                serverGUIController.secondConnected = true;
                serverGUIController.secondTextClear = true;
                break;
            }
            case 36:{
                serverGUIController.thirdConnected = true;
                serverGUIController.thirdTextClear = true;
                break;
            }
            case 37:{
                serverGUIController.fourthCOnnected = true;
                serverGUIController.fourthTextClear = true;
                break;
            }

        }

    }

    void answerError(dk.i1.diameter.Message answer, ConnectionKey connkey, int result_code, AVP[] error_avp) {
        answer.hdr.setError(true);
        answer.add(new AVP_Unsigned32(ProtocolConstants.DI_RESULT_CODE, result_code));
        for (AVP avp : error_avp)
            answer.add(avp);
        try {
            answer(answer, connkey);
        } catch (dk.i1.diameter.node.NotAnAnswerException ex) {
        }
    }

    private byte[] generateChallenge() {
        Random generator = new Random();
        byte[] byteChallenge = new byte[32];
        char c;
        int j;
        for( int i = 0; i < 32; i++) {
            byteChallenge[i] = (byte) ((char) generator.nextInt(90)+32);
        }

        return  byteChallenge;
    }

    public void addUser(String username, String password, String secret) {
        userToPasswordDict.put(username, password);
        userToSecretDict.put(username, secret);
    }

    public void deleteUser(String username) {
        userToPasswordDict.remove(username);
        userToSecretDict.remove(username);
    }
    public void changeUserPass(String username, String newPass) {
        userToPasswordDict.remove(username);
        userToPasswordDict.put(username, newPass);
    }

    public void changeUserSecret (String username, String secret) {
        userToSecretDict.remove(username);
        userToSecretDict.put(username, secret);
    }

    public byte[] caluculateMD5(byte[] id, byte[] secret, byte[] chellange) {
        String log = "";
        java.security.MessageDigest md = null;
        byte[] word = concatenateBytes(
                concatenateBytes(id,secret), chellange);
        printBytesAsString(word, "licze md5 dla ");
        log = getBytesAsString(word, "licze md5 dla ");
        PrintedStrings.stringsToPrint.add(log);

        try {
            md = java.security.MessageDigest.getInstance("MD5");
            md.reset();
            md.update(word);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    void printBytesAsString(byte[] bytes, String text)  {
        char[] charChallange2 = new char[bytes.length];
        for( int i = 0; i < bytes.length; i++) {
            charChallange2[i] = (char) bytes[i];
        }
        System.out.println(text + String.valueOf(charChallange2));
    }

    String getBytesAsString(byte[] bytes, String text)  {
        char[] charChallange2 = new char[bytes.length];
        for( int i = 0; i < bytes.length; i++) {
            charChallange2[i] = (char) bytes[i];
        }

        return text + String.valueOf(charChallange2);
    }

    byte[] concatenateBytes(byte[] a, byte[] b) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write( a );
            outputStream.write( b );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray( );
    }

    void incrementIndex() {
        if (lastId == 255) {
            lastId = 0;
        }
        else {
            lastId++;
        }
    }

}
package sample;

import dk.i1.diameter.ProtocolConstants;
import dk.i1.diameter.node.*;

import java.io.IOException;

/**
 * Created by Kamil on 2016-11-27.
 */
public class DiameterServer  {



    public DiameterAAServer addAAServer(String host_id, String realm, int port) {
        Capability capability = new Capability();
        capability.addAuthApp(ProtocolConstants.DIAMETER_APPLICATION_CREDIT_CONTROL);

        NodeSettings node_settings;
        try {
            node_settings  = new NodeSettings(
                    host_id, realm,
                    99999, //vendor-id
                    capability,
                    port,
                    "bb_server", 0x01000000);
        } catch (InvalidSettingException e) {
            System.out.println(e.toString());
            return null;
        }
        DiameterAAServer aaServer = new DiameterAAServer(node_settings);
        try {
            aaServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTransportProtocolException e) {
            e.printStackTrace();
        }
        return aaServer;
    }
}
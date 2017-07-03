package sample;

import java.io.IOException;

/**
 * Created by Bartłomiej on 06.01.2017.
 */
public class ServerStarter {

    private static String[] args;

    public ServerStarter(String[] args){
        this.args = args;

    }

    public static void start(String secret){
        Thread t =new Thread(new Runnable() {
            @Override
            public void run() {
                if(args.length<2) {
                    System.out.println("Usage: <host-id> <realm> [<port>]");
                    return;
                }

                String host_id = args[0];
                String realm = args[1];
                int port;
                if(args.length>=3)
                    port = Integer.parseInt(args[2]);
                else
                    port = 3868;

                DiameterServer ds = new DiameterServer();
                DiameterAAServer aaServer = ds.addAAServer(host_id,realm,port);
                aaServer.addUser("user@janusz.pl", "aaa",secret);
                aaServer.addUser("user@example.pl", "aaa", secret);
                aaServer.setClusterAddress("224.0.0.1");

                System.out.println("Hit enter to terminate server");


                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                aaServer.stop(50); //Stop but allow 50ms graceful shutdown
            }
        });
        t.start();
    }
}

import java.io.InputStream;
import java.io.OutputStream;

public class Client2Server extends Thread{
    private InputStream client2proxy;
    private OutputStream proxy2server;

    public Client2Server(InputStream client2proxy, OutputStream proxy2server) {
        this.client2proxy = client2proxy;
        this.proxy2server = proxy2server;
    }

    public void run() {
        int length;
        byte bytes[] = new byte[1024];
        while(true){
            try {
                if ((length = client2proxy.read(bytes)) > 0) {
                    proxy2server.write(bytes, 0, length);   //将http请求写到目标主机
                    proxy2server.flush();
                } else if (length < 0)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

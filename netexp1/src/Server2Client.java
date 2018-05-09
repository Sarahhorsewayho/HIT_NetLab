import java.io.InputStream;
import java.io.OutputStream;

public class Server2Client extends Thread{
    private InputStream server2proxy;
    private OutputStream proxy2client;

    public Server2Client(InputStream server2proxy, OutputStream proxy2client) {
        this.server2proxy = server2proxy;
        this.proxy2client = proxy2client;
    }

    public void run() {
        int length;
        byte bytes[] = new byte[1024];
        while(true){
            try {
                if ((length = server2proxy.read(bytes)) > 0) {
                    proxy2client.write(bytes, 0, length); //把服务器返回的数据写回客户机
                    proxy2client.flush();
                } else if (length < 0)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
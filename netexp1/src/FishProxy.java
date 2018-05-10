import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.InterruptedException;

public class FishProxy extends Thread{

    private Socket connectionSocket;    //与客户端连接的Socket

    public FishProxy(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run () {

        InputStream client2proxy = null ,server2proxy = null;
        OutputStream proxy2client = null ,proxy2server = null;
        Socket proxySocket = null;  //与目标服务器连接的socket
        String firstLine = "", host = "";
        String requestline = "";
        StringBuilder httpHeader = new StringBuilder();
        try {
            client2proxy = connectionSocket.getInputStream();
            proxy2client = connectionSocket.getOutputStream();

            //解析http头部
            InputStreamReader inputStreamReader = new InputStreamReader(client2proxy);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            firstLine = bufferedReader.readLine();
            requestline = firstLine;
            String urlStr = extractUrl(firstLine);
            URL url = new URL(urlStr);  //封装url
            System.out.println(getName() + "++++++" + url.getHost());
            if (!url.getHost().equals("today.hit.edu.cn")) {    //正确转向引导网站
                System.out.println("第二个分支");
                httpHeader.append(requestline); httpHeader.append("\r\n");
                httpHeader.append("Host: "); httpHeader.append(url.getHost()); httpHeader.append("\r\n");
                httpHeader.append("Proxy-Connection: keep-alive"); httpHeader.append("\r\n");
                httpHeader.append("Upgrade-Insecure-Requests: 1"); httpHeader.append("\r\n");
                httpHeader.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); httpHeader.append("\r\n");
                httpHeader.append("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) " +
                        "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1 Safari/605.1.15"); httpHeader.append("\r\n");
                httpHeader.append("Accept-Language: zh-cn"); httpHeader.append("\r\n");
                httpHeader.append("Accept-Encoding: gzip, deflate"); httpHeader.append("\r\n");
                httpHeader.append("Connection: keep-alive"); httpHeader.append("\r\n");
                httpHeader.append("\r\n");
                try {
                    proxySocket = new Socket(url.getHost(), 80);
                } catch (Exception e) {
                }
                if (proxySocket != null) {
                    proxy2server = proxySocket.getOutputStream();
                    server2proxy = proxySocket.getInputStream();
                    proxy2server.write(httpHeader.toString().getBytes());
                    pipe(client2proxy, server2proxy, proxy2server, proxy2client);   //建立通信管道
                }
            } else {    //发出初始请求后，引导向引导网站
                host = "www.hit.edu.cn";
                requestline = "GET http://www.hit.edu.cn HTTP/1.1";
                httpHeader.append(requestline); httpHeader.append("\r\n");
                httpHeader.append("Host: www.hit.edu.cn"); httpHeader.append("\r\n");
                httpHeader.append("Proxy-Connection: keep-alive"); httpHeader.append("\r\n");
                httpHeader.append("Upgrade-Insecure-Requests: 1"); httpHeader.append("\r\n");
                httpHeader.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"); httpHeader.append("\r\n");
                httpHeader.append("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) " +
                        "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1 Safari/605.1.15"); httpHeader.append("\r\n");
                httpHeader.append("Accept-Language: zh-cn"); httpHeader.append("\r\n");
                httpHeader.append("Accept-Encoding: gzip, deflate"); httpHeader.append("\r\n");
                httpHeader.append("Connection: keep-alive"); httpHeader.append("\r\n");
                httpHeader.append("\r\n");
                try {
                    proxySocket = new Socket(host, 80);
                } catch (Exception e) {
                }
                if (proxySocket != null) {
                    proxy2server = proxySocket.getOutputStream();
                    server2proxy = proxySocket.getInputStream();
                    proxy2server.write(httpHeader.toString().getBytes());
                    pipe(client2proxy, server2proxy, proxy2server, proxy2client);   //建立通信管道
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connectionSocket.close();   //关闭套接字
                client2proxy.close();
                proxy2client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                proxySocket.close();    //关闭套接字
                proxy2server.close();
                server2proxy.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从http请求头的第一行提取请求的url
     * @param firstLine http请求头第一行
     * @return url
     */
    public String extractUrl(String firstLine) {
        String[] tokens = firstLine.split(" ");
        String URL = "";
        for (int index = 0; index < tokens.length; index++) {
            if (tokens[index].startsWith("http://")) {
                URL = tokens[index];    //获取url
                break;
            }
        }
        return URL;
    }

    /**
     * 建立通信管道
     * @param client2proxy 客户机向代理服务器所发送的报文信息
     * @param server2proxy 代理服务器向目标服务器的输出流
     * @param proxy2server 服务器返回的信息
     * @param proxy2client 代理服务器向客户机的输出流
     */
    public void pipe(InputStream client2proxy, InputStream server2proxy, OutputStream proxy2server, OutputStream proxy2client) {
        Client2Server client2Server = new Client2Server(client2proxy, proxy2server);    //单开线程建立客户端和代理服务器之间的通信
        Server2Client server2Client = new Server2Client(server2proxy, proxy2client);    //单开线程建立代理服务器和目标服务器之间的通信
        client2Server.start();
        server2Client.start();
        try {
            client2Server.join();
            server2Client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}



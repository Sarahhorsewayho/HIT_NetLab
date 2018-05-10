import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;
import java.lang.InterruptedException;

public class HttpProxy extends Thread{

    public ArrayList<String> bannedWebsite; //禁止访问网站列表
    public ArrayList<String> bannedUsers;   //过滤用户列表

    private Socket connectionSocket;    //与客户端连接的Socket

    public HttpProxy(Socket connectionSocket) {
        bannedWebsite = new ArrayList<String>();
        bannedUsers = new ArrayList<String>();
        bannedWebsite.add("jwts.hit.edu.cn");   //添加禁止访问的网站
        bannedUsers.add("127.0.0.1");   //添加禁止访问的用户
        this.connectionSocket = connectionSocket;
    }

    /**
     * 判断某网站是否存在于禁止访问列表中
     * @param host 主机名
     * @return 布尔值
     */
    public boolean isBannedWebsite (String host) {
        for (String w : bannedWebsite) {
            if (host.equals(w))
                return true;
        }
        return false;
    }

    /**
     * 判断该用户是否禁止访问
     * @param ip 主机ip
     * @return 布尔值
     */
    public boolean isBannedUsers (String ip) {
        for (String u :bannedUsers) {
            if(ip.equals(u)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run () {

        InputStream client2proxy = null ,server2proxy = null;
        OutputStream proxy2client = null ,proxy2server = null;
        Socket proxySocket = null;  //与目标服务器连接的socket
        String firstLine = "", host="";
        try {
            client2proxy = connectionSocket.getInputStream();
            proxy2client = connectionSocket.getOutputStream();

            String ip = InetAddress.getLocalHost().getHostAddress();    //获取本机ip
            System.out.println("本机IP: " + ip);

            //解析http头部首行
            while (true) {
                int c = client2proxy.read();
                if (c == -1)
                    break;  // -1为结尾标志
                if (c == '\r' || c == '\n')
                    break;  // 读入第一行数据,从中获取目标主机url
                firstLine = firstLine + (char) c;
            }
            System.out.println(getName() + ": 请求行：" + firstLine);
            String urlStr = extractUrl(firstLine);
            URL url = new URL(urlStr);  //封装url

            //实现附加功能：网站过滤、用户过滤
            if(isBannedWebsite(url.getHost())) {    // 实现网站过滤
                connectionSocket.close();
                System.out.println(url.getHost() + " --- 此网站禁止访问！");
            } else if (isBannedUsers(ip)) {
                connectionSocket.close();
                System.out.println(ip + " --- 此用户被禁止访问！");
            }

            //如不存在禁用状况，正常与目标服务器建立连接
            try {
                proxySocket = new Socket(url.getHost(), 80);    //利用封装对象的方法获取主机名
            } catch (Exception e) {
            }
            if (proxySocket != null) {
                    proxy2server = proxySocket.getOutputStream();
                    server2proxy = proxySocket.getInputStream();
                    proxy2server.write(firstLine.getBytes());
                    pipe(client2proxy, server2proxy, proxy2server, proxy2client);   //建立通信管道
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



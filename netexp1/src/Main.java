import java.io.*;
import java.net.*;

public class Main {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = null;

        System.out.println("代理服务器正在监听......");

        try {
            serverSocket = new ServerSocket(10250); //欢迎套接字
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept(); //生成连接套接字，建立连接
                    new HttpProxy(socket).start();  //启动主线程
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("主线程启动失败");
                }
            }
        } catch (Exception e) {
            System.out.println("代理服务器启动失败");
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

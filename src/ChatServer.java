import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("채팅 서버가 시작되었습니다.");
        ServerSocket serverSocket = new ServerSocket(23456);  // 포트 23456 사용

        try {
            while (true) {
                // 클라이언트 연결 수락 시 로그 출력
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 클라이언트가 연결되었습니다: " + clientSocket.getInetAddress());

                new Handler(clientSocket).start();  // 클라이언트 핸들러 실행
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 클라이언트 출력 스트림을 서버에서 관리
                writers.add(out);

                String nickname = in.readLine();  // 클라이언트로부터 닉네임 수신
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(nickname + "의 메시지: " + message);  // 서버 로그에 메시지 출력

                    if (message.startsWith("READ")) {
                        // 읽음 확인 메시지를 발신자에게 전달 (원래 메시지 뒤에 읽음 추가)
                        String originalMessage = message.substring(5) + " (읽음)";
                        for (PrintWriter writer : writers) {
                            writer.println("UPDATE " + originalMessage);  // 읽음 상태 업데이트
                        }
                    } else {
                        // 모든 클라이언트에 닉네임과 메시지 전송
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + nickname + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.DefaultCaret;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatClient {
    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chat Client");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    String nickname;  // 닉네임을 저장할 변수

    public ChatClient() {
        // GUI 구성 및 이벤트 리스너 설정
        textField.setEditable(true);  // 텍스트 필드를 기본 활성화 상태로 설정
        messageArea.setEditable(false); // 메시지 영역은 편집 불가

        // 메시지 창 크기 조정 및 스크롤 기능 추가
        messageArea.setLineWrap(true);  // 텍스트가 창을 넘으면 자동으로 줄바꿈
        messageArea.setWrapStyleWord(true); // 단어 기준으로 줄바꿈

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // 메시지 창의 크기 설정
        messageArea.setPreferredSize(new Dimension(500, 400));
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 창 크기 설정 및 위치 조정
        frame.setSize(600, 500);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null); // 화면 중앙에 위치

        // 스크롤 동작 개선: 새 메시지 추가 시 자동으로 스크롤을 가장 아래로 내림
        DefaultCaret caret = (DefaultCaret) messageArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // 입력된 메시지를 서버로 전송
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (out != null) {
                    out.println(textField.getText());  // 서버로 메시지 전송
                    textField.setText("");  // 텍스트 필드를 비움
                } else {
                    messageArea.append("서버에 연결되지 않았습니다.\n");
                }
            }
        });
    }

    private void run() throws IOException {
        // 닉네임 입력 받기
        nickname = JOptionPane.showInputDialog(
            frame,
            "Enter your nickname:",
            "Nickname selection",
            JOptionPane.PLAIN_MESSAGE);

        // 서버 주소 입력 받기
        String serverAddress = JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chat Room",
            JOptionPane.QUESTION_MESSAGE);

        // 서버에 연결
        Socket socket = new Socket(serverAddress, 23456);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // 닉네임 서버로 전송
        out.println(nickname);

        // 서버로부터의 메시지를 처리
        while (true) {
            String line = in.readLine();
            if (line != null) {
                if (line.startsWith("MESSAGE")) {
                    // 메시지에 타임스탬프 추가
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    String message = "[" + timestamp + "] " + line.substring(8);  // "MESSAGE " 부분을 잘라냄
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append(message + "\n");
                    });

                    // 읽음 확인 메시지 서버에 전송
                    out.println("READ " + message);
                } else if (line.startsWith("UPDATE")) {
                    // 메시지를 업데이트하여 읽음 상태를 표시
                    String updatedMessage = line.substring(7);  // "UPDATE " 부분을 잘라냄
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append(updatedMessage + "\n");  // 읽음 메시지 표시
                    });
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run(); 
    }
}

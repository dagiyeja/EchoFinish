package uni.server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServerMain extends JFrame implements ActionListener{
	JPanel p_north; 
	JTextField t_port;
	JButton bt_start;
	JTextArea area;
	JScrollPane scroll;
	int port=7777;
	ServerSocket server;
	Thread thread; //서버 운영 쓰레드
	
	public ServerMain() {
		p_north = new JPanel();
		t_port = new JTextField(Integer.toString(port) ,10);
		bt_start = new JButton("가동");
		area = new JTextArea();
		scroll = new JScrollPane(area);
		p_north.add(t_port);
		p_north.add(bt_start);
		add(p_north, BorderLayout.NORTH);
		add(scroll);
		
		bt_start.addActionListener(this);
		
		setBounds(600,100,300,400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	//서버를 가동한다!!
	public void startServer(){
		bt_start.setEnabled(false); //버튼 비활성화
		try {
			port=Integer.parseInt(t_port.getText());
			server=new ServerSocket(port);
			area.append("서버 생성\n");
			
			thread=new Thread(){
				public void run() {
					try {
						while(true){ 
							Socket socket=server.accept(); //메인스레드가 아니라 따로 쓰레드에서 수행하므로 지연발생해도 괜찮다. socket-종이컵, 대화나누는건 아바타가
							String ip=socket.getInetAddress().getHostAddress(); //접속자 IP받아옴.   
							area.append(ip+"접속자 발견\n");
							
							//접속자마다 아바타 생성해주고 대화를 나눌 수 있도록 해주자!!
							Avatar av=new Avatar(socket, area);
							av.start(); 
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start(); //쓰레드 동작
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		startServer();
	}
	
	public static void main(String[] args) {
		new ServerMain();
	}
}
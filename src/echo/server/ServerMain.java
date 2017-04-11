package echo.server;

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
	ServerSocket server; //접속 감지용 소켓, 대화용 아님
	
	Thread thread; //서버 가동용 쓰레드!!
	BufferedReader buffr;
	BufferedWriter buffw;
	
	public ServerMain() {
		p_north=new JPanel();
		t_port=new JTextField(Integer.toString(port),10);
		bt_start=new JButton("가동");
		area=new JTextArea();
		scroll=new JScrollPane(area);
		
		p_north.add(t_port);
		p_north.add(bt_start);
		add(p_north, BorderLayout.NORTH);
		add(scroll);
		
		bt_start.addActionListener(this);
		
		setBounds(600,100,300,400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}
	//서버 생성 및 가동
	//예외의 종류- checked Exception -예외처리가 강요됨
	
	//				  runtime Exception -예외처리 강요X ->비정상 종료
	public void startServer(){
		bt_start.setEnabled(false); //비활성화! 한번 누르면 그 이상 못누름
		
		
		try {
			port=Integer.parseInt(t_port.getText());
			server=new ServerSocket(port); //생성
			area.append("서버 준비됨..\n");
			
			//가동
			//실행부라 불리는 메인쓰레드는 절대 
			//무한 루프나 대기 혹은 지연상태에 빠지게 해서는 안된다!!
			//왜?실행부는 유저들의 이벤트를 감지하거나, 프로그램을 운영을 해야 하므로
			//무한루프나 대기에 빠지면, 본연의 역할을 할 수 없게 된다!
			//스마트폰 개발 분야에서는 이와 같은 코드는 이미 컴파일타임부터 에러 발생함!
			//이벤트 감지는 메인 스레드 본연의 업무임
			Socket socket=server.accept(); //종이컵 반환
			area.append("서버 가동..\n");
			
			//클라이언트는 대화를 하기 위해 접속한 것이므로,
			//접속이 되는 순간 스트림을 얻어놓자!!
			buffr=new BufferedReader(new InputStreamReader(socket.getInputStream())); //듣기용
			buffw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); //말하기용
			
			//클라이언트의 메세지 받기!!
			String data;
			while(true){ //데이터를 계속 읽어야 하니까 무한루프
				data=buffr.readLine(); //클라이언트의 메세지
				area.append("클라이언트의 말:"+data+"\n");
				
				
				buffw.write(data+"\n"); //보내기
				buffw.flush(); //버퍼 비우기!

			}
			
		} catch (NumberFormatException e) { 
			//코드를 작성하는 타이밍에 발생하는 에러 -checked exception, 문법 타이밍이 아님. 이걸 설정하면 숫자 아닌 것을 입력한 경우 비정상 종료시킴.
			//강요하지 않은 예외
			JOptionPane.showMessageDialog(this, "포트는 숫자로 넣어라!");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			server=new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void actionPerformed(ActionEvent e) {
		thread=new Thread(){
			public void run() {
				startServer();
			}
		};
		thread.start();
	}
	
	//메인 스레드는 무한루프 상태에 두면 안된다
	public static void main(String[] args) {
		new ServerMain();
	}


}

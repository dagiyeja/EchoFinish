package echo.client;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import db.DBManager;

public class ClientMain extends JFrame implements ItemListener,ActionListener{
	JPanel p_north;
	Choice choice;
	JTextField t_port, t_input;
	JButton bt_connect;
	JTextArea area;
	JScrollPane scroll;
	int port=7777;
	DBManager manager;
	ArrayList<Chat> list=new ArrayList<Chat>();
	Socket socket; //대화용 소켓!! 따라서 스트림 뽑아낼거임
	String ip;
	ClientThread ct;

	
	public ClientMain() {
		p_north=new JPanel();
		choice=new Choice();
		choice.setPreferredSize(new Dimension(100, 5));
		t_port=new JTextField(Integer.toString(port),5);
		t_input=new JTextField();
		bt_connect=new JButton("접속");
		area=new JTextArea();
		scroll=new JScrollPane(area);
		manager=DBManager.getInstance();
		
		p_north.add(choice);
		p_north.add(t_port);
		p_north.add(bt_connect);
		add(p_north, BorderLayout.NORTH);
		add(scroll);
		add(t_input, BorderLayout.SOUTH);
		
		loadIP(); //chat 테이블 디비 얻어옴
		
		for(int i=0; i<list.size(); i++){
			choice.add(list.get(i).getName());
		}
		
		//초이스와 아이템리스너 연결
		choice.addItemListener(this);
		bt_connect.addActionListener(this);
		t_input.addKeyListener(new KeyAdapter() {
			
			public void keyReleased(KeyEvent e) {
				int key=e.getKeyCode();				
				if(key==KeyEvent.VK_ENTER){
					String msg=t_input.getText();
					ct.send(msg); //보내기
					t_input.setText("");  //입력한 글씨 지우기
					//실시간으로 청취중 보내면 send실행
				}
			}
			
		});
		
		setBounds(300, 100, 300, 400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	//데이터베이스 가져오기!!
	public void loadIP(){
		Connection con=manager.getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		String sql="select * from chat order by chat_id asc";
		try {
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			
			//rs의 모든 데이터를 dto로 옮기는 과정..
			while(rs.next()){
				Chat dto=new Chat(); //레코드 1건을 받음
				dto.setChat_id(rs.getInt("chat_id"));
				dto.setName(rs.getString("name"));
				dto.setIp(rs.getString("ip"));
				
				list.add(dto);

			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			manager.disConnect(con); //연결 해제 DBManager 통해 수행
		}
		
	
		
	}

	public void itemStateChanged(ItemEvent e) {
		Choice ch=(Choice)e.getSource(); //초이스에서 선택한 인덱스 ->초이스로 형변환
		int index=ch.getSelectedIndex();
		Chat chat=list.get(index);
		this.setTitle(chat.getIp());
		ip=chat.getIp(); //멤버변수에도 대입!! 그래야 써먹을 수 있을것
		
	}
	
	//서버에 접속을 시도하자!!
	public void connect(){
		//소켓 생성시 접속이 발생함!!
		
		try {
			port=Integer.parseInt(t_port.getText()); //멤버변수에도 대입!! 그래야 써먹을 수 있을것	
			socket=new Socket(ip, port);
			
			//실시간으로 서버에 메세지를 청취하기 위해, 쓰레드를 생성하여 대화업무를 다 맡겨버리자!!
			//따라서 종이컵&실의 보유자는 동생(ClientThread)이다
			//헷갈리지 않게 듣고 말하는 것은 동생에 두자
			ct=new ClientThread(socket, area);
			ct.start();
		
			ct.send("안녕??");
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {		
			e.printStackTrace();
		} catch (IOException e) {		
			e.printStackTrace();
		}
	}
	
	

	
	public void actionPerformed(ActionEvent e) {
		connect();
		
	}

	public static void main(String[] args) {
		new ClientMain();

	}

}

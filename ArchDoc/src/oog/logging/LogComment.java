package oog.logging;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogComment extends JFrame implements ActionListener{

	private JTextArea content;
	
	public LogComment() throws HeadlessException {
		this.setBounds(400, 100, 400, 200);
		this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
		this.setTitle("Reason");
				
		content=new JTextArea();
		content.setLineWrap(true);
		JScrollPane jsp=new JScrollPane(content);
		this.add(jsp,BorderLayout.CENTER);
		
		JButton b1= new JButton();
		b1.setText("OK");
		LogWriter.Reason = "";
		b1.addActionListener(this);
		
		this.add(b1, BorderLayout.SOUTH);
		
		this.setVisible(true);
	}
	
    public synchronized void actionPerformed(ActionEvent e)
    {
		LogWriter.CmmLock = true;
		LogWriter.Reason = content.getText();
//		System.out.print(LogWriter.Reason);
		this.dispose();
		this.notify();
    }

}

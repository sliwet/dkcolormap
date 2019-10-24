import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class DKColorMapMain extends JFrame {
	DKColorMap cmap;

	private JPanel contentPane;
	public boolean plotspectra = false;
	public int refreshRate = 10;
	public int [] startstop = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DKColorMapMain frame = new DKColorMapMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DKColorMapMain() {
		setResizable(false);
		setTitle("DKColorMap");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 317, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnBrowse = new JButton("Browse Datafile");
		btnBrowse.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                int result = fc.showOpenDialog(null);
                if(result == JFileChooser.APPROVE_OPTION){
                	cmap = new DKColorMap(fc.getSelectedFile().getPath());
                }
			}
		});
		btnBrowse.setBounds(10, 11, 287, 45);
		contentPane.add(btnBrowse);

		JButton btnTimeElapsed = new JButton("Plot column data at selected X");
		btnTimeElapsed.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnTimeElapsed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		        new DataPlotSingle(cmap.getYdataAtX(),cmap.getSelectedXInfo(),null,null,null,null);
			}
		});
		btnTimeElapsed.setBounds(10, 69, 287, 37);
		contentPane.add(btnTimeElapsed);

		JButton btnAtSpecificTime = new JButton("Plot row data at selected Y");
		btnAtSpecificTime.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnAtSpecificTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        new DataPlotSingle(cmap.getXDataAtY(),cmap.getSelectedYInfo(),null,null,null,null);
			}
		});
		btnAtSpecificTime.setBounds(10, 120, 287, 37);
		contentPane.add(btnAtSpecificTime);
	}
}

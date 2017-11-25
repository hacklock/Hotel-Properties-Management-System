/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.coder.hms.ui.external;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.coder.hms.beans.SessionBean;
import com.coder.hms.daoImpl.CustomerDaoImpl;
import com.coder.hms.daoImpl.PaymentDaoImpl;
import com.coder.hms.daoImpl.ReservationDaoImpl;
import com.coder.hms.daoImpl.RoomDaoImpl;
import com.coder.hms.entities.Customer;
import com.coder.hms.entities.Payment;
import com.coder.hms.entities.ReportObject;
import com.coder.hms.entities.Reservation;
import com.coder.hms.entities.Room;
import com.coder.hms.ui.extras.AllreservationRenderer;
import com.coder.hms.ui.extras.CustomTableHeaderRenderer;
import com.coder.hms.utils.LoggingEngine;
import com.ibm.icu.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

/**
 *
 * @author dell-pc
 */
public class AllReservationsWindow extends JFrame {

    /**
	 * 
	 */
    private JButton editButton;
    private JTextField searchField;
    private JTable reservationTable;
    private long selectedReservationId;
    private JDateChooser endDateChooser;
    private JScrollPane mainPanelScroller;
    private JDateChooser startDateChooser;
    private static LoggingEngine loggingEngine;
    private ReservationDaoImpl reservationDaoImpl;
    private static final long serialVersionUID = 1L;
    private JLabel filterLbl, startDateLbl, stopDateLbl;
    private TableRowSorter<DefaultTableModel> tableRowSorter;
    private JPanel centeredPanel, upperPanel, searchFieldsHolder;
    private final AllreservationRenderer CTR = new AllreservationRenderer();
    private final CustomTableHeaderRenderer THR = new CustomTableHeaderRenderer();
    private static final SessionBean S_BEAN = SessionBean.getSESSION_BEAN();
    private final String LOGOPATH = "/com/coder/hms/icons/main_logo(128X12).png";
	final String [] columnNames = {"ID", "GROUP NAME", "CHECKIN ", "CHECKOUT", "AGENCY", 
			"AGENCY REF NO", "ROOM NUMBER", "TOTAL DAYS", "HOST TYPE", "PAYMENT STATUS", "BOOK STATUS"};
	private final DefaultTableModel model = new DefaultTableModel(columnNames, 0);
	private JButton btnFetch;
	/**
     * Creates new form AllReservationsWindow
     */
    public AllReservationsWindow() {
        initComponents();
    }

    private void initComponents() {

        upperPanel = new JPanel();
        filterLbl = new JLabel();
        editButton = new JButton();
        stopDateLbl = new JLabel();
        startDateLbl = new JLabel();
        centeredPanel = new JPanel();
        searchField = new JTextField();
        reservationTable = new JTable();
        final Date localDate = new Date();
        searchFieldsHolder = new JPanel();
        endDateChooser = new JDateChooser();
        startDateChooser = new JDateChooser();
        mainPanelScroller = new JScrollPane();
        loggingEngine = LoggingEngine.getInstance();
        
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Coder HPMSA - [All Reservations]");
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setFont(new Font("Microsoft Sans Serif", 0, 12)); // NOI18N
        setMinimumSize(new Dimension(1100, 750));
        setPreferredSize(new Dimension(1312, 800));
        setVisible(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(LOGOPATH)));

        upperPanel.setBackground(new Color(6, 109, 149));
        upperPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        upperPanel.setPreferredSize(new Dimension(1312, 70));

        editButton.setIcon(new ImageIcon(getClass().
        		getResource("/com/coder/hms/icons/main_edit.png"))); // NOI18N
        editButton.setText("Edit Reservation");
        editButton.setFont(new Font("Arial", Font.BOLD, 12));
        editButton.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
        editButton.addActionListener(ActionListener -> {
        	getEditButtonAction();
        });

        startDateLbl.setText("Start date : ");
        startDateChooser.setDate(localDate);
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        stopDateLbl.setText("End date : ");
        endDateChooser.setDate(localDate);
        endDateChooser.setDateFormatString("yyyy-MM-dd");

        
        btnFetch = new JButton("Fetch");
        btnFetch.setIcon(new ImageIcon(AllReservationsWindow.class.getResource("/com/coder/hms/icons/menu_fetch.png")));
        btnFetch.setMinimumSize(new Dimension(110, 45));
        btnFetch.setPreferredSize(new Dimension(110, 45));
        btnFetch.setFont(new Font("Arial", Font.BOLD, 12));
        btnFetch.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnFetch.addActionListener(ActionListener -> {

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			if (startDateChooser.getDate().equals(endDateChooser.getDate())) {
				final InformationFrame frame = new InformationFrame();
				frame.setMessage("Start date equals end date!Please be sure you're choose right date.");
				frame.setVisible(true);
			}
			else if (endDateChooser.getDate().before(startDateChooser.getDate())) {
				final InformationFrame frame = new InformationFrame();
				frame.setMessage("End date cannot be greather than start date!");
				frame.setVisible(true);
			}
			else {
				final List<Reservation> reservationsList = reservationDaoImpl.getReservationBetweenTwoDates(
						sdf.format(startDateChooser.getDate()), sdf.format(endDateChooser.getDate()));
				model.setRowCount(0);

				for (int i = 0; i < reservationsList.size(); i++) {

					model.addRow(new Object[] { reservationsList.get(i).getId(), reservationsList.get(i).getGroupName(),
							reservationsList.get(i).getCheckinDate(), reservationsList.get(i).getCheckoutDate(),
							reservationsList.get(i).getAgency(), reservationsList.get(i).getAgencyRefNo(),
							reservationsList.get(i).getTheNumber(), reservationsList.get(i).getTotalDays(),
							reservationsList.get(i).getHostType(), reservationsList.get(i).getPaymentStatus(),
							reservationsList.get(i).getBookStatus() });
				}
			}

		});
        
        final GroupLayout upperPanelLayout = new GroupLayout(upperPanel);
        upperPanelLayout.setHorizontalGroup(
        	upperPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(upperPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(editButton, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(startDateLbl, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(startDateChooser, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
        			.addGap(34)
        			.addComponent(stopDateLbl, GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(endDateChooser, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(btnFetch, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
        			.addGap(498))
        );
        upperPanelLayout.setVerticalGroup(
        	upperPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(upperPanelLayout.createSequentialGroup()
        			.addGroup(upperPanelLayout.createParallelGroup(Alignment.TRAILING)
        				.addGroup(upperPanelLayout.createSequentialGroup()
        					.addGap(15)
        					.addGroup(upperPanelLayout.createParallelGroup(Alignment.CENTER)
        						.addComponent(startDateChooser, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
        						.addComponent(endDateChooser, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
        						.addComponent(stopDateLbl, GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
        						.addComponent(btnFetch, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(upperPanelLayout.createSequentialGroup()
        					.addGap(11)
        					.addGroup(upperPanelLayout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(editButton, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
        						.addComponent(startDateLbl, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))))
        			.addGap(13))
        );
        upperPanel.setLayout(upperPanelLayout);

        getContentPane().add(upperPanel, BorderLayout.PAGE_START);

        centeredPanel.setBackground(new Color(6, 109, 149));
        centeredPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        centeredPanel.setLayout(new BorderLayout());

        mainPanelScroller.setBorder(null);
        mainPanelScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        mainPanelScroller.setAutoscrolls(true);

        reservationTable.setModel(model);
        reservationTable.setAutoCreateRowSorter(true);
        reservationTable.setColumnSelectionAllowed(false);
        reservationTable.setCellSelectionEnabled(false);
        reservationTable.setRowSelectionAllowed(true);
        
        reservationTable.setDefaultRenderer(Object.class, CTR);
        reservationTable.addMouseListener(rowSelectorListener());
        reservationTable.getTableHeader().setDefaultRenderer(THR);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        mainPanelScroller.setViewportView(reservationTable);
        tableRowSorter = new TableRowSorter<>(model);
        reservationTable.setRowSorter(tableRowSorter);
        reservationTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (reservationTable.getColumnModel().getColumnCount() > 0) {
            reservationTable.getColumnModel().getColumn(0).setPreferredWidth(5);
            reservationTable.getColumnModel().getColumn(1).setPreferredWidth(110);
            reservationTable.getColumnModel().getColumn(9).setPreferredWidth(50);
        }

        centeredPanel.add(mainPanelScroller, BorderLayout.CENTER);

        searchFieldsHolder.setBackground(new Color(6, 109, 149));
        searchFieldsHolder.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        filterLbl.setFont(new Font("MS Reference Sans Serif", 1, 14)); // NOI18N
        filterLbl.setIcon(new ImageIcon(getClass().getResource("/com/coder/hms/icons/logging.png"))); // NOI18N
        filterLbl.setText("Filter : ");
        searchFieldsHolder.add(filterLbl);

        searchField.setHorizontalAlignment(JTextField.LEFT);
        searchField.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 13));
        searchField.setToolTipText("Specialize your rows by typing here");
        searchField.setPreferredSize(new Dimension(1150, 30));
        searchField.addKeyListener(new KeyAdapter() {
		
            @Override
	    public void keyTyped(KeyEvent evt) {
                searchFieldKeyTyped(evt);
            }
        });
        searchFieldsHolder.add(searchField);
        
        centeredPanel.add(searchFieldsHolder, BorderLayout.PAGE_START);
        getContentPane().add(centeredPanel, BorderLayout.CENTER);

    	reservationDaoImpl = new ReservationDaoImpl();
    	
    	populatemainTable(model);
        pack();
    }

    private void searchFieldKeyTyped(KeyEvent evt) {
        
    	String modifiedQuery = "(?i)" + searchField.getText();
    	tableRowSorter.setRowFilter(RowFilter.regexFilter(modifiedQuery));
    }

    public void populatemainTable(DefaultTableModel defaultTableModel) {
    	
    	defaultTableModel.setRowCount(0);
    	
    	final List<Reservation> reservationsList = reservationDaoImpl.getAllReservations();
    	
    	for (int i = 0; i < reservationsList.size(); i++) {
			
    		model.addRow(new Object[]{
    				reservationsList.get(i).getId(), reservationsList.get(i).getGroupName(),
    				reservationsList.get(i).getCheckinDate(), reservationsList.get(i).getCheckoutDate(),
    				reservationsList.get(i).getAgency(), reservationsList.get(i).getAgencyRefNo(),
    				reservationsList.get(i).getTheNumber(), reservationsList.get(i).getTotalDays(),
    				reservationsList.get(i).getHostType(), reservationsList.get(i).getPaymentStatus(),
    				reservationsList.get(i).getBookStatus()
    				});
		}
    }
    
    private MouseListener rowSelectorListener() {
 		final MouseAdapter mouseAdapter = new MouseAdapter() {
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 				int selectedIndex = reservationTable.getSelectedRow();
 				
 				 if (selectedIndex < 0) {
 					reservationTable.revalidate();
 					reservationTable.repaint();
                                }
 				 
 				 selectedReservationId = Long.parseLong(String.valueOf(reservationTable.getValueAt(selectedIndex, 0)));
 				 
 				super.mouseClicked(e);
 			}
 			
		};
		return mouseAdapter;
 	}
    
    private void getEditButtonAction() {
    	
    	if(selectedReservationId >= 0) {
    		
    		final Reservation reservation = reservationDaoImpl.findReservationById(selectedReservationId);
    		showUpdateReservationWin(reservation);
    		
    	}
    }
    
    private void showUpdateReservationWin(Reservation theReservation) {
    	
    	Room room = null;
        Payment payment = null;
        String customerCountry = "";
        final RoomDaoImpl roomDaoImpl = new RoomDaoImpl();
        final List<Room> roomList = roomDaoImpl.getAllRooms();
        final  PaymentDaoImpl paymentDaoImpl = new PaymentDaoImpl();
        final CustomerDaoImpl customerDaoImpl = new CustomerDaoImpl();
        final List<Customer> customerList = customerDaoImpl.getAllCustomers();

        
            final ReportObject reportBean = new ReportObject();
            Reservation foundRes = theReservation;

            loggingEngine.setMessage("[Blockade window] Required reservation found : " + foundRes.toString());
            final UpdateReservationWindow nex = new UpdateReservationWindow();

            for (Room searchedRoom : roomList) {
                if (searchedRoom.getReservationId() == foundRes.getId()) {
                    room = searchedRoom;
                }
            }

            for (Customer cst : customerList) {
                if (cst.getReservationId() == foundRes.getId()) {
                    customerCountry = cst.getCountry();
                    nex.setRoomInfoTableRows(new Object[]{room.getNumber(), room.getType(), cst.getFirstName(),
                        cst.getLastName()});
                }
            }
            reportBean.setUserName(S_BEAN.getNickName());

            nex.setRezIdField(foundRes.getId());
            reportBean.setId(foundRes.getId());

            nex.setNameSurnameField(foundRes.getGroupName());
            reportBean.setGroupName(foundRes.getGroupName());

            nex.setCheckinDate(foundRes.getCheckinDate());
            reportBean.setCheckinDate(foundRes.getCheckinDate());

            nex.setCheckoutDate(foundRes.getCheckoutDate());
            reportBean.setCheckoutDate(foundRes.getCheckoutDate());

            nex.setTotalDaysField(foundRes.getTotalDays());
            reportBean.setTotalDays(foundRes.getTotalDays());

            nex.setReservNote(foundRes.getNote());

            nex.setAgency(foundRes.getAgency());
            reportBean.setAgency(foundRes.getAgency());

            nex.setHostType(foundRes.getHostType());
            reportBean.setHostType(foundRes.getHostType());

            nex.setCreditType(foundRes.getCreditType());
            nex.setReservStatus(foundRes.getBookStatus());

            nex.setRoomNumber(room.getNumber());
            reportBean.setTheNumber(room.getNumber());

            nex.setRoomType(room.getType());
            reportBean.setRoomType(room.getType());

            nex.setPersonCountSpinner(room.getPersonCount());

            nex.setPriceOfRoom(room.getPrice());
            reportBean.setPrice(room.getPrice());

            nex.setCurrency(room.getCurrency());
            reportBean.setType(room.getCurrency());

            nex.setAgencyRefNo(foundRes.getAgencyRefNo());
            reportBean.setAgencyRefNo(foundRes.getAgencyRefNo());

            nex.setReferanceNo(foundRes.getReferanceNo());
            nex.setCustomerCountry(customerCountry);

            nex.setRoomCountTableRows(new Object[]{room.getNumber(), room.getType(), room.getPersonCount(),
                room.getPrice(), room.getCurrency()});

            if (foundRes.getPaymentStatus()) {

                payment = paymentDaoImpl.getEarlyPaymentByRoomNumber(room.getNumber());
                nex.setEarlyPaymetTableRows(
                        new Object[]{payment.getTitle(), payment.getPaymentType(), payment.getPrice(),
                            payment.getCurrency(), payment.getExplanation(), payment.getDateTime()});
                final InformationFrame infoFrame = new InformationFrame();
                infoFrame.setMessage("Early payment : " + payment.getPrice() + payment.getCurrency());

                reportBean.setPaymentStatus(true);
                reportBean.setPaymentType(payment.getPaymentType());
                reportBean.setBalance(payment.getPrice().toString());
                reportBean.setCurrency(payment.getCurrency());
                infoFrame.setVisible(true);
            } else {

                reportBean.setPaymentStatus(false);
                reportBean.setPaymentType("No payment");
                reportBean.setBalance("0");
                reportBean.setCurrency(room.getCurrency());
            }

            loggingEngine.setMessage("Reservation window is populated successfully.");
            nex.setReportBean(reportBean);
            nex.setVisible(true);
    }

}

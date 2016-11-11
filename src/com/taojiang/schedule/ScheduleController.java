package com.taojiang.schedule;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import com.taojiang.cpu.CPUS;
import com.taojiang.queue.Process;
import com.taojiang.queue.Queue;
import com.taojiang.tableclass.TProcess;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ScheduleController extends Thread implements Initializable{
	public static final String image_url_1 = "image/cpu_spare.png";
	public static final String image_url_2 = "image/cpu_busy.png";
	// �����Ƿ������ӽ��̵��ź���
	static boolean signal = true;
	// ����һЩ���ȷ������ַ�������
	private static final String FCFS = "FCFS";
	private static final String PRIORITY = "Priority";
	private static final String RR = "RR";
	private static final String DYNAMIC_PRIORITY = "Dynamic Priority";
	// ���浱ǰʹ�õĵ��ȷ�����Ĭ��ΪFCFS
	static String scheduleMethod = FCFS;
	// ���������������ڳ�ʼ��TableColumn
	private String[] COLUMN_NAME = {"������","���ȼ�","����ʱ��",
			"�����С","��ʼλ��","��һ����������"};
	private String[] COLUMN_CELL_VALUE = {"proName","proPriority","proRuntime",
			"proStoreSize","proStoreStart","proNextName"};
	// �����ĸ�TableView��Observablist
	private static ObservableList<TProcess> poolObList;
	private static ObservableList<TProcess> readyObList;
	private static ObservableList<TProcess> suspandObList;
	private static ObservableList<TProcess> waitObList;
	// �����ĸ�����
	private static Queue poolQueue;
	private static Queue readyQueue;
	private static Queue suspandQueue;
	private static Queue waitQueue;
	// ��������CPU��ÿ��CPUΪ����
	static CPUS cpus;
	// ӳ��FXML��ߵĿؼ�
	@FXML private TextField tf_proName;
	@FXML private TextField tf_proRuntime;
	@FXML private TextField tf_proPriority;
	@FXML private TextField tf_proMemNeeded;
	@FXML private Button bt_addProcess;
	@FXML private ComboBox<String> cb_selectMethod;
	@FXML private Button bt_startSchedule;
	@FXML private TableView<TProcess> table_pool;
	@FXML private TableView<TProcess> table_ready;
	@FXML private TableView<TProcess> table_suspand;
	@FXML private TableView<TProcess> table_wait;
	@FXML public ImageView iv_cpu1;
	@FXML public ImageView iv_cpu2;
	@FXML public ProgressBar pb_cpu1_1;
	@FXML public ProgressBar pb_cpu1_2;
	@FXML public ProgressBar pb_cpu2_1;
	@FXML public ProgressBar pb_cpu2_2;
	@FXML public Label lb_cpu1_1;
	@FXML public Label lb_cpu1_2;
	@FXML public Label lb_cpu2_1;
	@FXML public Label lb_cpu2_2;
/**************************************���º�����****************************************/
	// ScheduleController��ʼ���ĺ������Զ�����
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// ʵ��������
		poolQueue = new Queue("�󱸶���");
		readyQueue = new Queue("��������");
		suspandQueue = new Queue("�������");
		waitQueue = new Queue("�ȴ�����");
		// ʵ����CPU
		cpus = new CPUS(2,this);
		// ʵ�����ĸ�TableView��Observablist
		poolObList = FXCollections.observableArrayList();
		readyObList = FXCollections.observableArrayList();
		suspandObList = FXCollections.observableArrayList();
		waitObList = FXCollections.observableArrayList();
		initTableColumn(table_pool);
		initTableColumn(table_ready);
		initTableColumn(table_suspand);
		initTableColumn(table_wait);
		table_pool.setItems(poolObList);
		table_ready.setItems(readyObList);
		table_suspand.setItems(suspandObList);
		table_wait.setItems(waitObList);
		// ΪComboBox���item�͵���¼�
		cb_selectMethod.getItems().addAll("Priority","FCFS","RR","Dynamic Priority");
		cb_selectMethod.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue == oldValue)
					return;
				switch (newValue) {
				case "FCFS":
					scheduleMethod = FCFS;
					break;
				case "Priority":
					scheduleMethod = PRIORITY;
					break;
				case "RR":
					scheduleMethod = RR;
					break;
				case "Dynamic Priority":
					scheduleMethod = DYNAMIC_PRIORITY;
					break;
				default:
					break;
				}
				System.out.println(newValue);
			}
		});

		iv_cpu1.setImage(new Image(image_url_1));
		iv_cpu2.setImage(new Image(image_url_1));
		this.start();
	}
	// ΪTableView���TableColumn
	public void initTableColumn(TableView<TProcess> table){
		for(int i = 0;i < 6;i++){
			TableColumn<TProcess, String> column= new TableColumn<TProcess, String>(COLUMN_NAME[i]);
			column.setCellValueFactory(new PropertyValueFactory<>(COLUMN_CELL_VALUE[i]));
			table.getColumns().add(column);
		}
	}
	// add process button response event
	public void addProButtonAction(){
		signal = false;
		String str1 = tf_proName.getText().toString();
		String str2 = tf_proRuntime.getText().toString();
		String str3 = tf_proPriority.getText().toString();
		String str4 = tf_proMemNeeded.getText().toString();
		if(str1.equals("")||str2.equals("")||str3.equals("")||str4.equals("")){
			System.out.println("system$: " + "���ʧ�ܣ���д����������Ϣ");
			return;
		}
		TProcess pro = new TProcess(str1,str3,str2,str4,"0","");
		if(readyQueue.length() < 8){
			// ��������δ����ֱ����ӵ���������
			addProcess(pro,readyQueue,readyObList);
		}else{
			addProcess(pro,poolQueue,poolObList);
		}
//		System.out.println("readyQueue��Ϣ���£�");
//		readyQueue.print();
//		System.out.println("readyObList��Ϣ����:");
//		for (int i = 0; i < readyObList.size(); i++) {
//			System.out.println(readyObList.get(i).getProName());
//		}
		tf_proName.clear();
		tf_proRuntime.clear();
		tf_proPriority.clear();
		tf_proMemNeeded.clear();
		signal = true;
	}
	// �������ӽ���,����ʾ�ڽ�����
	private void addProcess(TProcess pro, Queue queue, ObservableList<TProcess> list) {
		queue.insertProcess(pro);
		list.add(pro);
	}
	// �Ӷ����Ƴ����̣����ӽ������Ƴ�
	private Process removeProcess(Queue queue,ObservableList<TProcess> list,int index){
		list.remove(index);
		Process process = queue.remove(index);
//		System.out.println("readyQueue��Ϣ���£�");
//		readyQueue.print();
//		System.out.println("readyObList��Ϣ����:");
//		for (int i = 0; i < readyObList.size(); i++) {
//			System.out.println(readyObList.get(i).getProName());
//		}
		return process;
	}
	// start schedule button response event
	public void startScheduleButtonClicked(){
//		this.start();
	}

	// �����㷨
	public synchronized void run() {
		while(true){
			// ���󱸶��н��̵��ȵ���������
			Thread.currentThread().notify();
			while(readyQueue.length() < 8 && poolQueue.length() > 0){
				Process process;
				process = removeProcess(poolQueue, poolObList, 0);
				if(process != null){
					TProcess tProcess = new TProcess(process.getProName(),
							""+process.getProPriority(),
							""+process.getProRuntime(),
							""+process.getProStoreSize(),
							""+process.getProStoreStart(),
							process.getProPointed()!=null?
									process.getProPointed().getProName():"");
					addProcess(tProcess, readyQueue, readyObList);
				}
			}
//			if(readyQueue.length() != 0 ||poolQueue.length() != 0){
//				System.out.println(readyQueue.length()+" "+poolQueue.length()+" "+cpus.isSpare());
//			}
			// �Ӿ���������ѡ����̣���CPU����
			while(readyQueue.length() > 0 && cpus.isSpare()){
				// ���ȼ������㷨
				if(scheduleMethod == PRIORITY){
					// ��������
					readyQueue.sort();
					// ��ObservableList����
					readyObList.sort(compare);
				}
				while(!signal){
					// ��ʾ��ǰ�Ƿ���Խ���remove����
					System.out.println("*********************************************");
				}
				// �������Ķ��׽��̴Ӿ�������ɾ��
				Process process = removeProcess(readyQueue, readyObList, 0);
				// ���ý��̽���CPU����
				cpus.work(process);
			}
		}

	}

	Comparator<TProcess> compare = new Comparator<TProcess>() {

		@Override
		public int compare(TProcess o1, TProcess o2) {
			if(Integer.parseInt(o1.getProPriority().getValue())
					>Integer.parseInt(o2.getProPriority().getValue()))
				return 1;
			else if(Integer.parseInt(o1.getProPriority().getValue())
					<Integer.parseInt(o2.getProPriority().getValue()))
				return -1;
			else
				return 0;
		}
	};
}
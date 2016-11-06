package com.taojiang.cpu;
import com.taojiang.queue.Process;
import com.taojiang.schedule.LanchWnd;
import com.taojiang.schedule.ScheduleController;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
public class CPU {
	public final int CPU_STATE_SPARE = 0;  // cpu����
	public final int CPU_STATE_WORK = 1;   // cpu����

	public String cpuName;                     // cpu����
	public Process process1;                   // cpu��ǰ����Ľ���1
	public Process process2;                   // cpu��ǰ����Ľ���2
	public int tracks;                         // ����CPU�ĵ���
	public int state;                          // cpu��ǰ״̬
	public ScheduleController scheduleController;

	public CPU(String cpuName,ScheduleController scheduleController){
		this.cpuName = cpuName;
		process1 = null;
		process2 = null;
		state = CPU_STATE_SPARE;
		this.scheduleController = scheduleController;
	}
	// ����CPU����Ľ���
	public void setProcess(Process process){
		int which;
		System.out.println(getCpuName());
		if(process1 == null){
			process1 = process;
			// ���´�cpu��״̬
			updateState();
			// ȷ�����Ǹ�����ִ��
			if(getCpuName().equals("cpu1")){
				which = 0;
			}else{
				which = 2;
			}
		}else{
			process2 = process;
			updateState();
			if(getCpuName().equals("cpu1")){
				which = 1;
			}else{
				which = 3;
			}
		}
		Thread thread = new Thread(new Runnable() {
			final int dealTime = process.getProRuntime();
			final int whichPB = which;
			@Override
			public void run() {
				animation(dealTime,whichPB);
			}
		});

		thread.start();

	}

	private void updateState() {
		// TODO Auto-generated method stub
		if(process1 != null && process2 != null)
			state = CPU_STATE_WORK;
		else
			state = CPU_STATE_SPARE;
		System.out.println(getCpuName() + "״̬���³ɹ�" + state);
	}

	public String getCpuName() {
		// TODO Auto-generated method stub
		return cpuName;
	}
	//���������������ĺ���
	protected void animation(int totalTime,int which) {
		switch (which) {
		case 0:
			animation(scheduleController.pb_cpu1_1, scheduleController.lb_cpu1_1, totalTime, 0);
			break;
		case 1:
			animation(scheduleController.pb_cpu1_2, scheduleController.lb_cpu1_2, totalTime, 1);
			break;
		case 2:
			animation(scheduleController.pb_cpu2_1, scheduleController.lb_cpu2_1, totalTime, 2);
			break;
		case 3:
			animation(scheduleController.pb_cpu2_2, scheduleController.lb_cpu2_2, totalTime, 3);
			break;
		default:
			break;
		}
	}

	public synchronized void animation(ProgressBar pb,Label lb,int totalTime,int which){
		 Animation animation = new Transition() {
		     {
		         setCycleDuration(Duration.millis(totalTime*1000));
		     }

		     protected void interpolate(double frac) {
		         final int n = Math.round(totalTime * (float) frac);
//		         System.out.println(frac + " " + n);
		         lb.setText(""+Math.round(totalTime * (float)(1- frac)));
//		         System.out.println(pb==null?"pb si null":"pb is not null" + which);
		        pb.setProgress(frac);
		         if(frac == 1.0){
		        	 lb.setText("0");
		        	 pb.setProgress(0F);
					if(which % 2 == 0)
						process1 = null;
					else
						process2 = null;
					updateState();
		         }
		     }

		 };
		 animation.play();
	}
}
package com.taojiang.memory;

public interface Memorys {
	public void initMemory(); // ��ʼ���ڴ�
	public int allocateMemory(int size,String proName);// �����ڴ�,���ؿɷ����������ʼ��ַ
//	public void insertZone(int start,int size,boolean state,String proName);// �������
	public void insertZone(int size,boolean state,String proName);// �������
	public void recycleMemory(String proName);// ���ݽ������ƻ������ڴ�
	public void merge(int p); // �ϲ����ڷ���
	public void print();
}

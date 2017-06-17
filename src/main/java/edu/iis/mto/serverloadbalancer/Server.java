package edu.iis.mto.serverloadbalancer;

import org.hamcrest.Matcher;

public class Server {

	public double currentLoadPercentage;
	public int capacity;

	public Server(int size) {
		capacity = size;
	}

	public boolean contains(Vm theVm) {
		return true;
	}

}

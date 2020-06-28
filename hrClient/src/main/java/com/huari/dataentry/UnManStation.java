package com.huari.dataentry;

import java.util.ArrayList;

public class UnManStation {

	public String name = "";
	public String id = "";
	public double lon;
	public double lan;
	public byte isavailable;
	public byte iskongtiao;
	public String[] switcharray;
	public ArrayList<String> switchlist;
	public String info = "";
	public String state = "";
	public String server;

	public String getServer() {
		return server;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UnManStation){
			if (server.equals(((UnManStation) obj).getServer())) {
				return true;
			}
		}
		return false;
	}
}

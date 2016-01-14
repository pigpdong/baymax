package com.tongbanjie.baymax.druid;

import java.io.IOException;

public class AP implements Appendable {
	
	StringBuffer sb = new StringBuffer();

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		sb.append("|" + csq);
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		sb.append("|("+start+","+end+")" + csq);
		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		sb.append("|" + c);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}

}

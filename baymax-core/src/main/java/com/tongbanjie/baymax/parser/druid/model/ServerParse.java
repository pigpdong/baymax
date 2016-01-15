/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.tongbanjie.baymax.parser.druid.model;

import java.util.regex.Pattern;

/**
 * @author mycat
 */
public final class ServerParse {

	public static final int OTHER = -1;
	public static final int BEGIN = 1;
	public static final int COMMIT = 2;
	public static final int DELETE = 3;
	public static final int INSERT = 4;
	public static final int REPLACE = 5;
	public static final int ROLLBACK = 6;
	public static final int SELECT = 7;
	public static final int SET = 8;
	public static final int SHOW = 9;
	public static final int START = 10;
	public static final int UPDATE = 11;
	public static final int KILL = 12;
	public static final int SAVEPOINT = 13;
	public static final int USE = 14;
	public static final int EXPLAIN = 15;
	public static final int KILL_QUERY = 16;
	public static final int HELP = 17;
	public static final int MYSQL_CMD_COMMENT = 18;
	public static final int MYSQL_COMMENT = 19;
	public static final int CALL = 20;
	public static final int DESCRIBE = 21;
    public static final int LOAD_DATA_INFILE_SQL = 99;
    public static final int DDL = 100;
    private static final  Pattern pattern = Pattern.compile("(load)+\\s+(data)+\\s+\\w*\\s*(infile)+",Pattern.CASE_INSENSITIVE);


}
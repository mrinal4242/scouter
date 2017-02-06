/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package scouter.client.xlog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import scouter.client.model.TextProxy;
import scouter.client.model.XLogData;
import scouter.client.server.GroupPolicyConstants;
import scouter.client.server.Server;
import scouter.client.server.ServerManager;
import scouter.client.util.SqlMakerUtil;
import scouter.client.xlog.views.XLogProfileView;
import scouter.lang.CountryCode;
import scouter.lang.step.*;
import scouter.util.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ProfileText {
	
	public static void build(final String date, StyledText text, XLogData xperf, Step[] profiles, int spaceCnt,
             int serverId) {
		 build(date, text, xperf, profiles, serverId, false);
	}

    public static void build(final String date, StyledText text, XLogData xperf, Step[] profiles,
                             int serverId, boolean bindSqlParam) {
        build(date, text, xperf, profiles, serverId, bindSqlParam, false);
    }
	 
    public static void build(final String date, StyledText text, XLogData xperf, Step[] profiles,
                             int serverId, boolean bindSqlParam, boolean isSimplified) {

        boolean truncated = false;

        if (profiles == null) {
            profiles = new Step[0];
        }
        profiles = SortUtil.sort(profiles);
        XLogUtil.loadStepText(serverId, date, profiles);

        String error = TextProxy.error.getLoadText(date, xperf.p.error, serverId);
        Color blue = text.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        Color dmagenta = text.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
        Color red = text.getDisplay().getSystemColor(SWT.COLOR_RED);

        Color dred = text.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
        Color dgreen = text.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);

        Color dblue = text.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
        Color dcyan = text.getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
        Color dyellow = text.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
        Color dgray = text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);

        java.util.List<StyleRange> sr = new ArrayList<StyleRange>();

        int slen = 0;

        final StringBuffer sb = new StringBuffer();
        sb.append("► txid    = ");
        slen = sb.length();
        sb.append(Hexa32.toString32(xperf.p.txid)).append("\n");
        sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
        if (xperf.p.gxid != 0) {
            sb.append("► gxid    = ");
            slen = sb.length();
            sb.append(Hexa32.toString32(xperf.p.gxid)).append("\n");
            sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
        }
        if (xperf.p.caller != 0) {
            sb.append("► caller    = ");
            slen = sb.length();
            sb.append(Hexa32.toString32(xperf.p.caller)).append("\n");
            sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
        }
        sb.append("► objName = ").append(xperf.objName).append("\n");
        sb.append("► endtime = ").append(FormatUtil.print(new Date(xperf.p.endTime), "yyyyMMdd HH:mm:ss.SSS")).append("\n");
        sb.append("► elapsed = ").append(FormatUtil.print(xperf.p.elapsed, "#,##0")).append(" ms\n");
        sb.append("► service = ").append(TextProxy.service.getText(xperf.p.service)).append("\n");
        if (error != null) {
            sb.append("► error   = ");
            slen = sb.length();
            sb.append(error).append("\n");
            sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
        }

        sb.append("► ipaddr=" + IPUtil.toString(xperf.p.ipaddr) + ", ");
        sb.append("userid=" + xperf.p.userid);
        sb.append("\n► cpu=" + FormatUtil.print(xperf.p.cpu, "#,##0") + " ms, ");
        sb.append("kbytes=" + xperf.p.kbytes);
//		sb.append("bytes=" + xperf.p.bytes + ", ");
//		sb.append("status=" + xperf.p.status);
        if (xperf.p.sqlCount > 0) {
            sb.append("\n► sqlCount=" + xperf.p.sqlCount + ", ");
            sb.append("sqlTime=" + FormatUtil.print(xperf.p.sqlTime, "#,##0") + " ms");
        }
        if (xperf.p.apicallCount > 0) {
            sb.append("\n► ApiCallCount=" + xperf.p.apicallCount + ", ");
            sb.append("ApiCallTime=" + FormatUtil.print(xperf.p.apicallTime, "#,##0") + " ms");
        }

        String t = TextProxy.userAgent.getLoadText(date, xperf.p.userAgent, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► userAgent=" + t);
        }

        t = TextProxy.referer.getLoadText(date, xperf.p.referer, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► referer=" + t);
        }

        t = TextProxy.group.getLoadText(date, xperf.p.group, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► group=" + t);
        }
        if (StringUtil.isNotEmpty(xperf.p.countryCode)) {
            sb.append("\n► country=" + CountryCode.getCountryName(xperf.p.countryCode));
        }
        t = TextProxy.city.getLoadText(date, xperf.p.city, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► city=" + t);
        }
        t = TextProxy.web.getLoadText(date, xperf.p.webHash, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► webName=" + t).append("  webTime=" + xperf.p.webTime + " ms");
        }
        t = TextProxy.login.getLoadText(date, xperf.p.login, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► login=" + t);
        }
        t = TextProxy.desc.getLoadText(date, xperf.p.desc, serverId);
        if (StringUtil.isNotEmpty(t)) {
            sb.append("\n► desc=" + t);
        }
        if (xperf.p.hasDump == 1) {
            sb.append("\n► dump=Y");
        }
        sb.append("\n");

        sb.append("------------------------------------------------------------------------------------------\n");
        sb.append("    p#      #    	  TIME         T-GAP   CPU          CONTENTS\n");
        sb.append("------------------------------------------------------------------------------------------\n");
        if (profiles.length == 0) {
            sb.append("\n                     ( No xlog profile collected ) ");
            text.setText(sb.toString());
            // for (int i = 0; i < sr.size(); i++) {
            text.setStyleRanges(sr.toArray(new StyleRange[sr.size()]));
            // }
            return;
        }

        long stime = xperf.p.endTime - xperf.p.elapsed;
        long prev_tm = stime;
        long prev_cpu = 0;

        sb.append("        ");
        sb.append(" ");
        sb.append("[******]");
        sb.append(" ");
        sb.append(FormatUtil.print(new Date(stime), "HH:mm:ss.SSS"));
        sb.append("   ");
        sb.append(String.format("%6s", "0"));
        sb.append(" ");
        sb.append(String.format("%6s", "0"));
        sb.append("  start transaction \n");
        // sr.add(style(slen, sb.length() - slen, dblue, SWT.NORMAL));

        long tm = xperf.p.endTime;
        long cpu = xperf.p.cpu;
        int sumCnt = 1;
        HashMap<Integer, Integer> indent = new HashMap<Integer, Integer>();
        for (int i = 0; i < profiles.length; i++) {

            if (truncated)
                break;

            if (profiles[i] instanceof StepSummary) {
                sb.append("        ").append(" ");
                sb.append(String.format("[%06d]", sumCnt++));
                sb.append(" ");

                StepSummary sum = (StepSummary) profiles[i];
                switch (sum.getStepType()) {
                    case StepEnum.METHOD_SUM:
                        XLogProfileView.isSummary = true;

                        MethodSum p = (MethodSum) sum;
                        slen = sb.length();

                        String m = TextProxy.method.getText(p.hash);
                        if (m == null)
                            m = Hexa32.toString32(p.hash);
                        sb.append(m).append(" ");

                        sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));

                        sb.append(" count=").append(FormatUtil.print(p.count, "#,##0"));
                        sb.append(" time=").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
                        sb.append(" cpu=").append(FormatUtil.print(p.cputime, "#,##0"));

                        sb.append("\n");
                        break;
                    case StepEnum.SQL_SUM:
                        XLogProfileView.isSummary = true;
                        SqlSum sql = (SqlSum) sum;
                        slen = sb.length();
                        toString(sb, sql, serverId);
                        sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.APICALL_SUM:
                        XLogProfileView.isSummary = true;
                        ApiCallSum apicall = (ApiCallSum) sum;
                        slen = sb.length();
                        toString(sb, apicall);
                        sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.SOCKET_SUM:
                        XLogProfileView.isSummary = true;
                        SocketSum socketSum = (SocketSum) sum;
                        slen = sb.length();
                        toString(sb, socketSum);
                        sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.CONTROL:

                        sb.delete(sb.length() - 9, sb.length());

                        sb.append("[******]");
                        sb.append(" ");
                        sb.append(FormatUtil.print(new Date(tm), "HH:mm:ss.SSS"));
                        sb.append("   ");
                        sb.append(String.format("%6s", FormatUtil.print(tm - prev_tm, "#,##0")));
                        sb.append(" ");
                        sb.append(String.format("%6s", FormatUtil.print(cpu - prev_cpu, "#,##0")));
                        sb.append("  ");
                        slen = sb.length();
                        toString(sb, (StepControl) sum);
                        sr.add(style(slen, sb.length() - slen, dred, SWT.NORMAL));
                        sb.append("\n");

                        truncated = true;

                        break;
                }
                continue;
            }

            StepSingle stepSingle = (StepSingle) profiles[i];
            tm = stepSingle.start_time + stime;
            cpu = stepSingle.start_cpu;

            // sr.add(style(sb.length(), 6, blue, SWT.NORMAL));
            int p1 = sb.length();
            String pid = String.format("[%06d]", stepSingle.parent);
            sb.append((stepSingle.parent == -1) ? "    -   " : pid);
            sb.append(" ");
            sb.append(String.format("[%06d]", stepSingle.index));
            sb.append(" ");
            sb.append(FormatUtil.print(new Date(tm), "HH:mm:ss.SSS"));
            sb.append("   ");
            sb.append(String.format("%6s", FormatUtil.print(tm - prev_tm, "#,##0")));
            sb.append(" ");
            sb.append(String.format("%6s", FormatUtil.print(XLogUtil.getCpuTime(stepSingle), "#,##0")));
            sb.append("  ");
            int lineHead = sb.length() - p1;

            int space = 0;
            if (indent.containsKey(stepSingle.parent)) {
                space = indent.get(stepSingle.parent) + 1;
            }
            indent.put(stepSingle.index, space);
            lineHead += space;
            while (space > 0) {
                sb.append(" ");
                space--;
            }

            switch (stepSingle.getStepType()) {
                case StepEnum.METHOD:
                    slen = sb.length();
                    toString(sb, (MethodStep) stepSingle, isSimplified);
                    sr.add(style(slen, 1, dyellow, SWT.BOLD));
                    break;
                case StepEnum.METHOD2:
                    slen = sb.length();
                    toString(sb, (MethodStep) stepSingle, isSimplified);
                    sr.add(style(slen, 1, dyellow, SWT.BOLD));
                    MethodStep2 m2 = (MethodStep2) stepSingle;
                    if (m2.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(m2.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.SQL:
                case StepEnum.SQL2:
                case StepEnum.SQL3:
                    SqlStep sql = (SqlStep) stepSingle;
                    slen = sb.length();
                    toString(sb, sql, serverId, lineHead, bindSqlParam);
                    sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));
                    if (sql.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(sql.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.MESSAGE:
                    slen = sb.length();
                    toString(sb, (MessageStep) stepSingle);
                    sr.add(style(slen, sb.length() - slen, dgreen, SWT.NORMAL));
                    break;
                case StepEnum.HASHED_MESSAGE:
                    slen = sb.length();
                    toString(sb, (HashedMessageStep) stepSingle);
                    sr.add(style(slen, sb.length() - slen, dgreen, SWT.NORMAL));
                    break;
                case StepEnum.DUMP:
                    slen = sb.length();
                    toString(sb, (DumpStep) stepSingle, lineHead);
                    sr.add(style(slen, sb.length() - slen, dgray, SWT.NORMAL));
                    break;
                case StepEnum.APICALL:
                    ApiCallStep apicall = (ApiCallStep) stepSingle;
                    slen = sb.length();
                    toString(sb, apicall);
                    sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
                    if (apicall.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(apicall.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.THREAD_SUBMIT:
                    ThreadSubmitStep threadSubmit = (ThreadSubmitStep) stepSingle;
                    slen = sb.length();
                    toString(sb, threadSubmit);
                    sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
                    if (threadSubmit.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(threadSubmit.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.SOCKET:
                    SocketStep socket = (SocketStep) stepSingle;
                    slen = sb.length();
                    toString(sb, socket);
                    sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                    if (socket.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(socket.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
            }
            sb.append("\n");
            prev_cpu = cpu;
            prev_tm = tm;
        }

        if (!truncated) {

            tm = xperf.p.endTime;
            cpu = xperf.p.cpu;
            sb.append("        ");
            sb.append(" ");
            sb.append("[******]");
            sb.append(" ");
            sb.append(FormatUtil.print(new Date(tm), "HH:mm:ss.SSS"));
            sb.append("   ");
            sb.append(String.format("%6s", FormatUtil.print(tm - prev_tm, "#,##0")));
            sb.append(" ");
            sb.append(String.format("%6s", FormatUtil.print(cpu - prev_cpu, "#,##0")));
            sb.append("  end of transaction \n");

        }
        sb.append("------------------------------------------------------------------------------------------\n");

        text.setText(sb.toString());
        text.setStyleRanges(sr.toArray(new StyleRange[sr.size()]));

    }

    public static void buildThreadProfile(XLogData data, StyledText text, Step[] profiles) {
        if (profiles == null) {
            profiles = new Step[0];
        }
        int serverId = data.serverId;
        String date = DateUtil.yyyymmdd(data.p.endTime);
        profiles = SortUtil.sort(profiles);
        XLogUtil.loadStepText(serverId, date, profiles);
        Color blue = text.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        Color dmagenta = text.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
        Color red = text.getDisplay().getSystemColor(SWT.COLOR_RED);
        Color dred = text.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
        Color dgreen = text.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
        java.util.List<StyleRange> sr = new ArrayList<StyleRange>();
        int slen = 0;
        final StringBuffer sb = new StringBuffer();

        sb.append("------------------------------------------------------------------------------------------\n");
        sb.append("    p#      #    	  TIME         T-GAP   CPU          CONTENTS\n");
        sb.append("------------------------------------------------------------------------------------------\n");
        if (profiles.length == 0) {
            sb.append("\n                     ( No xlog profile collected ) ");
            text.setText(sb.toString());
            text.setStyleRanges(sr.toArray(new StyleRange[sr.size()]));
            return;
        }

        long stime = data.p.endTime - data.p.elapsed;
        long prev_tm = stime;
        long tm = stime;
        int prev_cpu = -1;
        int cpu = 0;
        int sumCnt = 1;
        HashMap<Integer, Integer> indent = new HashMap<Integer, Integer>();
        for (int i = 0; i < profiles.length; i++) {
            if (profiles[i] instanceof StepSummary) {
                sb.append("        ").append(" ");
                sb.append(String.format("[%06d]", sumCnt++));
                sb.append(" ");

                StepSummary sum = (StepSummary) profiles[i];
                switch (sum.getStepType()) {
                    case StepEnum.METHOD_SUM:
                        MethodSum p = (MethodSum) sum;
                        slen = sb.length();
                        String m = TextProxy.method.getText(p.hash);
                        if (m == null)
                            m = Hexa32.toString32(p.hash);
                        sb.append(m).append(" ");

                        sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));

                        sb.append(" count=").append(FormatUtil.print(p.count, "#,##0"));
                        sb.append(" time=").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
                        sb.append(" cpu=").append(FormatUtil.print(p.cputime, "#,##0"));

                        sb.append("\n");
                        break;
                    case StepEnum.SQL_SUM:
                        SqlSum sql = (SqlSum) sum;
                        slen = sb.length();
                        toString(sb, sql, serverId);
                        sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.APICALL_SUM:
                        ApiCallSum apicall = (ApiCallSum) sum;
                        slen = sb.length();
                        toString(sb, apicall);
                        sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.SOCKET_SUM:
                        SocketSum socketSum = (SocketSum) sum;
                        slen = sb.length();
                        toString(sb, socketSum);
                        sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                        sb.append("\n");
                        break;
                    case StepEnum.CONTROL:
                        sb.delete(sb.length() - 9, sb.length());
                        sb.append("[******]");
                        sb.append(" ");
                        sb.append(FormatUtil.print(new Date(data.p.endTime), "HH:mm:ss.SSS"));
                        sb.append("   ");
                        sb.append(String.format("%6s", FormatUtil.print(data.p.elapsed, "#,##0")));
                        sb.append(" ");
                        sb.append(String.format("%6s", FormatUtil.print(data.p.cpu, "#,##0")));
                        sb.append("  ");
                        slen = sb.length();
                        toString(sb, (StepControl) sum);
                        sr.add(style(slen, sb.length() - slen, dred, SWT.NORMAL));
                        sb.append("\n");
                        break;
                }
                continue;
            }

            StepSingle stepSingle = (StepSingle) profiles[i];
            tm = stime + stepSingle.start_time;
            cpu = stepSingle.start_cpu;

            // sr.add(style(sb.length(), 6, blue, SWT.NORMAL));
            int p1 = sb.length();
            String pid = String.format("[%06d]", stepSingle.parent);
            sb.append((stepSingle.parent == -1) ? "    -   " : pid);
            sb.append(" ");
            sb.append(String.format("[%06d]", stepSingle.index));
            sb.append(" ");
            sb.append(FormatUtil.print(new Date(tm), "HH:mm:ss.SSS"));
            sb.append("   ");
            sb.append(String.format("%6s", FormatUtil.print(tm - prev_tm, "#,##0")));
            sb.append(" ");
            if (prev_cpu == -1) {
                sb.append(String.format("%6s", FormatUtil.print(0, "#,##0")));
            } else {
                sb.append(String.format("%6s", FormatUtil.print(XLogUtil.getCpuTime(stepSingle), "#,##0")));
            }

            sb.append("  ");
            int lineHead = sb.length() - p1;

            int space = 0;
            if (indent.containsKey(stepSingle.parent)) {
                space = indent.get(stepSingle.parent) + 1;
            }
            indent.put(stepSingle.index, space);
            lineHead += space;
            while (space > 0) {
                sb.append(" ");
                space--;
            }

            switch (stepSingle.getStepType()) {
                case StepEnum.METHOD:
                    toString(sb, (MethodStep) stepSingle, false);
                    break;
                case StepEnum.METHOD2:
                    toString(sb, (MethodStep) stepSingle, false);
                    MethodStep2 m2 = (MethodStep2) stepSingle;
                    if (m2.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(m2.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.SQL:
                case StepEnum.SQL2:
                case StepEnum.SQL3:
                    SqlStep sql = (SqlStep) stepSingle;
                    slen = sb.length();
                    toString(sb, sql, serverId, lineHead, false);
                    sr.add(style(slen, sb.length() - slen, blue, SWT.NORMAL));
                    if (sql.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(sql.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.MESSAGE:
                    slen = sb.length();
                    toString(sb, (MessageStep) stepSingle);
                    sr.add(style(slen, sb.length() - slen, dgreen, SWT.NORMAL));
                    break;
                case StepEnum.HASHED_MESSAGE:
                    slen = sb.length();
                    toString(sb, (HashedMessageStep) stepSingle);
                    sr.add(style(slen, sb.length() - slen, dgreen, SWT.NORMAL));
                    break;
                case StepEnum.DUMP:
                    slen = sb.length();
                    toString(sb, (DumpStep) stepSingle, lineHead);
                    sr.add(style(slen, sb.length() - slen, dgreen, SWT.NORMAL));
                    break;
                case StepEnum.APICALL:
                    ApiCallStep apicall = (ApiCallStep) stepSingle;
                    slen = sb.length();
                    toString(sb, apicall);
                    sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
                    if (apicall.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(apicall.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.THREAD_SUBMIT:
                    ThreadSubmitStep threadSubmit = (ThreadSubmitStep) stepSingle;
                    slen = sb.length();
                    toString(sb, threadSubmit);
                    sr.add(underlineStyle(slen, sb.length() - slen, dmagenta, SWT.NORMAL, SWT.UNDERLINE_LINK));
                    if (threadSubmit.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(threadSubmit.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
                case StepEnum.SOCKET:
                    SocketStep socket = (SocketStep) stepSingle;
                    slen = sb.length();
                    toString(sb, socket);
                    sr.add(style(slen, sb.length() - slen, dmagenta, SWT.NORMAL));
                    if (socket.error != 0) {
                        slen = sb.length();
                        sb.append("\n").append(TextProxy.error.getText(socket.error));
                        sr.add(style(slen, sb.length() - slen, red, SWT.NORMAL));
                    }
                    break;
            }
            sb.append("\n");
            prev_cpu = cpu;
            prev_tm = tm;
        }

        sb.append("------------------------------------------------------------------------------------------\n");

        text.setText(sb.toString());
        text.setStyleRanges(sr.toArray(new StyleRange[sr.size()]));
    }

    public static void toString(StringBuffer sb, ApiCallStep p) {
        String m = TextProxy.apicall.getText(p.hash);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        sb.append("call: ").append(m).append(" ").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        if (p.txid != 0) {
            sb.append(" <" + Hexa32.toString32(p.txid) + ">");
        }
    }

    public static void toString(StringBuffer sb, HashedMessageStep p) {
        String m = TextProxy.hashMessage.getText(p.hash);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        sb.append(m).append(" #").append(FormatUtil.print(p.value, "#,##0")).append(" ").append(FormatUtil.print(p.time, "#,##0")).append(" ms");
    }

    public static void toString(StringBuffer sb, DumpStep p, int lineHead) {
        sb.append("<auto generated thread dump>:[").append(p.threadId).append("] ").append(p.threadName).append('\n');
        sb.append(StringUtil.leftPad("", lineHead)).append("   -> State : ").append(p.threadState).append('\n');
        if(StringUtil.isNotEmpty(p.lockName)) {
            sb.append(StringUtil.leftPad("", lineHead)).append("   -> Lock : ").append(p.lockName).append('\n');
        }
        if(StringUtil.isNotEmpty(p.lockOwnerName)) {
            sb.append(StringUtil.leftPad("", lineHead)).append("   -> Lock Owner : ").append(p.lockOwnerName).append('\n');
        }
        if(p.lockOwnerId > 0) {
            sb.append(StringUtil.leftPad("", lineHead)).append("   -> Lock Owner Id ").append(p.lockOwnerId).append('\n');
        }

        for(int stackElementHash : p.stacks) {
            String m = TextProxy.stackElement.getText(stackElementHash);
            if(m == null) {
                m = Hexa32.toString32(stackElementHash);
            }
            sb.append(StringUtil.leftPad("", lineHead)).append(m).append('\n');
        }
    }

    public static void toString(StringBuffer sb, ThreadSubmitStep p) {
        String m = TextProxy.apicall.getText(p.hash);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        sb.append("thread: ").append(m).append(" ").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        if (p.txid != 0) {
            sb.append(" <" + Hexa32.toString32(p.txid) + ">");
        }
    }

    public static void toString(StringBuffer sb, SocketStep p) {
        String ip = IPUtil.toString(p.ipaddr);
        sb.append("socket: ").append(ip == null ? "unknown" : ip).append(":").append(p.port + " ")
                .append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
    }

    public static void toString(StringBuffer sb, ApiCallSum p) {
        String m = TextProxy.apicall.getText(p.hash);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        sb.append("call: ").append(m).append(" ");
        sb.append(" count=").append(FormatUtil.print(p.count, "#,##0"));
        sb.append(" time=").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        sb.append(" cpu=").append(FormatUtil.print(p.cputime, "#,##0"));
        if (p.error > 0) {
            sb.append(" error=").append(FormatUtil.print(p.error, "#,##0"));
        }
    }

    public static void toString(StringBuffer sb, SocketSum p) {
        String ip = IPUtil.toString(p.ipaddr);
        sb.append("socket: ").append(ip == null ? "unknown" : ip).append(":").append(p.port + " ");
        sb.append(" count=").append(FormatUtil.print(p.count, "#,##0"));
        sb.append(" time=").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        if (p.error > 0) {
            sb.append(" error=").append(FormatUtil.print(p.error, "#,##0"));
        }
    }

    public static void toString(StringBuffer sb, SqlStep p, int serverId, int lineHead, boolean bindParam) {
        if (p instanceof SqlStep2) {
            sb.append(SqlXType.toString(((SqlStep2) p).xtype));
        }
        String m = TextProxy.sql.getText(p.hash);
        m = spacing(m, lineHead);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        Server server = ServerManager.getInstance().getServer(serverId);
        boolean showParam = true;
        if (server != null) {
            showParam = server.isAllowAction(GroupPolicyConstants.ALLOW_SQLPARAMETER);
        }
        if (showParam && bindParam) {
        	sb.append(SqlMakerUtil.replaceSQLParameter(m, p.param));
        } else {
        	sb.append(m);
        }
        if (StringUtil.isEmpty(p.param) == false) {
            if (bindParam == false) {
	            sb.append("\n").append(StringUtil.leftPad("", lineHead));
	            sb.append("[").append(showParam ? p.param : "******").append("]");
            }
        }
        if (p instanceof SqlStep3) {
            int updatedCount = ((SqlStep3) p).updated;
            if (updatedCount > SqlStep3.EXECUTE_RESULT_SET) {
                sb.append(" <Affected Rows : " + updatedCount + ">");
            } else if (updatedCount == SqlStep3.EXECUTE_UNKNOWN_COUNT) {
                sb.append(" <Affected Rows : unknown>");
            }
        }
        sb.append(" ").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
    }

    public static String spacing(String m, int lineHead) {
        if (m == null)
            return m;
        String dummy = StringUtil.leftPad("", lineHead);
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader sr = new BufferedReader(new StringReader(m));
            String s = null;
            while ((s = sr.readLine()) != null) {
                s = StringUtil.trim(s);
                if (s.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("\n").append(dummy);
                    }
                    sb.append(s);
                }

            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    public static void toString(StringBuffer sb, SqlSum p, int serverId) {
        String m = TextProxy.sql.getText(p.hash);
        if (m == null)
            m = Hexa32.toString32(p.hash);
        sb.append(m);
        if (StringUtil.isEmpty(p.param) == false) {
            Server server = ServerManager.getInstance().getServer(serverId);
            boolean showParam = server.isAllowAction(GroupPolicyConstants.ALLOW_SQLPARAMETER);
            sb.append(" [").append(showParam ? p.param : "******").append("]");
        }
        sb.append(" count=").append(FormatUtil.print(p.count, "#,##0"));
        sb.append(" time=").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        sb.append(" cpu=").append(FormatUtil.print(p.cputime, "#,##0"));
        if (p.error > 0) {
            sb.append(" error=").append(FormatUtil.print(p.error, "#,##0"));
        }
    }

    public static void toString(StringBuffer sb, MessageStep p) {
        sb.append(p.message);
    }

    public static void toString(StringBuffer sb, StepControl p) {
        sb.append(p.message);
    }

    public static void toString(StringBuffer sb, MethodStep p, boolean isSimplified) {
        String m = TextProxy.method.getText(p.hash);
        if (m == null) {
            m = Hexa32.toString32(p.hash);
        }

        if(isSimplified) {
            String simple = simplifyMethod(m);
            sb.append(simple).append(" [").append(FormatUtil.print(p.elapsed, "#,##0")).append("ms]").append("  --- [Full Name] ").append(m);
        } else {
            sb.append(m).append(" ").append(FormatUtil.print(p.elapsed, "#,##0")).append(" ms");
        }
    }

    public static String simplifyMethod(String method) {
        String[] parts = StringUtil.split(method, '.');
        if(parts.length >= 2) {
            String methodName = parts[parts.length - 1];
            int bracePos = methodName.indexOf('(');

            return parts[parts.length - 2] + "." + methodName.substring(0, bracePos) + "";
        } else {
            return method;
        }
    }

    public static StyleRange style(int start, int length, Color c, int f) {
        StyleRange t = new StyleRange();
        t.start = start;
        t.length = length;
        t.foreground = c;
        t.fontStyle = f;
        return t;
    }

    public static StyleRange style(int start, int length, int f) {
        StyleRange t = new StyleRange();
        t.start = start;
        t.length = length;
        t.fontStyle = f;
        return t;
    }

    public static StyleRange style(int start, int length, Color c, int f, Color backc) {
        StyleRange t = new StyleRange();
        t.start = start;
        t.length = length;
        t.foreground = c;
        t.fontStyle = f;
        t.background = backc;
        return t;
    }

    public static StyleRange underlineStyle(int start, int length, Color c, int fontStyle, int underlineStyle) {
        StyleRange t = new StyleRange();
        t.start = start;
        t.length = length;
        t.foreground = c;
        t.fontStyle = fontStyle;
        t.underline = true;
        t.underlineStyle = underlineStyle;
        return t;
    }
}

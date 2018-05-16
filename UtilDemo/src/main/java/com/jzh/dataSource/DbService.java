package com.jzh.dataSource;


import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.StringUtils;


public class DbService {

    private static Log log = LogFactory.getLog(DbService.class);
    private static String infoMsg = "->IF DbService";

    public static String dataSourceName = "dataSource";

    private static DataSource dataSource = null;

    /**
     * 通过Spring取一个数据库连接
     * @return 数据库连接
     * @throws DbAccessException
     */
	/*public static Connection getConnection() throws DbAccessException{
		try {
			if(dataSource == null){
				dataSource = (DataSource) ComponentFactory.getComponentByItsName(dataSourceName);
			}
			return DataSourceUtils.getConnection(dataSource);
		} catch (Exception e) {
			throw new DbAccessException(e);
		}
	}*/
    //本地连接
    public static Connection getConnection() throws DbAccessException{
        String url = "jdbc:oracle:thin:@106.14.215.174:1521:orcl";
        String username = "animation";
        String password = "animation";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new DbAccessException(e);
        }
    }

    /**
     * 返回一个数据库连接到spring连接池
     * @param connection 数据库连接
     */
    public static void releaseConnection(Connection connection){
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    /**
     * 关闭游标，准备语句，释放数据库连接
     * @param rs 游标
     * @param ps 准备语句
     * @param conn 数据库连接
     */
    public static void closeRsPsConn(ResultSet rs, PreparedStatement ps,
                                     Connection conn){
        if(rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
            }
        if(ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
            }
        releaseConnection(conn);
    }

    /**
     * 关闭准备语句，释放数据库连接
     * @param ps 准备语句
     * @param conn 数据库连接
     */
    public static void closePsConn(PreparedStatement ps, Connection conn){
        if(ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
            }
        releaseConnection(conn);
    }

    /**
     * 关闭一个游标
     * @param rs 游标
     */
    public final static void closeResultSet(ResultSet rs){
        if(rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
            }
    }

    /**
     * 关闭执行语句 ，释放数据库
     * @param cs 执行语句
     * @param conn 数据库连接
     */
    public final static void closeCsConn(CallableStatement cs, Connection conn){
        if(cs != null)
            try {
                cs.close();
            } catch (SQLException e) {
            }
        releaseConnection(conn);
    }

    /**
     * 复制问号times次，中间以逗号隔开
     * @param times 次数
     * @return 问号字符串  e.g "?,?,?,?,?" times = 5
     */
    public static String dupeAsk(int times){
        StringBuffer ret = new StringBuffer();
        if(times > 0){
            for (int i = 0; i < times-1; i++)
                ret.append("?,");
            ret.append("?");
        }
        return ret.toString();
    }

    /**
     * 将list转化为字符串数组
     * @param list
     * @return	字符串数组
     */
    public static String[] list2StringArr(List<Object> list){
        if(list == null)
            return null;
        String[] ret = new String[list.size()];
        Object obj;
        for (int i = 0; i < ret.length; i++) {
            obj = list.get(i);
            ret[i] = obj == null ? null : obj.toString();
        }
        return ret;
    }

    /**
     * 将ResultSet转换为二维list，外层list内含arraylist
     * @param rs
     * @return list对象
     * @throws SQLException
     */
    public static List<Object> getListFromRs(ResultSet rs) throws SQLException{
        List<Object> retList = new ArrayList<Object>();
        ResultSetMetaData remeta = rs.getMetaData();
        int columnCount = remeta.getColumnCount();
        String recordValue = "";
        ArrayList<Object> oneList = null;
        while(rs.next()){
            oneList = new ArrayList<Object>();
            for (int i = 1; i <= columnCount; i++) {
                recordValue = rs.getString(i);
                oneList.add(recordValue == null ? "" : recordValue.toString());
            }
            retList.add(oneList);
        }
        return retList;
    }

    /**
     * 将ResultSet转换为二维list，外层list内含LinkedHashMap
     * @param rs
     * @return list对象
     * @throws SQLException
     */
    public static List<Object> getListFromRsForMap(ResultSet rs) throws SQLException{
        List<Object> retList = new ArrayList<Object>();
        ResultSetMetaData remeta = rs.getMetaData();
        int columnCount = remeta.getColumnCount();
        String recordValue = "";
        LinkedHashMap<String, Object> m = null;
        while(rs.next()){
            m = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                recordValue = rs.getString(i);
                m.put(remeta.getColumnName(i), recordValue == null ? "" : recordValue.toString());
            }
            retList.add(m);
        }
        return retList;
    }

    public static String getStringArrayOut(String[] inStrs){
        if(inStrs == null || inStrs.length == 0)
            return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inStrs.length; i++) {
            sb.append(inStrs[i] + ", ");
        }
        String out = sb.toString();
        if(out.endsWith(", "))
            out = out.substring(0, out.length()-2);
        return out;
    }

    /**
     * 输出 参数类型数组 缓存
     */
    public static HashMap<String, Object> ProcOutParamTypesMap = new HashMap<String, Object>();

    /**
     * 取出输出参数类型数组
     * @param conn 用来取输出参数情况的连接，不对其做提交动作
     * @param procName [包名.]过程名
     * @return 输出参数类型数组
     * @throws SQLException
     */
    public static int[] getOutParamTypes(Connection conn, String procName)
            throws SQLException{
        PreparedStatement ps = null;
        ResultSet paramTypes = null;
        int[] resu = null;
        try {
            if(ProcOutParamTypesMap.containsKey(procName)){
                resu = (int[]) ProcOutParamTypesMap.get(procName);
            }else{
                int userPos = procName.indexOf("->");
                String getOTSQL;
                if(userPos == -1){
                    getOTSQL = "SELECT data_type, in_out FROM user_arguments WHERE data_level = 0 AND object_name = ?";
                }else{
                    String userName = procName.substring(0, userPos);
                    getOTSQL = "SELECT data_type, in_out FROM user_arguments WHERE owner = '"
                            + userName.toUpperCase()
                            + "' AND data_level = 0 AND object_name = ?";
                }
                int dotPos = procName.indexOf(".");
                if(dotPos != -1) //有包
                    getOTSQL += " AND package_name = ?";
                getOTSQL += " ORDER BY in_out, sequence";
                ps = conn.prepareStatement(getOTSQL);
                if(dotPos != -1){
                    ps.setString(1, procName.substring(dotPos+1).toUpperCase());
                    ps.setString(2, procName.substring(userPos == -1 ? 0 : userPos + 2, dotPos).toUpperCase());
                }else{
                    ps.setString(1, userPos == -1 ? procName : procName.substring(userPos + 2).toUpperCase());
                }
                paramTypes = ps.executeQuery();

                ArrayList<Object> outParamTypes = new ArrayList<Object>();
                while(paramTypes.next()){
                    String data_type = paramTypes.getString(1);
                    String in_out = paramTypes.getString(2);
                    if(in_out.equals("OUT")){
                        if(data_type.equals("REF CURSOR")){
                            outParamTypes.add(new Integer(OracleTypes.CURSOR));
                        }else{
                            outParamTypes.add(new Integer(OracleTypes.VARCHAR));
                        }
                    }
                }

                resu = new int[outParamTypes.size()];
                for (int i = 0; i < resu.length; i++)
                    resu[i] = ((Integer)(outParamTypes.get(i))).intValue();
                ProcOutParamTypesMap.put(procName, resu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }finally{
            try {
                if(paramTypes != null)
                    paramTypes.close();
                if(ps != null)
                    ps.close();
            } catch (SQLException e) {
            }
        }
        return resu;
    }

    /**
     * 执行sql查询服务
     * @param strSql 查询sql语句
     * @param inParas 参数数组
     * @return 查询结果数据集（二维数组形式）
     * @throws DbAccessException
     */
    public static List<Object> executeQuery(String strSql, String[] inParas)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(inParas != null)
                for (int i = 0; i < inParas.length; i++)
                    ps.setString(i+1, inParas[i]);
            rs = ps.executeQuery();
            return getListFromRs(rs);
        } catch (SQLException e) {
            log.error(infoMsg+".excuteQuery sql=["+strSql+"] inParas = "
                    + getStringArrayOut(inParas));
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
    }

    /**
     * 执行sql更新服务
     * @param strSql 更新sql语句（insert update delete）
     * @param inParas 参数数组，代替sql中的问号的参数值
     * @return sql执行影响的记录条数
     * @throws DbAccessException
     */
    public static int executeUpdate(String strSql, String[] inParas)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(inParas != null)
                for (int i = 0; i < inParas.length; i++)
                    ps.setString(i+1, inParas[i]);
            int n = ps.executeUpdate();//更新sql语句
            return n;
        } catch (SQLException e) {
            log.error(infoMsg+".executeUpdate sql=["+strSql+"] inParas = "
                    + getStringArrayOut(inParas));
            throw new DbAccessException(e);
        }finally{
            closePsConn(ps, conn);
        }
    }

    /**
     * 执行sql更新服务
     * @param strSql 更新sql语句（insert update delete）
     * @param inParas 参数数组，代替sql中的问号的参数值
     * @return sql执行影响的记录条数
     * @throws DbAccessException
     */
    public static int executeUpdate(String strSql, List<Object> inParas)
            throws DbAccessException{
        return executeUpdate(strSql,
                (String[])inParas.toArray(new String[inParas.size()]));
    }

    /**
     * 执行Truncate
     * @param strSql 待执行的Truncate语句
     * @return
     */
    public static boolean executeTruncate(String strSql){
        Connection conn = null;
        boolean truncateResult = false;
        try {
            conn =  getConnection();
            truncateResult = conn.createStatement().execute(strSql);
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }finally{
            try {
                releaseConnection(conn);
            } catch (Exception e) {
                throw new DbAccessException(e);
            }
        }
        return truncateResult;
    }

    /**
     * 执行存储过程服务
     * @param proc		[用户名->][包名.]过程名
     * @param inParams		过程输入参数数组
     * @param throwExpFlag	是否将过程第一个返回值视为成功与否的标志
     * 						若throwExpFlag为true，且proc的第一个返回值为非0值，则认为存储过程需要进行回滚，
     * 						将抛出DbAccessException，可通过DbAccessException.getMessage()获得过程第二个参数返回的提示信息
     * @return	过程返回结果集
     * @throws DbAccessException
     */
    public static List<Object> executeProcedure(String proc, String[] inParams,
                                                boolean throwExpFlag) throws DbAccessException{
        Connection conn = null;
        CallableStatement cs = null;
        int[] outParamTypes = null;
        List<Object> resultsets = new ArrayList<Object>();
        try {
            conn = getConnection();
            outParamTypes = getOutParamTypes(conn, proc);
            String procStr = "{call "+proc.replaceAll("\\-\\>", ".")+"("
                    +dupeAsk(inParams.length + outParamTypes.length)+ ")}";
            cs = conn.prepareCall(procStr);
            if(inParams != null)
                for (int i = 0; i < inParams.length; i++)
                    cs.setString(i, inParams[i-1]);
            for (int j = 0; j < outParamTypes.length; j++)
                cs.registerOutParameter(inParams.length + j, outParamTypes[j-1]);
            cs.execute();

            ArrayList<Object> outParams = new ArrayList<Object>();
            Object obj = null;
            for (int j = 0; j < outParamTypes.length; j++) {
                obj = cs.getObject(inParams.length+j);
                if(j == outParamTypes.length && throwExpFlag){
                    if(!"0".equals(obj))
                        throw new DbAccessException((String)obj);
                }
                if(obj instanceof ResultSet){
                    resultsets.add(obj);
                    outParams.add(getListFromRs((ResultSet) obj));
                }else{
                    outParams.add(obj);
                }
            }
            return outParams;
        } catch (Exception e) {
            log.error(infoMsg+".executeProcedure proc=["+proc+"] inParas = "
                    + getStringArrayOut(inParams));
            throw e instanceof DbAccessException ? (DbAccessException)e : new DbAccessException(e);
        }finally{
            for (int i = 0; i < resultsets.size(); i++) {
                closeResultSet((ResultSet)resultsets.get(i));
            }
            closeCsConn(cs, conn);
        }
    }

    /**
     * 执行存储过程服务
     * @param proc		[用户名->][包名.]过程名
     * @param inParams		过程输入参数数组
     * @return	过程返回结果集
     * @throws DbAccessException
     */
    public static List<Object> executeProcedure(String proc, String[] inParams)
            throws DbAccessException{
        return executeProcedure(proc, inParams, false);
    }

    /**
     * 执行存储过程服务
     * @param proc		[用户名->][包名.]过程名
     * @param inParams		过程输入参数list
     * @return	过程返回结果集
     * @throws DbAccessException
     */
    public static List<Object> executeProcedure(String proc, List<Object> inParams)
            throws DbAccessException{
        return executeProcedure(proc, list2StringArr(inParams));
    }

    /**
     * 执行存储过程服务
     * @param proc		[用户名->][包名.]过程名
     * @param inParams		过程输入参数list
     * @param throwExpFlag	是否将过程第一个返回值视为成功与否的标志
     * 						若throwExpFlag为true，且proc的第一个返回值为非0值，则认为存储过程需要进行回滚，
     * 						将抛出DbAccessException，可通过DbAccessException.getMessage()获得过程第二个参数返回的提示信息
     * @return	过程返回结果集
     * @throws DbAccessException
     */
    public static List<Object> executeProcedure(String proc, List<Object> inParams,
                                                boolean throwExpFlag) throws DbAccessException{
        return executeProcedure(proc, list2StringArr(inParams), throwExpFlag);
    }

    /**
     * 执行存储过程服务，但不提交事务，异常也不会回滚；不关闭连接。返回数据中ResultSet任然为ResultSet，不会转换为List
     * 注：此方法返回的ArrayList结构为{0=List of ResultSet，1=cs}，此方法返回了CallableStatement给使用者
     * 由使用者在处理完ResultSet后进行cs的关闭动作
     * @param conn	数据库连接
     * @param proc	[用户名->][包名.]过程名
     * @param inParams	过程输入参数数组
     * @return
     * @throws DbAccessException
     */
    public static ArrayList<Object> executeProcdureNoCommit(
            Connection conn, String proc, String[] inParams)
            throws DbAccessException{
        CallableStatement cs = null;

        try {
            int[] outParamTypes = getOutParamTypes(conn, proc);
            String procStr = "{call "+proc.replaceAll("\\-\\>", ".")+"("
                    +dupeAsk(inParams.length+outParamTypes.length)+")}";
            cs = conn.prepareCall(procStr);
            for (int i = 1; i < inParams.length; i++)
                cs.setString(i, inParams[i-1]);
            for (int j = 1; j < outParamTypes.length; j++)
                cs.registerOutParameter(inParams.length+j, outParamTypes[j-1]);

            cs.execute();

            ArrayList<Object> outParams = new ArrayList<Object>();
            for (int j = 1; j < outParamTypes.length; j++) {
                Object obj = null;
                try {
                    obj = cs.getObject(inParams.length+j);
                } catch (SQLException e) {
                }
                outParams.add(obj);
            }
            ArrayList<Object> ret = new ArrayList<Object>();
            ret.add(outParams);
            ret.add(cs);
            return ret;

        } catch (SQLException e) {
            log.error(infoMsg+".executeProcedure proc=["+proc+"] inParas = "
                    + getStringArrayOut(inParams));
            throw new DbAccessException(e);
        }

    }

    /**
     * 执行存储过程服务，但不提交事务，异常也不会回滚；不关闭连接。返回数据中ResultSet任然为ResultSet，不会转换为List
     * 注：此方法返回的ArrayList结构为{0=List of ResultSet，1=cs}，此方法返回了CallableStatement给使用者
     * 由使用者在处理完ResultSet后进行cs的关闭动作
     * @param conn	数据库连接
     * @param proc	[用户名->][包名.]过程名
     * @param inParams	过程输入参数数组
     * @return
     * @throws DbAccessException
     */
    public static ArrayList<Object> executeProcdureNoCommit(
            Connection conn, String proc, List<Object> inParams)
            throws DbAccessException{
        return executeProcdureNoCommit(conn, proc, list2StringArr(inParams));
    }

    /**
     * 执行sql批量更新服务，所有sql执行完毕提交
     * @param strSql	更新sql语句（insert update delete）
     * @param param	参数数组二维数组，替代sql中问号的参数值
     * @return	sql执行影响的记录条数（可能返回-2，说明数据库因优化不能返回明确条数）
     * @throws DbAccessException
     */
    public static int[] executeBatchUpdate(String strSql, String[][] param)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(param!=null)
                for (int i = 0; i < param.length; i++) {
                    for (int j = 0; j < StringUtils.countOccurrencesOf(strSql, "?"); j++)
                        ps.setObject(j+1, param[i][j]);
                    ps.addBatch();
                }
            int[] n = ps.executeBatch();
            return n;
        } catch (Exception e) {
            log.error(infoMsg + ".executeBatchUpdate sql=["+strSql+"]");
            throw new DbAccessException(e);
        }finally{
            closePsConn(ps, conn);
        }
    }

    /**
     *  执行sql批量更新服务,到达提交点时提交
     * @param strSql	更新sql语句（insert update delete）
     * @param param	参数数组二维数组，替代sql中问号的参数值
     * @param executePoint	执行点
     * @return	sql执行影响的记录条数（可能返回-2，说明数据库因优化不能返回明确条数）
     * @throws DbAccessException
     */
    public static int[] executeBatchUpdate(String strSql, String[][] param,
                                           int executePoint)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            int[] resu = null;
            if(param!=null){
                resu = new int[param.length];
                int startPoint = 0;
                for (int i = 0; i < param.length; i++){
                    for (int j = 0; j < param[i].length; j++)
                        ps.setObject(j+1, param[i][j]);
                    ps.addBatch();
                    if(i % executePoint == executePoint -1 || i==param.length-1){
                        int[] n = ps.executeBatch();
                        for (int k = startPoint; k <= i; k++)
                            resu[k] = n[k-startPoint];
                        startPoint = i+1;
                    }
                }
            }
            return resu;
        } catch (Exception e) {
            log.error(infoMsg + ".executeBatchUpdate sql=["+strSql+"]");
            throw new DbAccessException(e);
        }finally{
            closePsConn(ps, conn);
        }
    }

    /**
     * 执行sql，返回第一行第一列结果
     * @param strSql	被执行的sql，带？需绑定形式，不需要分号
     * @param inParams	输入绑定参数
     * @return	返回单个String结果
     * @throws DbAccessException
     */
    public static String executeQuerySngl(String strSql, String[] inParams)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(inParams!=null)
                for (int i = 0; i < inParams.length; i++)
                    ps.setString(i+1, inParams[i]);
            rs = ps.executeQuery();
            if(rs.next())
                return rs.getString(1);
            return null;
        } catch (Exception e) {
            log.error(infoMsg + ".executeQuerySngl sql=["+strSql+"]"
                    +" inParams ="+getStringArrayOut(inParams));
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
    }

    /**
     * 执行sql查询服务
     * @param strSql	查询sql语句
     * @param inParas	参数数组
     * @return	查询结果数据集
     * @throws DbAccessException
     */
    public static ResultSet query(String strSql, String[] inParas)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(inParas!=null)
                for (int i = 0; i < inParas.length; i++)
                    ps.setString(i+1, inParas[i]);
            rs = ps.executeQuery();
            return rs;
        } catch (Exception e) {
            log.error(infoMsg + " Query sql=["+strSql+"]"
                    +" inParams ="+getStringArrayOut(inParas));
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
    }

    /**
     * 执行sql，返回第一行记录
     * @param strSql	被执行的sql，带？需绑定形式，不需要分号
     * @param inParams	输入绑定参数
     * @return	返回单个String结果
     * @throws DbAccessException
     */
    public static List<String> executeQuerySnglRow(String strSql, String[] inParams)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(strSql);
            if(inParams!=null)
                for (int i = 0; i < inParams.length; i++)
                    ps.setString(i+1, inParams[i]);
            rs = ps.executeQuery();

            ResultSetMetaData rsmeta = rs.getMetaData();
            int columnCount = rsmeta.getColumnCount();
            String recordValue = "";
            ArrayList<String> oneList = null;
            if(rs.next()){
                oneList = new ArrayList<String>();
                for (int i = 1; i <= columnCount; i++) {
                    recordValue = rs.getString(i);
                    oneList.add(recordValue == null ? "" :recordValue.toString());
                }
            }
            return oneList;
        } catch (Exception e) {
            log.error(infoMsg + ".executeQuerySnglRow sql=["+strSql+"]"
                    +" inParams ="+getStringArrayOut(inParams));
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
    }

    /*
     * 用法String sysdatetime = getDbDATETIME(); YYYY-MM-DDHHMMSS
     * String sysdate = sysdatetime.subString(0,10);
     * String systime = sysdatetime.subString(10,16);
     */
    private static long plusToDBTime = Long.MIN_VALUE;
    private static SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
    private static SimpleDateFormat fmtMicro = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * 重新获取数据库与前台服务器之间的差异，并置入缓存，作为前台取后台数据库时间的方法
     */
    public static void refreshDbTime2Cache(){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT systimestamp FROM dual");
            rs = ps.executeQuery();
            rs.next();
            plusToDBTime = rs.getTimestamp(1).getTime()-System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
    }

    /**
     * 获取数据库时间
     * @return	返回格式为yyyyMMddHHmmss的数据库时间
     */
    public static String getDbDATETIME(){
        if(plusToDBTime == Long.MIN_VALUE)
            refreshDbTime2Cache();
        Date nowDate = new Date(plusToDBTime + System.currentTimeMillis());
        return fmt.format(nowDate);
    }

    /**
     * 获取数据库微秒时间
     * @return	返回格式为yyyyMMddHHmmssSSS的数据库时间
     */
    public static String getMicroSecondDbDATETIME(){
        if(plusToDBTime == Long.MIN_VALUE)
            refreshDbTime2Cache();
        Date nowDate = new Date(plusToDBTime + System.currentTimeMillis());
        return fmtMicro.format(nowDate);
    }

    /**
     * 提交一个数据库连接
     * @param conn	数据库连接
     * @throws DbAccessException
     */
    public final static void commitConn(Connection conn)
            throws DbAccessException{
        if(conn!=null)
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new DbAccessException(e);
            }
    }

    /**
     * 回滚一个数据库连接
     * @param conn	数据库
     * @throws DbAccessException
     */
    public final static void rollbackConn(Connection conn)
            throws DbAccessException{
        if(conn!=null)
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new DbAccessException(e);
            }
    }

    /**
     * 应用反射机制返回单条记录
     * @param sql 语句
     * @param params 占位符
     * @param tJavabean 类，这里我用的是（SmartHome_mysql.class）
     *        javabean，我理解的是一个高度封装组件，成员为私有属性，只能
     *        通过set/get方法赋值和取值
     * @return 泛型
     * @throws DbAccessException
     */
    public static <T> T returnSimpleResultFirstRow(String sql,List<Object> params,Class<T> tJavabean)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T tResult = null;
        int index = 1;
        try{
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(params != null && !params.isEmpty()){
                for(int i = 0;i<params.size();i++){
                    ps.setObject(index++, params.get(i));
                }
            }
            rs = ps.executeQuery(sql);
            //封装resultset
            ResultSetMetaData metaData = rs.getMetaData();//获得列的信息
            int columnLength = metaData.getColumnCount();//获得列的长度
            if(rs.next()){//只取查询的到的第一个值
                tResult = tJavabean.newInstance();//通过反射机制创建一个实例
                for(int i = 0;i<columnLength;i++){
                    String metaDateKey = metaData.getColumnName(i+1);
                    Object resultsetValue = rs.getObject(metaDateKey);
                    if(resultsetValue == null){
                        resultsetValue = "";
                    }
                    //获取列的属性，无论是公有。保护还是私有，都可以获取
                    Field field = tJavabean.getDeclaredField(metaDateKey);
                    field.setAccessible(true);//打开javabean的访问private权限
                    field.set(tResult, resultsetValue);//给javabean对应的字段赋值
                }
            }
        } catch (Exception e) {
            log.error(infoMsg + ".returnSimpleResultFirstRow sql=["+sql+"]"
                    +" inParams ="+getStringArrayOut(list2StringArr(params))
                    +" class ="+tJavabean.getName());
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
        return tResult;
    }

    /**
     * 通过反射机制访问数据库，并返回多条记录
     * @param sql 语句
     * @param params 占位符
     * @param tJavabean ,会执行javabean类里面的toString方法
     * @return
     * @throws DbAccessException
     */
    public static <T> List<T> returnMultipleResult(String sql, List<Object> params, Class<T> tJavabean)
            throws DbAccessException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<T>();
        int index = 1;
        try{
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(params != null && !params.isEmpty()){
                for(int i = 0;i<params.size();i++)
                    ps.setObject(index, params.get(i));
            }
            rs = ps.executeQuery(sql);
            //封装resultset
            ResultSetMetaData metaData = rs.getMetaData();//取出列的信息
            int columnLength = metaData.getColumnCount();//获取列数
            while(rs.next()){
                T tResult = tJavabean.newInstance();//通过反射机制创建一个对象
                for(int i = 0;i<columnLength;i++){
                    String metaDataKey = metaData.getColumnName(i+1);
                    Object resultsetValue = rs.getObject(metaDataKey);
                    if(resultsetValue == null)
                        resultsetValue = "";
                    Field field = tJavabean.getDeclaredField(metaDataKey);
                    field.setAccessible(true);
                    field.set(tResult, resultsetValue);
                }
                list.add(tResult);
            }
        } catch (Exception e) {
            log.error(infoMsg + ".returnMultipleResult sql=["+sql+"]"
                    +" inParams ="+getStringArrayOut(list2StringArr(params))
                    +" class ="+tJavabean.getName());
            throw new DbAccessException(e);
        }finally{
            closeRsPsConn(rs, ps, conn);
        }
        return list;
    }

}

package com.stb.tools;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动生成MyBatis的实体类、实体映射XML文件、Mapper
 * @author heshuang
 * @create 2018-07-31 16:23
 **/
public class SQLToBean {

    private static String pan = "d:/";

    private final String type_char = "char";

    private final String type_date = "date";

    private final String type_timestamp = "timestamp";

    private final String type_datetime = "datetime";

    private final String type_int = "int";

    private final String type_bigint = "bigint";

    private final String type_text = "text";

    private final String type_bit = "bit";

    private final String type_decimal = "decimal";

    private final String type_blob = "blob";


    private final String moduleName = "bean"; // 对应模块名称（根据自己模块做相应调整!!!务必修改^_^）

    private final String bean_path = pan + "hs_factory/entity";

    private final String mapper_path = pan + "hs_factory/mapper";

    private final String xml_path = pan + "hs_factory/mapper/xml";

    private final String bean_package = "com.hs." + moduleName + ".entity";

    private final String mapper_package = "com.hs." + moduleName + ".mapper";


    private final String driverName = "com.mysql.jdbc.Driver";

    private  String dbName = "";

    private  String user = "";

    private  String password = "";

    private String url = "jdbc:mysql://127.0.0.1:3306/" + dbName + "?characterEncoding=utf8";

    private String tableName = null;

    private String beanName = null;

    private String mapperName = null;

    private Connection conn = null;


    public SQLToBean(){

    }

    public SQLToBean(String ip, String port, String dbName, String pan, String user, String password) {
        this.url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?characterEncoding=utf8";
        this.pan = pan + ":/";
        this.user = user;
        this.password = password;
    }

    public static void main(String[] args) {
        try {
            new SQLToBean().generate();
            // 自动打开生成文件的目录
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean init() {
        boolean flag = true;
        try {
            createFile();
            Class.forName(driverName);
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    public void closeConn() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //创建文件夹
    private void createFile() {
        String url = pan + "hs_factory";
        File file = new File(url);
        if (!file.exists()) {
            file.mkdir();
        }
    }


    /**
     * 获取所有的表
     *
     * @return
     * @throws SQLException
     */
    private List<String> getTables() throws SQLException {
        List<String> tables = new ArrayList<String>();
        PreparedStatement pstate = conn.prepareStatement("show tables");
        ResultSet results = pstate.executeQuery();
        while (results.next()) {
            String tableName = results.getString(1);
            //          if ( tableName.toLowerCase().startsWith("yy_") ) {
            tables.add(tableName);
            //          }
        }
        return tables;
    }


    private void processTable(String table) {
        if(table.substring(0,1).equals("t") && table.indexOf("_") == 2){
            table = table.substring(3,table.length());
        }
        StringBuffer sb = new StringBuffer(table.length());
        String tableNew = table.toLowerCase();
        String[] tables = tableNew.split("_");
        String temp = null;
        for (int i = 0; i < tables.length; i++) {
            temp = tables[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        beanName = sb.toString();
        mapperName = beanName + "Mapper";
    }


    private String processType(String type) {
        if (type.indexOf(type_char) > -1) {
            return "String";
        } else if (type.indexOf(type_bigint) > -1) {
            return "Long";
        } else if (type.indexOf(type_int) > -1) {
            return "Integer";
        } else if (type.indexOf(type_date) > -1) {
            return "java.util.Date";
        } else if (type.indexOf(type_text) > -1) {
            return "String";
        } else if (type.indexOf(type_timestamp) > -1) {
            return "java.util.Date";
        } else if (type.indexOf(type_datetime) > -1) {
            return "java.util.Date";
        } else if (type.indexOf(type_bit) > -1) {
            return "Boolean";
        } else if (type.indexOf(type_decimal) > -1) {
            return "java.math.BigDecimal";
        } else if (type.indexOf(type_blob) > -1) {
            return "byte[]";
        }
        return null;
    }

    private String olType(String type) {
        if (type.contains("(")) {
            type = type.substring(0, type.indexOf("(")).toUpperCase();
            if (type.equals("INT") || type.equals("TINYINT")) {
                type = "INTEGER";
            }

        }
        return type;
    }


    private String processField(String field) {
        StringBuffer sb = new StringBuffer(field.length());
        //field = field.toLowerCase();
        String[] fields = field.split("_");
        String temp = null;
        sb.append(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            temp = fields[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        return sb.toString();
    }


    /**
     * 将实体类名首字母改为小写
     *
     * @param beanName
     * @return
     */
    private String processResultMapId(String beanName) {
        if (beanName.length() > 0) {
            return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
        }
        return beanName;
    }

    /**
     * 构建类上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException
     */
    private BufferedWriter buildClassComment(BufferedWriter bw, String text) throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("/**");
        bw.newLine();
        bw.write(" * " + text);
        bw.newLine();
        bw.write(" **/");
        return bw;
    }


    /**
     * 构建方法上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException
     */
    private BufferedWriter buildMethodComment(BufferedWriter bw, String text) throws IOException {
        bw.newLine();
        bw.write("\t/**");
        bw.newLine();
        bw.write("\t * ");
        bw.newLine();
        bw.write("\t * " + text);
        bw.newLine();
        bw.write("\t * ");
        bw.newLine();
        bw.write("\t **/");
        return bw;
    }


    /**
     * 生成实体类
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildEntityBean(List<String> columns, List<String> types, List<String> comments, String tableComment)
            throws IOException {
        File folder = new File(bean_path);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File beanFile = new File(bean_path, beanName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + bean_package + ";");
        bw.newLine();
        bw.write("import java.io.Serializable;");
        bw.newLine();
        //bw.write("import lombok.Data;");
        //      bw.write("import javax.persistence.Entity;");
        bw = buildClassComment(bw, "Created by he_shuang on " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        bw.newLine();
        //      bw.write("@Entity");
        //bw.write("@Data");
        //bw.newLine();
        bw.write("public class " + beanName + " implements Serializable {");
        bw.newLine();
        bw.newLine();
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            bw.write("\t/**" + comments.get(i) + "**/");
            bw.newLine();
            bw.write("\tprivate " + processType(types.get(i)) + " " + processField(columns.get(i)) + ";");
            bw.newLine();
            bw.newLine();
        }
        bw.newLine();
        // 生成get 和 set方法
        String tempField = null;
        String _tempField = null;
        String tempType = null;
        for (int i = 0; i < size; i++) {
            tempType = processType(types.get(i));
            _tempField = processField(columns.get(i));
            tempField = _tempField.substring(0, 1).toUpperCase() + _tempField.substring(1);
            bw.newLine();
            //          bw.write("\tpublic void set" + tempField + "(" + tempType + " _" + _tempField + "){");
            bw.write("\tpublic void set" + tempField + "(" + tempType + " " + _tempField + "){");
            bw.newLine();
            //          bw.write("\t\tthis." + _tempField + "=_" + _tempField + ";");
            bw.write("\t\tthis." + _tempField + " = " + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
            bw.write("\tpublic " + tempType + " get" + tempField + "(){");
            bw.newLine();
            bw.write("\t\treturn " + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
        }
        bw.newLine();
        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }


    /**
     * 构建Mapper文件
     *
     * @throws IOException
     */
    private void buildMapper() throws IOException {
        File folder = new File(mapper_path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File mapperFile = new File(mapper_path, mapperName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperFile), "utf-8"));
        bw.write("package " + mapper_package + ";");
        bw.newLine();
        bw.newLine();
        bw.write("import " + bean_package + "." + beanName + ";");
        bw.newLine();
        bw.write("import org.apache.ibatis.annotations.Param;");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.ArrayList;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.write("import java.util.HashMap;");
        bw.newLine();
        bw = buildClassComment(bw, mapperName + "数据库操作接口类");
        bw.newLine();
        bw.newLine();
        //      bw.write("public interface " + mapperName + " extends " + mapper_extends + "<" + beanName + "> {");
        bw.write("public interface " + mapperName + "{");
        bw.newLine();
        bw.newLine();
        // ----------定义Mapper中的方法Begin----------
        bw = buildMethodComment(bw, "查询（根据主键ID查询）");
        bw.newLine();
        bw.write("\t" + beanName + "  get(@Param(\"id\") Long id);");
        bw.newLine();
        bw = buildMethodComment(bw, "获取列表");
        bw.newLine();
        bw.write("\tList<" + beanName + ">  getList(Map<String,Object> map);");
        bw = buildMethodComment(bw, "分页获取列表");
        bw.newLine();
        bw.write("\tList<" + beanName + ">  getPageList(Map<String,Object> map);");
        bw.newLine();
        bw = buildMethodComment(bw, "删除（根据主键ID删除）");
        bw.newLine();
        bw.write("\t" + "int delete(@Param(\"id\") Long id);");
        bw.newLine();
        bw = buildMethodComment(bw, "添加");
        bw.newLine();
        bw.write("\t" + "int addAndId(" + beanName + " data);");
        bw.newLine();
        bw = buildMethodComment(bw, "修改 （匹配有值的字段）");
        bw.newLine();
        bw.write("\t" + "int update(Map<String,Object> map);");
        bw.newLine();
        bw = buildMethodComment(bw, "修改（根据主键ID修改）");
        bw.newLine();
        bw.write("\t" + "int updateById(" + beanName + " data);");
        bw.newLine();

        // ----------定义Mapper中的方法End----------
        bw.newLine();
        bw.write("}");
        bw.flush();
        bw.close();
    }


    /**
     * 构建实体类映射XML文件
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildMapperXml(List<String> columns, List<String> types, List<String> comments) throws IOException {
        File folder = new File(xml_path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File mapperXmlFile = new File(xml_path, mapperName + ".xml");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
        bw.newLine();
        bw.write("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
        bw.write("<mapper namespace=\"" + mapper_package + "." + mapperName + "\">");
        bw.newLine();
        bw.newLine();

        bw.write("\t<!--实体映射-->");
        bw.newLine();
        bw.write("\t<resultMap id=\"" + this.processResultMapId(beanName) + "ResultMap\" type=\"" + bean_package + "." + beanName + "\">");
        bw.newLine();
        bw.write("\t\t<!--" + comments.get(0) + "-->");
        bw.newLine();
        bw.write("\t\t<id column=\"" + columns.get(0) + "\" jdbcType=\"" + olType(types.get(0)) + "\" property=\"" + this.processField(columns.get(0)) + "\"/>");
        bw.newLine();
        int size = columns.size();
        for (int i = 1; i < size; i++) {
            bw.write("\t\t<!--" + comments.get(i) + "-->");
            bw.newLine();
            bw.write("\t\t<result column=\""
                    + columns.get(i) + "\" jdbcType=\"" + olType(types.get(i)) + "\" property=\"" + this.processField(columns.get(i)) + "\" />");
            bw.newLine();
        }
        bw.write("\t</resultMap>");

        bw.newLine();
        bw.newLine();
        bw.newLine();

        // 下面开始写SqlMapper中的方法
        // this.outputSqlMapperMethod(bw, columns, types);
        buildSQL(bw, columns, types);

        bw.write("</mapper>");
        bw.flush();
        bw.close();
    }


    private void buildSQL(BufferedWriter bw, List<String> columns, List<String> types) throws IOException {
        int size = columns.size();
        // 通用结果列
        bw.write("\t<!-- 通用查询结果列-->");
        bw.newLine();
        bw.write("\t<sql id=\"columns\">");
        bw.newLine();

        bw.write("\t\t id,");
        for (int i = 1; i < size; i++) {
            bw.write("\n\t\t" + columns.get(i));
            if (i != size - 1) {
                bw.write(",");
            }
        }

        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();


        // 查询（根据主键ID查询）
        bw.write("\t<!-- 查询（根据主键ID查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"get\" resultType=\""
                + bean_package + "." + beanName + "\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"columns\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + " ,jdbcType = " + olType(types.get(0)) + "}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 查询完

        // 查询（根据条件查询）
        bw.write("\t<!-- 查询（根据条件查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"getList\" resultMap=\""
                + processResultMapId(beanName) + "ResultMap\" parameterType=\"map\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"columns\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t <include refid=\"selectSql\" /> ");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 查询完
        // 分页查询（根据条件查询）
        bw.write("\t<!-- 查询（根据条件查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"getPageList\" resultMap=\""
                + processResultMapId(beanName) + "ResultMap\" parameterType=\"" + bean_package + "." + beanName + "\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"columns\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t <include refid=\"selectSql\" /> ");
        bw.newLine();
        bw.write("\t\t ORDER BY id DESC limit #{pageIndex,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 分页查询完

        // 删除（根据主键ID删除）
        bw.write("\t<!--删除：根据主键ID删除-->");
        bw.newLine();
        bw.write("\t<delete id=\"delete\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
        bw.newLine();
        bw.write("\t\t DELETE FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + " ,jdbcType = " + olType(types.get(0)) + "}");
        bw.newLine();
        bw.write("\t</delete>");
        bw.newLine();
        bw.newLine();
        // 删除完


        // 添加insert方法
        bw.write("\t<!-- 添加 -->");
        bw.newLine();
        bw.write("\t<insert id=\"addAndId\" parameterType=\"" + bean_package + "." + beanName + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write(" \t\t(");
        for (int i = 0; i < size; i++) {
            bw.write(columns.get(i));
            if (i != size - 1) {
                bw.write(",\n\t\t");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t\t VALUES ");
        bw.newLine();
        bw.write(" \t\t(");
        for (int i = 0; i < size; i++) {
            bw.write("#{" + processField(columns.get(i)) + " ,jdbcType = " + olType(types.get(i)) + "}");
            if (i != size - 1) {
                bw.write(",\n\t\t");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t\t<selectKey order=\"AFTER\" keyProperty=\"" + processField(columns.get(0)) + "\" resultType=\"java.lang.Integer\">SELECT @@IDENTITY as " + processField(columns.get(0)) + "\n" +
                "        </selectKey>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
        // 添加insert完

        String tempField = null;
        //---------------  insert方法（匹配有值的字段）
       /* bw.write("\t<!-- 添加 （匹配有值的字段）-->");
        bw.newLine();
        bw.write("\t<insert id=\"insertSelective\" parameterType=\"" + bean_package + "." + beanName + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();


        for (int i = 0; i < size; i++) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t " + columns.get(i) + ",");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }

        bw.newLine();
        bw.write("\t\t </trim>");
        bw.newLine();

        bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();

        tempField = null;
        for (int i = 0; i < size; i++) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + "!=null\">");
            bw.newLine();
            bw.write("\t\t\t\t #{" + tempField + "},");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }

        bw.write("\t\t </trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();*/
        //---------------  完毕


        // 修改update方法
        bw.write("\t<!-- 修 改-->");
        bw.newLine();
        bw.write("\t<update id=\"updateById\" parameterType=\"" + bean_package + "." + beanName + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write(" \t\t <set> ");
        bw.newLine();

        tempField = null;
        for (int i = 1; i < size; i++) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">");
            bw.newLine();
            if (i == size - 1) {
                bw.write("\t\t\t\t " + columns.get(i) + " = #{" + tempField + " ,jdbcType = " + olType(types.get(i)) + "}");
            } else {
                bw.write("\t\t\t\t " + columns.get(i) + " = #{" + tempField + " ,jdbcType = " + olType(types.get(i)) + "},");
            }
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }

        bw.newLine();
        bw.write(" \t\t </set>");
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + " ,jdbcType = " + olType(types.get(0)) + "}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();
        // update方法完毕

        // ----- 修改（匹配有值的字段）
        bw.write("\t<!-- 修 改-->");
        bw.newLine();
        bw.write("\t<update id=\"update\" parameterType=\"map\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write("\t\t SET ");

        bw.newLine();
        tempField = null;
        for (int i = 1; i < size; i++) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t " + columns.get(i) + " = #{" + tempField + " ,jdbcType = " + olType(types.get(0)) + "}");
            if (i != size - 1) {
                bw.write(",");
            }
            bw.newLine();
        }

        bw.write("\t\t <include refid=\"selectSql\" /> ");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();

        //查询条件
        bw.write("\t<!--查询条件-->");
        bw.newLine();
        bw.write("\t<sql id=\"selectSql\">");
        bw.newLine();
        bw.write("\t <where>");
        bw.newLine();
        bw.newLine();
        bw.write("\t </where>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();

    }


    /**
     * 获取所有的数据库表注释
     *
     * @return
     * @throws SQLException
     */
    private Map<String, String> getTableComment() throws SQLException {
        Map<String, String> maps = new HashMap<String, String>();
        PreparedStatement pstate = conn.prepareStatement("show table status");
        ResultSet results = pstate.executeQuery();
        while (results.next()) {
            String tableName = results.getString("NAME");
            String comment = results.getString("COMMENT");
            maps.put(tableName, comment);
        }
        return maps;
    }


    public void generate() throws ClassNotFoundException, SQLException, IOException {
        //init();
        String prefix = "show full fields from ";
        List<String> columns = null;
        List<String> types = null;
        List<String> comments = null;
        PreparedStatement pstate = null;
        List<String> tables = getTables();
        Map<String, String> tableComments = getTableComment();
        for (String table : tables) {
            columns = new ArrayList<String>();
            types = new ArrayList<String>();
            comments = new ArrayList<String>();
            pstate = conn.prepareStatement(prefix + table);
            ResultSet results = pstate.executeQuery();
            while (results.next()) {
                columns.add(results.getString("FIELD"));
                types.add(results.getString("TYPE"));
                comments.add(results.getString("COMMENT"));
            }
            tableName = table;
            processTable(table);
            //          this.outputBaseBean();
            String tableComment = tableComments.get(tableName);
            buildEntityBean(columns, types, comments, tableComment);
            buildMapper();
            buildMapperXml(columns, types, comments);
        }
        conn.close();
    }

}
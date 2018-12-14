package com.eason.grade.excel;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.eason.grade.students.Classes;
import com.eason.grade.students.Student;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Excel 工具类,负责导入导出所有的 excel 数据
 *
 * @author Eason
 */
public class ExcelUtils {
    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14,
                    WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {

            e.printStackTrace();
        }
    }

    /**
     * 初始化 Excel 的文件信息.文件名/列名/ sheet 名字
     *
     * @param fileName  文件名
     * @param colName   列名
     * @param sheetName sheet 名字
     */
    public static boolean initExcel(String fileName, String[] colName, String sheetName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName,
                    arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            workbook.write();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 通过反射,取出一个对象的所有属性值
     *
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    private static Object[] parseObjFieldValue(Object object) throws IllegalAccessException {
        Class obClass = object.getClass();
        Field[] fields = obClass.getDeclaredFields();
        Object[] value = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            value[i] = fields[i].get(object);
        }

        Collections.reverse(Arrays.asList(value));

        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean writeObjListToExcel(List<T> objList,
                                                  String fileName, Context c) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),
                        workbook);
                WritableSheet sheet = writebook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    Object[] objs = parseObjFieldValue(objList.get(j));
                    for (int i = 0; i < objs.length; i++) {
                        sheet.addCell(new Label(i, j + 1, objs[i].toString(),
                                arial12format));
                    }
                }
                writebook.write();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            return false;
        }
    }

    public static Classes parseExcel(File f, Context ctx) throws IOException, BiffException {
        Classes classes = new Classes();
        ArrayList<Student> students = new ArrayList<Student>();
        Workbook course = null;
        course = Workbook.getWorkbook(f);
        Sheet sheet = course.getSheet(0);
        String className = f.getName();

        classes.setName(className);

        Cell cell = null;
        for (int i = 1; i < sheet.getRows(); i++) {
            Student student = new Student();
            cell = sheet.getCell(1, i);
            student.setSid(Long.parseLong(cell.getContents()));
            cell = sheet.getCell(2, i);
            student.setName(cell.getContents());

            student.setClasses(classes);
            Log.d("Excel Parse", "Row" + i + "---------" + student.toString());
            students.add(student);
        }

        classes.setStudents(students);

        return classes;
    }

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName
                .substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static List<String[]> readExcel(String filePath) throws Exception {
        return readExcel(filePath, 0);
    }

    public static List<String[]> readExcel(String filePath, int sheet) throws Exception {
        if (filePath.trim().toLowerCase().endsWith(".xls")) {
            return readXLS(new File(filePath), sheet);
        } else {
            return readXLSX(new File(filePath), sheet);
        }
    }

    public static List<String[]> readXLS(File file, int sheetIndex) throws Exception {
        List<String[]> list = new ArrayList<String[]>();
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetIndex);
            int columnCount = sheet.getColumns();
            int rowCount = sheet.getRows();
            for (int i = 0; i < rowCount; i++) {
                String[] item = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    Cell cell = sheet.getCell(j, i);
                    String str = "";
                    if (cell.getType() == CellType.NUMBER) {
                        str = ((NumberCell) cell).getValue() + "";
                    } else if (cell.getType() == CellType.DATE) {
                        str = "" + ((DateCell) cell).getDate();
                    } else {
                        str = "" + cell.getContents();
                    }
                    item[j] = str;
                }
                list.add(item);
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
        return list;
    }

    public static List<String[]> readXLSX(File file, int sheet) throws Exception {
        List<String[]> list = new ArrayList<String[]>();
        ArrayList<String> item = new ArrayList<String>();
        String v = null;
        boolean flat = false;
        List<String> ls = new ArrayList<String>();
        ZipFile xlsxFile = new ZipFile(file);
        ZipEntry sharedStringXML = xlsxFile
                .getEntry("xl/sharedStrings.xml");
        InputStream inputStream = xlsxFile.getInputStream(sharedStringXML);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");
        int evtType = xmlParser.getEventType();
        while (evtType != XmlPullParser.END_DOCUMENT) {
            switch (evtType) {
                case XmlPullParser.START_TAG:
                    String tag = xmlParser.getName();
                    if (tag.equalsIgnoreCase("t")) {
                        ls.add(xmlParser.nextText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
            }
            evtType = xmlParser.next();
        }
        ZipEntry sheetXML = xlsxFile.getEntry("xl/worksheets/sheet" + (sheet + 1) + ".xml");
        InputStream inputStreamsheet = xlsxFile.getInputStream(sheetXML);
        XmlPullParser xmlParsersheet = Xml.newPullParser();
        xmlParsersheet.setInput(inputStreamsheet, "utf-8");
        int evtTypesheet = xmlParsersheet.getEventType();
        while (evtTypesheet != XmlPullParser.END_DOCUMENT) {
            switch (evtTypesheet) {
                case XmlPullParser.START_TAG:
                    String tag = xmlParsersheet.getName();
                    if (tag.equalsIgnoreCase("row")) {
                    } else if (tag.equalsIgnoreCase("c")) {
                        String t = xmlParsersheet.getAttributeValue(null, "t");
                        if (t != null) {
                            flat = true;
                        } else {
                            flat = false;
                        }
                    } else if (tag.equalsIgnoreCase("v")) {
                        v = xmlParsersheet.nextText();
                        if (v != null) {
                            if (flat) {
                                item.add(ls.get(Integer.parseInt(v)));
                            } else {
                                item.add(v);
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (xmlParsersheet.getName().equalsIgnoreCase("row")
                            && v != null) {
                        list.add(item.toArray(new String[item.size()]));
                        item = new ArrayList<String>();
                    }
                    break;
            }
            evtTypesheet = xmlParsersheet.next();
        }
        return list;
    }

}

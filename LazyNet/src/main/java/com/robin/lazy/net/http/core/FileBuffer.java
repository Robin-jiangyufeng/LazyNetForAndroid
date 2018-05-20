package com.robin.lazy.net.http.core;

import com.robin.lazy.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;

/**
 * 目标文件缓冲器(主要用于下载文件的保存 )
 *
 * @author 江钰锋
 * @version [版本号, 2015年1月16日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class FileBuffer {
    /**
     * 以只读方式打开。调用结果对象的任何 write 方法都将导致抛出 IOException。
     */
    public static final String MODE_R = "r";

    /**
     * 打开以便读取和写入。如果该文件尚不存在，则尝试创建该文件。
     */
    public static final String MODE_RW = "rw";

    /**
     * 打开以便读取和写入，对于 "rw"，还要求对文件的内容或元数据的每个更新都同步写入到底层存储设备。
     */
    public static final String MODE_RWS = "rws";

    /**
     * 打开以便读取和写入，对于 "rw"，还要求对文件内容的每个更新都同步写入到底层存储设备。
     */
    public static final String MODE_RWD = "rwd";

    /**
     * 临时文件后缀名
     */
    private static final String TEMP_SUFFIX = ".temp";

    /**
     * 目标文件
     */
    private File file;

    /**
     * 临时文件
     */
    private File tempFile;

    /**
     * 存储临时文件信息的文件
     */
    private File tempFileInfor;

    /**
     * Java配置文件
     */
    private Properties props;

    public FileBuffer(String mSavePath, String fileName) {
        this(new File(mSavePath), fileName, MODE_RW);
    }

    public FileBuffer(File baseDirFile, String fileName) {
        this(baseDirFile, fileName, MODE_RW);
    }

    public FileBuffer(String mSavePath, String fileName, String mode) {
        this(new File(mSavePath), fileName, mode);
    }

    public FileBuffer(File baseDirFile, String fileName, String mode) {
        if (!baseDirFile.exists()) {
            baseDirFile.mkdirs();
        }
        file = new File(baseDirFile, fileName);
        tempFile = new File(baseDirFile, fileName + TEMP_SUFFIX);
        tempFileInfor = new File(baseDirFile, fileName + TEMP_SUFFIX + "!");
        props = new Properties();
    }

    /**
     * 设置文件权限
     *
     * @param file
     * @param mode
     */
    private void setFilePermission(File file, String mode) {
        if (file == null || !file.exists()) return;
        if(MODE_R.equals(mode)){//以只读方式打开。调用结果对象的任何 write 方法都将导致抛出 IOException
            file.setReadOnly();
        }else if(MODE_RW.equals(mode)){
            file.setReadable(true);
            file.setWritable(true);
        }else if(MODE_RWS.equals(mode)){
        }else if(MODE_RWD.equals(mode)){

        }
    }

    /**
     * 获取临时文件
     *
     * @return
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    public File getTempFile()
            throws IOException {
        if (tempFileInfor != null && !tempFileInfor.exists()) {
            tempFileInfor.createNewFile();
        }
        if (tempFile != null && !tempFile.exists()) {
            tempFile.createNewFile();
        }
        return tempFile;
    }

    /**
     * 判断文件是否存在
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isExists() {
        return file != null && file.exists();
    }

    /**
     * 判断是否存在临时文件
     *
     * @see [类、类#方法、类#成员]
     */
    public boolean isExistsTempFile() {
        return (tempFileInfor != null && tempFileInfor.exists()) && (tempFile != null && tempFile.exists());
    }

    /**
     * 删除临时文件
     *
     * @see [类、类#方法、类#成员]
     */
    public void deleteTempFile() {
        if (tempFileInfor != null && tempFileInfor.exists()) {
            tempFileInfor.delete();
        }
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    /**
     * 读取下载信息文件
     */
    public TempFileInfor getTempFileInfor() {
        if (!isExistsTempFile())
            return null;
        TempFileInfor tfInfor = null;
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(tempFileInfor));
            props.load(in);
            String startPos = props.getProperty("startPos");
            String endPos = props.getProperty("endPos");
            String lenght = props.getProperty("lenght");
            if (StringUtils.isNotNull(startPos) && StringUtils.isNotNull(endPos) && StringUtils.isNotNull(lenght)) {
                tfInfor = new TempFileInfor();
                tfInfor.setStartPos(Long.parseLong(startPos));
                tfInfor.setEndPos(Long.parseLong(endPos));
                tfInfor.setLenght(Long.parseLong(lenght));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tfInfor;
    }

    /**
     * 保存临时文件下载信息
     *
     * @param startPos 文件开始读写位置
     * @param endPos   文件结束读写位置
     * @param lenght   文件长度
     * @throws IOException
     * @see [类、类#方法、类#成员]
     */
    public void saveTempFileInfor(long startPos, long endPos, long lenght) {
        WeakReference<FileOutputStream> dfOut = null;
        try {
            dfOut = new WeakReference<FileOutputStream>(new FileOutputStream(tempFileInfor));
            if (props != null) {
                // 调用 Hashtable 的方法 put，使用 getProperty 方法提供并行性。
                // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
                props.setProperty("startPos", String.valueOf(startPos));
                props.setProperty("endPos", String.valueOf(endPos));
                props.setProperty("lenght", String.valueOf(lenght));
                // 以适合使用 load 方法加载到 Properties 表中的格式，
                // 将此 Properties 表中的属性列表（键和元素对）写入输出流
                props.store(dfOut.get(), "author: robin-jiang@qq.com");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("属性文件更新错误");
        } finally {
            try {
                if (dfOut != null) {
                    dfOut.get().flush();
                    dfOut.get().close();
                    dfOut = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存目标文件(这时目标文件才会正真生成,临时文件会被删除)
     *
     * @see [类、类#方法、类#成员]
     */
    public void save() {
        boolean isSuccess = tempFile.renameTo(file);// 下载成功,修改临时文件为下载后的文件
        if (isSuccess) {
            deleteTempFile();
        } else {
            save();
        }
    }

    /**
     * 关闭文件缓冲器
     *
     * @see [类、类#方法、类#成员]
     */
    public void close() {
        if (props != null) {
            props.clear();
            props = null;
        }
        if (file != null) {
            file = null;
        }
        if (tempFile != null) {
            tempFile = null;
        }
        if (tempFileInfor != null) {
            tempFileInfor = null;
        }
    }

    /**
     * 临时文件的信息
     *
     * @author 江钰锋
     * @version [版本号, 2015年1月16日]
     * @see [相关类/方法]
     * @since [产品/模块版本]
     */
    public class TempFileInfor {
        /**
         * 上一次开始读写的位置
         */
        private long startPos;

        /**
         * 上一次读写结束的位置
         */
        private long endPos;

        /**
         * 文件的长度
         */
        private long lenght;

        public long getStartPos() {
            return startPos;
        }

        public void setStartPos(long startPos) {
            this.startPos = startPos;
        }

        public long getEndPos() {
            return endPos;
        }

        public void setEndPos(long endPos) {
            this.endPos = endPos;
        }

        public long getLenght() {
            return lenght;
        }

        public void setLenght(long lenght) {
            this.lenght = lenght;
        }

    }

}

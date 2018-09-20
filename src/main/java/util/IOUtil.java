package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class IOUtil {

    // 关闭字节输入流
    public static void closeInputStream(InputStream...iss) throws IOException {
        for (InputStream is : iss) {
            if (is != null) {
                is.close();
            }
        }
    }

    // 关闭字节输出流
    public static void closeOutputStream(OutputStream...oss) throws IOException {
        for (OutputStream os : oss) {
            if (os != null) {
                os.close();
            }
        }
    }

    // 关闭字符输入流
    public static void closeReader(Reader...readers) throws IOException {
        for (Reader reader : readers) {
            if (reader != null) {
                reader.close();
            }
        }
    }

    // 关闭字符输出流
    public static void closeWriter(Writer...writers) throws IOException {
        for (Writer writer : writers) {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

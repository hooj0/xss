package com.masget.xss.wrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * ServletInputStream wrapper
 * 
 * @author hoojo
 * @createDate 2018年2月1日 下午2:20:34
 * @file FileServletInputStream.java
 * @package com.masget.xss.wrapper
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class FileServletInputStream extends ServletInputStream {
	
	private ReadListener readListener;
	private ByteArrayInputStream buffer;

    public FileServletInputStream(byte[] contents) {
        this.buffer = new ByteArrayInputStream(contents);
    }

    @Override
    public int read() throws IOException {
    	
    	int index;
        if (!isFinished()) {
            index = buffer.read();
            if (isFinished() && (readListener != null)) {
                try {
                    readListener.onAllDataRead();
                } catch (IOException ex) {
                    readListener.onError(ex);
                    throw ex;
                }
            }
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
		this.readListener = listener;
		
		if (!isFinished()) {
			try {
				readListener.onDataAvailable();
			} catch (IOException e) {
				readListener.onError(e);
			}
		} else {
			try {
				readListener.onAllDataRead();
			} catch (IOException e) {
				readListener.onError(e);
			}
		}
    }
}

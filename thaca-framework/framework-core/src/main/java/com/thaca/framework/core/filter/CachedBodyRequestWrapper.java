package com.thaca.framework.core.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Arrays;
import lombok.Getter;

@Getter
public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyRequestWrapper(HttpServletRequest request, int limit) throws IOException {
        super(request);
        byte[] raw = request.getInputStream().readAllBytes();
        this.cachedBody = raw.length <= limit ? raw : Arrays.copyOf(raw, limit);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                return stream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener l) {}
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}

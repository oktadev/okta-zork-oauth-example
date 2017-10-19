package com.okta.examples.zorkoauth.config;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import static com.okta.examples.zorkoauth.config.SpringSecurityWebAppConfig.VERSION;

// ensure that our /game endpoint always responds with 401 and not 302
public class AjaxGameFilter extends GenericFilterBean {

    private static final String GAME_PATH = VERSION + "/game";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (GAME_PATH.equals(request.getRequestURI())) {
            request = new AjaxHttpServletRequestWrapper(request);
        }
        filterChain.doFilter(request, servletResponse);
    }

    public class AjaxHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public AjaxHttpServletRequestWrapper(HttpServletRequest req) {
            super(req);
        }

        @Override
        public String getHeader(String name) {
            if ("X-Requested-With".equals(name)) { return "XMLHttpRequest"; }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> headers = java.util.Collections.list(super.getHeaderNames());
            headers.add("X-Requested-With");
            return java.util.Collections.enumeration(headers);
        }
    }
}

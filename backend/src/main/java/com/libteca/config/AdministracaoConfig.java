package com.libteca.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.core.Ordered;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
@Profile("!instalacao-banco")
public class AdministracaoConfig {
    public static class Sessoes implements HttpSessionListener {
        private final java.util.Set<HttpSession> sessoes=ConcurrentHashMap.newKeySet();
        public void sessionCreated(HttpSessionEvent e){sessoes.add(e.getSession());}
        public void sessionDestroyed(HttpSessionEvent e){sessoes.remove(e.getSession());}
        public void encerrarTodas(){for(var s:sessoes)try{s.invalidate();}catch(IllegalStateException ignored){}}
    }
    @Bean Sessoes sessoes(){return new Sessoes();}
    @Bean ServletListenerRegistrationBean<Sessoes> registrarSessoes(Sessoes sessoes){return new ServletListenerRegistrationBean<>(sessoes);}
    @Bean FilterRegistrationBean<Filter> administracaoExclusiva(){
        var trava=new ReentrantReadWriteLock(true);
        Filter filter=(request,response,chain)->{
            var req=(HttpServletRequest)request;
            String caminho=req.getRequestURI().substring(req.getContextPath().length());
            boolean exclusiva=("DELETE".equals(req.getMethod())&&"/api/biblioteca".equals(caminho))
                    ||("POST".equals(req.getMethod())&&"/api/biblioteca/transferir".equals(caminho));
            var lock=exclusiva?trava.writeLock():trava.readLock();lock.lock();
            try{chain.doFilter(request,response);}finally{lock.unlock();}
        };
        var registro=new FilterRegistrationBean<>(filter);
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE+20);registro.addUrlPatterns("/api/*");
        return registro;
    }
}

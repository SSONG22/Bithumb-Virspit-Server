package com.virspit.gateway.filter;

import com.virspit.gateway.error.ErrorCode;
import com.virspit.gateway.error.exception.InvalidValueException;
import com.virspit.gateway.error.exception.TokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

@Component
public class JwtUserRequestFilter extends
        AbstractGatewayFilterFactory<JwtUserRequestFilter.Config> implements Ordered {

    final Logger logger =
            LoggerFactory.getLogger(JwtUserRequestFilter.class);

    @Autowired
    private JwtValidator jwtValidator;

    @Override
    public int getOrder() {
        return -2; // -1 is response write filter, must be called before that
    }

    public static class Config {
        private String role;

        public Config(String role) {
            this.role = role;
        }

        public String getUser() {
            return Role.USER.getCode();
        }

        public String getAdmin() {
            return Role.ADMIN.getCode();
        }
    }

    @Bean
    public ErrorWebExceptionHandler myExceptionHandler() {
        return new MyWebExceptionHandler();
    }

    public class MyWebExceptionHandler implements ErrorWebExceptionHandler {
        private String errorCodeMaker(int errorCode) {
            return "{\"errorCode\":" + errorCode + "}";
        }

        @Override
        public Mono<Void> handle(
                ServerWebExchange exchange, Throwable ex) {
            logger.warn("in GATEWAY Exeptionhandler : " + ex);
            int errorCode = 999;
            if (ex.getClass() == NullPointerException.class) {
                errorCode = 61;
            } else if (ex.getClass() == ExpiredJwtException.class) {
                errorCode = 56;
            } else if (ex.getClass() == MalformedJwtException.class || ex.getClass() == SignatureException.class || ex.getClass() == UnsupportedJwtException.class) {
                errorCode = 55;
            } else if (ex.getClass() == IllegalArgumentException.class) {
                errorCode = 51;
            }

            byte[] bytes = errorCodeMaker(errorCode).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Flux.just(buffer));
        }
    }


    public JwtUserRequestFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().get("Authorization").get(0).substring(7);
            Map<String, Object> userInfo = jwtValidator.getUserParseInfo(token);
            ArrayList<String> arr = (ArrayList<String>) userInfo.get("role");
            logger.info("?" + arr);
            if (!arr.contains(config.getUser()) && !arr.contains(config.getAdmin())) {
                throw new InvalidValueException(token, ErrorCode.TOKEN_NOT_VALID);
            }
            return chain.filter(exchange);
        };
    }
}
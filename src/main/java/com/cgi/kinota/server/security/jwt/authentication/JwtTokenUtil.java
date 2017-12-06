/**
 * Kinota (TM) Copyright (C) 2017 CGI Group Inc.
 *
 * Licensed under GNU Lesser General Public License v3.0 (LGPLv3);
 * you may not use this file except in compliance with the License.
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * v3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License v3.0 for more details.
 *
 * You can receive a copy of the GNU Lesser General Public License
 * from:
 *
 * https://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 */

package com.cgi.kinota.server.security.jwt.authentication;

import com.cgi.kinota.server.security.jwt.JwtExpiredTokenException;
import com.cgi.kinota.server.servlet.DatabaseStatus;

import io.jsonwebtoken.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dfladung on 3/27/17.
 */
@Component
public class JwtTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStatus.class);

    public static final Integer JWT_COOKIE_EXPIRATION_S = 60 * 20; // 20 minutes
    public static final Long JWT_TOKEN_EXPIRATION_MS = 1000L * JWT_COOKIE_EXPIRATION_S; // 20 minutes
    public static final String JWT_TOKEN_HEADER = "Authorization";

    String secret;

    @Autowired
    public JwtTokenUtil(Environment env) {
        String s = null;
        if (env != null) {
            s = env.getProperty("sta.jwtSecret","ReplaceMeWithSomethingLongArbitraryAndHardToGuess");
        } else {
            s = "G#tg$%GH5Ju6k*k*(o886%&U^4y%#t$$#TG$@yu7k8htHY%J67j^k&*k97l9&7k5H4";
        }
        LOGGER.info("JWT secret is: " + s);
        secret = s;
    }

    public String createTokenForUser(String userName) {
        return createTokenForUser(userName, null);
    }

    public String createTokenForUser(String userName, List<String> roles) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("roles", StringUtils.join(roles, ","))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public User parseUserFromToken(String token) {
        try {
            String username = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            String roleString = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token).getBody().get("roles", String.class);
            List<SimpleGrantedAuthority> roles = new ArrayList<>();
            if (!StringUtils.isEmpty(roleString)) {
                String[] roleValues = StringUtils.split(roleString, ",");
                for (String roleValue : roleValues) {
                    roles.add(new SimpleGrantedAuthority(roleValue));
                }
            }
            return new User(username, token, roles);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
            throw new BadCredentialsException("Invalid JWT token: ", ex);
        } catch (ExpiredJwtException expiredEx) {
            throw new JwtExpiredTokenException("JWT Token expired", expiredEx);
        }
    }

    public String extractTokenFromHeader(HttpServletRequest req) {
        String token = null;
        String header = req.getHeader(JWT_TOKEN_HEADER);
        if (!StringUtils.isEmpty(header)) {
            if (StringUtils.startsWith(header.trim(), "Bearer")) {
                token = StringUtils.remove(header, "Bearer").trim();
            }
        }
        return token;
    }
}

package com.example.exodia.common.auth;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secretKeyRT}")
	private String secret;

	@Value("${jwt.expirationRT}")
	private long tokenValidity;

	public String createToken(String userNum, Long departmentId, Long positionId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("department_id", departmentId != null ? departmentId.toString() : "알 수 없음");
		claims.put("position_id", positionId != null ? positionId.toString() : "알 수 없음");

		System.out.println("Token claims: " + claims);

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(userNum)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenValidity))
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();
	}


	public Claims getClaimsFromToken(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(secret)
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid JWT Token");
		}
	}

	public Boolean isTokenExpired(String token) {
		return getClaimsFromToken(token).getExpiration().before(new Date());
	}

	public String getUserNumFromToken(String token) {
		return getClaimsFromToken(token).getSubject();
	}

	public String getDepartmentIdFromToken(String token) {
		return getClaimsFromToken(token).get("department_id", String.class);
	}

	public String getPositionIdFromToken(String token) {
		return getClaimsFromToken(token).get("position_id", String.class);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
			return !isTokenExpired(token);
		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public long getTokenExpiration() {
		return tokenValidity;
	}

}

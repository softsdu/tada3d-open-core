package com.zlp.platform.util.token;

import com.alibaba.fastjson.JSON;  

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zlp.platform.common.exception.login.UserExpiredException;

/**
 *	TokenUtil
 *
 *  @Package com.zlp.platform.util
 *	@author liyh
 *	@version V1.0 2019-06-03 16：05  新增
 */
public class TokenUtil implements Serializable {

    private static final long serialVersionUID = -3301605591108950415L;
    
    private static Logger logger=Logger.getLogger(TokenUtil.class); 

    static final String CLAIM_KEY_USERNAME = "sub";
    static final String CLAIM_KEY_AUDIENCE = "audience";
    static final String CLAIM_KEY_CREATED = "created";

    private static String secret = "centerSecret";

    private static Long expiration = 3*24*60*60L;
//    private static Long expiration = 1L;

    /*********生成token*************/
    public static String generateToken(SysUser sysUser) {

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(CLAIM_KEY_USERNAME, JSON.toJSON(new UserToken(sysUser)).toString());
        // claims.put(CLAIM_KEY_AUDIENCE, generateAudience(device));
        claims.put(CLAIM_KEY_CREATED, new Date());
        
        return generateToken(claims);

    }


    static String generateToken(Map<String, Object> claims) {
        //return Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate()).signWith(SignatureAlgorithm.HS512, secret).compact();
    	String token= Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate()).signWith(SignatureAlgorithm.HS512, secret).compact();
    	logger.info("token:"+token);	
    	return token;
    }

    private static Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }


    /**
     * 从token  取出固定的数据
     * @param token
     * @return
     */
    public static UserToken getMemberFromToken(String token) {
        UserToken userToken = null;
        try {
            final Claims claims = getClaimsFromToken(token);
            userToken = JSON.parseObject(claims.getSubject(), UserToken.class);
        } 
        catch (ExpiredJwtException e) {
            //登录过期请重新登录
            throw new UserExpiredException(UserExpiredException.EXPIRED);
        	
            //userToken = null;
        	//throw new  NcpException("005", "登录错误", e);
			//this.addResponse(ncpEx.toJsonString());
        } 
        catch (Exception e) {
            userToken = null;
        }
        return userToken;
    }


    private static Claims getClaimsFromToken(String token) {
        Claims claims = null;
        claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 得到用户id
     * @param token
     * @return
     */
    public static String getMemberId(String token ) {
        UserToken userToken = null;
        try {
            final Claims claims = getClaimsFromToken(token);
            userToken = JSON.parseObject(claims.getSubject(), UserToken.class);
        } 
        catch (ExpiredJwtException e) {
            throw new UserExpiredException(UserExpiredException.EXPIRED);
        } 
        catch (Exception e) {
            userToken = null;
        }
        return userToken.getUserid();
    }
}

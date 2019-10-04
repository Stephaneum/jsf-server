package tools;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

public class Jwt {

    private static final long validityInMilliseconds = 30000;
    private static final Key key;

    static {
        byte[] jwtKeyBytes = Base64.getDecoder().decode("68L+oFpn9zdzwLEogNQ3Qdr9gg5RfXMji4mDu1py1Gw=");
        key = new SecretKeySpec(jwtKeyBytes, 0, jwtKeyBytes.length, "HmacSHA256");
    }

    public static String generateToken(int userID) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .claim("userID", userID)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }


    public static Claims getData(String token) {
        if(isValid(token))
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        else
            return null;
    }

    public static boolean isValid(String token) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
        return !claims.getBody().getExpiration().before(new Date());
    }

}

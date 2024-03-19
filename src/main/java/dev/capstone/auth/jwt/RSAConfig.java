<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/jwt/RSAConfig.java
package dev.webserver.auth.jwt;
========
package dev.capstone.auth.jwt;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/jwt/RSAConfig.java

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

record RSAConfig() {

    /**
     * generates a java.security.KeyPair pub & priv key at runtime
     */
    private static KeyPair RSAKEYPAIR() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate com.nimbusds.jose.jwk.RSAKey at runtime
     * */
    public static RSAKey GENERATERSAKEY() {
        KeyPair keyPair = RSAKEYPAIR();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

}

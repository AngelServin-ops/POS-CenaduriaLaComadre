package negocio.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera y verifica hashes de contraseñas con PBKDF2 (incluido en Java, sin dependencias).
 * Formato guardado: iteraciones:salBase64:hashBase64
 */
public final class Contrasenas {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 210_000;
    private static final int LONGITUD_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Contrasenas() {
    }

    public static String hashear(String contrasena) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] hash = derivar(contrasena.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACIONES + ":" + b64.encodeToString(sal) + ":" + b64.encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        int iteraciones = Integer.parseInt(partes[0]);
        byte[] sal = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        byte[] calculado = derivar(contrasena.toCharArray(), sal, iteraciones);
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] derivar(char[] contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo generar el hash de la contraseña", e);
        }
    }
}

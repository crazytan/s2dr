package Client;

/**
 * A wrapper class for certificates for de/serialization
 */
public class Certificate {

    // name of the owner of the certificate
    public String subject;

    // signature of the certificate, hex represented
    public String signature;

    // time of validation, sample format "Mon Nov 30 2015"
    public String validto;

    // public key, in pkcs8 format
    public String publicKey;

    // fingerprint of the certificate, not used
    public String fingerprint;
}

package Client;

/*
 * A class representing the client side of s2dr service.
 */
public class Client {
    public void init_session(String hostname) {}

    public void check_out(UID document_id) {}

    public void check_in(UID document_id, SecurityFlag flag) {}

    public void delegate(UID document_id, Client c, int time,
                         Permission p, boolean propagationFlag) {}

    public void safe_delete(UID document_id) {}

    public void terminate() {}

    public static Client All;
}

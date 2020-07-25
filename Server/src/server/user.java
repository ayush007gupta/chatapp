package Server;


import java.io.Serializable;


class user implements Serializable {

    String password;
    String username;
    public user(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

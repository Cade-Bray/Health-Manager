package com.cadebray.healthmanager;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String password;
    private String email;
    private boolean authorized;

    // Constructors, getters, and setters.

    /**
     * Default constructor.
     */
    @SuppressWarnings("unused")
    public User() {
        // Default constructor
        this.password = "";
        this.email = "";
        this.authorized = false;
    }

    /**
     * Constructor with parameters.
     * @param password The password of the user.
     * @param email The email of the user.
     * @param is_authorized Whether the user is authorized.
     */
    @SuppressWarnings("unused")
    public User(String password, String email, boolean is_authorized) {
        // When it comes time I'd implement the method for salting here.
        this.password = password;
        this.email = email;
        this.authorized = is_authorized;
    }

    /**
     * Getter for the id. This is the primary key.
     * @return The id of the user.
     */
    public long getId() {
        return id;
    }

    /**
     * It shouldn't be used outside of the class because
     * @param id The id to set as an int. This will be the primary key. do
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Getter for the password.
     * @return The password of the user. Soon to be salted password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for the password.
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for the email.
     * @return The email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for the email.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for the is_authorized.
     * @return Whether the user is authorized.
     */
    public boolean getAuthorized() {
        return this.authorized;
    }

    /**
     * Setter for the is_authorized.
     * @param is_authorized Whether the user is authorized.
     */
    public void setAuthorized(boolean is_authorized) {
        this.authorized = is_authorized;
    }
}

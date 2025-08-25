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
    private String goal;
    private String units;
    private String phone;

    // Constructors, getters, and setters.

    /**
     * Default constructor.
     */
    @SuppressWarnings("unused")
    public User() {
        // Default constructor
        this.password = "";
        this.email = "";
        this.goal = "";
        this.units = "";
        this.authorized = false;
        this.phone = "";
    }

    /**
     * Constructor with parameters.
     * @param password The password of the user.
     * @param email The email of the user.
     * @param is_authorized Whether the user is authorized.
     */
    @SuppressWarnings("unused")
    public User(String password, String email, String goal_weight, String units, String phone,
                boolean is_authorized) {
        // When it comes time I'd implement the method for salting here.
        this.password = password;
        this.email = email;
        this.goal = goal_weight;
        this.units = units;
        this.authorized = is_authorized;
        this.phone = phone;
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
     * Getter for the goal weight.
     * @return The goal weight of the user.
     */
    public String getGoal() {
        return goal;
    }

    /**
     * Setter for the goal weight.
     * @param goal_weight The goal weight to set.
     */
    public void setGoal(String goal_weight) {
        this.goal = goal_weight;
    }

    /**
     * Getter for the units.
     * @return The units of the user.
     */
    public String getUnits() {
        return units;
    }

    /**
     * Setter for the units.
     * @param units The units to set.
     */
    public void setUnits(String units) {
        this.units = units;
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

    /**
     * Getter for the phone number.
     * @return The phone number of the user.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Setter for the phone number.
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}

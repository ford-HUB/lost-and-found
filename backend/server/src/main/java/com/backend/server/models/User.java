package com.backend.server.models;

import jakarta.persistence.*;



@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    @Column(
        nullable = false,
        length = 255
    )
    private String fullname;

    
    @Column(
        nullable = false,
        length = 255
    )
    private String contact_number;

    
    @Column(
        nullable = false,
        length = 255
    )
    private String address;

    @OneToOne
    @JoinColumn(name = "accounts")
    private Account account;

    public User() {} // initialize contructor

    public User(Long user_id, String fullname, String contact_number, String address, Account account) {
        this.user_id = user_id;
        this.fullname = fullname;
        this.contact_number = contact_number;
        this.address = address;
        this.account = account;
    }

    // @getters
    public Long getUserId() { return user_id; }
    public String getFullname() { return fullname; }
    public String getContactNumber() { return contact_number; }
    public String getAddress() { return address; }
    public Account getAccount() { return account; }

    // @setters
    public void setUserId(Long userId) { this.user_id = userId; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setContactNumber(String contactNumber) { this.contact_number = contactNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setAccount(Account account) { this.account = account; }

}

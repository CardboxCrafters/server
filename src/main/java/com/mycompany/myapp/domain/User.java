package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.base.BaseEntity;
import com.mycompany.myapp.domain.enums.UserStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder @Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public void withdrawUser(){
        this.status = UserStatus.INACTIVE;
    }

    public void resetUser(String name, String phoneNumber){
        this.username = name;
        this.phoneNumber = phoneNumber;
        this.status = UserStatus.ACTIVE;
    }
}

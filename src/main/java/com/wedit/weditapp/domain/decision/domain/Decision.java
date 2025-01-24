package com.wedit.weditapp.domain.decision.domain;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.BaseTimeEntity;
import com.wedit.weditapp.domain.shared.DecisionSide;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "decisions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Decision extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer addPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private DecisionSide side;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;


    @Builder
    private Decision(String name, String phoneNumber, Integer addPerson, DecisionSide side, Invitation invitation){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.addPerson = addPerson;
        this.side = side;
        this.invitation = invitation;
    }

    public static Decision createDecision(String name, String phoneNumber, Integer addPerson, DecisionSide side, Invitation invitation){
        return Decision.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .addPerson(addPerson)
                .side(side)
                .invitation(invitation)
                .build();
    }
}

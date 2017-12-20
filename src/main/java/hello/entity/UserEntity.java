package hello.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.persistence.*;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clubuser")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String grade;
    private String college;
    private String major;
    private String department;
    private String userIdentity = "officer";
    @Column(unique = true)
    private String phone;
    @Column(unique = true)
    private String qq;
    @Column(unique = true)
    private String wechat;
    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String wxID;

    @Transient
    private static byte[] sharedSecret;

    @OneToMany
    @JoinColumn(name = "author_id")
    private Set<NoticeEntity> notices = new HashSet<NoticeEntity>();

    @OneToMany
    @JoinColumn(name = "author_id")
    private Set<ActivityEntity> activities = new HashSet<ActivityEntity>();

    public UserEntity() {
    }

    public UserEntity(
            String name, String grade, String college, String major,
            String department,String phone,
            String qq, String wechat, String email
    ) {
        this.name = name;
        this.grade = grade;
        this.college = college;
        this.major = major;
        this.department = department;
        this.phone = phone;
        this.qq = qq;
        this.wechat = wechat;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWxID() {
        return wxID;
    }

    public void setWxID(String wxID) {
        this.wxID = wxID;
    }

    public Set<NoticeEntity> getNotices() {
        return notices;
    }

    public void setNotices(Set<NoticeEntity> notices) {
        this.notices = notices;
    }

    public Set<ActivityEntity> getActivities() {
        return activities;
    }

    public void setActivities(Set<ActivityEntity> activities) {
        this.activities = activities;
    }

    @Transient
    private static byte[] getSharedSecret() {
        if (sharedSecret == null) {
            SecureRandom random = new SecureRandom();
            sharedSecret = new byte[32];
            random.nextBytes(sharedSecret);
        }
        return sharedSecret;
    }

    @Transient
    public static void setSharedSecret() {
        SecureRandom random = new SecureRandom();
        sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);
    }

    public String generateAuthToken(String openid) {
        // Create HMAC signer
        try {
            JWSSigner signer = new MACSigner(getSharedSecret());
            // Prepare JWT with claims set
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(openid)
                    .issuer("patrickcty")
                    .expirationTime(new Date(new Date().getTime() + 3600 * 1000))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            // Apply the HMAC protection
            try {
                signedJWT.sign(signer);
            } catch (JOSEException e) {
                // todo:把这些都替换为 logger
                System.out.println(e);
            }
            return signedJWT.serialize();
        } catch (KeyLengthException e) {
            System.out.println(e);
            return "";
        }
    }

    public static String checkAuthToken(String authToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(authToken);
            try {
                JWSVerifier verifier = new MACVerifier(sharedSecret);
                if (signedJWT.verify(verifier) &&
                        new Date().before(
                                signedJWT.getJWTClaimsSet().
                                        getExpirationTime()) &&
                        signedJWT.getJWTClaimsSet().getIssuer().equals("patrickcty")
                        ) {
                    return signedJWT.getJWTClaimsSet().getSubject();
                }
                else {
                    return "";
                }
            } catch (JOSEException e) {
                System.out.println(e);
                return "";
            }
        } catch (ParseException e) {
            System.out.println(e);
            return "";
        }
    }
}
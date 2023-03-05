package io.github.gprindevelopment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class FioBandwidthOutput {

    @Id
    @GeneratedValue
    private Long id;

    private Double bandwidthValue;

    private String byteUnit;

    private String lambdaName;

    @Column(length = 2000)
    private String rawOutput;

    private Long timestamp;
}

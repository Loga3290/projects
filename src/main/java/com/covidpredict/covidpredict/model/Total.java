package com.covidpredict.covidpredict.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Total {
    public int other;
    public int confirmed;
    public int deceased;
    public int recovered;
    public int tested;
    public int vaccinated1;
    public int vaccinated2;
}

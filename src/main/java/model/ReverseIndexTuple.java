package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor

@Getter
@Setter
public class ReverseIndexTuple {
    private String documentName;
    private Integer noOcc;
    private Double termFrequency;
}

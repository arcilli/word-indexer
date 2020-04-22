package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class DirectIndexTuple {
    private String word;
    private Integer noOcc;
    private Double termFrequency = -1.0;

}

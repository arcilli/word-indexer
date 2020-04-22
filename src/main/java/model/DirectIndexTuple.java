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

    // Used for creating a partial-filled object.
    public DirectIndexTuple(String word, Integer noOcc) {
        this.word = word;
        this.noOcc = noOcc;
    }
}
